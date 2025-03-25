package es.upm.pdl.procesador.practicajs.errores;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.upm.pdl.procesador.practicajs.excepciones.FileNotCreatedException;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class GestorErrores {
    private static final Map<Integer, String> errores = initErrores();
    private static final Map<String, String> tokensDictionary = initTokensDictionary();

    private static final String ERROR_FILE = "." + File.separator + "errores.txt";

    private static final String AN_LEXICO = "Analizador Lexico";
    private static final String AN_SINTACTICO = "Analizador Sintactico";
    private static final String AN_SEMANTICO = "Analizador Semantico";

    private static final String ERROR_ESCRITURA = "Error al escribir en el fichero de errores";
    private static final String ENCABEZADO_ERROR = "Error : %s : linea %d : ";

    private static BufferedWriter erroresWriter;

    private static int numLinea = 1;

    private GestorErrores() {
    }

    public static void initWriter() throws GestorErroresException {
        if (erroresWriter == null) {
            try {
                erroresWriter = FileUtils.createBufferedWriter(ERROR_FILE);
            } catch (IOException | FileNotCreatedException e) {
                throw new GestorErroresException("Error al crear el fichero de errores", e);
            }
        }
    }

    private static Map<Integer, String> initErrores() {
        return Map.ofEntries(
                Map.entry(30, "No se reconoce el caracter '%s' en ASCII %n"),
                Map.entry(31, "Existe un caracter ASCII de control '%s' %n"),
                Map.entry(32, "No se reconoce el operador '%s' %n"),
                Map.entry(33, "'%s' no es un caracter ASCII imprimible %n"),
                Map.entry(34, "No se reconoce el operador '&%s' %n"),
                Map.entry(35, "No se reconoce el operador '/%s' %n"),
                Map.entry(36, "El numero '%s' ha excedido los 16 bits %n"),
                Map.entry(37, "La cadena '%s' ha excedido los 64 caracteres %n"),
                Map.entry(40, "Las sentencias return deben ir dentro de las funciones %n"),
                Map.entry(41,
                        "En las sentencias condicionales, se necesita una condicion logica, sin embargo, es de tipo %s %n"),
                Map.entry(42, "La funcion %s debe devolver %s pero esta devolviendo %s %n"),
                Map.entry(43,
                        "Ambos lados de la asignacion deben ser del mismo tipo. En cambio el valor de %s es: %s y su valor asignado %s %n"),
                Map.entry(44,
                        "En ambos lados de la asignacion con operador &= deben ser valores enteros. En cambio el valor de %s es %s y su valor asignado %s %n"),
                Map.entry(45, "Llamada a una funcion %s no definida previamente %n"),
                Map.entry(46, "El numero de argumentos no corresponde con los definidos en la funcion %s %n"),
                Map.entry(47, "El tipo de los argumentos no corresponde con los definidos en la funcion %s %n"),
                Map.entry(48, "Las operaciones de salida solo admiten enteros o cadenas, no se admite %s %n"),
                Map.entry(49, "Las operaciones de entrada solo admiten enteros o cadenas, no se admite %s %n"),
                Map.entry(50, "Los operandos en una expresion && deben ser de tipo logico %n"),
                Map.entry(51, "Los operandos en una expresion %s deben ser del mismo tipo %n"),
                Map.entry(52, "Los operandos en una expresion %s deben ser de tipo entero  %n"),
                Map.entry(53, "Este identificador '%s' ya ha sido declarado %n"),
                Map.entry(60, "Se esperaba uno de los siguientes tokens: %s y se ha encontrado '%s' %n"));
    }       

    private static Map<String, String> initTokensDictionary() {
        return Map.ofEntries(
                Map.entry("ID", "id"),
                Map.entry("SUMA", "+"),
                Map.entry("DOBLEEQ", "=="),
                Map.entry("DOBLEAND", "&&"),
                Map.entry("ASIGAND", "&="),
                Map.entry("ASIG", "="),
                Map.entry("PARENTIZDA", "("),
                Map.entry("PARENTDCHA", ")"),
                Map.entry("LLAVEIZDA", "{"),
                Map.entry("LLAVEDCHA", "}"),
                Map.entry("PYC", ";"),
                Map.entry("COMA", ","),
                Map.entry("FUNCTION", "function"),
                Map.entry("VAR", "var"),
                Map.entry("RETURN", "return"),
                Map.entry("IF", "if"),
                Map.entry("ELSE", "else"),
                Map.entry("INT", "int"),
                Map.entry("BOOLEAN", "boolean"),
                Map.entry("STRING", "string"),
                Map.entry("VOID", "void"),
                Map.entry("ENTERO", "ent"),
                Map.entry("CADENA", "cad"),
                Map.entry("OUTPUT", "output"),
                Map.entry("INPUT", "input"));
    }

    public static void error(int estado, String... arguments) throws GestorErroresException {
        initWriter();
        if (estado >= 30 && estado < 40) {
            try {
                erroresWriter.write(String.format(ENCABEZADO_ERROR, AN_SINTACTICO, numLinea) + String.format(errores.get(estado), ((Object []) arguments)));
            } catch (IOException e) {
                throw new GestorErroresException(ERROR_ESCRITURA, e);
            }
        } else if (estado >= 40 && estado < 55) {
            try {
                erroresWriter.write(String.format(ENCABEZADO_ERROR, AN_SEMANTICO, numLinea) + String.format(errores.get(estado), ((Object []) arguments)));
            } catch (IOException e) {
                throw new GestorErroresException(ERROR_ESCRITURA, e);
            }
        } else if (estado >= 60) {
            try {
                if (estado == 61) {
                    erroresWriter.write(String.format(errores.get(estado), AN_SINTACTICO, numLinea, tokensDictionary.get(arguments[0])));
                }
            } catch (IOException e) {
                throw new GestorErroresException(ERROR_ESCRITURA, e);
            }
        }
    }

    private static Object[] concatArgs (Object... args) {
        return args;
    }

    public static void error(String token, List<String> esperados) throws GestorErroresException {
        initWriter();
        try {
            String esperadosString = esperados.stream().map(t -> "'" + tokensDictionary.get(t) + "'").collect(Collectors.joining(" , "));
            erroresWriter.write(String.format(ENCABEZADO_ERROR, AN_SINTACTICO, numLinea) + String.format(errores.get(60), esperadosString, tokensDictionary.get(token)));
        } catch (IOException e) {
            throw new GestorErroresException(ERROR_ESCRITURA, e);
        }
    }

    public static void close() throws GestorErroresException {
        initWriter();
        try {
            erroresWriter.close();
        } catch (IOException e) {
            throw new GestorErroresException("Error al cerrar el fichero de errores", e);
        }
    }

    public static void newLine() {
        numLinea++;
    }
    
}
