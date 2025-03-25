package es.upm.pdl.procesador.practicajs.analizadores;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisSemanticoException;
import es.upm.pdl.procesador.practicajs.errores.GestorErrores;
import es.upm.pdl.procesador.practicajs.errores.GestorErroresException;
import es.upm.pdl.procesador.practicajs.excepciones.FileNotCreatedException;
import es.upm.pdl.procesador.practicajs.excepciones.SymbolAlreadyInsertException;
import es.upm.pdl.procesador.practicajs.items.AccionSemantica;
import es.upm.pdl.procesador.practicajs.items.Simbolo;
import es.upm.pdl.procesador.practicajs.loaders.accionesSemantico.AccionesSemanticasLoader;
import es.upm.pdl.procesador.practicajs.loaders.accionesSemantico.AccionSemanticaDoesNotExistException;
import es.upm.pdl.procesador.practicajs.tablas.TablaSimbolos;

public class AnalizadorSemantico {
    private static TablaSimbolos tablaActual;
    private static TablaSimbolos tablaGlobal;
    private static TablaSimbolos tablaLocal;

    public static final String TIPO_OK = "tipo_ok";
    public static final String TIPO_ERROR = "tipo_error";
    public static final String TIPO_ENTERO = "ent";
    public static final String TIPO_LOGICO = "log";
    public static final String TIPO_CADENA = "cad";
    public static final String TIPO_FUNCION = "funcion";
    public static final String TIPO_VACIO = "vacio";

    private static final String MODO_VALOR = "1";

    private int nTablas = 0;

    private static int desplG;
    private int desplL;

    private static boolean zonaDecl;

    private AccionSemantica[] acciones;

