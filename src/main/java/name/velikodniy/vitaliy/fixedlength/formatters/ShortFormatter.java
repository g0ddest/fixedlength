package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class ShortFormatter extends Formatter<Short> {
    @Override
    public Short asObject(String string, FixedField field) {
        return Short.parseShort(string);
    }

    @Override
    public String asString(Short object, FixedField field) {
        return object.toString();
    }
}
