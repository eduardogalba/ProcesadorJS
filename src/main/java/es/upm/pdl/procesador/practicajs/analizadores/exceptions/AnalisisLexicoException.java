package es.upm.pdl.procesador.practicajs.analizadores.exceptions;

public class AnalisisLexicoException extends Exception {

    private static final long serialVersionUID = 1L;

    public AnalisisLexicoException(String message) {
        super(message);
    }

    public AnalisisLexicoException(String message, Throwable cause) {
        super(message, cause);
    }

}
