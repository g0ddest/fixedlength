package name.velikodniy.vitaliy.fixedlength.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Marks a class as a line type in a fixed-length file and
 * specifies how to identify lines of this type.
 *
 * <p>For single-type files, the annotation can be omitted or
 * used with default values (all lines will match). For
 * mixed-format files, use {@link #startsWith()} and/or
 * {@link #predicate()} to distinguish line types.
 *
 * <p>Example:
 * <pre>{@code
 * &#64;FixedLine(startsWith = "HDR")
 * public class HeaderRecord { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FixedLine {
    /**
     * A prefix string that lines of this type must start with.
     *
     * <p>An empty string (the default) means no prefix matching
     * is performed.
     *
     * @return the line prefix, or empty for no prefix filtering
     */
    String startsWith() default "";

    /**
     * A custom predicate class used to determine whether a line
     * belongs to this type.
     *
     * <p>The predicate class must have a public no-argument
     * constructor. Predicate instances are cached per
     * {@code FixedLength} instance.
     *
     * <p>Defaults to {@link DefaultPredicate} which accepts all
     * lines.
     *
     * @return the predicate class for line-type identification
     */
    Class<? extends Predicate<String>> predicate()
            default DefaultPredicate.class;

    /**
     * Default predicate that accepts all lines. Used as the
     * default value for {@link FixedLine#predicate()}.
     */
    class DefaultPredicate implements Predicate<String> {
        @Override
        public boolean test(String line) {
            return true;
        }
    }
}
