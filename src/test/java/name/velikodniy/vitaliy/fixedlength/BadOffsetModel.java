package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class BadOffsetModel {
    @FixedField(offset = 0, length = 5)
    public String value;
}
