package name.velikodniy.vitaliy.fixedlength.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FixedLine {
    /**
     * Indicator of line type. It should start with this string
     *
     * @return Indicator of line type
     */
    String startsWith() default "";

    /**
     * Predicate to check if the line is of this type.
     *
     * @return Predicate to check the line type.
     */
    Class<? extends Predicate<String>> predicate() default DefaultPredicate.class;

    class DefaultPredicate implements Predicate<String> {
        @Override
        public boolean test(String line) {
            return true; // Default predicate always returns true
        }
    }
}
