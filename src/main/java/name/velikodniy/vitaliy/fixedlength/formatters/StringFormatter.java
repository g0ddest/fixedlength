package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class StringFormatter extends Formatter<String> {
    @Override
    public String asObject(String string, FixedField field) {
        return string;
    }
}
