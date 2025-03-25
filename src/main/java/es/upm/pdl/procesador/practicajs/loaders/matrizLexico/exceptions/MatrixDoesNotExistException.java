package es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions;

public class MatrixDoesNotExistException extends Exception {

    private static final long serialVersionUID = 1L;

    public MatrixDoesNotExistException(String message) {
        super(message);
    }

    public MatrixDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
