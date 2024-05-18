package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

@FixedLine
public class EmployeeWithEmptyAnnotation implements Row {
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 11, length = 10, align = Align.LEFT)
    String lastName;
}
