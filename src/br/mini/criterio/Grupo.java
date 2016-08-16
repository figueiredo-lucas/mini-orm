package br.mini.criterio;

import java.util.ArrayList;
import java.util.List;

import br.mini.Constantes.Sql;

/**
 * Classe responsável pela utilização das funcionalidades de agrupamento de parâmetros
 *
 * @author figueiredo-lucas
 *
 */
public class Grupo {

    /**
     * Lista para armazenar a lista de grupos possíveis
     */
    private List<List<Integer>> gruposIndicesList;

    /**
     * Lista com os dados do grupo atual para a utilização no {@link Criterio#construirCondicao}
     */
    private List<Integer> grupoAtualList;

    /**
     * Construtor padrão
     */
    Grupo() {
        gruposIndicesList = new ArrayList<List<Integer>>();
    }

    /**
     * Cria um novo grupo e o adiciona na lista de grupos
     */
    public void criarNovoGrupo() {
        gruposIndicesList.add(new ArrayList<Integer>());
    }

    /**
     * Método responsável por adicionar um novo índice ao último grupo criado
     *
     * @param indice
     */
    public void adicionarIndiceAoGrupo(Integer indice) {
        gruposIndicesList.get(gruposIndicesList.size() - 1).add(indice);
    }

    /**
     * Método utilizado pelo {@link Criterio#construirCondicao} para verificar se é necessário fechar um grupo já aberto
     *
     * @param sb
     * @param grupoAberto
     * @param indice
     * @return status do grupo (true > aberto | false > fechado)
     */
    boolean fecharGrupo(StringBuilder sb, boolean grupoAberto, Integer indice) {
        if (grupoAberto && !existeIndiceEmGrupo(indice, false)) {
            grupoAberto = false;
            sb.append(Sql.FECHA_PARENT);
        }
        return grupoAberto;
    }

    /**
     * Método utilizado pelo {@link Criterio#construirCondicao} para verificar se é necessário abrir um novo grupo
     *
     * @param sb
     * @param grupoAberto
     * @param indice
     * @return status do grupo (true > aberto | false > fechado)
     */
    boolean abrirGrupo(StringBuilder sb, boolean grupoAberto, Integer indice) {
        if (!grupoAberto && existeIndiceEmGrupo(indice, true)) {
            grupoAberto = true;
            sb.append("(");
        }
        return grupoAberto;
    }

    /**
     * Método privado responsável pela validação da existência do parâmetro atual na lista de grupos
     *
     * @param indice
     * @param abrindoGrupo
     * @return existencia de indice no grupo atual
     */
    private boolean existeIndiceEmGrupo(Integer indice, boolean abrindoGrupo) {
        if (grupoAtualList == null || (!grupoAtualList.contains(indice) && !grupoAtualList.contains(indice - 1))
                || abrindoGrupo) {
            grupoAtualList = null;
            for (List<Integer> grupoList : gruposIndicesList) {
                if (grupoList.contains(indice)) {
                    grupoAtualList = grupoList;
                    break;
                }
            }
        }
        return grupoAtualList != null && grupoAtualList.contains(indice);
    }
}
