package es.upm.pdl.procesador.practicajs.loaders.accionesSemantico;

public class AccionSemanticaDoesNotExistException extends Exception {
    private static final long serialVersionUID = 1L;

    public AccionSemanticaDoesNotExistException(String message) {
        super(message);
    }

    public AccionSemanticaDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
