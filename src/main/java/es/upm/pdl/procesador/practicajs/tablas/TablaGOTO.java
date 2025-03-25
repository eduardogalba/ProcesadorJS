package es.upm.pdl.procesador.practicajs.tablas;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import es.upm.pdl.procesador.practicajs.items.AccionSintactica;
import es.upm.pdl.procesador.practicajs.items.Regla;
import es.upm.pdl.procesador.practicajs.items.Simbolo;
import es.upm.pdl.procesador.practicajs.items.Token;

public class TablaGOTO {

    private Map<String, List<String>> gotoTable;
    private Map<String, List<String>> actionTable;
    private Regla<String, Integer>[] reglas;

    public TablaGOTO(Map<String, List<String>> gotoTable, Map<String, List<String>> actionTable,
            Regla<String, Integer>[] reglas) {
        this.gotoTable = gotoTable;
        this.actionTable = actionTable;
        this.reglas = reglas;
    }

    public AccionSintactica<Character, Integer> getAction(Token<String, Entry<String, Simbolo>> token, int estado)
            throws NumberFormatException {
        String accionString = actionTable.get(token.getTipo()).get(estado);

        if (accionString.equals(" ")) {
            return null;
        } else if ((accionString.length() == 1) && accionString.charAt(0) == 'a') {
            return new AccionSintactica<>('a', null);
        } else if ((accionString.charAt(0) == 'r' || accionString.charAt(0) == 'd')) {
            return new AccionSintactica<>(accionString.charAt(0), Integer.parseInt(accionString.substring(1)));
        }

        return null;
    }

    public int ruleSteps(int regla) {
        return reglas[regla].getNtokens();
    }

    public int goTo(int estado, int regla) throws NumberFormatException {
        String reglaAntecedente = reglas[regla].getAntecedente();
        return Integer.parseInt(gotoTable.get(reglaAntecedente).get(estado));
    }

    public List<String> posiblesTokens(int estado) {
        return actionTable.keySet().stream().filter(k -> !actionTable.get(k).get(estado).equals(" "))
                .collect(Collectors.toList());
    }

    public boolean buscarInterseccion(String tokenToFit, String tokenChosen, List<Integer> pilaEstados) {
        int estadoCima = pilaEstados.get(pilaEstados.size() - 1);
        List<String> posibles = posiblesTokens(estadoCima);
        if (posibles != null && posibles.size() == 1) {
            if (posibles.get(0).equals(tokenToFit)) {
                return true;
            } else if (posibles.get(0).equals(tokenChosen)) {
                AccionSintactica<Character, Integer> accion = getAction(new Token<>(tokenChosen, null), estadoCima);
                if (accion.getAccion() == 'd') {
                    pilaEstados.add(accion.getEstado());
                    return buscarInterseccion(tokenToFit, tokenChosen, pilaEstados);
                } else if (accion.getAccion() == 'r') {
                    int ruleSteps = ruleSteps(accion.getEstado());
                    if (ruleSteps > pilaEstados.size()) {
                        return false;
                    }
                    for (int i = 0, j = pilaEstados.size() - 1; i < ruleSteps && j >= 0; i++, j--) {
                        pilaEstados.remove(pilaEstados.get(j));
                    }
                    estadoCima = pilaEstados.get(pilaEstados.size() - 1);
                    int nuevoEstado = goTo(estadoCima, accion.getEstado());
                    pilaEstados.add(nuevoEstado);
                    return buscarInterseccion(tokenToFit, tokenChosen, pilaEstados);
                }
            }
        } else if (posibles.size() > 1) {
            return posibles.contains(tokenToFit);
        }

        return false;
    }

    public String tokenIntermedio(String tokenPost, List<Integer> pilaEstados) {
        int estado = pilaEstados.get(pilaEstados.size() - 1);
        List<String> posibles = posiblesTokens(estado);
        List<Integer> nuevaLista = new LinkedList<>();
        nuevaLista.addAll(pilaEstados);
        for (String token : posibles) {
            AccionSintactica<Character, Integer> accion = getAction(new Token<>(token, null), estado);
            int nuevoEstado;
            if (accion == null) {
                continue;
            } else if (accion.getAccion() == 'd') {
                nuevoEstado = accion.getEstado();
                nuevaLista.add(nuevoEstado);
            } else if (accion.getAccion() == 'r') {
                int ruleSteps = ruleSteps(accion.getEstado());
                if (ruleSteps > nuevaLista.size())
                    continue;
                for (int i = 0, j = nuevaLista.size() - 1; i < ruleSteps && j > 0; i++, j--) {
                    nuevaLista.remove(nuevaLista.get(j));
                }
                nuevoEstado = goTo(nuevaLista.get(nuevaLista.size() - 1), accion.getEstado());
                nuevaLista.add(nuevoEstado);
            }

            if (buscarInterseccion(tokenPost, token, nuevaLista)) {
                return token;
            }

            nuevaLista.clear();
            nuevaLista.addAll(pilaEstados);
        }

        return null;
    }
}