    public List<Object> obtenerAtributos(Integer estadosCima, List<List<Object>> attrsCima) {
        /* Saco el identificador de la accion semantica de donde los tenga guardado */
        /* Esto llamara Method.invoke para buscar la funcion */
        try {
            String accionSemantica = getAccionSemantica(estadosCima);
            Method funcion = getClass().getDeclaredMethod(accionSemantica, List.class);
            Collections.reverse(attrsCima);
            return (List<Object>) funcion.invoke(this, attrsCima);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TablaSimbolos getTablaActual() {
        return tablaActual;
    }

    public static void destroyTablas() throws IOException, FileNotCreatedException {
        if (tablaGlobal != null) tablaGlobal.destroy();
        if (tablaLocal != null) tablaLocal.destroy();
    }

    public static void destroyTSGlobal() throws IOException, FileNotCreatedException {
        tablaGlobal.destroy();
    }

    public static void insertaTipoTS(Entry<String, Simbolo> posTS, String tipo) {
        posTS.getValue().setTipo(tipo);
    }

    public static void insertaDesplTS(Entry<String, Simbolo> posTS, int despl) {
        posTS.getValue().setDespl(despl);
    }

    public static void insertaTipoRetorno(Entry<String, Simbolo> posTS, String tipoRet) {
        posTS.getValue().setTipoRetorno(tipoRet);
    }

    public static void insertaEtiqTS(Entry<String, Simbolo> posTS, String etiq) {
        posTS.getValue().setEtiqueta(nuevaEtiqueta(posTS));
    }

    public static String nuevaEtiqueta(Entry<String, Simbolo> posTS) {
        return String.format("etiq_%s", obtenerLexemaTS(posTS));
    }

    public static void insertaTipoParamsTS(Entry<String, Simbolo> posTS, List<String> tipos) {
        posTS.getValue().setTipoParams(tipos);
    }

    public static void insertaNumParamsTS(Entry<String, Simbolo> posTS, int numParams) {
        posTS.getValue().setNumParams(numParams);
    }

    public static void insertaModoParamsTS(Entry<String, Simbolo> posTS, List<String> modos) {
        posTS.getValue().setModoParams(modos);
    }

    public static String obtenerTipoTS(Entry<String, Simbolo> posTS) {
        return posTS.getValue().getTipo();
    }

    public static String obtenerLexemaTS(Entry<String, Simbolo> posTS) {
        return posTS.getKey();
    }

    public static List<String> obtenerTipoParamsTS(Entry<String, Simbolo> posTS) {
        return posTS.getValue().getTipoParams();
    }

    public static String obtenerTipoRetornoTS(Entry<String, Simbolo> posTS) {
        return posTS.getValue().getTipoRetorno();
    }

    public static int obtenerNumParamsTS(Entry<String, Simbolo> posTS) {
        return posTS.getValue().getNumParams();
    }

    public static List<String> obtenerModoParamsTS(Entry<String, Simbolo> posTS) {
        return posTS.getValue().obtenerModoParams();
    }

    public static int obtenerDesplTS(Entry<String, Simbolo> posTS) {
        return posTS.getValue().getDespl();
    }

    public static Entry<String, Simbolo> buscaTS(String lexema) {
        return tablaActual.obtenerPosTS(lexema);
    }

    public static Entry<String, Simbolo> insertarTS(String lexema)
            throws SymbolAlreadyInsertException, GestorErroresException {
        Entry<String, Simbolo> posTS = tablaActual.obtenerPosTS(lexema);
        if (posTS == null) {
            if (tablaActual == tablaLocal) {
                if (zonaDecl) {
                    return tablaActual.insertar(lexema);
                } else {
                    posTS = tablaGlobal.obtenerPosTS(lexema);
                    if (posTS != null) {
                        return posTS;
                    } else {
                        posTS = tablaGlobal.insertar(lexema);
                        tablaGlobal.insertaTipoTS(posTS, TIPO_ENTERO);
                        tablaGlobal.insertaDesplTS(posTS, desplG);
                        desplG += 2;
                        return posTS;
                    }
                }
            } else {
                posTS = tablaGlobal.insertar(lexema);
                tablaGlobal.insertaTipoTS(posTS, TIPO_ENTERO);
                tablaGlobal.insertaDesplTS(posTS, desplG);
                desplG += 2;
                return posTS;
            }
        } else {
            GestorErrores.error(53, lexema);
            return posTS;
        }
    }

    public void iniciar() throws AnalisisSemanticoException {

        try {
            tablaGlobal = new TablaSimbolos(nTablas++);
            tablaActual = tablaGlobal;

            desplG = 0;

            zonaDecl = false;

            acciones = AccionesSemanticasLoader.loadSemanticActions();
        } catch (AccionSemanticaDoesNotExistException e) {
            throw new AnalisisSemanticoException("Error al cargar las acciones semanticas", e);
        }

    }

    public void compReturn (List<List<Object>> attrsCima) throws GestorErroresException {
        String BtipoRet = (String) attrsCima.get(0).get(1);

        if (!TIPO_VACIO.equals(BtipoRet)) {
            GestorErrores.error(40, "");
        }
    }

    public List<Object> compTipoSentComp(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String Btipo = (String) attrsCima.get(0).get(0);
        String BtipoRet = (String) attrsCima.get(0).get(1);

        String P1tipo = (String) attrsCima.get(1).get(0);
        String P1tipoRet = (String) attrsCima.get(1).get(1);

        if (!TIPO_VACIO.equals(BtipoRet)) {
            atributos.add(TIPO_ERROR);
            atributos.add(TIPO_ERROR);
        } else {
            if (TIPO_ERROR.equals(Btipo)) {
                atributos.add(TIPO_ERROR);
                atributos.add(TIPO_ERROR);
            } else {
                atributos.add(P1tipo);
                atributos.add(P1tipoRet);
            }
        }

        return atributos;
    }

    public List<Object> programaVacio(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_OK);
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> funcionContinua(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_OK);
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> declaracion(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Ttipo = (String) attrsCima.get(1).get(0);
        int Tancho = (int) attrsCima.get(1).get(1);
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(2).get(0);
        insertaTipoTS(posTS, Ttipo);
        if (tablaActual == tablaGlobal) {
            insertaDesplTS(posTS, desplG);
            desplG += Tancho;
        } else {
            insertaDesplTS(posTS, desplL);
            desplL += Tancho;
        }
        atributos.add(TIPO_OK);
        atributos.add(TIPO_VACIO);

        return atributos;
    }

    public List<Object> sentCondComp(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(2).get(0);
        String Ctipo = (String) attrsCima.get(5).get(0);
        String CtipoRet = (String) attrsCima.get(5).get(1);
        String Itipo = (String) attrsCima.get(7).get(0);
        String ItipoRet = (String) attrsCima.get(7).get(1);

        if (TIPO_LOGICO.equals(Etipo)) {
            if (TIPO_ERROR.equals(Ctipo) || TIPO_ERROR.equals(Itipo)) {
                atributos.add(TIPO_ERROR);
            } else {
                atributos.add(TIPO_OK);
            }
        } else {
            GestorErrores.error(41, Etipo);
            atributos.add(TIPO_ERROR);
        }

        if (TIPO_VACIO.equals(CtipoRet)) {
            atributos.add(ItipoRet);
        } else {
            if (CtipoRet.equals(ItipoRet)) {
                atributos.add(ItipoRet);
            } else {
                atributos.add(TIPO_ERROR);
            }
        }

        return atributos;
    }

    public List<Object> sentCondSimple(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(2).get(0);
        String Stipo = (String) attrsCima.get(4).get(0);
        String StipoRet = (String) attrsCima.get(4).get(1);

        if (TIPO_LOGICO.equals(Etipo)) {
            atributos.add(Stipo);
        } else {
            GestorErrores.error(41, Etipo);
            atributos.add(TIPO_ERROR);
        }

        atributos.add(StipoRet);

        return atributos;
    }

    public List<Object> sentCompSentSimpl(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Stipo = (String) attrsCima.get(0).get(0);
        String StipoRet = (String) attrsCima.get(0).get(1);

        atributos.add(Stipo);
        atributos.add(StipoRet);

        return atributos;
    }

    public void configurarFuncion() {
        tablaLocal = new TablaSimbolos(nTablas++);
        tablaActual = tablaLocal;
        desplL = 0;

    }

    public List<Object> obtenerIDFuncion(List<List<Object>> attrsCima) {
        Entry<String, Simbolo> F2pos = (Entry<String, Simbolo>) attrsCima.get(2).get(0);
        String F1tipo = (String) attrsCima.get(1).get(0);
        tablaActual.setNombreTabla(F2pos.getValue().getLexema());
        insertaTipoRetorno(F2pos, F1tipo);
        insertaTipoTS(F2pos, TIPO_FUNCION);
        insertaEtiqTS(F2pos, nuevaEtiqueta(F2pos));
        return null;
    }

    public List<Object> configurarParamsFuncion(List<List<Object>> attrsCima) {
        Entry<String, Simbolo> F2pos = (Entry<String, Simbolo>) attrsCima.get(2).get(0);

        List<String> F3tipoParams = (List<String>) attrsCima.get(3).get(0);
        int F3nParams = (int) attrsCima.get(3).get(1);
        List<String> F3modoParams = (List<String>) attrsCima.get(3).get(2);

        insertaTipoParamsTS(F2pos, F3tipoParams);
        insertaNumParamsTS(F2pos, F3nParams);
        insertaModoParamsTS(F2pos, F3modoParams);

        for (int i = 0; i < F3nParams / 2; i++) {
            Entry<String, Simbolo> posTSIzda = tablaLocal.obtenerPosTS(i);
            Entry<String, Simbolo> posTSDcha = tablaLocal.obtenerPosTS(F3nParams - i - 1);
            int desplIzda = obtenerDesplTS(posTSIzda);
            insertaDesplTS(posTSIzda, obtenerDesplTS(posTSDcha));
            insertaDesplTS(posTSDcha, desplIzda);
        }

        return null;
    }

    public List<Object> funcionCompleta(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> F2pos = (Entry<String, Simbolo>) attrsCima.get(2).get(0);

        String F2tipo = obtenerTipoTS(F2pos);

        if (TIPO_ERROR.equals(F2tipo)) {
            atributos.add(TIPO_ERROR);
        } else {
            atributos.add(TIPO_OK);
        }

        return atributos;
    }

    public List<Object> completarFuncion(List<List<Object>> attrsCima)
            throws AnalisisSemanticoException, GestorErroresException {
        List<Object> atributos = new LinkedList<>();

        String F1tipo = (String) attrsCima.get(1).get(0);

        Entry<String, Simbolo> F2pos = (Entry<String, Simbolo>) attrsCima.get(2).get(0);

        String Ctipo = (String) attrsCima.get(5).get(0);
        String CtipoRet = (String) attrsCima.get(5).get(1);

        String tipoFuncion;
        String tipoRetFuncion;

        if (TIPO_ERROR.equals(Ctipo)) {
            tipoFuncion = TIPO_ERROR;
            tipoRetFuncion = CtipoRet;
            atributos.add(TIPO_ERROR);
        } else if (TIPO_ERROR.equals(CtipoRet)) {
            tipoFuncion = TIPO_ERROR;
            tipoRetFuncion = TIPO_ERROR;
            atributos.add(TIPO_ERROR);
        } else if (!CtipoRet.equals(F1tipo) && !TIPO_VACIO.equals(CtipoRet)) {
            GestorErrores.error(42, obtenerLexemaTS(F2pos), F1tipo, CtipoRet);
            tipoFuncion = TIPO_ERROR;
            tipoRetFuncion = TIPO_ERROR;
            atributos.add(TIPO_ERROR);
        } else {
            tipoFuncion = TIPO_FUNCION;
            tipoRetFuncion = CtipoRet;
            atributos.add(TIPO_FUNCION);
        }

        insertaTipoTS(F2pos, tipoFuncion);
        insertaTipoRetorno(F2pos, tipoRetFuncion);

        try {
            tablaLocal.destroy();
            tablaActual = tablaGlobal;
        } catch (IOException | FileNotCreatedException e) {
            throw new AnalisisSemanticoException("Error al destruir la tabla de simbolos local", e);
        }

        return atributos;
    }

    public List<Object> asignarTipoRetFuncion(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Htipo = (String) attrsCima.get(0).get(0);
        atributos.add(Htipo);
        return atributos;
    }

    public List<Object> asignarIDFuncion(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> F2pos = (Entry<String, Simbolo>) attrsCima.get(0).get(0);
        atributos.add(F2pos);
        return atributos;
    }

    public List<Object> asignarParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(attrsCima.get(1).get(0));
        atributos.add(attrsCima.get(1).get(1));
        atributos.add(attrsCima.get(1).get(2));
        return atributos;
    }

    public List<Object> compTipoSentsComp(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Btipo = (String) attrsCima.get(0).get(0);
        String BtipoRet = (String) attrsCima.get(0).get(1);
        String C1tipo = (String) attrsCima.get(1).get(0);
        String C1tipoRet = (String) attrsCima.get(1).get(1);
        if (TIPO_ERROR.equals(Btipo)) {
            atributos.add(TIPO_ERROR);
        } else {
            atributos.add(C1tipo);
        }
        if (TIPO_VACIO.equals(BtipoRet)) {
            atributos.add(C1tipoRet);
        } else if (TIPO_VACIO.equals(C1tipoRet)) {
            atributos.add(BtipoRet);
        } else {
            if (BtipoRet.equals(C1tipoRet)) {
                atributos.add(C1tipoRet);
            } else {
                atributos.add(TIPO_ERROR);
            }
        }
        return atributos;
    }

    public List<Object> sentCompVacia(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_OK);
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> asignarTipoFuncion(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Ttipo = (String) attrsCima.get(0).get(0);
        int Tancho = (int) attrsCima.get(0).get(1);
        atributos.add(Ttipo);
        atributos.add(Tancho);
        return atributos;
    }

    public List<Object> asignarTipoFuncVacio(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> insertaTipoListaParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Ttipo = (String) attrsCima.get(0).get(0);
        int Tancho = (int) attrsCima.get(0).get(1);
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(1).get(0);

        insertaTipoTS(posTS, Ttipo);
        insertaDesplTS(posTS, desplL);

        desplL += Tancho;

        List<String> tipoParams = new LinkedList<>();
        tipoParams.add(Ttipo);
        atributos.add(tipoParams);

        atributos.add(1);

        List<String> modoParams = new LinkedList<>();
        modoParams.add(MODO_VALOR);
        atributos.add(modoParams);

        return atributos;
    }

    public List<Object> concatenaListaParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        List<Object> params = (List<Object>) insertaTipoListaParams(attrsCima);
        List<String> AtipoParams = (List<String>) params.get(0);
        int AnParams = (int) params.get(1);
        List<String> AmodoParams = (List<String>) params.get(2);

        List<String> KtipoParams = (List<String>) attrsCima.get(2).get(0);
        int KnParams = (int) attrsCima.get(2).get(1);
        List<String> KmodoParams = (List<String>) attrsCima.get(2).get(2);

        AtipoParams.addAll(KtipoParams);
        atributos.add(AtipoParams);
        atributos.add(AnParams + KnParams);
        AmodoParams.addAll(KmodoParams);
        atributos.add(AmodoParams);

        zonaDecl = false;

        return atributos;
    }

