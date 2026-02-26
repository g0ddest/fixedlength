package name.velikodniy.vitaliy.fixedlength.annotation;

import name.velikodniy.vitaliy.fixedlength.Align;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a field (or a record constructor parameter) as a
 * fixed-length field and defines its position, width, and
 * formatting rules within a line.
 */
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface FixedField {
    /**
     * The 1-based character offset where this field starts
     * in the line.
     *
     * @return 1-based start position of the field
     */
    int offset();

    /**
     * The number of characters this field occupies in the line.
     *
     * @return width of the field in characters
     */
    int length();

    /**
     * Alignment of the value within the field. Determines
     * which side is padded when the value is shorter than
     * {@link #length()}.
     *
     * @return alignment of the field (default {@link Align#RIGHT})
     */
    Align align() default Align.RIGHT;

    /**
     * The character used for padding. Padding is added on the
     * side determined by {@link #align()} and stripped during
     * parsing.
     *
     * @return the padding character (default is a space)
     */
    char padding() default ' ';

    /**
     * Format pattern for date and time fields (e.g.
     * {@code "yyyyMMdd"}, {@code "HHmmss"}).
     *
     * @return the format pattern, or empty if not applicable
     */
    String format() default "";

    /**
     * Implicit decimal shift for numeric fields. The raw
     * integer value is divided by 10<sup>n</sup> during parsing
     * and multiplied by 10<sup>n</sup> during formatting. For
     * example, {@code "000101"} with {@code divide = 2} produces
     * {@code BigDecimal("1.01")}.
     *
     * @return the power of ten to divide by (default 0, meaning
     *         no division)
     */
    int divide() default 0;

    /**
     * Regular expression pattern for ignoring field content.
     * If the field value matches this pattern, it is treated
     * as absent (set to {@code null}).
     *
     * @return regex pattern for content to ignore, or empty
     *         to accept all content
     */
    String ignore() default "";

    /**
     * Whether to keep empty strings as field values.
     * If {@code true}, whitespace-only values are kept as-is.
     * If {@code false} (the default), they are set to
     * {@code null}.
     *
     * @return {@code true} to preserve empty strings
     */
    boolean allowEmptyStrings() default false;

    /**
     * Fallback value to use during formatting when the field
     * value is {@code null}. If this is not set and the field
     * value is {@code null}, the field is filled with the
     * {@link #padding()} character to preserve positional
     * alignment.
     *
     * @return the fallback string for {@code null} values, or
     *         empty to use padding
     */
    String fallbackStringForNullValue() default "";
}
