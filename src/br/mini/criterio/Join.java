package br.mini.criterio;

import br.mini.enums.JoinEnum;

/**
 * Classe para encapsular os parâmetros vindos através do método {@link Criterio#addJoin}
 *
 * <br>
 * <br>
 * <b>aliasPrincipal</b> = {@link #classePrincipal}.@Tabela.nome descapitalizada + {@link #indicePrincipal} <br>
 * <b>aliasSecundario</b> = {@link #classeSecundaria}.@Tabela.nome descapitalizada + {@link #indiceSecundaria} <br>
 * Código gerado: <br>{@link #tipoJoin} JOIN {@link #classeSecundaria}.@Tabela.nome
 * <b>aliasSecundario</b> ON (<b>aliasPrincipal</b>. {@link #campoClassePrincipal} = <b>aliasSecundario</b>.
 * {@link #campoClasseSecundaria})
 *
 * @author figueiredo-lucas
 *
 */
public class Join {

    /**
     * Classe principal do join - tabela já referenciada
     */
    private Class<?> classePrincipal;

    /**
     * Classe secundária do join - tabela a ser referenciada
     */
    private Class<?> classeSecundaria;

    /**
     * Tipo de join utilizado
     */
    private JoinEnum tipoJoin;

    /**
     * Campo da classe principal a ser referenciado
     */
    private String campoClassePrincipal;

    /**
     * Campo da classe secundária a ser referenciada
     */
    private String campoClasseSecundaria;

    /**
     * Indice da classe principal - necessário caso haja mais de um referência para a tabela principal
     */
    private int indicePrincipal;

    /**
     * Indice da classe secundária - necessário caso haja mais de um referência para a tabela secundária
     */
    private int indiceSecundaria;

    Join(JoinEnum tipoJoin, Class<?> classePrincipal, Class<?> classeSecundaria, String campoClassePrincipal,
            String campoClasseSecundaria, int indice) {
        this.tipoJoin = tipoJoin;
        this.classePrincipal = classePrincipal;
        this.classeSecundaria = classeSecundaria;
        this.campoClassePrincipal = campoClassePrincipal;
        this.campoClasseSecundaria = campoClasseSecundaria;
        this.indicePrincipal = indice;
        this.indiceSecundaria = 1;
    }

    Class<?> getClassePrincipal() {
        return classePrincipal;
    }

    public Class<?> getClasseSecundaria() {
        return classeSecundaria;
    }

    String getTipoJoin() {
        String txtTipoJoin = null;
        switch (tipoJoin) {
            case INNER:
                txtTipoJoin = "INNER JOIN";
                break;
            case LEFT:
                txtTipoJoin = "LEFT OUTER JOIN";
                break;
            case RIGHT:
                txtTipoJoin = "RIGHT OUTER JOIN";
                break;
        }
        return txtTipoJoin;
    }

    String getCampoClassePrincipal() {
        return campoClassePrincipal;
    }

    String getCampoClasseSecundaria() {
        return campoClasseSecundaria;
    }

    int getIndicePrincipal() {
        return indicePrincipal;
    }

    int getIndiceSecundaria() {
        return indiceSecundaria;
    }

    void setIndiceSecundaria(int indiceSecundaria) {
        this.indiceSecundaria = indiceSecundaria;
    }
}
