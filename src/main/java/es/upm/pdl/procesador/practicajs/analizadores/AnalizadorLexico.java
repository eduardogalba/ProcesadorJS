package es.upm.pdl.procesador.practicajs.analizadores;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisLexicoException;
import es.upm.pdl.procesador.practicajs.automatas.AutomataFD;
import es.upm.pdl.procesador.practicajs.errores.GestorErrores;
import es.upm.pdl.procesador.practicajs.errores.GestorErroresException;
import es.upm.pdl.procesador.practicajs.excepciones.FileNotCreatedException;
import es.upm.pdl.procesador.practicajs.excepciones.SymbolAlreadyInsertException;
import es.upm.pdl.procesador.practicajs.items.PR;
import es.upm.pdl.procesador.practicajs.items.Token;
import es.upm.pdl.procesador.practicajs.items.Simbolo;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.InvalidMatrixException;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.MatrixDoesNotExistException;
import es.upm.pdl.procesador.practicajs.loaders.tablaPR.PRTableLoader;
import es.upm.pdl.procesador.practicajs.tablas.TablaPR;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class AnalizadorLexico {

    private TablaPR tablaPR;

    private AutomataFD automataFD;

    private static final String TOKENS_FILE = "." + File.separator + "tokens.txt";
    private static final String TOKEN_PRINT_FORMAT = "<%s, %s>%n";


    private static final int MAX_BITS_ENTERO = 16;
    
    private static final int MAX_SIZE_CADENA = 64;
    private static final int MAX_SIZE_ENTER0 = (2 << MAX_BITS_ENTERO) - 1;

    private BufferedWriter tokens;
    private BufferedReader codFuente;

    private char lastChar = 0;


    public AnalizadorLexico (String filepath) throws AnalisisLexicoException {
        this.tablaPR = PRTableLoader.loadPRTable();
        
        try {
            this.automataFD = new AutomataFD("lexico.txt");
            this.codFuente = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8));
            tokens = FileUtils.createBufferedWriter(TOKENS_FILE);
        } catch (MatrixDoesNotExistException | InvalidMatrixException e) {
            throw new AnalisisLexicoException("Error al cargar la matriz de transición", e);
        } catch (FileNotCreatedException | IOException e) {
            throw new AnalisisLexicoException("Error al crear los ficheros", e);
        }
        
    }


    public Token<String, Entry<String, Simbolo>> getToken()  throws AnalisisLexicoException, GestorErroresException {
        try {
            Token<String, Entry<String, Simbolo>> token = null;
            lastChar = (lastChar == 0)  ? (char) codFuente.read() : lastChar;
            while (codFuente.ready() && token == null) {

                token = procesarCaracter();
            }

            return (token != null) ? token : generarToken("$", (Entry<String, Simbolo>) null);
        } catch (IOException e) {
            throw new AnalisisLexicoException("Error al leer el fichero de código fuente", e);
        } catch (SymbolAlreadyInsertException e) {
            throw new AnalisisLexicoException("Error al insertar un símbolo en la tabla de símbolos", e);
        } 
    }

    private Token<String, Entry<String, Simbolo>> procesarCaracter() throws IOException, SymbolAlreadyInsertException, GestorErroresException, AnalisisLexicoException {
        StringBuilder lexema = new StringBuilder();
        int num = 0;
        automataFD.reset();
        while (!automataFD.esFinal()) {
            int accion = automataFD.nextAccion(lastChar);
            int estado = automataFD.nextEstado(lastChar);

            if (automataFD.esError()) {
                return manejarError(estado, lexema);
            }

            switch (accion) {
                case 'A':
                    lexema.append(lastChar);
                    leer();
                    break;
                case 'B':
                    num = Integer.parseInt(String.valueOf(lastChar));
                    leer();
                    break;
                case 'C':
                    num = num * 10 + Integer.parseInt(String.valueOf(lastChar));
                    leer();
                    break;
                case 'G':
                    return analizarIdent(lexema.toString());
                case 'H':
                    if (num > MAX_SIZE_ENTER0) {
                        GestorErrores.error(36, Integer.toString(num));
                        return null;
                    }
                    
                    return generarToken("ENTERO", num);
                case 'I':
                    if (lexema.length() > MAX_SIZE_CADENA) {
                        GestorErrores.error(37, lexema.toString());
                        return null;
                    }

                    leer();
                    return generarToken("CADENA", lexema.toString());
                    
                case 'J':
                    leer();
                    return generarToken("DOBLEAND", (Entry<String, Simbolo>)null);
                    
                case 'K':
                    leer();
                    return generarToken("ASIGAND", (Entry<String, Simbolo>)null);
                    
                case 'L':
                    leer();
                    break;
                    
                case 'M':
                    leer();
                    return generarToken("DOBLEEQ", (Entry<String, Simbolo>)null);
                    
                case 'N':
                    return generarToken("ASIG", (Entry<String, Simbolo>)null);
                    
                case 'O':
                    leer();
                    return generarToken("SUMA", (Entry<String, Simbolo>)null);
                    
                case 'P':
                    leer();
                    return generarToken("COMA", (Entry<String, Simbolo>)null);
                    
                case 'Q':
                    leer();
                    return generarToken("PYC", (Entry<String, Simbolo>)null);
                    
                case 'R':
                    leer();
                    return generarToken("PARENTIZDA", (Entry<String, Simbolo>)null);
                    
                case 'S':
                    leer();
                    return generarToken("PARENTDCHA", (Entry<String, Simbolo>)null);
                    
                case 'T':
                    leer();
                    return generarToken("LLAVEIZDA", (Entry<String, Simbolo>)null);
                    
                case 'U':
                    leer();
                    return generarToken("LLAVEDCHA", (Entry<String, Simbolo>)null);
                    
                default:
                    break;
            }
        }

        return null;
    }

    private void leer () throws IOException {
        lastChar = (char) codFuente.read();
        if (lastChar == '\n') {
            GestorErrores.newLine();
        }
    }

    private Token<String, Entry<String, Simbolo>> manejarError (int estado, StringBuilder lexema) throws IOException, GestorErroresException, AnalisisLexicoException {
        Pattern pattern = Pattern.compile("[ \\t]");
        Matcher matcher = pattern.matcher(Character.toString(lastChar));
        while (codFuente.ready() && !matcher.find()) {
            if (lastChar == '\n') {
                GestorErrores.newLine();
                break;
            } else if (lastChar == 13) {
                lastChar = (char) codFuente.read();
                continue;
            }
            
            lexema.append(lastChar);
            lastChar = (char) codFuente.read();
            matcher = pattern.matcher(Character.toString(lastChar));
        }
        GestorErrores.error(estado, lexema.toString());
        return getToken();
    }

    private Token<String, Entry<String, Simbolo>> analizarIdent (String lexema) throws IOException, SymbolAlreadyInsertException, GestorErroresException {

        Entry<String, PR> posTPR = buscaTPR(lexema);
        if (posTPR != null) {
            return generarToken(posTPR.getKey(), (Entry<String, Simbolo>) null);
        } else {
            Entry<String, Simbolo> posTS = buscaTS(lexema);
            if (posTS == null) {
                posTS = insertaTS(lexema);
            } 
            return generarToken("ID", posTS);
        }

    }

    private Entry<String, Simbolo> buscaTS (String lexema) {
        return AnalizadorSemantico.buscaTS(lexema);
    }

    private Entry<String, Simbolo> insertaTS (String lexema) throws SymbolAlreadyInsertException, GestorErroresException {
        return AnalizadorSemantico.insertarTS(lexema);
    }

    private Entry<String, PR> buscaTPR (String lexema) {
        return tablaPR.buscar(lexema);
    }

    private Token<String, Entry<String, Simbolo>> generarToken (String token, Entry<String, Simbolo> valor) throws IOException {
        if (token.equals("$")) {
            tokens.close();
        } else {
            tokens.write(String.format(TOKEN_PRINT_FORMAT, token, valor == null ? "" : System.identityHashCode(valor)));
        }
 
        return new Token<>(token, valor);
    }

    private Token<String, Entry<String, Simbolo>> generarToken (String token, String valor) throws IOException {
        tokens.write(String.format(TOKEN_PRINT_FORMAT, token, valor));
        return new Token<>(token, null);
    }

    private Token<String, Entry<String, Simbolo>> generarToken (String token, Integer valor) throws IOException {
        tokens.write(String.format(TOKEN_PRINT_FORMAT, token, valor));
        return new Token<>(token, null);
    }

    public void close() throws IOException, FileNotCreatedException {
        AnalizadorSemantico.getTablaActual().exportar();
        codFuente.close();
        tokens.close();
    }

    
}
