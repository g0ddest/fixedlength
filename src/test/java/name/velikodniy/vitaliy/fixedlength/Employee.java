package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee implements Row {

    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 11, length = 10, align = Align.LEFT)
    String lastName;

    @FixedField(offset = 21, length = 10, align = Align.LEFT)
    protected String title;

    @FixedField(offset = 31, length = 6, padding = '0')
    private BigDecimal salary;

    @FixedField(offset = 37, length = 8, format = "MMddyyyy")
    public LocalDate hireDate;

}
