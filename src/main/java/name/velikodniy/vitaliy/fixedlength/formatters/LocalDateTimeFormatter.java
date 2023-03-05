package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeFormatter extends Formatter<LocalDateTime> {

    private static final String DEFAULT_FORMAT = "MMddyyyy HHmmss";

    private static DateTimeFormatter format(FixedField field) {
        return DateTimeFormatter.ofPattern(!field.format().isEmpty() ? field.format() : DEFAULT_FORMAT);
    }

    @Override
    public LocalDateTime asObject(String string, FixedField field) {
        return LocalDateTime.parse(string, format(field));
    }

    @Override
    public String asString(LocalDateTime object, FixedField field) {
        return object.format(format(field));
    }
}
