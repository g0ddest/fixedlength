package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeFormatter extends Formatter<LocalTime> {
    @Override
    public LocalTime asObject(String string, FixedField field) {
        String format = "HHmmss";
        if (!field.format().isEmpty()) {
            format = field.format();
        }

        return LocalTime.parse(string, DateTimeFormatter.ofPattern(format));
    }
}
