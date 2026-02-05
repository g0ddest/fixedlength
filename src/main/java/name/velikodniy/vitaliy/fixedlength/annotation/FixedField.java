package name.velikodniy.vitaliy.fixedlength.annotation;

import name.velikodniy.vitaliy.fixedlength.Align;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface FixedField {
    /**
     * Offset of the field
     * @return offset of the field
     */
    int offset();

    /**
     * Length of the field
     *
     * @return length of the field
     */
    int length();

    /**
     * Align of fixed format field, default is RIGHT
     *
     * @return align of fixed format field
     */
    Align align() default Align.RIGHT;

    /**
     * Padding chars that will be trimmed. It depends on align. By default, it is whitespace.
     *
     * @return padding chars that will be trimmed
     */
    char padding() default ' ';

    /**
     * Format for formattable fields like LocalDate
     *
     * @return Format for formattable fields like LocalDate
     */
    String format() default "";

    /**
     * If number fields should be divided. For example, we have 000101, and we need to get BigDecimal 1.01
     *
     * @return divide to 10^(divide)
     */
    int divide() default 0;

    /**
     * Ignore field content if is matches this regular expression pattern.
     * @return pattern matching content to ignore
     */
    String ignore() default "";

    /**
     * Allows empty string as value, by default it is false.
     * If true - values will be kept as is
     * If false - value will be null
     * @return allows empty string as value boolean
     */
    boolean allowEmptyStrings() default false;

    /**
     * Value to use as fallback during formatting, if the field content is null.
     * If this is not set and the field value is null, the field will be skipped completely during formatting.
     * @return value to use for formatting in case the field value is null
     */
    String fallbackStringForNullValue() default "";
}
