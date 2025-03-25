package es.upm.pdl.procesador.practicajs.excepciones;

public class SymbolAlreadyInsertException extends Exception {

    private static final long serialVersionUID = 1L;

    public SymbolAlreadyInsertException(String lexema) {
        super("El simbolo " + lexema + " ya ha sido insertado en la tabla de simbolos");
    }
}
