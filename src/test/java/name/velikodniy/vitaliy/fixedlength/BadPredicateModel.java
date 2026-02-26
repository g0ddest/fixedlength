package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

@FixedLine(predicate = BadPredicate.class)
public class BadPredicateModel implements Row {

    @FixedField(offset = 1, length = 5, align = Align.LEFT)
    public String value;

}
