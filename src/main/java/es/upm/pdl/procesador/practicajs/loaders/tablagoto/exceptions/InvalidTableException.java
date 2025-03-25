package es.upm.pdl.procesador.practicajs.loaders.tablagoto.exceptions;

public class InvalidTableException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public InvalidTableException(String message) {
        super(message);
    }

    public InvalidTableException(String message, Throwable cause) {
        super(message, cause);
    }

}
