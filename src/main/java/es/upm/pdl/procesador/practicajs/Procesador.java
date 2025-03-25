package es.upm.pdl.procesador.practicajs;

import java.io.File;

import es.upm.pdl.procesador.practicajs.analizadores.AnalizadorLexico;
import es.upm.pdl.procesador.practicajs.analizadores.AnalizadorSintactico;
import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisLexicoException;
import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisSemanticoException;
import es.upm.pdl.procesador.practicajs.analizadores.exceptions.AnalisisSintacticoException;
import es.upm.pdl.procesador.practicajs.errores.GestorErrores;
import es.upm.pdl.procesador.practicajs.errores.GestorErroresException;



public class Procesador {

    private static final String COD_FUENTE = "." + File.separator + "prueba9.txt";

    public static void main(String[] args) throws AnalisisLexicoException, AnalisisSintacticoException, GestorErroresException, AnalisisSemanticoException {
        //AnalizadorLexico anLex = new AnalizadorLexico(COD_FUENTE);
        AnalizadorLexico anLex = new AnalizadorLexico(args[0]);
        AnalizadorSintactico anSint = new AnalizadorSintactico(anLex);
        anSint.analizar();
        GestorErrores.close();
    }
}