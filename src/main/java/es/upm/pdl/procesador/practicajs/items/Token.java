package es.upm.pdl.procesador.practicajs.items;

public class Token <S, I> {
    private S tipo;
    private I posTs;

    public Token (S key, I value) {
        this.tipo = key;
        this.posTs = value;
    }

    public S getTipo() {
        return tipo;
    }

    public I getPosTs() {
        return posTs;
    }
}
