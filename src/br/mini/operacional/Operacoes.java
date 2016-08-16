package br.mini.operacional;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import br.mini.Constantes;
import br.mini.Utils;
import br.mini.annotations.Coluna;
import br.mini.annotations.PseudoColuna;
import br.mini.annotations.PseudoTabela;
import br.mini.annotations.Tabela;
import br.mini.criterio.Criterio;
import br.mini.database.Connector;
import br.mini.exception.MiniException;
import br.mini.exception.MiniRuntimeException;

/**
 * Classe principal do framework.
 *
 * @author figueiredo-lucas
 *
 */
public class Operacoes {

    protected Operacoes() {
    }

    private PreparedStatement ps;

    /**
     * Salva ou Atualiza o objeto passado pelo parâmetro
     *
     * @param obj
     * @return true caso a operação seja bem sucedida, false caso não.
     * @throws br.mini.exception.MiniException
     */
    public boolean salvarOuAtualizar(final Object obj) throws MiniException {
        try {
            final Class<?> classe = obj.getClass();
            final Field campoPk = Utils.encontrarCampoPk(classe);
            campoPk.setAccessible(true);
            final boolean existePk = campoPk.getAnnotation(Coluna.class).isPk() && campoPk.get(obj) != null;
            if (existePk) {
                return atualizar(obj);
            } else {
                return salvar(obj);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new MiniException(Constantes.FALHA_OBTER_CAMPO, ex);
        }
    }

    /**
     * Salva o objeto passado pelo parâmetro
     *
     * @param obj
     * @return true caso a operação seja bem sucedida, false caso não.
     * @throws br.mini.exception.MiniException
     */
    public boolean salvar(Object obj) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder strInsert = StringCreator.criaInsert(obj);
            ps = conn.prepareStatement(strInsert.toString());
            return percorrerClasse(ps, obj, obj.getClass());
        } catch (SQLException | IllegalAccessException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_PERSISTIR_DADO, ex);
        }
    }

    /**
     * Atualiza o objeto passado pelo parâmetro
     *
     * @param obj
     * @return true caso a operação seja bem sucedida, false caso não.
     * @throws br.mini.exception.MiniException
     */
    public boolean atualizar(Object obj) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder strUpdate = StringCreator.criaUpdate(obj);
            ps = conn.prepareStatement(strUpdate.toString());
            return percorrerClasse(ps, obj, obj.getClass());
        } catch (IllegalArgumentException | IllegalAccessException | SQLException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_PERSISTIR_DADO, ex);
        }
    }

    /**
     * Exclui o objeto passado por parâmetro
     *
     * @param obj Objeto a ser removido
     * @return true caso a remoção seja bem sucedida, lança runtime exception caso dê erro
     * @throws br.mini.exception.MiniException
     */
    public boolean excluir(Object obj) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            int contPk = 0;
            StringBuilder strDelete = StringCreator.criaDelete(obj);
            ps = conn.prepareStatement(strDelete.toString());
            Class<?> classe = obj.getClass();
            if (classe.isAnnotationPresent(Tabela.class)) {
                final Field campoPk = Utils.encontrarCampoPk(classe);
                campoPk.setAccessible(true);
                atribuirValorPreparedStatement(ps, campoPk, obj, contPk);
                ps.execute();
                conn.commit();
                return true;
            }
            throw new MiniException(Constantes.FALHA_ANOTACAO_TABELA);
        } catch (SQLException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_EXCLUIR_DADO, ex);
        }
    }

    /**
     * Processa os valores do dado {@link StringCreator} com seus parâmetros definidos
     *
     * @param <T>
     * @param classe Classe a ser processada
     * @param criterio Criterio com os parâmetros a serem processados
     * @return Lista de valores do tipo da classe com os dados parâmetros
     * @throws MiniException
     */
    public <T> List<T> processarValores(Class<T> classe, Criterio criterio) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder strSelect = StringCreator.criaSelect(classe);
            if (criterio.getCondicao() != null) {
                strSelect.append(" ").append(criterio.getCondicao());
            } else {
                criterio.construirCriterio(strSelect, classe);
                if (Connector.showSql) {
                    System.out.println(strSelect.toString());
                }

                ps = conn.prepareStatement(strSelect.toString());
                criterio.atribuirValoresPorCondicao(ps);
                if (criterio.getQtdMaxima() > 0) {
                    ps.setMaxRows(criterio.getQtdMaxima());
                }
                return obterLista(classe, Boolean.FALSE);
            }
            return encontrar(classe, strSelect);
        } catch (SQLException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_OBTER_LISTA, ex);
        } finally {
            Pooling.QueryCache.limparMapa();
        }
    }

    private <T> List<T> obterLista(final Class<T> classe, final Boolean isUnion) throws MiniException,
            SQLException {
        final List<T> valores = new ArrayList<>();
        final ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            final T obj;
            if (isUnion) {
                obj = populaObjetoUnion(classe, rs);
            } else {
                obj = populaObjeto(classe, rs);
            }
            valores.add(obj);
        }
        ps.getResultSet().close();
        ps.close();
        return valores;
    }

    /**
     * Processa os valores do dado <code>StringCreator</code> com seus parâmetros definidos
     *
     * @param <T>
     * @param classe Classe com a anotação de {@link PseudoTabela}
     * @param criterio Criterio com os parâmetros a serem utilizados
     * @return Lista de valores da {@link PseudoTabela} passada como parâmetro
     * @throws MiniException
     */
    public <T> List<T> processarPseudoClasse(Class<T> classe, Criterio criterio) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder strSelect = StringCreator.criaSelectUnion(classe);
            if (criterio.getCondicao() != null) {
                strSelect.append(" ").append(criterio.getCondicao());
                ps = conn.prepareStatement(strSelect.toString());
            } else {
                criterio.construirCriterioUnion(strSelect, classe);
                if (Connector.showSql) {
                    System.out.println(strSelect.toString());
                }
                ps = conn.prepareStatement(strSelect.toString());
                criterio.atribuirValoresPorCondicao(ps);
            }
            return obterLista(classe, Boolean.TRUE);
        } catch (SQLException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_OBTER_LISTA, ex);
        } finally {
            Pooling.QueryCache.limparMapa();
        }
    }

    /**
     * Processa os valores do dado <code>StringCreator</code> com seus parâmetros definidos e retorna um Inteiro com a
     * quantidade de registros
     *
     * @param <T>
     * @param classe Classe a ser percorrida
     * @param criterio Critério com os parâmetros a serem adicionados
     * @return Inteiro com a quantidade retornada pelo COUNT
     * @throws MiniException
     */
    public <T> Integer processarCount(Class<T> classe, Criterio criterio) throws MiniException {
        final Connection conn = Connector.getConnection();
        Integer total = 0;
        try {
            StringBuilder strSelect = criterio.construirCriterio(StringCreator.criaCount(classe), classe);
            if (Connector.showSql) {
                System.out.println(strSelect.toString());
            }
            ps = conn.prepareStatement(strSelect.toString());
            criterio.atribuirValoresPorCondicao(ps);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("TOTAL");
            }
            ps.getResultSet().close();
            ps.close();
            return total;
        } catch (SQLException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_OBTER_COUNT, ex);
        }
    }

    /**
     * Processa os valores do dado <code>StringCreator</code> com seus parâmetros definidos e gera uma lista paginada
     *
     * @param <T>
     * @param classe Classe a ser processada
     * @param criterio Critério com os parâmetros a serem processados
     * @param rownumDe rownum inicial
     * @param rownumAte rownum final
     * @return Lista de valores paginados do tipo da classe
     * @throws MiniException
     */
    public <T> List<T> processarValoresPaginados(Class<T> classe, Criterio criterio, int rownumDe, int rownumAte)
            throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder strSelect = StringCreator.criaSelect(classe);
            if (criterio.getCondicao() != null) {
                strSelect.append(" ").append(criterio.getCondicao());
            } else {
                strSelect = criterio.construirCriterio(strSelect, classe, Boolean.TRUE, rownumDe, rownumAte);
                if (Connector.showSql) {
                    System.out.println(strSelect.toString());
                }
                ps = conn.prepareStatement(strSelect.toString());
                criterio.atribuirValoresPorCondicao(ps);
                return obterLista(classe, Boolean.FALSE);
            }
            return encontrar(classe, strSelect);
        } catch (final SQLException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_OBTER_LISTA, ex);
        } finally {
            Pooling.QueryCache.limparMapa();
        }
    }

    /**
     * Seleciona todos os objetos referentes a classe passada pelo parâmetro
     *
     * @param <T>
     *
     * @param classe
     * @return uma lista de objetos ou null.
     * @throws MiniException
     */
    public <T> List<T> encontrarTodos(final Class<T> classe) throws MiniException {
        final Connection conn = Connector.getConnection();
        if (classe.getAnnotation(Tabela.class).cacheable() && Pooling.EntityCache.existeNaLista(classe)) {
            return Pooling.EntityCache.obterListaPorClasse(classe);
        }
        StringBuilder strSelect = StringCreator.criaSelect(classe);
        if (Utils.Clobs.existeClob(classe)) {
            strSelect = StringCreator.criaSelectClob(classe, strSelect);
        }
        try {
            final List<T> valores = encontrar(classe, strSelect);
            Pooling.EntityCache.adicionarListaCache(classe, valores);
            return valores;
        } catch (Exception ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_OBTER_LISTA, ex);
        } finally {
            Pooling.QueryCache.limparMapa();
        }
    }

    /**
     * Selecionar objeto por sua PK ou seu conjunto de PKs.
     *
     * @param <T>
     *
     * @param classe
     * @param pks
     * @return o objeto encontrado ou null
     * @throws MiniException
     */
    public <T> T encontrarPorPK(Class<T> classe, String... pks) throws MiniException {
        final ResultSet rs;
        final PreparedStatement pStmt;
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder sb = StringCreator.criaSelectComPk(classe);
            pStmt = conn.prepareStatement(sb.toString());
            atribuirValorPkPreparedStatement(pStmt, pks);
            rs = pStmt.executeQuery();
            if (rs.next()) {
                T obj = populaObjeto(classe, rs);
                return obj;
            }
            pStmt.getResultSet().close();
            pStmt.close();
            return null;
        } catch (SQLException ex) {
            closeConnException(conn);
            throw new MiniRuntimeException(Constantes.FALHA_OBTER_OBJETO, ex);
        }
    }

    private <T> T encontrarSubClasses(Class<T> classe, String... pks) throws MiniException {
        final ResultSet rs;
        final PreparedStatement pStmt;
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder sb = StringCreator.criaSelectComPk(classe);
            pStmt = conn.prepareStatement(sb.toString());
            atribuirValorPkPreparedStatement(pStmt, pks);
            rs = pStmt.executeQuery();
            if (rs.next()) {
                T obj = populaObjeto(classe, rs);
                return obj;
            }
            pStmt.getResultSet().close();
            pStmt.close();
        } catch (SQLException ex) {
            closeConnException(conn);
            throw new MiniException(Constantes.FALHA_OBTER_OBJETO, ex);
        }
        return null;
    }

    /**
     * Executa uma consulta genérica.
     *
     * @param query
     * @return ResultSet com os valores ou nulo caso ocorra um erro.
     * @throws MiniException
     */
    public ResultSet executarConsulta(String query) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            return Connector.executar(query, conn);
        } catch (SQLException ex) {
            throw new MiniException(ex);
        } finally {
            closeConnException(conn);
        }
    }

    /**
     * Executa uma consulta genérica parametrizada. A quantidade de parâmetros deve corresponder a quantidade de
     * wildcards "?".
     *
     * @param query query a ser rodada
     * @param parametros parametros da query
     * @return ResultSet com os valores ou nulo caso ocorra um erro.
     * @throws MiniException
     */
    public ResultSet executarConsulta(String query, List<Object> parametros) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            return Connector.executar(query, parametros, conn);
        } catch (SQLException ex) {
            throw new MiniException(ex);
        } finally {
            closeConnException(conn);
        }
    }

    /**
     * Método para salvar dados de uma lista <b>N:M</b>
     *
     * @param <T>
     * @param <Z>
     * @param obj Lista a ser persistida
     * @return true caso a persistencia seja bem sucedida, false caso haja erro
     * @throws br.mini.exception.MiniException
     */
    public <T extends Collection<Z>, Z> boolean salvarRelacionamento(T obj) throws MiniException {
        for (Z val : obj) {
            if (!salvarOuAtualizar(val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Método para remover dados de uma lista <b>N:M</b>
     *
     * @param <T>
     * @param <Z>
     * @param obj Lista a ser removida
     * @return true caso a remoção seja bem sucedida, false caso haja erro
     * @throws br.mini.exception.MiniException
     */
    public <T extends Collection<Z>, Z> boolean removerRelacionamento(T obj) throws MiniException {
        for (Z val : obj) {
            if (!excluir(val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Método responsável por obter o arquivo de um campo BLOB do banco
     *
     * @param classe Classe a ser buscada
     * @param nomeCampo Nome do campo com o arquivo
     * @param pk PK da tabela referenciada na classe
     * @return Arquivo encontrado
     * @throws MiniException
     */
    public InputStream buscarArquivo(Class<?> classe, String nomeCampo, String pk) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            StringBuilder sb = StringCreator.criaSelectPkArquivo(classe, nomeCampo);
            PreparedStatement pStatement = conn.prepareStatement(sb.toString());
            atribuirValorPkPreparedStatement(pStatement, pk);
            ResultSet rs = pStatement.executeQuery();
            if (rs.next()) {
                return rs.getBinaryStream(nomeCampo);
            }
        } catch (Exception ex) {
            throw new MiniException(Constantes.FALHA_OBTER_OBJETO, ex);
        } finally {
            closeConnException(conn);
        }
        return null;
    }

    /**
     * Encontra os valores da string passada por parâmetro retornando uma <code>List&lt;T&gt;</code>. <br />
     * Onde <code>T</code> é passado por parâmetro
     *
     * @param classe Classe a ser encontrada
     * @param strSelect String de select a ser percorrida
     * @return Lista de dados encontrados
     * @throws MiniException
     */
    private <T> List<T> encontrar(Class<T> classe, StringBuilder strSelect) throws MiniException {
        final Connection conn = Connector.getConnection();
        final List<T> classList = new ArrayList<>();
        final Statement stmt;
        final ResultSet rs;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(strSelect.toString());
            while (rs.next()) {
                T obj = populaObjeto(classe, rs);
                classList.add(obj);
            }
            rs.close();
            stmt.close();
            return classList;
        } catch (SQLException sqle) {
            closeConnException(conn);
            throw new MiniException(sqle);
        }
    }

    /**
     * Popula o objeto com os dados da linha atual do ResultSet
     *
     * @param classe
     * @param rs
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws MiniException
     * @throws IllegalArgumentException
     */
    private <T> T populaObjeto(Class<T> classe, ResultSet rs) throws MiniException {
        try {
            Coluna col;
            Object dado;
            T obj = classe.newInstance();
            for (Field campo : classe.getDeclaredFields()) {
                campo.setAccessible(true);
                if (campo.isAnnotationPresent(Coluna.class) && !InputStream.class.isAssignableFrom(campo.getType())) {
                    col = campo.getAnnotation(Coluna.class);
                    dado = rs.getObject(col.nome());
                    if (col.isFk() && dado != null) {
                        campo.set(obj, buscarValor(campo, dado));
                    } else {
                        atribuirVariavel(dado, campo, obj);
                    }
                }
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException | SecurityException | SQLException
                | IllegalArgumentException ex) {
            throw new MiniException(Constantes.FALHA_POPULAR_OBJETO, ex);
        }
    }

    /**
     * Popula o objeto com os dados da linha atual do ResultSet
     *
     * @param classe
     * @param rs
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws MiniException
     * @throws IllegalArgumentException
     */
    private <T> T populaObjetoUnion(Class<T> classe, ResultSet rs) throws MiniException {
        try {
            PseudoColuna col;
            Object dado;
            T obj = classe.newInstance();
            Field[] campos = classe.getDeclaredFields();
            if (classe.getSuperclass() != null && classe.getSuperclass() != Object.class) {
                List<Field> camposList = new ArrayList<>();
                camposList.addAll(Arrays.asList(classe.getSuperclass().getDeclaredFields()));
                camposList.addAll(Arrays.asList(campos));
                campos = new Field[camposList.size()];
                camposList.toArray(campos);
            }
            for (Field campo : campos) {
                campo.setAccessible(true);
                if (campo.isAnnotationPresent(PseudoColuna.class)) {
                    col = campo.getAnnotation(PseudoColuna.class);
                    if (col.referencia() == Object.class) {
                        dado = rs.getObject(col.alias());
                    } else {
                        dado = rs.getObject(col.campoReferencia());
                    }
                    atribuirVariavel(dado, campo, obj);
                }
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException | SecurityException | SQLException ex) {
            throw new MiniException(Constantes.FALHA_POPULAR_OBJETO, ex);
        }
    }

    private Object buscarValor(Field campo, Object dado) throws MiniException {
        Object retorno = Pooling.QueryCache.obterObjetoExistente(campo.getType(), dado);
        try {
            if (retorno == null) {
                retorno = encontrarSubClasses(campo.getType(), dado.toString());
                if (retorno != null) {
                    Pooling.QueryCache.adicionarObjetoNoMapa(retorno, dado);
                }
            }
        } catch (Exception ex) {
            throw new MiniException(ex);
        }
        return retorno;
    }

    private void atribuirVariavel(Object dado, Field campo, Object obj) throws MiniException {
        try {
            if (dado != null) {
                if (String.class.isAssignableFrom(campo.getType())) {
                    Object aux = dado;
                    if (java.sql.Clob.class.isAssignableFrom(dado.getClass())) {
                        aux = Utils.Clobs.clobToString((Clob) dado).trim();
                    } else {
                        aux = aux.toString().trim();
                    }
                    campo.set(obj, aux);
                    return;
                }
                if (Calendar.class.isAssignableFrom(campo.getType())) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(((Timestamp) dado).getTime());
                    campo.set(obj, cal);
                    return;
                }
                if (Number.class.isAssignableFrom(campo.getType())) {
                    campo.set(obj, dado);
                    return;
                }
                if (InputStream.class.isAssignableFrom(campo.getType())) {
                    try {
                        campo.set(obj, ((Blob) dado).getBinaryStream());
                    } catch (SQLException e) {
                        throw new MiniException(Constantes.FALHA_CAMPO_BLOB, e);
                    }
                }
            } else {
                campo.set(obj, dado);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new MiniException(Constantes.FALHA_POPULAR_OBJETO, ex);
        }
    }

    /**
     * Atribuição dos valores das Pks para o PreparedStatement
     *
     * @param ps
     * @param pks
     * @throws SQLException
     */
    private void atribuirValorPkPreparedStatement(PreparedStatement ps, String... pks) throws SQLException {
        for (int indice = 0; indice < pks.length; indice++) {
            ps.setString(indice + 1, pks[indice]);
        }
    }

    /**
     * Percorre a classe a fim de validar as anotações e atribuir os valores aos seus respectivos atributos
     *
     * @param ps
     * @param obj
     * @param classe
     * @return true caso a persistencia seja bem sucedida, false caso haja erro
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws MiniException
     */
    private boolean percorrerClasse(PreparedStatement ps, Object obj, Class<?> classe) throws MiniException,
            IllegalAccessException, SQLException {
        final Connection conn = Connector.getConnection();
        String PK;
        List<Field> chavesList = new ArrayList<>();
        if (classe.isAnnotationPresent(Tabela.class)) {
            Tabela tbl = classe.getAnnotation(Tabela.class);
            String tipoTrans = null;
            Field[] campos = classe.getDeclaredFields();
            int subIndice = 0;
            for (int indice = 0; indice < campos.length; indice++) {
                campos[indice].setAccessible(true);
                if (campos[indice].isAnnotationPresent(Coluna.class)
                        && !("U".equals(tipoTrans) && (InputStream.class.isAssignableFrom(campos[indice].getType()) && campos[indice]
                        .get(obj) == null))) {
                    Coluna col = campos[indice].getAnnotation(Coluna.class);

                    if (col.isPk() && campos[indice].get(obj) == null) {
                        PK = KeyGen.getPk(tbl, col);
                        tipoTrans = "C";
                        campos[indice].set(obj, PK);
                    } else if (col.isPk() && campos[indice].get(obj) != null) {
                        tipoTrans = "U";
                        chavesList.add(campos[indice]);
                        continue;
                    }
                    atribuirValorPreparedStatement(ps, campos[indice], obj, indice - chavesList.size() - subIndice);
                } else {
                    subIndice++;
                }
            }
            for (int indice = 0; indice < chavesList.size(); indice++) {
                atribuirValorPreparedStatement(ps, chavesList.get(indice), obj,
                        campos.length - indice - chavesList.size() - subIndice);
            }
            ps.execute();
            ps.close();
            conn.commit();
            conn.close();
            return true;
        }
        throw new MiniException(Constantes.FALHA_ANOTACAO_TABELA);
    }

    /**
     * Deve-se atribuir valores para os wildcards do PreparedStatement gerado pela string.
     *
     * @param ps
     * @param campo
     * @param obj
     * @param indice
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws MiniException
     */
    private void atribuirValorPreparedStatement(PreparedStatement ps, Field campo, Object obj, int indice)
            throws IllegalArgumentException, IllegalAccessException, SQLException, MiniException {
        if (campo.isAnnotationPresent(Coluna.class) && campo.getAnnotation(Coluna.class).isFk()
                && campo.get(obj) != null) {
            ps.setObject(indice + 1, buscarPk(campo, obj));
        } else {
            atribuirPorCampo(ps, campo, obj, indice + 1);
        }
    }

    private void atribuirPorCampo(PreparedStatement ps, Field campo, Object obj, int indice)
            throws IllegalArgumentException, SQLException, IllegalAccessException {
        if (campo.get(obj) != null) {
            if (String.class.isAssignableFrom(campo.getType())) {
                if (campo.getAnnotation(Coluna.class).isClob()) {
                    ps.setClob(indice, new StringReader((String) campo.get(obj)));
                } else {
                    ps.setString(indice, (String) campo.get(obj));
                }
                return;
            }
            if (Calendar.class.isAssignableFrom(campo.getType())) {
                ps.setTimestamp(indice, new Timestamp(((Calendar) campo.get(obj)).getTimeInMillis()));
                return;
            }
            if (Number.class.isAssignableFrom(campo.getType())) {
                ps.setBigDecimal(indice, (BigDecimal) campo.get(obj));
                return;
            }
            if (InputStream.class.isAssignableFrom(campo.getType())) {
                ps.setBlob(indice, (InputStream) campo.get(obj));
                return;
            }
        }
        ps.setObject(indice, null);
    }

    /**
     * Busca da PK dentro do objeto filho da entidade que está sendo persistida
     *
     * @param campo Campo a ser percorrido
     * @param obj Objeto para obter o valor do campo
     * @return Novo objeto através da PK
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws MiniException
     */
    private Object buscarPk(Field campo, Object obj) throws IllegalArgumentException, IllegalAccessException,
            SQLException, MiniException {
        final Connection conn = Connector.getConnection();
        final Object sub = campo.get(obj);
        final Class<?> classe = sub.getClass();
        for (Field f : classe.getDeclaredFields()) {
            if (f.isAnnotationPresent(Coluna.class) && f.getAnnotation(Coluna.class).isPk()) {
                f.setAccessible(true);
                if (f.get(sub) == null) {
                    StringBuilder strInsert = StringCreator.criaInsert(obj);
                    PreparedStatement pStmt = conn.prepareStatement(strInsert.toString());
                    percorrerClasse(pStmt, sub, classe);
                }
                return f.get(sub);
            }
        }
        return null;
    }

    private void closeConnException(final Connection conn) throws MiniException {
        try {
            if (!conn.isClosed()) {
                conn.rollback();
            }
            conn.close();
        } catch (SQLException sqlE) {
            throw new MiniException(Constantes.FALHA_FECHAR_CONEXAO, sqlE);
        }
    }
}
