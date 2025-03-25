package es.upm.pdl.procesador.practicajs.analizadores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.util.Pair;
import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisLexicoException;
import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisSemanticoException;
import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisSintacticoException;
import es.upm.pdl.procesador.practicajs.errores.GestorErrores;
import es.upm.pdl.procesador.practicajs.errores.GestorErroresException;
import es.upm.pdl.procesador.practicajs.excepciones.FileNotCreatedException;
import es.upm.pdl.procesador.practicajs.items.AccionSintactica;
import es.upm.pdl.procesador.practicajs.items.Simbolo;
import es.upm.pdl.procesador.practicajs.items.Token;
import es.upm.pdl.procesador.practicajs.loaders.tablagoto.TableDoesNotExistException;
import es.upm.pdl.procesador.practicajs.loaders.tablagoto.GOTOTableLoader;
import es.upm.pdl.procesador.practicajs.loaders.tablagoto.exceptions.InvalidTableException;
import es.upm.pdl.procesador.practicajs.tablas.TablaGOTO;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class AnalizadorSintactico {
    private Deque<Integer> pilaTokens;
    private Deque<List<Object>> pilaAtributos;

    private TablaGOTO tablaGOTO;

    private AnalizadorLexico anLexico;
    private AnalizadorSemantico anSemantico;

    /* Con esta variable puedo conocer la posicion relativa en la regla */
    private int nAccDespl;

    private static final String PARSE_FILE = "." + File.separator + "parse.txt";

    private static Map<String, Simbolo> tokensFicticios;
    private int ficticios = 0;
    private static final String FICTICIO_FORMAT = "FICTICIO%d";

    private BufferedWriter parse;

    public AnalizadorSintactico(AnalizadorLexico lexico) throws AnalisisSintacticoException {
        this.anLexico = lexico;
        this.nAccDespl = 0;
        this.anSemantico = new AnalizadorSemantico();
        pilaTokens = new ArrayDeque<>();
        pilaAtributos = new ArrayDeque<>();
        pilaAtributos.push(new LinkedList<>());
        pilaTokens.push(0);

        try {
            parse = FileUtils.createBufferedWriter(PARSE_FILE);
            parse.write("ascendente ");
            tablaGOTO = GOTOTableLoader.loadTable();
        } catch (IOException | FileNotCreatedException e) {
            throw new AnalisisSintacticoException("Error creating parse file", e);
        } catch (TableDoesNotExistException e) {
            throw new AnalisisSintacticoException("Error loading table file", e);
        } catch (InvalidTableException e) {
            throw new AnalisisSintacticoException("Error reading table file", e);
        }
    }

    public void analizar() throws AnalisisLexicoException, GestorErroresException, AnalisisSintacticoException,
            AnalisisSemanticoException {
        boolean aceptado = false;
        anSemantico.iniciar();
        Token<String, Entry<String, Simbolo>> tokenSig = null;
        Token<String, Entry<String, Simbolo>> tokenAnt = null;
        Token<String, Entry<String, Simbolo>> token = pideToken();
        if (token.getTipo().equals("ID")) {
            List<Object> atributos = new LinkedList<>();
            atributos.add(token.getPosTs());
            pilaAtributos.push(atributos);
        }

        while (!aceptado) {
            AccionSintactica<Character, Integer> accion = tablaGOTO.getAction(token, pilaTokens.peek());
            if (accion == null) {
                try {
                    tokenSig = token;
                    List<Integer> ultimosEstados = pilaTokens.stream()
                            .limit(8)
                            .collect(Collectors.toList());

                    Collections.reverse(ultimosEstados);

                    token = manejarError(token.getTipo(), ultimosEstados);
                    if (token == null) {
                        token = pideToken();
                        accion = tablaGOTO.getAction(token, pilaTokens.peek());
                    }

                    if (accion == null) {
                        parse.close();
                        anLexico.close();
                        GestorErrores.close();
                        AnalizadorSemantico.destroyTablas();
                        throw new AnalisisSintacticoException("No se pudo manejar error sintactico");
                    }
                    accion = tablaGOTO.getAction(token, pilaTokens.peek());
                } catch (IOException | FileNotCreatedException e) {
                    throw new AnalisisSintacticoException("Error closing parse file", e);
                }
            }

            List<Object> atributos;
            List<List<Object>> ultimosNattr;
            switch (accion.getAccion()) {
                case 'd':
                    /*
                     * Nota mental: Cada push vas a introducirlo en una lista y le vas
                     * a pasar esta lista al analizador semantico para que meta en la
                     * pila de atributos los atributos
                     */
                    if (token.getTipo().equals("ID")) {
                        atributos = new LinkedList<>();
                        atributos.add(token.getPosTs());
                        pilaAtributos.push(atributos);
                    } else {
                        if (accion.getEstado() == 86) {
                            ultimosNattr = pilaAtributos.stream()
                                    .limit(6)
                                    .collect(Collectors.toList());
                            Collections.reverse(ultimosNattr);
                            atributos = anSemantico.completarFuncion(ultimosNattr);

                            if (atributos != null) {
                                pilaAtributos.push(atributos);
                            } else {
                                System.out.println(String.format("Error en desplazamiento %d", accion.getEstado()));
                            }
                        } else {
                            pilaAtributos.push(new LinkedList<>());
                        }
                    }

                    if (accion.getEstado() == 16 || accion.getEstado() == 17 || accion.getEstado() == 18) {
                        AnalizadorSemantico.setZonaDecl(true);
                    } else if (accion.getEstado() == 39) {
                        AnalizadorSemantico.setZonaDecl(false);
                    }

                    pilaTokens.push(accion.getEstado());

                    if (tokenSig != null) {
                        token = tokenSig;
                        tokenSig = null;
                    } else {
                        tokenAnt = token;
                        token = pideToken();
                    }
                    break;
                case 'r':
                    int ruleSteps = tablaGOTO.ruleSteps(accion.getEstado());
                    ultimosNattr = new LinkedList<>();
                    for (int i = 0; i < ruleSteps; i++) {
                        pilaTokens.pop();
                        ultimosNattr.add(pilaAtributos.pop());
                    }

                    atributos = anSemantico.obtenerAtributos(accion.getEstado(), ultimosNattr);
                    if (atributos != null) {
                        pilaAtributos.push(atributos);
                    } else {
                        System.out.println(String.format("Error en la regla %d", accion.getEstado()));
                    }

                    if (accion.getEstado() == 9) {
                        anSemantico.configurarFuncion();
                    } else if (accion.getEstado() == 10) {
                        ultimosNattr = pilaAtributos.stream()
                                .limit(3)
                                .collect(Collectors.toList());
                        Collections.reverse(ultimosNattr);
                        anSemantico.obtenerIDFuncion(ultimosNattr);
                    } else if (accion.getEstado() == 11) {
                        ultimosNattr = pilaAtributos.stream()
                                .limit(4)
                                .collect(Collectors.toList());
                        Collections.reverse(ultimosNattr);
                        anSemantico.configurarParamsFuncion(ultimosNattr);
                    }

                    /*
                     * Se han consumido todos los simbolos de la regla, se resetea el desplazamiento
                     * de acciones'Desplazar'
                     */
                    nAccDespl = nAccDespl - ruleSteps + 1;
                    int sigEstado = tablaGOTO.goTo(pilaTokens.peek(), accion.getEstado());
                    if (sigEstado == 2) {
                        ultimosNattr = pilaAtributos.stream()
                                .limit(1)
                                .collect(Collectors.toList());
                        anSemantico.compReturn(ultimosNattr);

                    }
                    pilaTokens.push(sigEstado);
                    generarParse(accion.getEstado());
                    break;
                case 'a':
                    aceptado = true;
                    try {
                        parse.close();
                        AnalizadorSemantico.destroyTSGlobal();
                    } catch (IOException | FileNotCreatedException e) {
                        throw new AnalisisSintacticoException("Error closing parse file", e);
                    }
                    break;
                default:
                    throw new AnalisisSintacticoException("Error en la tabla de acciones");
            }

        }

    }

    private Token<String, Entry<String, Simbolo>> pideToken() throws AnalisisLexicoException, GestorErroresException {
        return anLexico.getToken();
    }

    private void generarParse(int regla) throws AnalisisSintacticoException {
        try {
            parse.write(String.format("%d ", regla));
        } catch (IOException e) {
            throw new AnalisisSintacticoException("Error writing to parse file", e);
        }
    }

    private Token<String, Entry<String, Simbolo>> manejarError(String tokenActual, List<Integer> ultimosEstados)
            throws GestorErroresException {
        int estado = ultimosEstados.get(ultimosEstados.size() - 1);
        GestorErrores.error(tokenActual, tablaGOTO.posiblesTokens(estado));
        String tokenCorrecto = tablaGOTO.tokenIntermedio(tokenActual, ultimosEstados);
        if (tokenCorrecto != null) {
            if (tokenCorrecto.equals("ID")) {
                String lexema = String.format(FICTICIO_FORMAT, ficticios++);
                tokensFicticios.put(lexema, new Simbolo(lexema));
                return new Token<>("ID", buscarTokenFicticio(lexema));
            } else {
                return new Token<>(tokenCorrecto, null);
            }
        }

        return null;
    }

    public Entry<String, Simbolo> buscarTokenFicticio(String lexema) {
        for (Entry<String, Simbolo> entry : tokensFicticios.entrySet()) {
            if (entry.getKey().equals(lexema)) {
                return entry;
            }
        }

        return null;
    }
}
