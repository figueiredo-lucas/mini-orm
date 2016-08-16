package br.mini.operacional;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.mini.Constantes.Sql;
import br.mini.Utils;
import br.mini.annotations.Coluna;
import br.mini.annotations.PseudoColuna;
import br.mini.annotations.PseudoTabela;
import br.mini.annotations.Tabela;

public final class StringCreator {

    private StringCreator() {
    }

    /**
     * Cria-se um insert genérico a partir dos parâmetros de tabela e colunas do Objeto
     *
     * @param obj
     * @return
     */
    static StringBuilder criaInsert(Object obj) {
        StringBuilder str = new StringBuilder("INSERT INTO ");
        StringBuilder interrogs = new StringBuilder();
        Class<?> classe = obj.getClass();
        if (classe.isAnnotationPresent(Tabela.class)) {
            str.append(classe.getAnnotation(Tabela.class).nome());
            str.append(" (");
            Field[] campos = classe.getDeclaredFields();
            for (int indice = 0; indice < campos.length; indice++) {
                if (campos[indice].isAnnotationPresent(Coluna.class)) {
                    str.append(campos[indice].getAnnotation(Coluna.class).nome());
                    interrogs.append(Sql.PARAM);
                    if (indice < campos.length - 1 && campos[indice + 1].isAnnotationPresent(Coluna.class)) {
                        str.append(Sql.VIRGULA + Sql.ESPACO);
                        interrogs.append(Sql.VIRGULA + Sql.ESPACO);
                    }
                }
            }
            str.append(") VALUES (").append(interrogs).append(")");
        }
        return str;
    }

    /**
     * Cria um update genérico a partir dos parametros de tabela e colunas do Objeto
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    static StringBuilder criaUpdate(Object obj) throws IllegalArgumentException, IllegalAccessException {
        StringBuilder strUpdate = new StringBuilder("UPDATE ");
        StringBuilder strWhere = new StringBuilder();
        Class<?> classe = obj.getClass();
        boolean isInputStream;
        if (classe.isAnnotationPresent(Tabela.class)) {
            strUpdate.append(classe.getAnnotation(Tabela.class).nome());
            strUpdate.append(" SET ");
            Field[] campos = classe.getDeclaredFields();
            for (int indice = 0; indice < campos.length; indice++) {
                campos[indice].setAccessible(true);
                if (campos[indice].isAnnotationPresent(Coluna.class)
                        && (!InputStream.class.isAssignableFrom(campos[indice].getType()) || campos[indice].get(obj) != null)) {
                    if (!campos[indice].getAnnotation(Coluna.class).isPk()) {
                        strUpdate.append(campos[indice].getAnnotation(Coluna.class).nome()).append(" = ?");
                        if (indice < campos.length - 1 && campos[indice + 1].isAnnotationPresent(Coluna.class)) {
                            campos[indice + 1].setAccessible(true);
                            isInputStream = InputStream.class.isAssignableFrom(campos[indice + 1].getType());
                            if (!isInputStream || (isInputStream && campos[indice + 1].get(obj) != null)
                                    || !(indice + 1 == campos.length - 1)) {
                                strUpdate.append(Sql.VIRGULA + Sql.ESPACO);
                            }
                        }
                    } else {
                        if (!strWhere.toString().equals("")) {
                            strWhere.append(" AND ");
                        }
                        strWhere.append(" WHERE ").append(campos[indice].getAnnotation(Coluna.class).nome()).append(" = ?");
                    }
                }
            }
            strUpdate.append(strWhere);
        }
        return strUpdate;
    }

    /**
     * Cria um delete genérico a partir dos parametros de tabela e colunas do Objeto
     *
     * @param obj
     * @return
     */
    static StringBuilder criaDelete(Object obj) {
        StringBuilder strUpdate = new StringBuilder("DELETE FROM ");
        StringBuilder strWhere = new StringBuilder();
        Class<?> classe = obj.getClass();
        if (classe.isAnnotationPresent(Tabela.class)) {
            strUpdate.append(classe.getAnnotation(Tabela.class).nome());
            for (Field campo : classe.getDeclaredFields()) {
                if (campo.isAnnotationPresent(Coluna.class)) {
                    if (campo.getAnnotation(Coluna.class).isPk()) {
                        if (!strWhere.toString().equals("")) {
                            strWhere.append(" AND ");
                        }
                        strWhere.append(" WHERE ").append(campo.getAnnotation(Coluna.class).nome()).append(" = ?");
                    }
                }
            }
            strUpdate.append(strWhere);
        }
        return strUpdate;
    }

