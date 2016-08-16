package br.mini.criterio;

import java.util.ArrayList;
import java.util.List;

import br.mini.enums.JoinEnum;
import br.mini.enums.LogicEnum;
import br.mini.enums.OrderEnum;
import br.mini.enums.ParamEnum;

/**
 * Classe responsável pela criação dos critérios para consulta parametrizada
 *
 * @author figueiredo-lucas
 *
 */
public class CriterioBase {

    /**
     * Lista de parâmetros setados através do {@link #addCriterio}
     */
    protected final List<Parametros> parametrosList;

    /**
     * Builder de condição possível a ser atribuída através do {@link #setCondicao(String)}
     */
    protected final StringBuilder condicao;

    /**
     * Lista dos joins setados através do {@link #addJoin}
     */
    protected final List<Join> joinList;

    /**
     * Lista de order by setados através do {@link #addOrderBy}
     */
    protected final List<Ordenacao> orderList;

    /**
     * Agrupamento de parâmetros por parênteses
     */
    protected final Grupo grupo;

    /**
     * Quantidade máxima de registros a serem retornados para o critério atual
     */
    protected int qtdMaxima = 0;

    /**
     * Consutrutor único da classe.
     */
    protected CriterioBase() {
        grupo = new Grupo();
        condicao = new StringBuilder();
        joinList = new ArrayList<Join>();
        orderList = new ArrayList<Ordenacao>();
        parametrosList = new ArrayList<Parametros>();
    }

    /**
     * Getter do grupo para manipulação. A mesma podendo ser feita através da classe {@link Grupo}
     *
     * @return Grupo para manipulação
     */
    public Grupo getGrupo() {
        return grupo;
    }

    /**
     * Getter para a lista de parâmetros setados através da API para manipulação
     *
     * @return Lista de parâmetros já atribuídos através de {@link #addCriterio}
     */
    public List<Parametros> getParametrosList() {
        return parametrosList;
    }

    /**
     * Getter da variável de instância {@link #condicao}
     *
     * @return StringBuilder com a condição gerada
     */
    public StringBuilder getCondicao() {
        return condicao;
    }

    /**
     * Atribuição da variável de instância {@link #condicao} para consultas sem utilizar da API Criterio
     *
     * @param condicao
     */
    public void setCondicao(String condicao) {
        this.condicao.append(condicao);
    }

    /**
     * Adiciona um critério de busca com o operador lógico padrão: <code>Criterio.AND</code>
     *
     * @param campo
     * @param parametro
     * @param valor
     */
    public Integer addCriterio(String campo, ParamEnum parametro, Object valor) {
        return addCriterio(campo, parametro, valor, LogicEnum.AND);
    }

    /**
     * Adiciona um critério de busca com um operador lógico <code>Criterio.AND</code> ou <code>Criterio.OR</code>
     *
     * @param campo
     * @param parametro
     * @param valor
     * @param opLogico
     */
    public Integer addCriterio(String campo, ParamEnum parametro, Object valor, LogicEnum opLogico) {
        return addCriterio(campo, parametro, valor, opLogico, null);
    }

    /**
     * Adiciona um critério de busca para a subclasse do join com o operador lógico padrão: <code>Criterio.AND</code>
     *
     * @param campo
     * @param parametro
     * @param valor
     * @param joinRef
     */
    public Integer addCriterio(String campo, ParamEnum parametro, Object valor, Join joinRef) {
        return addCriterio(campo, parametro, valor, LogicEnum.AND, joinRef);
    }

    /**
     * Adiciona um critério de busca para a subclasse do join com um operador lógico <code>Criterio.AND</code> ou
     * <code>Criterio.OR</code>
     *
     * @param campo
     * @param parametro
     * @param valor
     * @param opLogico
     * @param joinRef
     */
    public Integer addCriterio(String campo, ParamEnum parametro, Object valor, LogicEnum opLogico, Join joinRef) {
        final Parametros param = new Parametros(campo, parametro, valor, opLogico, joinRef);
        parametrosList.add(param);
        return parametrosList.size() - 1;
    }

