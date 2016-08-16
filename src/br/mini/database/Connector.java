package br.mini.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import br.mini.Constantes;
import br.mini.exception.MiniException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * Classe responsável por obter a conexão para o pool
 *
 * @author figueiredo-lucas <br>
 *
 */
public class Connector {

    public static String owner = "";
    public static boolean gravaLog = false;
    public static boolean showSql = false;

    private static DataSource DATA_SOURCE;

    private Connector() throws MiniException {
        Map<String, String> mapaConn = XMLParser.getMapParams();

        if (mapaConn.containsKey("owner")) {
            owner = mapaConn.get("owner");
        }
        if (mapaConn.containsKey("log")) {
            gravaLog = Boolean.parseBoolean(mapaConn.get("log"));
        }
        if (mapaConn.containsKey("showSql")) {
            showSql = Boolean.parseBoolean(mapaConn.get("showSql"));
        }
        try {
            DATA_SOURCE = (DataSource) new InitialContext().lookup(mapaConn.get("dataSource"));
        } catch (NamingException ex) {
            throw new MiniException(Constantes.FALHA_DS, ex);
        }
            
    }

    /**
     * Método responsável por obter a conexão do pool ou abrir uma nova conexão através de uma url de banco.
     *
     * Há a funcionalidade de tentativas. Caso o banco esteja instável, antes de retornar um erro de conexão falha, é
     * tentado recuperar três vezes a mesma.
     *
     * @param tentativa
     * @return nova conexão
     * @throws MiniException
     */
    private static Connection conectar() throws MiniException {
        try {
            return DATA_SOURCE.getConnection();
        } catch (Exception ex) {
            throw new MiniException(Constantes.FALHA_CONEXAO, ex);
        }
    }

    /**
     * Método responsável por executar uma consulta com parâmetros.
     *
     * @param query
     * @param parametros
     * @param conn
     * @return ResultSet com os dados executados
     * @throws SQLException
     */
    public static ResultSet executar(String query, List<Object> parametros,
            Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(query);
        for (int i = 0; i < parametros.size(); i++) {
            st.setObject(i + 1, parametros.get(i));
        }
        ResultSet rs = st.executeQuery();
        return rs;
    }

    /**
     * Método responsável por executar uma consulta.
     *
     * @param query
     * @param conn
     * @return ResultSet com os dados executados
     * @throws SQLException
     */
    public static ResultSet executar(String query, Connection conn)
            throws SQLException {
        return executar(query, new ArrayList<Object>(), conn);
    }

    /**
     * Método estático para obter a nova conexão de banco.
     *
     * @return nova conexão
     * @throws MiniException
     */
    public static Connection getConnection() throws MiniException {
        return conectar();
    }
    
}
