package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class BadLengthModel {
    @FixedField(offset = 1, length = 0)
    public String value;
}
