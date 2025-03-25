package es.upm.pdl.procesador.practicajs.items;

import java.util.List;

public class AccionSemantica {
    private List<Integer> estados;
    private String accion;

    public AccionSemantica(List<Integer> secuenciaEstados, String accion) {
        this.estados = secuenciaEstados;
        this.accion = accion;
    }

    public String getAccionSemantica (List<Integer> secuenciaEstados) {
        if (secuenciaEstados.equals(estados)) {
            return accion;
        }
        return null;
    }
    
}
