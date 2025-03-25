package es.upm.pdl.procesador.practicajs.loaders.tablagoto;

public class TableDoesNotExistException extends Exception {

    private static final long serialVersionUID = 1L;

    public TableDoesNotExistException(String message) {
        super(message);
    }

    public TableDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