    public List<Object> tipoParamVacio(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();

        List<String> tipoParams = new LinkedList<>();
        tipoParams.add(TIPO_VACIO);
        atributos.add(tipoParams);

        atributos.add(0);

        List<String> modoParams = new LinkedList<>();
        modoParams.add(MODO_VALOR);
        atributos.add(modoParams);

        zonaDecl = false;

        return atributos;
    }

    public List<Object> contInsTipoParam(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Ttipo = (String) attrsCima.get(1).get(0);
        int Tancho = (int) attrsCima.get(1).get(1);
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(2).get(0);

        insertaTipoTS(posTS, Ttipo);
        insertaDesplTS(posTS, desplL);

        desplL += Tancho;

        List<String> tipoParams = new LinkedList<>();
        tipoParams.add(Ttipo);
        atributos.add(tipoParams);

        atributos.add(1);

        List<String> modoParams = new LinkedList<>();
        modoParams.add(MODO_VALOR);
        atributos.add(modoParams);

        return atributos;
    }

    public List<Object> contConcatListaParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        List<Object> params = (List<Object>) contInsTipoParam(attrsCima);

        List<String> KtipoParams = (List<String>) params.get(0);
        int KnParams = (int) params.get(1);
        List<String> KmodoParams = (List<String>) params.get(2);

