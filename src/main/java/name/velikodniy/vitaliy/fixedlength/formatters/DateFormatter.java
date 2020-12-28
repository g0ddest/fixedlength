package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter extends Formatter<Date> {
    @Override
    public Date asObject(String string, FixedField field) {
        String format = "yyyyMMdd";
        if (!field.format().isEmpty())
            format = field.format();
        try {
            return new SimpleDateFormat(format).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }
}
