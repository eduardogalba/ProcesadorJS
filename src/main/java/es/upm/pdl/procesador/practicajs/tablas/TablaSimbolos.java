package es.upm.pdl.procesador.practicajs.tablas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import es.upm.pdl.procesador.practicajs.analizadores.AnalizadorSemantico;
import es.upm.pdl.procesador.practicajs.excepciones.FileNotCreatedException;
import es.upm.pdl.procesador.practicajs.excepciones.SymbolAlreadyInsertException;
import es.upm.pdl.procesador.practicajs.items.Simbolo;
import es.upm.pdl.procesador.practicajs.utils.FileUtils;

public class TablaSimbolos {

    private Map<String, Simbolo> simbolos;
    private int nivel;

    private String nombreTabla = "";

    private static BufferedWriter simbWriter;

    private static final String SIMBOLOS_FILE = "." + File.separator + "simbolos%s.txt";

    public TablaSimbolos(int id) {
        simbolos = new LinkedHashMap<>();
        this.nivel = id;
    }

    public Entry<String, Simbolo> obtenerPosTS(int pos) {
        return (Entry<String, Simbolo>) simbolos.entrySet().toArray()[pos];
    }

    public Entry<String, Simbolo> obtenerPosTS(String lexema) {
        if (!simbolos.containsKey(lexema)) {
            return null;
        }

        for (Entry<String, Simbolo> simbolo : simbolos.entrySet()) {
            if (simbolo.getKey().equals(lexema)) {
                return simbolo;
            }
        }

        return null;
    }

    public String obtenerLexemaTS(Entry<String, Simbolo> posTS) {
        return posTS.getKey();
    }

    public String obtenerTipoTS(Entry<String, Simbolo> posTS) {
        return simbolos.get(obtenerLexemaTS(posTS)).getTipo();
    }

    public Entry<String, Simbolo> insertar(String lexema) throws SymbolAlreadyInsertException {
        if (!simbolos.containsKey(lexema)) {
            simbolos.put(lexema, new Simbolo(lexema));
            return obtenerPosTS(lexema);
        } else {
            throw new SymbolAlreadyInsertException(lexema);
        }
    }

    public void insertaTipoRetorno(Entry<String, Simbolo> posTS, String tipoRet) {
        simbolos.get(obtenerLexemaTS(posTS)).setTipoRetorno(tipoRet);
    }

    public void insertaTipoTS(Entry<String, Simbolo> posTS, String tipo) {
        simbolos.get(obtenerLexemaTS(posTS)).setTipo(tipo);
    }

    public void insertaDesplTS(Entry<String, Simbolo> posTS, int despl) {
        simbolos.get(obtenerLexemaTS(posTS)).setDespl(despl);
    }

    public void insertaEtiqTS(Entry<String, Simbolo> posTS) {
        simbolos.get(obtenerLexemaTS(posTS)).setEtiqueta(nuevaEtiqueta(posTS));
    }

    public void insertaTipoParamsTS(Entry<String, Simbolo> posTS, List<String> tipos) {
        simbolos.get(obtenerLexemaTS(posTS)).setTipoParams(tipos);
    }

    public List<String> obtenerTipoParamsTS(Entry<String, Simbolo> posTS) {
        return simbolos.get(obtenerLexemaTS(posTS)).getTipoParams();
    }

    public int obtenerNumParamsTS(Entry<String, Simbolo> posTS) {
        return simbolos.get(obtenerLexemaTS(posTS)).getNumParams();
    }

    public List<String> obtenerModoParamsTS(Entry<String, Simbolo> posTS) {
        return simbolos.get(obtenerLexemaTS(posTS)).obtenerModoParams();
    }

    public void insertaNumParamsTS(Entry<String, Simbolo> posTS, int numParams) {
        simbolos.get(obtenerLexemaTS(posTS)).setNumParams(numParams);
    }

    public void insertaModoParamsTS(Entry<String, Simbolo> posTS, List<String> modos) {
        simbolos.get(obtenerLexemaTS(posTS)).setModoParams(modos);
    }

    public void exportar() throws IOException, FileNotCreatedException {
        if (nivel == 0) {
            String tsgFileName = String.format(SIMBOLOS_FILE, "");
            File file = new File(tsgFileName);
            if (file.exists()) {
                file.delete();
            }
            simbWriter = FileUtils.createBufferedWriter(tsgFileName, true);
        } else {
            simbWriter = FileUtils.createBufferedWriter(String.format(SIMBOLOS_FILE, String.valueOf(nivel)), true);
        }
        
        simbWriter.write(String.format("TABLA %s # %d : %n", (nivel == 0) ? "PRINCIPAL" : String.format("de la FUNCION %s", nombreTabla), nivel));
        for (Entry<String, Simbolo> entry : simbolos.entrySet()) {
            Simbolo simbolo = entry.getValue();
            simbWriter.write(String.format("* LEXEMA : '%s'%n", simbolo.getLexema()));
            simbWriter.write("  Atributos : \n");
            simbWriter.write(
                    String.format("  + tipo : \t\t'%s'%n", (simbolo.getTipo() == null) ? "" : simbolo.getTipo()));

            if (simbolo.getTipo() != null && AnalizadorSemantico.TIPO_FUNCION.equals(simbolo.getTipo()) || simbolo.getNumParams() > 0) {
                simbWriter.write(String.format("   + numParam : \t\t%d%n", simbolo.getNumParams()));
                for (int i = 0; i < simbolo.getNumParams(); i++) {
                    simbWriter.write(String.format("    + TipoParam0%d : '%s'%n", i + 1, simbolo.getTipoParams(i)));
                    simbWriter.write(String.format("     + ModoParam0%d : '%s'%n", i + 1, simbolo.getModoParams(i)));
                }

                simbWriter.write(String.format("    + Tipo de retorno : '%s'%n",
                        simbolo.getTipoRetorno() == null ? "" : simbolo.getTipoRetorno()));

                simbWriter.write(String.format("   + EtiqFuncion : '%s'%n",
                        (simbolo.getEtiqueta() == null) ? "" : simbolo.getEtiqueta()));
            } else {
                simbWriter.write(
                    String.format("  + despl : \t\t%d%n", (simbolo.getDespl() == null) ? 0 : simbolo.getDespl()));
            }

        }
        simbWriter.write(String.format("------------------------------%n", nivel));

        if (nivel == 0) {
            boolean hayError = false;
            for (int i = 1; !hayError; i++) {
                File file = new File(String.format(SIMBOLOS_FILE, i));
                if (!file.exists()) {
                    hayError = true;
                } else {
                    FileUtils.concatenateFiles(file, simbWriter);
                    file.delete();
                }
            }
        }

        simbWriter.close();
    }

    public String nuevaEtiqueta(Entry<String, Simbolo> posTS) {
        return String.format("etiq_%s_%d", obtenerLexemaTS(posTS), nivel);
    }

    public void setNombreTabla(String nombreTabla) {
        this.nombreTabla = nombreTabla;
    }

    public int getNivel() {
        return nivel;
    }

    public void destroy() throws IOException, FileNotCreatedException {
        exportar();
        simbolos.clear();
    }
}
