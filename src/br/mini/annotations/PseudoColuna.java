package br.mini.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação responsável pelas descrições das colunas para a criação das PseudoTabelas.
 * <br>
 * <b>Restrita apenas para atributos.</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PseudoColuna {

    /**
     * <b>Obrigatório</b>
     * <br>
     * <br>
     * Atributo referente aos nomes das colunas.
     *
     * @return array com nomes das colunas
     */
    String[] colunas();

    /**
     * <b>Obrigatório</b>
     * <br>
     * <br>
     * Atributo referente ao alias para as colunas
     *
     * @return alias das colunas
     */
    String alias();

    /**
     * <b>Opcional</b> (default <b>Object.class</b>)
     * <br>
     * <br>
     * Atributo referente a classe de referência
     *
     * @return classe de referência
     */
    Class<?> referencia() default Object.class;

    /**
     * <b>Opcional</b> (default <b>""</b>)
     * <br>
     * <br>
     * Atributo referente ao campo que faz o vínculo das classes principais
     *
     * @return nome do campo
     */
    String campoVinculo() default "";

    /**
     * <b>Opcional</b> (default <b>""</b>)
     * <br>
     * <br>
     * Atributo referente ao campo que faz o vínculo das classes de referência
     *
     * @return booleano se é PK
     */
    String campoReferencia() default "";
}
