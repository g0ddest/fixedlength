package name.velikodniy.vitaliy.fixedlength.annotation;

import name.velikodniy.vitaliy.fixedlength.Align;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface FixedField {
    int offset();

    /**
     * Length of field
     *
     * @return
     */
    int length();

    /**
     * Align of fixed format field
     *
     * @return
     */
    Align align() default Align.RIGHT;

    /**
     * Padding chars that will be trimmed. It depends on align.
     *
     * @return
     */
    char padding() default ' ';

    /**
     * Format for formattable fields like LocalDate
     *
     * @return
     */
    String format() default "";

    /**
     * If number fields should be divided. For example we have 000101 and we need to get BigDecimal 1.01
     *
     * @return
     */
    int divide() default 0;
}
