package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class IntegerFormatter extends Formatter<Integer> {
    @Override
    public Integer asObject(String string, FixedField field) {
        return Integer.parseInt(string);
    }
}
