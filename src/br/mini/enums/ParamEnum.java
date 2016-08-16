package br.mini.enums;

public enum ParamEnum {

    IGUAL("="),
    DIFERENTE("<>"),
    MAIOR(">"),
    MAIOR_IGUAL(">="),
    MENOR("<"),
    MENOR_IGUAL("<="),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    IN("IN"),
    NOT_IN("NOT IN"),
    IS("IS"),
    IS_NOT("IS NOT");

    private String value;

    ParamEnum(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
