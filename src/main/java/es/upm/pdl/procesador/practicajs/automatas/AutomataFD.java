package es.upm.pdl.procesador.practicajs.automatas;

import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.MatrixLoader;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.TransitionMatrix;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.InvalidMatrixException;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.MatrixDoesNotExistException;

public class AutomataFD {
    private TransitionMatrix matrizTransicion;
    private int estadoActual;
    private char accionActual;
    private TransitionMatrix acciones;

    public AutomataFD (String analizador) throws MatrixDoesNotExistException, InvalidMatrixException {
        this.matrizTransicion = MatrixLoader.loadMatrix(analizador);
        this.acciones = MatrixLoader.loadMatrix("acciones_" + analizador);
        this.estadoActual = 0;
    }

    public int getEstado () {
        return estadoActual;
    }

    public int nextEstado (char simbolo) {
        estadoActual = matrizTransicion.getEstado(estadoActual, simbolo);
        return estadoActual;
    }

    public char getAccion () {
        return accionActual;
    }

    public char nextAccion (char simbolo) {
        accionActual = (char) acciones.getEstado(estadoActual, simbolo);
        return accionActual;
    }

    public boolean esFinal () {
        return matrizTransicion.isFinal(estadoActual);
    }

    public boolean esError () {
        return matrizTransicion.isError(estadoActual);
    }

    public void reset () {
        this.estadoActual = 0;
    }
}
