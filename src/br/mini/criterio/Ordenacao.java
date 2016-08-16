package br.mini.criterio;

import br.mini.Constantes.Sql;
import br.mini.enums.OrderEnum;

/**
 * Classe para encapsular os campos de order by vindos através do método {@link Criterio#addOrderBy}
 *
 * @see Tipos
 *
 * @author figueiredo-lucas
 */
class Ordenacao extends Tipos {

    /**
     * Enum utilizado para definir o tipo de ordenação
     */
    private OrderEnum order;

    /**
     * Construtor parametrizado para a classe
     *
     * @param campo
     * @param order
     * @param joinRef
     */
    Ordenacao(final String campo, final OrderEnum order, final Join joinRef) {
        super(campo, joinRef);
        this.order = order;
    }

    public OrderEnum getOrder() {
        return order;
    }

    public void setOrder(OrderEnum order) {
        this.order = order;
    }

    void adicionarOrdenacao(final StringBuilder sb, final String campo) {
        sb.append(campo == null ? "ORDER BY " : Sql.VIRGULA + Sql.ESPACO);
    }

}
