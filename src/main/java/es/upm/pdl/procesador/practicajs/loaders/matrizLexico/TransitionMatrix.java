package es.upm.pdl.procesador.practicajs.loaders.matrizLexico;

import java.util.List;

import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.InvalidMatrixException;

public class TransitionMatrix {
    private int[][] data;
    private int nEstados;
    private int nEstadosIntermedios;

    public TransitionMatrix(int nEstados, int rows, int cols, List<String> matrix) throws InvalidMatrixException {
        this.nEstados = nEstados;
        this.nEstadosIntermedios = rows;
        this.data = createMatrix(rows, cols,  matrix);
    }

    private int [][] createMatrix (int rows, int columns, List<String> matrixSt) throws InvalidMatrixException {
        int [][] matrixRes = new int [rows][columns];
        try {
            for (int i = 0; i < rows; i++) {
                String [] row = matrixSt.get(i).split("\\t");
                for (int j = 0; j < columns; j++) {
                    matrixRes[i][j] = Integer.parseInt(row[j]);
                }
            }
        } catch (NumberFormatException e) {
            throw new InvalidMatrixException("Error parsing matrix", e);
        }
        return matrixRes;

    }

    public int getnEstados() {
        return nEstados;
    }

    public int getnEstadosIntermedios() {
        return nEstadosIntermedios;
    }

    public boolean isFinal(int estado) {
        return estado >= nEstadosIntermedios;
    }

    public boolean isError(int estado) {
        return estado > nEstados;
    }

    public int getEstado(int estado, int caracter) {
        try {
            return data[estado][caracter];
        } catch (ArrayIndexOutOfBoundsException e) {
            return 30;
        }
    }
}
