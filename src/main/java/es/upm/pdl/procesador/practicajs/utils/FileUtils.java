package es.upm.pdl.procesador.practicajs.utils;

import es.upm.pdl.procesador.practicajs.excepciones.FileNotCreatedException;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.MatrixLoader;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.InvalidMatrixException;
import es.upm.pdl.procesador.practicajs.loaders.matrizLexico.exceptions.MatrixDoesNotExistException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class FileUtils {

	private FileUtils () {}

    private static File createFile (String path) throws FileNotCreatedException, IOException {
        File file = new File(path);
		if (!file.exists()) {
			boolean dirCreated = file.getParentFile().exists() || file.getParentFile().mkdirs(); // Create parent directories if they don't exist
			boolean fileCreated = dirCreated && file.createNewFile();

			if (!fileCreated) {
				throw new FileNotCreatedException("Failed to create tokens file.");
			}
		} else {
            file.delete();
            return createFile(path);
        }

        return file;
    }

	public static BufferedWriter createBufferedWriter (String path) throws IOException, FileNotCreatedException {
		File file = createFile(path);
		return new BufferedWriter(new FileWriter(file));
	}

	public static BufferedWriter createBufferedWriter (String path, boolean appendMode) throws IOException, FileNotCreatedException {
		File file = createFile(path);
		return new BufferedWriter(new FileWriter(file, appendMode));
	}

	public static void concatenateFiles(File sourceFile, BufferedWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) { // 'true' para modo append
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

	public static List<String> readAllLines(String filePath) throws IOException {
        InputStream resource = FileUtils.class.getResourceAsStream(filePath);
        if (resource == null) {
            return null;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

}
