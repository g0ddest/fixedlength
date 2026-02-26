package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.formatters.DateFormatter;
import name.velikodniy.vitaliy.fixedlength.formatters.Formatter;
import name.velikodniy.vitaliy.fixedlength.formatters.StringFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatterUnitTest {

    /**
     * Minimal stub for FixedField annotation used in unit tests.
     */
    @SuppressWarnings("all")
    private static FixedField stubField(String format) {
        return new FixedField() {
            public Class<? extends Annotation> annotationType() {
                return FixedField.class;
            }

            public int offset() { return 1; }

            public int length() { return 10; }

            public Align align() { return Align.RIGHT; }

            public char padding() { return ' '; }

            public String format() { return format; }

            public int divide() { return 0; }

            public String ignore() { return ""; }

            public boolean allowEmptyStrings() { return false; }

            public String fallbackStringForNullValue() {
                return "";
            }
        };
    }

    @Test
    @DisplayName("Formatter.parse returns null for null input")
    void parseNull() {
        StringFormatter f = new StringFormatter();
        assertNull(f.parse(null, stubField("")));
    }

    @Test
    @DisplayName("Formatter.parse delegates to asObject")
    void parseNonNull() {
        StringFormatter f = new StringFormatter();
        assertEquals("hello", f.parse("hello", stubField("")));
    }

    @Test
    @DisplayName("DateFormatter returns null for unparseable date")
    void dateFormatterBadInput() {
        DateFormatter f = new DateFormatter();
        Date result = f.asObject("NOTADATE", stubField(""));
        assertNull(result);
    }

    @Test
    @DisplayName("Formatter.instance throws for bad formatter class")
    void instanceBadFormatter() {
        Map<Class<? extends Serializable>,
                Class<? extends Formatter<? extends Serializable>>>
                formatters = Formatter.getDefaultFormatters();

        assertThrows(FixedLengthException.class, () ->
                Formatter.instance(formatters, Boolean.class));
    }
}
