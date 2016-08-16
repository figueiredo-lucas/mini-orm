package br.mini.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação responsável pela descrição da tabela.
 * <br>
 * <b>Restrita apenas para classes.</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Tabela {

    /**
     * <b>Obrigatório</b>
     * <br>
     * <br>
     * Atributo referente ao nome da tabela.
     *
     * @return nome da tabela
     */
    String nome();

    /**
     * <b>Opcional</b> (default <b>""</b>)
     * <br>
     * <br>
     * Atributo referente ao prefixo da PK da tabela
     *
     * @return prefixo da PK da tabela
     */
    String prefixo() default "";

    /**
     * <b>Opcional</b> (default <b>false</b>)
     * <br>
     * <br>
     * Atributo referente ao necessidade ou não de cache da tabela
     *
     * @return se a tabela é cacheável ou não
     */
    boolean cacheable() default false;

}
