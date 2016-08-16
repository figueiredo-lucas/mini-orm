package br.mini.criterio;

import java.sql.PreparedStatement;

import br.mini.Constantes.Sql;
import br.mini.Utils;
import br.mini.annotations.PseudoColuna;
import br.mini.annotations.PseudoTabela;
import br.mini.annotations.Tabela;
import br.mini.exception.MiniException;
import br.mini.operacional.ModeloPaginator;
import br.mini.operacional.StringCreator;

/**
 * Classe responsável pela criação dos critérios para consulta parametrizada
 *
 * @author figueiredo-lucas
 *
 */
public class Criterio extends CriterioBase {

    /**
     * Consutrutor único da classe.
     */
    public Criterio() {
        super();
    }

    /**
     * Método responsável pela construção do critério de union. Utilizado para classes com a anotação de
     * {@link PseudoTabela}
     *
     * @param strSelect String a ser concatenada
     * @param classe Classe a ser utilizada na criação da query
     * @return Query construída
     */
    public StringBuilder construirCriterioUnion(final StringBuilder strSelect, final Class<?> classe) {
        final StringBuilder strCriterio = new StringBuilder();
        strCriterio.append(" " + construirJuncao());
        strCriterio.append(" " + construirCondicaoUnion(classe));
        construirOrderUnion(classe, strCriterio);
        return strCriterio;
    }

    /**
     * Método responsável pela construção do critério em classes com a anotação {@link Tabela}
     *
     * @param strSelect String a ser concatenada
     * @param classe Classe a ser utilizada na criação da query
     * @return Query construída
     */
    public StringBuilder construirCriterio(final StringBuilder strSelect, final Class<?> classe) {
        return construirCriterio(strSelect, classe, Boolean.FALSE, 0, 0);
    }

    /**
     * Método responsável pela construção do critério em classes com a anotação {@link Tabela} a partir de uma paginação
     *
     * @param strSelect String a ser concatenada
     * @param classe Classe a ser utilizada na criação da query
     * @param registroPaginado Validação de registro paginado
     * @param rownumDe Parâmetro inicial do rownum
     * @param rownumAte Parâmetro final do rownum
     * @return Query construída
     */
    public StringBuilder construirCriterio(final StringBuilder strSelect, final Class<?> classe,
            final boolean registroPaginado, final Integer rownumDe, final Integer rownumAte) {
        StringBuilder strCriterio = new StringBuilder(strSelect);
        strCriterio.append(" " + construirJuncao());
        strCriterio.append(" " + construirCondicao(classe));

        boolean existeClob = Utils.Clobs.existeClob(classe);
        if (existeClob) {
            strCriterio = StringCreator.criaSelectClob(classe, strCriterio);
        }
        construirOrder(classe, strCriterio, existeClob);
        if (registroPaginado) {
            strCriterio = StringCreator.criaSelectRownum(classe, strSelect);
            construirRownum(strCriterio, rownumDe, rownumAte);
        }
        return strCriterio;
    }

    /**
     * Método responsável pela criação dos joins. <br>
     * Percorre {@link CriterioBase#joinList} e a partir do mesmo cria os JOINs conforme SQL ANSI
     *
     * @return String com os JOINS criados
     */
    StringBuilder construirJuncao() {
        StringBuilder sb = new StringBuilder("");
        if (joinList != null) {
            String tipoJoin;
            String nomeTabelaSecundaria;
            String nomeTabelaPrincipal;
            Class<?> classeSecundaria;
            Class<?> classePrincipal;
            String aliasSecundaria;
            for (Join join : joinList) {
                tipoJoin = join.getTipoJoin();
                // ADICIONA O TIPO DO JOIN
                sb.append(Sql.ESPACO + tipoJoin);
                classePrincipal = join.getClassePrincipal();
                classeSecundaria = join.getClasseSecundaria();
                nomeTabelaPrincipal = Utils.Criterios.obterNomeTabelaPrimaria(classePrincipal);
                nomeTabelaSecundaria = classeSecundaria.getAnnotation(Tabela.class).nome();
                // ADICIONA O NOME DA TABELA SECUNDÁRIA E SEU ALIAS
                aliasSecundaria = Utils.Strings.descapitalizaTabela(nomeTabelaSecundaria) + join.getIndiceSecundaria();
                sb.append(Sql.ESPACO + nomeTabelaSecundaria + Sql.ESPACO + aliasSecundaria);
                sb.append(" ON (");
                // ADICIONA ALIAS DA TABELA PRINCIPAL
                sb.append(Utils.Strings.descapitalizaTabela(nomeTabelaPrincipal) + join.getIndicePrincipal()
                        + Sql.PONTO);
                // ADICIONA COLUNA FK DA TABELA PRINCIPAL
                sb.append(join.getCampoClassePrincipal() + " = ");
                // ADICIONA ALIAS DA TABELA SECUNDÁRIA
                sb.append(aliasSecundaria + Sql.PONTO);
                // ADICIONA COLUNA PK DA TABELA SECUNDÁRIA
                sb.append(join.getCampoClasseSecundaria() + Sql.FECHA_PARENT);
            }
        }
        return sb;
    }

    /**
     * Percorre {@link CriterioBase#parametrosList} e partir do mesmo cria os parâmetros do tipo WHERE param1 AND param2
     * (...)
     *
     * @param principal Classe a ser utilizada na construção para {@link Tabela}
     * @return String com os parâmetros do WHERE setados
     */
    StringBuilder construirCondicao(Class<?> principal) {
        return construirCondicao(principal, Boolean.FALSE);
    }

