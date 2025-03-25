package es.upm.pdl.procesador.practicajs.tablas;

import java.util.Map;
import java.util.Map.Entry;

import es.upm.pdl.procesador.practicajs.items.PR;

public class TablaPR {

    private Map<String, PR> palabrasReservadas;

    public TablaPR(Map<String, PR> prSuppliers) {
        this.palabrasReservadas = prSuppliers;
    }

    public Entry<String, PR> buscar (String lexema) {

        for (Entry<String, PR> palabra : palabrasReservadas.entrySet()) {
            if (palabra.getValue().getLexema().equals(lexema)) {
                return palabra;
            }
        }

        return null;
    }
}
