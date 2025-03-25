package es.upm.pdl.procesador.practicajs.analizadores.exceptions;

public class AnalisisSintacticoException extends Exception {
    private static final long serialVersionUID = 1L;

    public AnalisisSintacticoException(String message) {
        super(message);
    }

    public AnalisisSintacticoException(String message, Throwable cause) {
        super(message, cause);
    }
}
