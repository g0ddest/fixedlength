package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class BadFallbackModel implements Row {

    @FixedField(
            offset = 1, length = 5,
            align = Align.LEFT,
            fallbackStringForNullValue = "TOOLONG"
    )
    public String value;

}
