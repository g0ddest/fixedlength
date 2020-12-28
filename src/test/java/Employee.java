import name.velikodniy.vitaliy.fixedlength.Align;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {

    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 10, length = 10, align = Align.LEFT)
    public String lastName;

    @FixedField(offset = 20, length = 10)
    public String title;

    @FixedField(offset = 30, length = 6)
    public BigDecimal salary;

    @FixedField(offset = 37, length = 8, format = "MMddyyyy")
    public LocalDate hireDate;

}
