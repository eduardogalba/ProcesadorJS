package es.upm.pdl.procesador.practicajs.loaders.matrizLexico;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.InvalidMatrixException;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.MatrixDoesNotExistException;
import es.upm.pdl.procesador.practicajs.loaders.tablagoto.exceptions.InvalidTableException;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class MatrixLoader {

    private static final String MATRIX_FILE = "/files/matrix_";

    private MatrixLoader() {
    }

    public static TransitionMatrix loadMatrix(String analizador) throws MatrixDoesNotExistException, InvalidMatrixException {
        String filePath = MATRIX_FILE + analizador;

        try {
            List<String> lines = FileUtils.readAllLines(filePath);
            if (lines == null) {
                String msg = String.format("Error reading matrix file %s", filePath);
                throw new InvalidMatrixException(msg);
            }

            String[] cabecera = lines.get(0).split(" ");
            int nEstados = Integer.parseInt(cabecera[0]);
            int rows = Integer.parseInt(cabecera[1]);
            int cols = Integer.parseInt(cabecera[2]);  

            List<String> matrix = lines.subList(1, lines.size());
            return new TransitionMatrix(nEstados, rows, cols, matrix);

        } catch(NoSuchFileException e){
            String msg = String.format("Matrix file %s not found", analizador);
            throw new MatrixDoesNotExistException(msg, e);

        }catch (IOException e) {
            String msg = String.format("Error reading matrix file %s: %s", analizador, e.getMessage());
            throw new InvalidMatrixException(msg, e);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            String msg = String.format("Error parsing matrix dimensions for file %s: %s", analizador, e.getMessage());
            throw new InvalidMatrixException(msg, e);
        }
    }

}
