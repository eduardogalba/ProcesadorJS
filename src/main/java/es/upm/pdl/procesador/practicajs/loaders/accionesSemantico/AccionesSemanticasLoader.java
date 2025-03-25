package es.upm.pdl.procesador.practicajs.loaders.accionesSemantico;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.upm.pdl.procesador.practicajs.items.AccionSemantica;
import es.upm.pdl.procesador.practicajs.loaders.tablagoto.exceptions.InvalidTableException;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class AccionesSemanticasLoader {

    private static final String ACCIONES_SEM_FILE = "/files/acciones_semantico.txt";

    private AccionesSemanticasLoader () {}

    public static AccionSemantica [] loadSemanticActions () throws AccionSemanticaDoesNotExistException {
        try {
            List<String> lines = FileUtils.readAllLines(ACCIONES_SEM_FILE);
            if (lines == null) {
                String msg = String.format("Semantic actions file not found: %s", ACCIONES_SEM_FILE);
                throw new InvalidTableException(msg);
            }
            AccionSemantica [] acciones = new AccionSemantica[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                Pattern pattern = Pattern.compile("\\{([0-9\\s]+)\\}\\s*(\\w+)");
                Matcher matcher = pattern.matcher(lines.get(i));

                if (matcher.find()) {
                    String numerosStr = matcher.group(1); 
                    String palabra = matcher.group(2);    
                    
                    List<Integer> numeros = new ArrayList<>();
                    String[] numerosArray = numerosStr.split("\\s+");
                    for (int j = numerosArray.length - 1; j >= 0; j--) {
                        numeros.add(Integer.parseInt(numerosArray[j]));  
                    }

                    acciones[i] = new AccionSemantica(numeros, palabra);
                }
            }
            return acciones;
        } catch (Exception e) {
            String msg = String.format("Error reading semantic actions file %s: %s", ACCIONES_SEM_FILE, e.getMessage());
            throw new AccionSemanticaDoesNotExistException(msg, e);
        } 
    }
}