        List<String> K1tipoParams = (List<String>) attrsCima.get(3).get(0);
        int K1nParams = (int) attrsCima.get(3).get(1);
        List<String> K1modoParams = (List<String>) attrsCima.get(3).get(2);

        KtipoParams.addAll(K1tipoParams);
        atributos.add(KtipoParams);
        atributos.add(KnParams + K1nParams);
        KmodoParams.addAll(K1modoParams);
        atributos.add(KmodoParams);

        return atributos;
    }

    public List<Object> finListaParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(new LinkedList<>());
        atributos.add(0);
        atributos.add(new LinkedList<>());

        return atributos;
    }

    public List<Object> sentElse(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Ctipo = (String) attrsCima.get(2).get(0);
        String CtipoRet = (String) attrsCima.get(2).get(1);
        atributos.add(Ctipo);
        atributos.add(CtipoRet);
        return atributos;
    }

    public List<Object> sentElseVacia(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_OK);
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> asignaTipoEntero(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_ENTERO);
        atributos.add(2);

        return atributos;
    }

    public List<Object> asignaTipoLogico(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_LOGICO);
        atributos.add(2);

        return atributos;
    }

    public List<Object> asignaTipoCadena(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_CADENA);
        atributos.add(128);

        return atributos;
    }

    public List<Object> sentAsigSimple(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(0).get(0);
        String idTipo = obtenerTipoTS(posTS);
        String Etipo = (String) attrsCima.get(2).get(0);
        if (TIPO_ERROR.equals(Etipo)) {
            atributos.add(TIPO_ERROR);
        } else if (idTipo.equals(Etipo)) {
            atributos.add(TIPO_OK);
        } else {
            GestorErrores.error(43, obtenerLexemaTS(posTS), idTipo, Etipo);
            atributos.add(TIPO_ERROR);
        }

        atributos.add(TIPO_VACIO);

        return atributos;
    }

    public List<Object> sentAsigLog(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(0).get(0);
        String idTipo = obtenerTipoTS(posTS);
        String Etipo = (String) attrsCima.get(2).get(0);
        if (TIPO_ERROR.equals(Etipo)) {
            atributos.add(TIPO_ERROR);
            return atributos;
        } else if (idTipo.equals(Etipo)) {
            if (Etipo.equals(TIPO_LOGICO)) {
                atributos.add(TIPO_OK);
            } else {
                GestorErrores.error(44, obtenerLexemaTS(posTS), idTipo, Etipo);
                atributos.add(TIPO_ERROR);
            }
        } else {
            GestorErrores.error(43, obtenerLexemaTS(posTS), idTipo, Etipo);
            atributos.add(TIPO_ERROR);
        }

        atributos.add(TIPO_VACIO);

        return atributos;
    }

    public List<Object> sentLlamadaFunc(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        boolean hayError = false;
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(0).get(0);
        List<String> LtipoParams = (List<String>) attrsCima.get(2).get(0);
        int LnParams = (int) attrsCima.get(2).get(1);

        List<String> StipoParams = obtenerTipoParamsTS(posTS);
        int SnParams = obtenerNumParamsTS(posTS);

        String idTipo = obtenerTipoTS(posTS);
        if (TIPO_ERROR.equals(idTipo)) {
            atributos.add(TIPO_ERROR);
        } else if (!TIPO_FUNCION.equals(idTipo)) {
            GestorErrores.error(45, obtenerLexemaTS(posTS));
            atributos.add(TIPO_ERROR);
        } else {
            if (SnParams != LnParams) {
                GestorErrores.error(46, obtenerLexemaTS(posTS));
                atributos.add(TIPO_ERROR);
            } else {
                for (int i = 0; i < SnParams && !hayError; i++) {
                    if (TIPO_ERROR.equals(LtipoParams.get(i))) {
                        hayError = true;
                        atributos.add(TIPO_ERROR);
                    } else if (!StipoParams.get(i).equals(LtipoParams.get(i))) {
                        GestorErrores.error(47, obtenerLexemaTS(posTS));
                        atributos.add(TIPO_ERROR);
                        hayError = true;
                    }
                }

                if (!hayError) {
                    atributos.add(TIPO_OK);
                }
            }
        }

        atributos.add(TIPO_VACIO);

        return atributos;
    }

    public List<Object> sentOutput(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(1).get(0);
        if (TIPO_ERROR.equals(Etipo)) {
            atributos.add(TIPO_ERROR);
        } else if (TIPO_ENTERO.equals(Etipo) || TIPO_CADENA.equals(Etipo)) {
            atributos.add(TIPO_OK);
        } else {
            GestorErrores.error(48, Etipo);
            atributos.add(TIPO_ERROR);
        }
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> sentInput(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(1).get(0);
        String idTipo = obtenerTipoTS(posTS);
        if (TIPO_ENTERO.equals(idTipo) || TIPO_CADENA.equals(idTipo)) {
            atributos.add(TIPO_OK);
        } else {
            GestorErrores.error(49, idTipo);
            atributos.add(TIPO_ERROR);
        }

        atributos.add(TIPO_VACIO);

        return atributos;
    }

    public List<Object> sentReturn(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String XtipoRet = (String) attrsCima.get(1).get(0);
        if (TIPO_ERROR.equals(XtipoRet)) {
            atributos.add(TIPO_ERROR);
        } else {
            atributos.add(TIPO_OK);
        }

        atributos.add(XtipoRet);
        return atributos;
    }

    public List<Object> insTipoPasoParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(0).get(0);
        List<String> tipoParams = new LinkedList<>();
        tipoParams.add(Etipo);
        atributos.add(tipoParams);
        atributos.add(1);
        return atributos;
    }

    public List<Object> concatListaPasoParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(0).get(0);
        List<String> LtipoParams = new LinkedList<>();
        LtipoParams.add(Etipo);
        List<String> QtipoParams = (List<String>) attrsCima.get(1).get(0);
        int QnParams = (int) attrsCima.get(1).get(1);

        LtipoParams.addAll(QtipoParams);
        atributos.add(LtipoParams);
        atributos.add(QnParams + 1);

        return atributos;
    }

    public List<Object> listaPasoParamsVacia(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        List<String> tipoParams = new LinkedList<>();
        tipoParams.add(TIPO_VACIO);
        atributos.add(tipoParams);
        atributos.add(0);
        return atributos;
    }

    public List<Object> contConcatPasoParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(1).get(0);
        List<String> LtipoParams = new LinkedList<>();
        LtipoParams.add(Etipo);
        List<String> QtipoParams = (List<String>) attrsCima.get(2).get(0);
        int QnParams = (int) attrsCima.get(2).get(1);

        LtipoParams.addAll(QtipoParams);
        atributos.add(LtipoParams);
        atributos.add(QnParams + 1);

        return atributos;
    }

    public List<Object> finPasoParams(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        List<Object> tipoParams = new LinkedList<>();
        tipoParams.add(TIPO_VACIO);
        atributos.add(tipoParams);
        atributos.add(0);
        return atributos;
    }

    public List<Object> exprValorRetorno(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(0).get(0);
        atributos.add(Etipo);
        return atributos;
    }

    public List<Object> exprValorRetornoVacio(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_VACIO);
        return atributos;
    }

    public List<Object> exprAndLogico(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String E1tipo = (String) attrsCima.get(0).get(0);
        String Rtipo = (String) attrsCima.get(2).get(0);
        if (E1tipo.equals(Rtipo)) {
            if (Rtipo.equals(TIPO_LOGICO)) {
                atributos.add(TIPO_LOGICO);
            } else {
                GestorErrores.error(50);
                atributos.add(TIPO_ERROR);
            }
        } else {
            GestorErrores.error(51, "&&");
            atributos.add(TIPO_ERROR);
        }

        return atributos;
    }

    public List<Object> exprToRelacional(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Rtipo = (String) attrsCima.get(0).get(0);
        atributos.add(Rtipo);
        return atributos;
    }

    public List<Object> exprEquivalencia(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String R1tipo = (String) attrsCima.get(0).get(0);
        String Utipo = (String) attrsCima.get(2).get(0);
        if (R1tipo.equals(Utipo)) {
            if (Utipo.equals(TIPO_ENTERO)) {
                atributos.add(TIPO_LOGICO);
            } else {
                GestorErrores.error(52, "==");
                atributos.add(TIPO_ERROR);
            }
        } else {
            GestorErrores.error(51, "==");
            atributos.add(TIPO_ERROR);
        }

        return atributos;
    }

    public List<Object> relacToAritm(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Utipo = (String) attrsCima.get(0).get(0);
        atributos.add(Utipo);
        return atributos;
    }

    public List<Object> exprSuma(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        String U1tipo = (String) attrsCima.get(0).get(0);
        String Vtipo = (String) attrsCima.get(2).get(0);
        if (U1tipo.equals(Vtipo)) {
            if (Vtipo.equals(TIPO_ENTERO)) {
                atributos.add(TIPO_ENTERO);
            } else {
                GestorErrores.error(52, "+");
                atributos.add(TIPO_ERROR);
            }
        } else {
            GestorErrores.error(51, "+");
            atributos.add(TIPO_ERROR);
        }

        return atributos;
    }

    public List<Object> exprIndividual(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Vtipo = (String) attrsCima.get(0).get(0);
        atributos.add(Vtipo);
        return atributos;
    }

    public List<Object> exprVariable(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(0).get(0);

        atributos.add(obtenerTipoTS(posTS));

        return atributos;
    }

    public List<Object> exprParentesis(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        String Etipo = (String) attrsCima.get(1).get(0);
        atributos.add(Etipo);
        return atributos;
    }

    public List<Object> exprEntero(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_ENTERO);
        return atributos;
    }

    public List<Object> exprLlamadaFunc(List<List<Object>> attrsCima) throws GestorErroresException {
        List<Object> atributos = new LinkedList<>();
        Entry<String, Simbolo> posTS = (Entry<String, Simbolo>) attrsCima.get(0).get(0);

        List<String> LtipoParams = (List<String>) attrsCima.get(2).get(0);
        int LnParams = (int) attrsCima.get(2).get(1);

        if (TIPO_ERROR.equals(obtenerTipoTS(posTS))) {
            atributos.add(TIPO_ERROR);
            return atributos;
        } else if (!TIPO_FUNCION.equals(obtenerTipoTS(posTS))) {
            insertaTipoTS(posTS, TIPO_ERROR);
            GestorErrores.error(45, obtenerLexemaTS(posTS));
            atributos.add(TIPO_ERROR);
            return atributos;
        }

        List<String> VtipoParams = obtenerTipoParamsTS(posTS);
        int VnParams = obtenerNumParamsTS(posTS);

        if (VnParams != LnParams) {
            GestorErrores.error(46, obtenerLexemaTS(posTS));
            atributos.add(TIPO_ERROR);
        } else {
            for (int i = 0; i < VnParams; i++) {
                if (TIPO_ERROR.equals(LtipoParams.get(i))) {
                    atributos.add(TIPO_ERROR);
                    return atributos;
                } else if (!LtipoParams.get(i).equals(VtipoParams.get(i))) {
                    GestorErrores.error(47, obtenerLexemaTS(posTS));
                    atributos.add(TIPO_ERROR);
                    return atributos;
                }
            }

            atributos.add(obtenerTipoRetornoTS(posTS));
        }

        return atributos;
    }

    public List<Object> exprCadena(List<List<Object>> attrsCima) {
        List<Object> atributos = new LinkedList<>();
        atributos.add(TIPO_CADENA);
        return atributos;
    }

    private String getAccionSemantica(Integer estados) {
        List<Integer> estadosCima = new LinkedList<>();
        estadosCima.add(estados);
        for (AccionSemantica accion : acciones) {
            String accionSemantica = accion.getAccionSemantica(estadosCima);
            if (accionSemantica != null) {
                return accionSemantica;
            }
        }

        return "";
    }

    public static void setZonaDecl(boolean zona) {
        zonaDecl = zona;
    }
}
