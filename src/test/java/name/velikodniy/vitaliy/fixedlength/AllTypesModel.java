package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.util.Date;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class AllTypesModel implements Row {

    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String name;

    @FixedField(offset = 11, length = 6, padding = '0')
    public Long longVal;

    @FixedField(offset = 17, length = 4, padding = '0')
    public Short shortVal;

    @FixedField(offset = 21, length = 8, format = "yyyyMMdd")
    public Date dateVal;

    @FixedField(offset = 29, length = 6, format = "HHmmss")
    public LocalTime timeVal;

    @FixedField(offset = 35, length = 15, format = "MMddyyyy HHmmss")
    public LocalDateTime dateTimeVal;

}
