package br.mini.enums;

/**
 * Enum responsável por definir os tipos de join
 *
 * @author figueiredo-lucas
 *
 */
public enum JoinEnum {

    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN");

    private String value;

    JoinEnum(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