    /**
     * Adiciona um join entre a classe principal e a classe secundária Gera um join da forma:<br />
     * <code><b>tipoJoin classeSecundaria ON (classePrincipal.campoClassePrincipal =
     * classeSecundaria.campoClasseSecundaria)</b></code>
     *
     * @param tipoJoin - Criterio.LEFT, INNER, RIGHT
     * @param classePrincipal - Classe a ser referenciada no <code>processarValores</code>
     * @param classeSecundaria - Classe que referencia a anterior
     * @param campoClassePrincipal - Nome do campo no banco que vai fazer a junção
     * @param campoClasseSecundaria - Nome do campo no banco que vai fazer a junção
     * @return {@link Join} criado através dos parâmetros passados
     */
    public Join addJoin(JoinEnum tipoJoin, Class<?> classePrincipal, Class<?> classeSecundaria,
            String campoClassePrincipal, String campoClasseSecundaria) {
        return addJoin(tipoJoin, classePrincipal, classeSecundaria, campoClassePrincipal, campoClasseSecundaria, 1);
    }

    /**
     * Adiciona um join entre a classe principal e a classe secundária Gera um join da forma:<br />
     * <code><b>tipoJoin classeSecundaria ON (classePrincipal.campoClassePrincipal =
     * classeSecundaria.campoClasseSecundaria)</b></cod
     *
     * @param tipoJoin - Criterio.LEFT, INNER, RIGHT
     * @param classePrincipal - Classe a ser referenciada e selecionada pelo @param indice
     * @param classeSecundaria - Classe que referencia a anterior
     * @param campoClassePrincipal - Nome do campo no banco que vai fazer a junção
     * @param campoClasseSecundaria - Nome do campo no banco que vai fazer a junção
     * @param indice - Indice da classe principal (Caso haja mais de um join feito com a mesma classe)
     * @return {@link Join} criado através dos parâmetros passados
     */
    public Join addJoin(JoinEnum tipoJoin, Class<?> classePrincipal, Class<?> classeSecundaria,
            String campoClassePrincipal, String campoClasseSecundaria, int indice) {
        Join j = new Join(tipoJoin, classePrincipal, classeSecundaria, campoClassePrincipal, campoClasseSecundaria,
                indice);
        verificarClasseSecundaria(j);
        joinList.add(j);
        return j;
    }

    /**
     * Adiciona um ORDER BY padrão ascendente pelo campo passado no parâmetro
     *
     * @param campo Campo a ser utilizado para a ordenação
     */
    public void addOrderBy(String campo) {
        addOrderBy(campo, OrderEnum.ASC);
    }

    /**
     * Adiciona um ORDER BY ascendente ou descendente dado pelo ENUM OrderEnum ordenando pelo campo passado no parâmetro
     *
     * @param campo Campo a ser utilizado para a ordenação
     * @param order Tipo de ordenação
     */
    public void addOrderBy(String campo, OrderEnum order) {
        addOrderBy(campo, order, null);
    }

    /**
     * Adiciona um ORDER BY ascendente ou descendente dado pelo ENUM OrderEnum ordenando pelo campo passado no parâmetro
     * com o alias do join de referência
     *
     * @param campo Campo a ser utilizado para a ordenação
     * @param order Tipo de ordenação
     * @param joinRef Join de referência do campo
     */
    public void addOrderBy(String campo, OrderEnum order, Join joinRef) {
        Ordenacao ordenacao = new Ordenacao(campo, order, joinRef);
        orderList.add(ordenacao);
    }

    /**
     * Atribui a quantidade máxima de linhas para ser retornada pela consulta.
     *
     * @param qtdMaxima Quantidade máxima de linhas a ser retornada pela consulta
     */
    public void setQtdMaximaDeLinhas(int qtdMaxima) {
        this.qtdMaxima = qtdMaxima;
    }

    /**
     * Getter de {@link #qtdMaxima}
     *
     * @return Quantidade máxima de linhas
     */
    public int getQtdMaxima() {
        return qtdMaxima;
    }

    /**
     * Método para validar se a classe secundária utilizada pelo JOIN já existe, atribuindo para a mesma um novo
     * identificador caso verdadeiro
     *
     * @param join
     */
    private void verificarClasseSecundaria(Join join) {
        for (Join j : joinList) {
            if (j.getClasseSecundaria().equals(join.getClasseSecundaria())) {
                join.setIndiceSecundaria(j.getIndiceSecundaria() + 1);
            }
        }
        if (join.getIndiceSecundaria() == 1 && join.getClasseSecundaria().equals(join.getClassePrincipal())) {
            join.setIndiceSecundaria(2);
        }
    }

}
