package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

import java.math.BigDecimal;
import java.time.LocalDate;

@FixedLine(startsWith = "Empl")
public class EmployeeMixed {

    @FixedField(offset = 5, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 15, length = 10, align = Align.LEFT)
    public String lastName;

    @FixedField(offset = 25, length = 10, align = Align.LEFT)
    public String title;

    @FixedField(offset = 35, length = 6, divide = 2)
    public BigDecimal salary;

    @FixedField(offset = 41, length = 8, format = "MMddyyyy")
    public LocalDate hireDate;
}
