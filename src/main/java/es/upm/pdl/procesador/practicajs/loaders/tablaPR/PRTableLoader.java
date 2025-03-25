package es.upm.pdl.procesador.practicajs.loaders.tablaPR;

import java.util.Map;

import es.upm.pdl.procesador.practicajs.items.PR;
import es.upm.pdl.procesador.practicajs.tablas.TablaPR;

public class PRTableLoader {
    private static final Map<String, PR> prSuppliers = Map.ofEntries(
            Map.entry("BOOLEAN", new PR("boolean")),
            Map.entry("FUNCTION", new PR("function")),
            Map.entry("IF", new PR("if")),
            Map.entry("ELSE", new PR("else")),
            Map.entry("INT", new PR("int")),
            Map.entry("INPUT", new PR("input")),
            Map.entry("OUTPUT", new PR("output")),
            Map.entry("RETURN", new PR("return")),
            Map.entry("STRING", new PR("string")),
            Map.entry("VAR", new PR("var")),
            Map.entry("VOID", new PR("void"))
        );

    private PRTableLoader () {}

    public static TablaPR loadPRTable () {
        return new TablaPR(prSuppliers);
    }
}
