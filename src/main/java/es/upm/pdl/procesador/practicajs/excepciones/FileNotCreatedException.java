package es.upm.pdl.procesador.practicajs.excepciones;

public class FileNotCreatedException extends Exception {

    private static final long serialVersionUID = 1L;

    public FileNotCreatedException(String message) {
        super(message);
    }

}
