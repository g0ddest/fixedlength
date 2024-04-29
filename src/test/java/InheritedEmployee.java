import name.velikodniy.vitaliy.fixedlength.Align;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InheritedEmployee extends AbstractPerson implements Row {

    @FixedField(offset = 21, length = 10, align = Align.LEFT)
    protected String title;

    @FixedField(offset = 31, length = 6, padding = '0')
    private BigDecimal salary;

    @FixedField(offset = 37, length = 8, format = "MMddyyyy")
    public LocalDate hireDate;

}
