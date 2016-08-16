package br.mini.criterio;

import br.mini.Constantes.Sql;
import br.mini.enums.LogicEnum;
import br.mini.enums.ParamEnum;

/**
 * Classe para encapsular os parâmetros vindos através do método {@link Criterio#addCriterio}
 *
 * @see Tipos
 *
 * @author figueiredo-lucas
 */
class Parametros extends Tipos {

    /**
     * Enum utilizado para definir o parâmetro
     */
    private ParamEnum enumParametro;

    /**
     * Parâmetro já parseado
     */
    private String parametro;

    /**
     * Enum utilizado para definir o operador lógico
     */
    private LogicEnum enumOpLogico;

    /**
     * Operador lógico já parseado
     */
    private String opLogico;

    /**
     * Valor para bind no campo
     */
    private Object valor;

    /**
     * Construtor parametrizado para a classe
     *
     * @param campo
     * @param enumParametro
     * @param valor
     * @param opLogicoEnum
     * @param joinRef
     */
    Parametros(final String campo, final ParamEnum enumParametro, final Object valor, final LogicEnum opLogicoEnum,
            final Join joinRef) {
        super(campo, joinRef);
        setParametro(enumParametro);
        setValor(valor);
        setOpLogico(opLogicoEnum);
    }

    /**
     * @return {@link #parametro}
     */
    String getParametro() {
        return parametro;
    }

    /**
     * Setter de {@link #enumParametro} e {@link #parametro}
     *
     * @param enumParametro
     */
    void setParametro(ParamEnum enumParametro) {
        this.enumParametro = enumParametro;
        this.parametro = enumParametro.getValue();
    }

    /**
     * @return {@link #enumOpLogico}
     */
    LogicEnum getEnumOpLogico() {
        return enumOpLogico;
    }

    /**
     * @return {@link #opLogico}
     */
    String getOpLogico() {
        if (opLogico == null) {
            opLogico = "AND";
        }
        return opLogico;
    }

    /**
     * Setter de {@link #enumOpLogico} e {@link #opLogico}
     *
     * @param enumOpLogico
     */
    void setOpLogico(LogicEnum enumOpLogico) {
        this.enumOpLogico = enumOpLogico;
        this.opLogico = enumOpLogico.toString();
    }

    /**
     * @return {@link #valor}
     */
    Object getValor() {
        return valor;
    }

    /**
     * Setter de {@link #valor}
     *
     * @param valor
     */
    void setValor(Object valor) {
        this.valor = valor;
    }

    /**
     * @return {@link #enumParametro}
     */
    ParamEnum getEnumParametro() {
        return enumParametro;
    }

    /**
     * Método utilizado pelo {@link Criterio#construirCondicao} para adição de operador lógico (AND|OR) ou WHERE
     *
     * @param sb
     */
    void adicionarOperadorLogico(final StringBuilder sb) {
        if (sb.toString().equals("")) {
            sb.append("WHERE ");
        } else {
            sb.append(getOpLogico() + Sql.ESPACO);
        }
    }

    /**
     * Método responsável pelo tratamento de como o parâmetro será adicionado na consulta de acordo com o ParamEnum
     *
     * @param campo
     * @param sb
     */
    void adicionarTipoParam(final String campo, final StringBuilder sb) {
        if (paramInOrNotIn()) {
            sb.append(campo + Sql.ESPACO + getParametro() + Sql.ESPACO + Sql.ABRE_PARENT + getValor().toString()
                    + Sql.FECHA_PARENT + Sql.ESPACO);
        } else if (paramIsOrIsNot()) {
            sb.append(campo + Sql.ESPACO + getParametro() + " NULL ");
        } else if (getValor().getClass().isAssignableFrom(String.class)) {
            sb.append("UPPER(" + campo + Sql.FECHA_PARENT + Sql.ESPACO + getParametro() + " UPPER(?) ");
        } else {
            sb.append(campo + Sql.ESPACO + getParametro() + " ? ");
        }
    }

    /**
     * Validação de existência de parâmetros para IN ou NOT IN
     *
     * @return existencia desse tipo de parâmetro
     */
    boolean paramInOrNotIn() {
        return ParamEnum.IN.equals(getEnumParametro()) || ParamEnum.NOT_IN.equals(getEnumParametro());
    }

    /**
     * Validação de existência de parâmetros para IS ou IS NOT
     *
     * @return existencia desse tipo de parâmetro
     */
    boolean paramIsOrIsNot() {
        return ParamEnum.IS.equals(getEnumParametro()) || ParamEnum.IS_NOT.equals(getEnumParametro());
    }

}