    /**
     * Cria um select genérico a partir dos parâmetros de tabela e colunas do Objeto
     *
     * @param classe
     * @return
     */
    static StringBuilder criaSelect(Class<?> classe) {
        StringBuilder strSelect = new StringBuilder("SELECT DISTINCT ");
        if (classe.isAnnotationPresent(Tabela.class)) {
            String alias = Utils.Strings.descapitalizaTabela(classe.getAnnotation(Tabela.class).nome()) + "1";
            Field[] campos = classe.getDeclaredFields();
            for (int indice = 0; indice < campos.length; indice++) {
                if (campos[indice].isAnnotationPresent(Coluna.class)
                        && !InputStream.class.isAssignableFrom(campos[indice].getType())
                        && !campos[indice].getAnnotation(Coluna.class).isClob()) {
                    strSelect.append(alias).append(Sql.PONTO).append(campos[indice].getAnnotation(Coluna.class).nome());
                    if (indice < campos.length - 1 && campos[indice + 1].isAnnotationPresent(Coluna.class)
                            && !InputStream.class.isAssignableFrom(campos[indice + 1].getType())
                            && !campos[indice + 1].getAnnotation(Coluna.class).isClob()) {
                        strSelect.append(Sql.VIRGULA + Sql.ESPACO);
                    }
                } else if (campos[indice].isAnnotationPresent(Coluna.class) && indice < campos.length - 1
                        && campos[indice + 1].isAnnotationPresent(Coluna.class)
                        && !campos[indice + 1].getAnnotation(Coluna.class).isClob()) {
                    strSelect.append(Sql.VIRGULA + Sql.ESPACO);
                }
            }
            strSelect.append(" FROM ").append(classe.getAnnotation(Tabela.class).nome()).append(Sql.ESPACO).append(alias);
        }
        return strSelect;
    }

    /**
     * Cria um select genérico com where em uma PK a partir dos parâmetros de tabela e colunas do Objeto
     *
     * @param classe
     * @return
     */
    static StringBuilder criaSelectComPk(Class<?> classe) {
        StringBuilder strSelect = new StringBuilder("SELECT ");
        StringBuilder strWhere = new StringBuilder();
        if (classe.isAnnotationPresent(Tabela.class)) {
            Field[] campos = classe.getDeclaredFields();
            for (int indice = 0; indice < campos.length; indice++) {
                if (campos[indice].isAnnotationPresent(Coluna.class)
                        && !InputStream.class.isAssignableFrom(campos[indice].getType())) {
                    strSelect.append(campos[indice].getAnnotation(Coluna.class).nome());
                    if (indice < campos.length - 1 && campos[indice + 1].isAnnotationPresent(Coluna.class)
                            && !InputStream.class.isAssignableFrom(campos[indice + 1].getType())) {
                        strSelect.append(Sql.VIRGULA + Sql.ESPACO);
                    }
                    if (campos[indice].getAnnotation(Coluna.class).isPk()) {
                        if (!strWhere.toString().equals("")) {
                            strWhere.append(" AND ");
                        }
                        strWhere.append(" WHERE ").append(campos[indice].getAnnotation(Coluna.class).nome()).append(" = ?");
                    }
                } else if (campos[indice].isAnnotationPresent(Coluna.class) && indice < campos.length - 1
                        && campos[indice + 1].isAnnotationPresent(Coluna.class)) {
                    strSelect.append(Sql.VIRGULA + Sql.ESPACO);
                }
            }
            strSelect.append(" FROM ").append(classe.getAnnotation(Tabela.class).nome()).append(strWhere.toString());
        }
        return strSelect;
    }

    /**
     * Cria um select genérico a ser utilizado para a busca de um arquivo a partir da pk da classe e do nome do campo
     * que contém os dados do arquivo
     *
     * @param classe
     * @param nomeCampo
     * @return String com select criado
     */
    static StringBuilder criaSelectPkArquivo(Class<?> classe, String nomeCampo) {
        StringBuilder strSelect = new StringBuilder("SELECT " + nomeCampo);
        StringBuilder strWhere = new StringBuilder();
        if (classe.isAnnotationPresent(Tabela.class)) {
            Field[] campos = classe.getDeclaredFields();
            for (Field campo : campos) {
                if (campo.isAnnotationPresent(Coluna.class) && campo.getAnnotation(Coluna.class).isPk()) {
                    strWhere.append(" WHERE ").append(campo.getAnnotation(Coluna.class).nome()).append(" = ?");
                    break;
                }
            }
            strSelect.append(" FROM ").append(classe.getAnnotation(Tabela.class).nome()).append(strWhere.toString());
        }
        return strSelect;
    }

