package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class LongFormatter extends Formatter<Long> {
    @Override
    public Long asObject(String string, FixedField field) {
        return Long.parseLong(string);
    }

    @Override
    public String asString(Long object, FixedField field) {
        return object.toString();
    }
}
