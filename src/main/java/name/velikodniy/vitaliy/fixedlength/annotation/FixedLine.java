package name.velikodniy.vitaliy.fixedlength.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FixedLine {
    /**
     * Indicator of line type. It should start with this string
     *
     * @return Indicator of line type
     */
    String startsWith();
}
