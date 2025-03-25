package es.upm.pdl.procesador.practicajs.items;

public class AccionSintactica<C, I> {
    private C action;
    private I estado;

    public AccionSintactica(C action, I estado) {
        this.action = action;
        this.estado = estado;
    }

    public C getAccion() {
        return action;
    }

    public I getEstado() {
        return estado;
    }
}
