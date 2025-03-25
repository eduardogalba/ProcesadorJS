package es.upm.pdl.procesador.practicajs.items;

public class Regla<A, K> {
    private A antecedente;
    private K nTokens;

    public Regla(A s, K k) {
        antecedente = s;
        nTokens = k;
    }

    public A getAntecedente() {
        return antecedente;
    }

    public K getNtokens() {
        return nTokens;
    }
}
