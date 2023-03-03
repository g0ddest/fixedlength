package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter extends Formatter<Date> {

    private static final String DEFAULT_FORMAT = "yyyyMMdd";

    private static SimpleDateFormat format(FixedField field) {
        return new SimpleDateFormat(!field.format().isEmpty() ? field.format() : DEFAULT_FORMAT);
    }

    @Override
    public Date asObject(String string, FixedField field) {
        try {
            return format(field).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String asString(Date object, FixedField field) {
        return format(field).format(object);
    }
}
