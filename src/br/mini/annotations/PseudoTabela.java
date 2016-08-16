package br.mini.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação responsável pelas descrições das tabelas utilizadas na criação das PseudoTabelas.
 * <br>
 * <b>Restrita apenas para classes.</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PseudoTabela {

    /**
     * <b>Obrigatório</b>
     * <br>
     * <br>
     * Atributo referente aos nomes das tabelas.
     *
     * @return array com nomes das colunas
     */
    String[] tabelas();

    /**
     * <b>Obrigatório</b>
     * <br>
     * <br>
     * Atributo referente ao alias para as tabelas
     *
     * @return alias das tabelas
     */
    String alias();

}
