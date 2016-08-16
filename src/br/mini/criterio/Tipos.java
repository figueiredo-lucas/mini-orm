package br.mini.criterio;

import br.mini.Utils;
import br.mini.Constantes.Sql;
import br.mini.Utils.Strings;
import br.mini.annotations.PseudoTabela;
import br.mini.annotations.Tabela;

/**
 * Classe abstrata que generaliza as possíveis utilizações dos dados de {@link #campo} através de suas especializações
 *
 * @author figueiredo-lucas
 *
 */
public abstract class Tipos {

    /**
     * Campo a ser utilizado
     */
    private String campo;

    /**
     * Join de referência, caso o campo seja necessário dentro de um Join já referenciado pelo {@link Criterio#addJoin}
     */
    private Join joinRef;

    protected Tipos(final String campo, final Join joinRef) {
        this.campo = campo;
        this.joinRef = joinRef;
    }

    /**
     * Método utilizado para obter o campo setado em {@link #campo}
     *
     * @return {@link #campo}
     */
    String getCampo() {
        return campo;
    }

    /**
     * Atribuição de valor da variável {@link #campo}
     *
     * @param campo
     */
    public void setCampo(String campo) {
        this.campo = campo;
    }

    /**
     * Método utilizado para obter {@link #joinRef}
     *
     * @return {@link #joinRef}
     */
    Join getJoinRef() {
        return joinRef;
    }

    /**
     * Setter de {@link #joinRef}
     *
     * @param joinRef
     */
    void setJoinRef(Join joinRef) {
        this.joinRef = joinRef;
    }

    /**
     * Método para obter o campo com o alias da tabela referenciada na classe de parâmetro
     *
     * @param principal
     * @return {@link #campo} com alias
     */
    String obterCampo(final Class<?> principal) {
        return obterCampo(principal, Boolean.FALSE);
    }

    /**
     * Método para obter o campo com o alias da tabela referenciada na classe de parâmetro e a validação caso exista
     * clob
     *
     * @param principal
     * @param existeClob
     * @return {@link #campo} com alias
     */
    String obterCampo(final Class<?> principal, final Boolean existeClob) {
        final String alias = existeClob ? "sub" : obterAlias(principal);
        return alias + Sql.PONTO + getCampo();
    }

    /**
     * Método para obter o campo com o alias da tabela referenciada na classe de parâmetro do tipo {@link PseudoTabela}
     *
     * @param principal
     * @return {@link #campo} com alias
     */
    String obterCampoPseudo(final Class<?> principal) {
        final String campo;
        final Class<?> referencia = Utils.Criterios.capturarCampoReferencia(getCampo(), principal);
        if (getJoinRef() == null) {
            if (referencia == null) {
                campo = principal.getAnnotation(PseudoTabela.class).alias() + Sql.PONTO + getCampo();
            } else {
                campo = Utils.Strings.descapitalizaTabela(referencia.getAnnotation(Tabela.class).nome()) + "1"
                        + Sql.PONTO + getCampo();
            }
        } else {
            campo = obterAlias(null) + Sql.PONTO + getCampo();
        }
        return campo;
    }

    /**
     * Método para obter o alias da tabela referenciada pela classe
     *
     * @param principal
     * @return alias da tabela
     */
    String obterAlias(final Class<?> principal) {
        if (getJoinRef() == null) {
            return Strings.descapitalizaTabela(principal.getAnnotation(Tabela.class).nome()) + "1";
        }
        String nomeTabelaSecundaria = getJoinRef().getClasseSecundaria().getAnnotation(Tabela.class).nome();
        return Utils.Strings.descapitalizaTabela(nomeTabelaSecundaria) + getJoinRef().getIndiceSecundaria();
    }

}
