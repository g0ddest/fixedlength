package name.velikodniy.vitaliy.fixedlength;

import java.math.BigDecimal;
import java.time.LocalDate;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public class EmployeeWithFallbackStrings implements Row {

    @FixedField(offset = 1, length = 10, align = Align.LEFT, fallbackStringForNullValue = " ")
    public String firstName;

    @FixedField(offset = 11, length = 10, align = Align.LEFT, fallbackStringForNullValue = " ")
    String lastName;

    @FixedField(offset = 21, length = 10, align = Align.LEFT, fallbackStringForNullValue = " ")
    protected String title;

    @FixedField(offset = 31, length = 6, align = Align.RIGHT, fallbackStringForNullValue = "0")
    private BigDecimal salary;

    @FixedField(offset = 37, length = 8, format = "MMddyyyy", ignore = "00000000", fallbackStringForNullValue = "00000000")
    public LocalDate hireDate;

}