    /**
     * Método para tratar o select existente para trazer o campo do CLOB também. Utilizado pois campos CLOB não aceitam
     * a clausula DISTINCT
     *
     * @param classe Classe a ser utilizada como referência de tabela
     * @param subString Substring já gerada a ser acoplada
     * @return String com o acoplamento de um novo select para obtenção de CLOB
     */
    public static StringBuilder criaSelectClob(Class<?> classe, StringBuilder subString) {
        StringBuilder strSelect = new StringBuilder("SELECT ");
        if (classe.isAnnotationPresent(Tabela.class)) {
            String alias = Utils.Strings.descapitalizaTabela(classe.getAnnotation(Tabela.class).nome()) + "_sup";
            Field[] campos = classe.getDeclaredFields();
            String PK = "";
            for (int indice = 0; indice < campos.length; indice++) {
                if (campos[indice].isAnnotationPresent(Coluna.class)
                        && campos[indice].getAnnotation(Coluna.class).isPk()) {
                    PK = campos[indice].getAnnotation(Coluna.class).nome();
                }
                if (campos[indice].isAnnotationPresent(Coluna.class)
                        && campos[indice].getAnnotation(Coluna.class).isClob()) {
                    strSelect.append(alias).append(Sql.PONTO).append(campos[indice].getAnnotation(Coluna.class).nome());
                    if (indice < campos.length - 1 && campos[indice + 1].isAnnotationPresent(Coluna.class)
                            && campos[indice + 1].getAnnotation(Coluna.class).isClob()) {
                        strSelect.append(Sql.VIRGULA).append(Sql.ESPACO);
                    }
                }
            }
            strSelect.append(", sub.* FROM (").append(subString).append(") sub ");
            strSelect.append("INNER JOIN ").append(classe.getAnnotation(Tabela.class).nome()).append(Sql.ESPACO).append(alias);
            strSelect.append(" ON (sub.").append(PK).append(" = ").append(alias).append(Sql.PONTO).append(PK ).append(")");
        }
        return strSelect;
    }

    /**
     * Método responsável por criar o select para {@link PseudoTabela}
     *
     * @param classe
     * @return Select gerado com os dados através da classe
     */
    static StringBuilder criaSelectUnion(Class<?> classe) {
        StringBuilder strSelect = new StringBuilder("");
        if (classe.isAnnotationPresent(PseudoTabela.class)) {
            String[] tabelas = classe.getAnnotation(PseudoTabela.class).tabelas();
            for (int indice = 0; indice < tabelas.length; indice++) {
                strSelect.append(" SELECT ");
                Field[] campos = classe.getDeclaredFields();
                if (classe.getSuperclass() != null && classe.getSuperclass() != Object.class) {
                    List<Field> camposList = new ArrayList<>();
                    camposList.addAll(Arrays.asList(classe.getSuperclass().getDeclaredFields()));
                    camposList.addAll(Arrays.asList(campos));
                    campos = new Field[camposList.size()];
                    camposList.toArray(campos);
                }
                for (int indCampo = 0; indCampo < campos.length; indCampo++) {
                    if (campos[indCampo].isAnnotationPresent(PseudoColuna.class)) {
                        strSelect.append(campos[indCampo].getAnnotation(PseudoColuna.class).colunas()[indice]);
                        strSelect.append(" AS ").append(campos[indCampo].getAnnotation(PseudoColuna.class).alias());
                        if (indCampo + 1 < campos.length) {
                            strSelect.append(Sql.VIRGULA + Sql.ESPACO);
                        }
                    }
                }
                strSelect.append(" FROM ").append(tabelas[indice]);
                if (indice + 1 < tabelas.length) {
                    strSelect.append(" UNION ");
                }
            }
            criaSelectPaiUnion(strSelect, classe);
            criaJoinUnion(strSelect, classe);
        }
        return strSelect;
    }