    /**
     * Percorre {@link CriterioBase#parametrosList} e partir do mesmo cria os parâmetros do tipo WHERE param1 AND param2
     * (...) respeitando as restrições das classes anotadas com {@link PseudoTabela}
     *
     * @param principal Classe a ser utilizada na construção da condição para as {@link PseudoTabela}
     * @return String com os parâmetros do WHERE setados
     */
    StringBuilder construirCondicaoUnion(Class<?> principal) {
        return construirCondicao(principal, Boolean.TRUE);
    }

    /**
     * Método responsável pela criação do order by. <br>
     * Percorre {@link CriterioBase#orderList} e a partir do mesmo cria o ORDER BY conforme SQL ANSI
     *
     * @param principal Classe principal a ser utilizada para ordenação
     * @param sb String a ser concatenada
     * @param existeClob Booleano com a existencia ou não de campo CLOB
     * @return String com o ORDER BY criado
     */
    void construirOrder(final Class<?> principal, final StringBuilder sb, final Boolean existeClob) {
        sb.append(Sql.ESPACO);
        if (orderList != null) {
            String campo = null;
            for (Ordenacao order : orderList) {
                order.adicionarOrdenacao(sb, campo);
                campo = order.obterCampo(principal, existeClob);
                if (order.getJoinRef() != null) {
                    final String subField = order.obterAlias(principal) + "_" + order.getCampo();
                    sb.insert(sb.indexOf("DISTINCT") + 9, campo + " as " + subField + Sql.VIRGULA + Sql.ESPACO);
                    campo = subField;
                }
                sb.append(campo + Sql.ESPACO + order.getOrder());
            }
        }
    }

    /**
     * Método responsável pela criação do order by. <br>
     * Percorre {@link CriterioBase#orderList} e a partir do mesmo cria o ORDER BY conforme SQL ANSI respeitando as
     * restrições das classes anotadas com {@link PseudoTabela}
     *
     * @param principal Classe principal de ordenação
     * @param sb StringBuilder com a String a ser concatenada
     * @return String com o ORDER BY criado
     */
    void construirOrderUnion(Class<?> principal, StringBuilder sb) {
        sb.append(Sql.ESPACO);
        if (orderList != null) {
            String aliasPrincipal = principal.getAnnotation(PseudoTabela.class).alias();
            String campo = null;
            for (Ordenacao order : orderList) {
                order.adicionarOrdenacao(sb, campo);
                Class<?> referencia = Utils.Criterios.capturarCampoReferencia(order.getCampo(), principal);
                if (referencia == null) {
                    campo = aliasPrincipal + Sql.PONTO + order.getCampo();
                } else {
                    campo = Utils.Strings.descapitalizaTabela(referencia.getAnnotation(Tabela.class).nome())
                            + Sql.PONTO + order.getCampo();
                }
                sb.append(campo + Sql.ESPACO + order.getOrder());
            }
        }
    }

    /**
     * Método responsável pela adição da cláusula necessária para paginar através do {@link ModeloPaginator}
     *
     * @param sb {@link StringBuilder} com a query já existente
     * @param rownumDe Parâmetro inicial do rownum
     * @param rownumAte Parâmetro final do rownum
     */
    public void construirRownum(StringBuilder sb, int rownumDe, int rownumAte) {
        sb.append(" WHERE sub3.RNUM BETWEEN " + rownumDe + " AND " + rownumAte + " ORDER BY sub3.RNUM ");
    }

    /**
     * Método responsável pela atribuição dos valores passados na {@link CriterioBase#parametrosList} para o
     * {@link PreparedStatement}
     *
     * @param ps {@link PreparedStatement} a ser atribuido
     * @throws MiniException
     */
    public void atribuirValoresPorCondicao(PreparedStatement ps) throws MiniException {
        if (parametrosList != null) {
            int indice = 1;
            for (Parametros parametro : parametrosList) {
                if (!parametro.paramInOrNotIn() && !parametro.paramIsOrIsNot()) {
                    Utils.Criterios.atribuirPorCampo(ps, parametro.getValor(), indice++);
                }
            }
        }
    }

    /**
     * Método privado com a lógica para percorrer a classe do parametro e através da mesma construir a condição
     * necessária. Tanto para {@link Tabela} quanto para {@link PseudoTabela}
     *
     * @param classe Classe a ser utilizada para a construção da condição
     * @param isUnion Booleano para utilização ou não dos campos de {@link PseudoColuna}
     * @return String com a condição a ser utilizada
     */
    private StringBuilder construirCondicao(final Class<?> classe, final Boolean isUnion) {
        final StringBuilder sb = new StringBuilder();
        if (parametrosList != null) {
            String campo;
            Integer indice = 0;
            boolean grupoAberto = false;
            for (Parametros parametro : parametrosList) {
                if (isUnion) {
                    campo = parametro.obterCampoPseudo(classe);
                } else {
                    campo = parametro.obterCampo(classe);
                }
                grupoAberto = grupo.fecharGrupo(sb, grupoAberto, indice);
                parametro.adicionarOperadorLogico(sb);
                grupoAberto = grupo.abrirGrupo(sb, grupoAberto, indice);
                parametro.adicionarTipoParam(campo, sb);
                indice++;
            }
            sb.append(grupoAberto ? Sql.FECHA_PARENT : "");
        }
        return sb;
    }

}
