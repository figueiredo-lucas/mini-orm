package br.mini.operacional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.mini.Constantes;
import br.mini.Utils;
import br.mini.annotations.Coluna;
import br.mini.annotations.Tabela;
import br.mini.database.Connector;
import br.mini.exception.MiniException;

/**
 * Classe responsável pela geração da chave primária das tabelas
 *
 * @author figueiredo-lucas
 *
 */
final class KeyGen {

    private static final String SELECT_CC = "SELECT CC_NEXT_VALUE FROM KEY_GEN WHERE CC_DOMAIN = ?";
    private static final String UPDATE_CC = "UPDATE KEY_GEN SET CC_NEXT_VALUE = CC_NEXT_VALUE + 1 WHERE CC_DOMAIN = ?";
    private static final String INSERT_CC = "INSERT INTO KEY_GEN(CC_DOMAIN, CC_NEXT_VALUE) VALUES (?, ?)";

    private KeyGen() {
    }

    /**
     * Busca primeira chave válida para o objeto passado por parâmetro para o método salvar(). A partir desse momento,
     * atualiza-se a coluna com o próximo valor.
     *
     * @param tbl Anotação de tabela com os dados de referência de banco
     * @param col Anotação da PK com os dados de referência de banco
     * @return Nova chave
     * @throws MiniException
     */
    static String getPk(Tabela tbl, Coluna col) throws MiniException {
        final Connection conn = Connector.getConnection();
        try {
            final PreparedStatement psSelect = conn.prepareStatement(SELECT_CC);
            String escape = "0000000";
            psSelect.setString(1, Utils.Strings.removeOwner(tbl.nome()) + col.nome());
            ResultSet rs = psSelect.executeQuery();
            final String retorno;
            if (rs.next()) {
                String concatPk = escape + rs.getBigDecimal(1).intValue();
                concatPk = tbl.prefixo() + concatPk.substring(concatPk.length() - (7 - tbl.prefixo().length()));
                rs.close();
                final PreparedStatement psUpdate = conn.prepareStatement(UPDATE_CC);
                psUpdate.setString(1, Utils.Strings.removeOwner(tbl.nome()) + col.nome());
                psUpdate.executeUpdate();
                conn.close();
                retorno = concatPk;
            } else {
                try (PreparedStatement psInsert = conn.prepareStatement(INSERT_CC)) {
                    psInsert.setString(1, Utils.Strings.removeOwner(tbl.nome()) + col.nome());
                    psInsert.setDouble(2, 2);
                    psInsert.executeUpdate();
                }
                retorno = tbl.prefixo() + ("0000001".substring(7 - (7 - tbl.prefixo().length())));
            }
            conn.commit();
            conn.close();
            return retorno;
        } catch (SQLException ex) {
            throw new MiniException(Constantes.FALHA_OBTER_NOVA_PK, ex);
        }
    }
}
