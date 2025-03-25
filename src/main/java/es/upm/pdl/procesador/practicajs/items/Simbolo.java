package es.upm.pdl.procesador.practicajs.items;

import java.util.Arrays;
import java.util.List;

public class Simbolo {
    private String lexema;
    private String tipo;
    private Integer despl;
    private Integer numParams;
    private String[] tipoParams;
    private String [] modoParams;
    private String tipoRetorno;
    private String etiqueta;

    public Simbolo (String lexema) {    
        this.lexema = lexema;
        this.tipo = null;
        this.despl = null;
        this.numParams = 0;
        this.tipoParams = null;
        this.modoParams = null;
        this.tipoRetorno = null;
    }

    public String getLexema () {
        return this.lexema;
    }

    public void setTipo (String type) {
        this.tipo = type;
    }

    public String getTipo () {
        return this.tipo;
    }

    public void setDespl (int despl) {
        this.despl = despl;
    }

    public Integer getDespl () {
        return this.despl;
    }

    public void setNumParams (int numParams) {
        this.numParams = numParams;
    }

    public Integer getNumParams () {
        return this.numParams;
    }

    public void setTipoParams (List<String> typeParams) {
        this.tipoParams = typeParams.toArray(new String[typeParams.size()]);
    }

    public List<String> getTipoParams () {
        return (tipoParams != null) ? Arrays.asList(tipoParams) : null;
    }

    public String getTipoParams (int i) {
        return tipoParams[i];
    }

    public String getModoParams(int i) {
        return modoParams[i];
    }

    public void setModoParams(List<String> modoParams) {
        this.modoParams = modoParams.toArray(new String[modoParams.size()]);
    }

    public List<String> obtenerModoParams () {
        return Arrays.asList(modoParams);
    }

    public void setTipoRetorno (String returnType) {
        this.tipoRetorno = returnType;
    }

    public String getTipoRetorno () {
        return this.tipoRetorno;
    }

    public void setDespl(Integer despl) {
        this.despl = despl;
    }

    public void setNumParams(Integer numParams) {
        this.numParams = numParams;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }


}
