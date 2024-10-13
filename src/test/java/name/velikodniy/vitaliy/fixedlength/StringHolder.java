package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class StringHolder {
    @FixedField(offset = 1, length = 3, padding = Character.MIN_VALUE, allowEmptyStrings = true)
    public String value;
}
