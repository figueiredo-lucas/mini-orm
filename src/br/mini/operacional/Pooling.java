package br.mini.operacional;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.mini.Constantes;
import br.mini.Utils;
import br.mini.annotations.Tabela;
import br.mini.exception.MiniException;
import br.mini.exception.MiniRuntimeException;

/**
 * Classe de pool para as entidades cacheadas para todo o sistema e para cada requisição
 *
 * Composta por duas Inner Classes {@link EntityCache} e {@link QueryCache}
 *
 * @author figueiredo-lucas
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class Pooling {

    private Pooling() {
    }

    /**
     * Classe responsável pelo gerenciamento dos objetos cacheados para o sistema
     *
     * @author figueiredo-lucas
     *
     */
    static final class EntityCache {

        /**
         * Mapa contendo as listas cacheadas. Como key, utiliza a própria classe
         */
        private static final Map<Class<?>, List> MAPA_OBJ_CLASSES = new HashMap<>();

        /**
         * Adiciona uma lista de valores no {@link #MAPA_OBJ_CLASSES}
         *
         * @param <V>
         * @param classe
         * @param valores
         */
        static <V> void adicionarListaCache(final Class<V> classe, final List<V> valores) {
            if (classe.getAnnotation(Tabela.class).cacheable()) {
                MAPA_OBJ_CLASSES.put(classe, valores);
            }
        }

        /**
         * Verifica se a classe a ser utilizada já existe no {@link #MAPA_OBJ_CLASSES}
         *
         * @param classe
         * @return booleano se contém ou não o valor
         */
        static boolean existeNaLista(final Class<?> classe) {
            return MAPA_OBJ_CLASSES.containsKey(classe);
        }

        /**
         * Obtém a lista no {@link #MAPA_OBJ_CLASSES} através da classe
         *
         * @param <V>
         * @param classe
         * @return lista de valores
         */
        static <V> List<V> obterListaPorClasse(final Class<V> classe) {
            return MAPA_OBJ_CLASSES.get(classe);
        }

        /**
         * Obtém um valor único dentro de uma lista já cacheada no {@link #MAPA_OBJ_CLASSES}
         *
         * @param <V>
         * @param classe
         * @param pk
         * @return valor único encontrado através da PK
         * @throws MiniException
         */
        static <V> V obterValorPorPk(final Class<V> classe, final String pk) throws MiniException {
            final List<V> valores = (List<V>) MAPA_OBJ_CLASSES.get(classe);
            try {
                for (V valor : valores) {
                    final Field campo = Utils.encontrarCampoPk(classe);
                    campo.setAccessible(true);
                    if (campo.get(valor).equals(pk)) {
                        return valor;
                    }
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                throw new MiniException(Constantes.FALHA_OBTER_CAMPO, ex);
            }
            throw new MiniRuntimeException(Constantes.VALOR_INEXISTENTE + classe.getName());
        }
    }

    /**
     * Classe responsável pelo gerenciamento de objetos cacheados para a requisição
     *
     * @author figueiredo-lucas
     *
     */
    static final class QueryCache {

        /**
         * Mapa dos objetos cacheados. Como key utiliza a concatenação do nome da classe + a PK
         */
        private static final Map<String, Object> MAPA_OBJ_EXISTENTES = new HashMap<>();

        /**
         * Adiciona objeto no {@link #MAPA_OBJ_EXISTENTES}
         *
         * @param valor
         * @param PK
         */
        static void adicionarObjetoNoMapa(final Object valor, final Object PK) {
            MAPA_OBJ_EXISTENTES.put(valor.getClass().getName() + PK.toString(), valor);
        }

        /**
         * Limpa o {@link #MAPA_OBJ_EXISTENTES} após a utilização
         */
        static void limparMapa() {
            MAPA_OBJ_EXISTENTES.clear();
        }

        /**
         * Obtém objeto do {@link #MAPA_OBJ_EXISTENTES} através da sua key
         *
         * @param classe
         * @param PK
         * @return objeto obtido ou null caso o objeto não exista
         */
        static Object obterObjetoExistente(final Class<?> classe, final Object PK) {
            return MAPA_OBJ_EXISTENTES.get(classe.getName() + PK.toString());
        }

    }

}
