package es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions;

public class InvalidMatrixException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidMatrixException(String message) {
        super(message);
    }

    public InvalidMatrixException(String message, Throwable cause) {
        super(message, cause);
    }
}
