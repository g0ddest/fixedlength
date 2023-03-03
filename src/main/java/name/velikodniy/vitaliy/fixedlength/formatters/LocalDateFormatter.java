package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateFormatter extends Formatter<LocalDate> {

    private static final String DEFAULT_FORMAT = "yyyyMMdd";

    private static DateTimeFormatter format(FixedField field) {
        return DateTimeFormatter.ofPattern(!field.format().isEmpty() ? field.format() : DEFAULT_FORMAT);
    }

    @Override
    public LocalDate asObject(String string, FixedField field) {
        return LocalDate.parse(string, format(field));
    }

    @Override
    public String asString(LocalDate object, FixedField field) {
        return object.format(format(field));
    }
}
