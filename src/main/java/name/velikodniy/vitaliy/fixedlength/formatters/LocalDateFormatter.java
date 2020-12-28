package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateFormatter extends Formatter<LocalDate> {
    @Override
    public LocalDate asObject(String string, FixedField field) {
        String format = "yyyyMMdd";
        if (!field.format().isEmpty())
            format = field.format();

        return LocalDate.parse(string, DateTimeFormatter.ofPattern(format));
    }
}
