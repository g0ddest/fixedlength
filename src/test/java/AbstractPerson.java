import name.velikodniy.vitaliy.fixedlength.Align;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public abstract class AbstractPerson {
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 11, length = 10, align = Align.LEFT)
    String lastName;
}
