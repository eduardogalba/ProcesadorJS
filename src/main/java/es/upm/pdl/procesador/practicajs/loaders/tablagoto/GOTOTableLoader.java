package es.upm.pdl.procesador.practicajs.loaders.tablagoto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.upm.pdl.procesador.practicajs.items.Regla;
import es.upm.pdl.procesador.practicajs.loaders.tablagoto.exceptions.InvalidTableException;
import es.upm.pdl.procesador.practicajs.tablas.TablaGOTO;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class GOTOTableLoader {
    private static final String FILES_PATH = "/files/";
    private static final String GOTO_FILE = FILES_PATH + "tabla_goto.txt";
    private static final String ACTION_FILE = FILES_PATH + "tabla_accion.txt";
    private static final String REGLAS_FILE = FILES_PATH + "reglas.txt";

    private GOTOTableLoader() {
    }

    public static TablaGOTO loadTable () throws TableDoesNotExistException, InvalidTableException {
        return new TablaGOTO(loadTableFromFile(GOTO_FILE), loadTableFromFile(ACTION_FILE), loadReglas());
    }

    private static Map<String, List<String>> loadTableFromFile(String filePath) throws TableDoesNotExistException, InvalidTableException {
        try {
            List<String> lines = FileUtils.readAllLines(filePath);
            if (lines == null) {
                String msg = String.format("Error reading table file %s", filePath);
                throw new InvalidTableException(msg);
            }
            String[] colums = lines.get(0).split("[ \t]+");
            List<String> matrix = lines.subList(1, lines.size());
            
            return fillTable(colums, matrix);
        } catch (IOException e) {
            String msg = String.format("Error reading table file %s: %s", filePath, e.getMessage());
            throw new InvalidTableException(msg, e);
        } catch (IndexOutOfBoundsException e) {
            String msg = String.format("Error with table dimensions from file %s: %s", filePath, e.getMessage());
            throw new InvalidTableException(msg, e);
        }
    }

    private static Map<String, List<String>> fillTable (String [] columns, List<String> matrix) {
        Map<String, List<String>> table = new HashMap<>();
        for (String column : columns) {
            table.put(column, new LinkedList<>());
        }

        for (String row : matrix) {
            String[] values = row.split("[\t]+");
            for (int i = 0; i < columns.length; i++) {
                table.get(columns[i]).add(values[i]);
            }
        }
        return table;
    }

    private static Regla<String, Integer> [] loadReglas() throws TableDoesNotExistException, InvalidTableException {
        try {
            List<String> lines = FileUtils.readAllLines(REGLAS_FILE);
            if (lines == null) {
                String msg = String.format("Error reading table file %s", REGLAS_FILE);
                throw new InvalidTableException(msg);
            }
            Regla<String, Integer> [] reglas = new Regla[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                String[] values = lines.get(i).split(" ");
                reglas[i] = new Regla<>(values[0], Integer.parseInt(values[1]));
            }
            return reglas;
        } catch (IOException e) {
            String msg = String.format("Error reading rules file %s: %s", REGLAS_FILE, e.getMessage());
            throw new TableDoesNotExistException(msg, e);
        } catch (IndexOutOfBoundsException e) {
            String msg = String.format("Error with rules dimensions in file %s: %s", REGLAS_FILE, e.getMessage());
            throw new TableDoesNotExistException(msg, e);
        }
    }
}
