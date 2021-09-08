package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeFormatter extends Formatter<LocalDateTime> {

    @Override
    public LocalDateTime asObject(String string, FixedField field) {
        String format = "MMddyyyy HHmmss";
        if (!field.format().isEmpty()) {
            format = field.format();
        }

        return LocalDateTime.parse(string, DateTimeFormatter.ofPattern(format));
    }
}
