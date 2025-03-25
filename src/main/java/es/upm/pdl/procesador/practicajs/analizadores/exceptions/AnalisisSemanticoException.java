package es.upm.pdl.procesador.practicajs.analizadores.exceptions;

public class AnalisisSemanticoException extends Exception {
    private static final long serialVersionUID = 1L;

    public AnalisisSemanticoException(String message) {
        super(message);
    }

    public AnalisisSemanticoException(String message, Throwable cause) {
        super(message, cause);
    }
}
