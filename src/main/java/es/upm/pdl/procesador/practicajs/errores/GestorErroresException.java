package es.upm.pdl.procesador.practicajs.errores;

public class GestorErroresException extends Exception {
    private static final long serialVersionUID = 1L;

    public GestorErroresException(String message) {
        super(message);
    }

    public GestorErroresException(String message, Throwable cause) {
        super(message, cause);
    }

}
