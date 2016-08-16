package br.mini.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação responsável pelas descrições de coluna para referência em banco.
 * <br>
 * <b>Restrita apenas para atributos.</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Coluna {

    /**
     * <b>Obrigatório</b>
     * <br>
     * <br>
     * Atributo referente ao nome da coluna.
     *
     * @return nome da coluna
     */
    String nome();

    /**
     * <b>Opcional</b> (default <b>false</b>)
     * <br>
     * <br>
     * Atributo referente a campos de PK
     *
     * @return booleano se é PK
     */
    boolean isPk() default false;

    /**
     * <b>Opcional</b> (default <b>false</b>)
     * <br>
     * <br>
     * Atributo referente a campos de FK
     *
     * @return booleano se é FK
     */
    boolean isFk() default false;

    /**
     * <b>Opcional</b> (default <b>false</b>)
     * <br>
     * <br>
     * Atributo referente a campos CLOB
     *
     * @return booleano se é um CLOB
     */
    boolean isClob() default false;

    /**
     * <b>Opcional</b> (default <b>0</b>)
     * <br>
     * <br>
     * Atributo referente ao tamanho do campo no banco.
     *
     * @return booleano se é PK
     */
    int tamanho() default 0;
}
