package br.mini;

/**
 * Classe de constantes do sistema
 *
 * @author figueiredo-lucas
 *
 */
public final class Constantes {

    private Constantes() {
    }

    public static final String XML_INVALIDO = "Dados do XML inválido, ou XML inexistente. Verifique o manual.";
    public static final String FALHA_CONEXAO = "Falha na conexão. Não foi possível obter uma conexão válida.";
    public static final String FALHA_FECHAR_CONEXAO = "Falha ao fechar conexão.";
    public static final String FALHA_DS = "Datasource não encontrado.";
    public static final String FALHA_CAMPO_CLOB = "Falha ao obter campo CLOB.";
    public static final String FALHA_CAMPO_BLOB = "Falha ao obter campo BLOB.";
    public static final String VALOR_INEXISTENTE = "Valor não existe na lista cacheada do objeto ";
    public static final String FALHA_OBTER_CAMPO = "Falha ao obter campo";
    public static final String FALHA_OBTER_LISTA = "Falha ao obter lista de objetos do banco";
    public static final String FALHA_OBTER_OBJETO = "Falha ao obter objeto do banco";
    public static final String FALHA_OBTER_COUNT = "Falha ao obter count";
    public static final String FALHA_PERSISTIR_DADO = "Falha ao persistir o dado";
    public static final String FALHA_EXCLUIR_DADO = "Falha ao persistir o dado";
    public static final String FALHA_ANOTACAO_TABELA = "Anotação de tabela não encontrada";
    public static final String FALHA_POPULAR_OBJETO = "Falha ao popular o objeto com os dados obtidos";
    public static final String FALHA_OBTER_NOVA_PK = "Falha ao obter uma nova PK";

    /**
     * Inner Class de constantes do sistema utilizadas para SQL
     *
     * @author figueiredo-lucas
     *
     */
    public static final class Sql {

        public static final String ESPACO = " ";
        public static final String PARAM = "?";
        public static final String PONTO = ".";
        public static final String VIRGULA = ",";
        public static final String FECHA_PARENT = ")";
        public static final String ABRE_PARENT = "(";
    }

}