    /**
     * Adição do select que engloba todos os parametros gerados do select passado por parâmetro. Método privado
     * utilizado apenas {@link #criaSelectUnion(Class)}
     *
     *
     * @param strSelect
     * @param classe
     */
    private static void criaSelectPaiUnion(StringBuilder strSelect, Class<?> classe) {
        String nomeTabela;
        StringBuilder sup = new StringBuilder(" SELECT ");
        Field[] campos = classe.getDeclaredFields();
        if (classe.getSuperclass() != null && classe.getSuperclass() != Object.class) {
            List<Field> camposList = new ArrayList<>();
            camposList.addAll(Arrays.asList(classe.getSuperclass().getDeclaredFields()));
            camposList.addAll(Arrays.asList(campos));
            campos = new Field[camposList.size()];
            camposList.toArray(campos);
        }
        for (int indCampo = 0; indCampo < campos.length; indCampo++) {
            if (campos[indCampo].isAnnotationPresent(PseudoColuna.class)) {
                nomeTabela = classe.getAnnotation(PseudoTabela.class).alias();
                PseudoColuna pseudoCol = campos[indCampo].getAnnotation(PseudoColuna.class);
                if (pseudoCol.referencia() == Object.class) {
                    sup.append(nomeTabela).append(Sql.PONTO).append(pseudoCol.alias());
                } else {
                    nomeTabela = Utils.Strings.descapitalizaTabela(pseudoCol.referencia().getAnnotation(Tabela.class)
                            .nome())
                            + "1";
                    sup.append(nomeTabela).append(Sql.PONTO).append(pseudoCol.campoReferencia());
                }
                if (indCampo + 1 < campos.length) {
                    sup.append(Sql.VIRGULA).append(Sql.ESPACO);
                }
            }
        }
        sup.append(" FROM (");
        strSelect.insert(0, sup);
        strSelect.append(" ) ").append(classe.getAnnotation(PseudoTabela.class).alias());
    }

    /**
     * Adição do join que utilizando os dados gerados do select passado por parâmetro. Método privado utilizado apenas
     * {@link #criaSelectUnion(Class)}. A junção é feita a partir do select externo criado pelo método
     * {@link #criaSelectPaiUnion(StringBuilder, Class)}
     *
     * @param strSelect
     * @param classe
     */
    private static void criaJoinUnion(StringBuilder strSelect, Class<?> classe) {
        StringBuilder sbJoin = new StringBuilder("");
        String nomeTabela;
        Field[] campos = classe.getDeclaredFields();
        for (Field campo : campos) {
            if (campo.isAnnotationPresent(PseudoColuna.class) && campo.getAnnotation(PseudoColuna.class).referencia() != Object.class) {
                PseudoColuna pseudoCol = campo.getAnnotation(PseudoColuna.class);
                nomeTabela = Utils.Strings.descapitalizaTabela(pseudoCol.referencia().getAnnotation(Tabela.class)
                        .nome())
                        + "1";
                if (sbJoin.indexOf(nomeTabela) < 0) {
                    sbJoin.append(" LEFT OUTER JOIN ");
                    sbJoin.append(pseudoCol.referencia().getAnnotation(Tabela.class).nome()).append(Sql.ESPACO).append(nomeTabela);
                    sbJoin.append(" ON (");
                    sbJoin.append(classe.getAnnotation(PseudoTabela.class).alias()).append(Sql.PONTO).append(pseudoCol.alias());
                    sbJoin.append(" = ");
                    sbJoin.append(nomeTabela).append(Sql.PONTO)
                            .append((pseudoCol.campoVinculo().equals("")
                                    ? Utils.capturaNomePk(pseudoCol.referencia())
                                    : pseudoCol.campoVinculo()))
                            .append(") ");
                }
            }
        }
        strSelect.append(sbJoin);
    }

    /**
     * Cria um select genérico a partir dos parâmetros de tabela e colunas do Objeto
     *
     * @param classe
     * @return
     */
    static StringBuilder criaCount(Class<?> classe) {
        StringBuilder strSelect = new StringBuilder("SELECT COUNT(DISTINCT ");
        if (classe.isAnnotationPresent(Tabela.class)) {
            String alias = Utils.Strings.descapitalizaTabela(classe.getAnnotation(Tabela.class).nome()) + "1";
            Field[] campos = classe.getDeclaredFields();
            for (Field campo : campos) {
                if (campo.isAnnotationPresent(Coluna.class) && campo.getAnnotation(Coluna.class).isPk()) {
                    strSelect.append(alias).append(Sql.PONTO).append(campo.getAnnotation(Coluna.class).nome());
                    break;
                }
            }
            strSelect.append(") as TOTAL FROM ").append(classe.getAnnotation(Tabela.class).nome()).append(Sql.ESPACO).append(alias);
        }
        return strSelect;
    }

    /**
     * Criação do select utilizado pelo {@link ModeloPaginator} para paginar.
     *
     * @param classe
     * @param subString
     * @return String com parâmetro ROWNUM
     */
    public static StringBuilder criaSelectRownum(Class<?> classe, StringBuilder subString) {
        StringBuilder strSelect = new StringBuilder("SELECT ");
        strSelect.append(" * FROM (SELECT ROWNUM RNUM, sub2.* FROM (").append(subString).append(") sub2) sub3 ");
        return strSelect;
    }
}
