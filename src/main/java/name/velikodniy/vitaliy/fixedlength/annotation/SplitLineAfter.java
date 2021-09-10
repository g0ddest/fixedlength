package name.velikodniy.vitaliy.fixedlength.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If several records can be on one line, this annotation specifies the method to call on the
 * current record to identify the index in the line for the next record. The specified method must
 * return an int.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SplitLineAfter {
}
