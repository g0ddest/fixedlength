package name.velikodniy.vitaliy.fixedlength.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method that returns the character index at which the
 * current line should be split to extract the next record.
 *
 * <p>When multiple records are packed into a single line, the
 * annotated method is called after the first record is parsed.
 * Its return value (an {@code int}) indicates the 0-based index
 * in the raw line where the next record starts. The remainder
 * of the line is then matched and parsed recursively.
 *
 * <p>If the returned index is zero, negative, or beyond the
 * line length, no split is performed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SplitLineAfter {
}
