package br.mini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Calendar;

import br.mini.annotations.Coluna;
import br.mini.annotations.PseudoColuna;
import br.mini.annotations.PseudoTabela;
import br.mini.annotations.Tabela;
import br.mini.database.Connector;
import br.mini.exception.MiniException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Classe com Utilitários para o framework
 *
 * @author figueiredo-lucas
 *
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Inner Class para tratar CLOBs
     *
     * @author figueiredo-lucas
     *
     */
    public static final class Clobs {

        /**
         * Transformação de CLOB para String
         *
         * @param data CLOB a ser transformado
         * @return String transformada
         * @throws MiniException
         */
        public static String clobToString(final java.sql.Clob data) throws MiniException {
            final StringBuilder sb = new StringBuilder();
            try {
                final Reader reader = data.getCharacterStream();
                try (BufferedReader br = new BufferedReader(reader)) {
                    int b;
                    while (-1 != (b = br.read())) {
                        sb.append((char) b);
                    }
                }
            } catch (final SQLException | IOException ex) {
                throw new MiniException(Constantes.FALHA_CAMPO_CLOB, ex);
            }
            return sb.toString();
        }

        /**
         * Verificação da existencia de campo do tipo CLOB na classe
         *
         * @param classe Classe a ser percorrida
         * @return true caso exista campo CLOB, false se não
         */
        public static boolean existeClob(final Class<?> classe) {
            for (Field campo : classe.getDeclaredFields()) {
                if (campo.isAnnotationPresent(Coluna.class) && campo.getAnnotation(Coluna.class).isClob()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Inner Class para tratamento de Strings
     *
     * @author figueiredo-lucas
     *
     */
    public static final class Strings {

        /**
         * Método para descapitalizar e remover underscore do nome da tabela
         *
         * @param nomeTabela Nome a ser tratado
         * @return Nome da tabela descapitalizado
         */
        public static String descapitalizaTabela(final String nomeTabela) {
            return removeOwner(nomeTabela.replace("_", "").toLowerCase());
        }

        /**
         * Método para remoção do owner para geração do alias
         *
         * @param nomeTabela Nome da tabela a ter o owner removido
         * @return Nome da tabela sem o owner
         */
        public static String removeOwner(String nomeTabela) {
            return nomeTabela.replace(Connector.owner.toLowerCase() + ".", "");
        }
    }

    /**
     * Inner Class para auxiliar na criação interna dos critérios
     *
     * @author figueiredo-lucas
     *
     */
    public static final class Criterios {

        /**
         * Método para obtenção do nome da tabela primária
         *
         * @param classe Classe para obter o nome da tabela
         * @return Nome da tabela primária
         */
        public static String obterNomeTabelaPrimaria(final Class<?> classe) {
            return classe.isAnnotationPresent(Tabela.class) ? classe.getAnnotation(Tabela.class).nome() : (classe
                    .isAnnotationPresent(PseudoTabela.class) ? classe.getAnnotation(PseudoTabela.class).alias() : "");
        }

        /**
         * Método para obtenção do campo de referência na {@link PseudoColuna}
         *
         * @param nomeCampo Nome do campo de referencia
         * @param classe Classe a ser percorrida
         * @return Classe do campo de referência
         */
        public static Class<?> capturarCampoReferencia(String nomeCampo, Class<?> classe) {
            for (Field campo : classe.getDeclaredFields()) {
                if (campo.isAnnotationPresent(PseudoColuna.class)
                        && campo.getAnnotation(PseudoColuna.class).campoReferencia().equalsIgnoreCase(nomeCampo)) {
                    return campo.getAnnotation(PseudoColuna.class).referencia();
                }
            }
            return null;
        }

        /**
         * Atribuição dos campos no {@link PreparedStatement}
         *
         * @param ps PreparedStatement com a query a ser utilizada
         * @param obj Objeto com o dado a ser utilizado
         * @param indice Índice do campo a ser setado no {@link PreparedStatement}
         * @throws MiniException
         */
        public static void atribuirPorCampo(PreparedStatement ps, Object obj, int indice) throws MiniException {
            try {
                if (obj != null) {
                    if (String.class.isAssignableFrom(obj.getClass())) {
                        ps.setString(indice, (String) obj);
                        return;
                    }
                    if (Calendar.class.isAssignableFrom(obj.getClass())) {
                        ps.setDate(indice, new Date(((Calendar) obj).getTime().getTime()));
                        return;
                    }
                    if (BigDecimal.class.isAssignableFrom(obj.getClass())) {
                        ps.setBigDecimal(indice, (BigDecimal) obj);
                        return;
                    }
                    if (Integer.class.isAssignableFrom(obj.getClass())) {
                        ps.setInt(indice, (Integer) obj);
                        return;
                    }
                    if (InputStream.class.isAssignableFrom(obj.getClass())) {
                        ps.setBlob(indice, new FileInputStream((File) obj));
                    }
                } else {
                    ps.setObject(indice, null);
                }
            } catch (SQLException | FileNotFoundException ex) {
                throw new MiniException(Constantes.FALHA_POPULAR_OBJETO, ex);
            }
        }
    }

    /**
     * Encontra o Field anotado com isPk = true
     *
     * @param classe
     * @return Field da PK
     */
    public static Field encontrarCampoPk(Class<?> classe) {
        Field c = null;
        for (Field campo : classe.getDeclaredFields()) {
            if (campo.isAnnotationPresent(Coluna.class) && campo.getAnnotation(Coluna.class).isPk()) {
                c = campo;
                break;
            }
        }
        return c;
    }

    /**
     * Captura o nome da PK na classe
     *
     * @param classe Classe a ser percorrida
     * @return Nome da PK
     */
    public static String capturaNomePk(Class<?> classe) {
        final Field campo = encontrarCampoPk(classe);
        return campo.getAnnotation(Coluna.class).nome();
    }

}
