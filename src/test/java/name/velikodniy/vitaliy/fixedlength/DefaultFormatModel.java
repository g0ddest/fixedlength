package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Model that uses date/time fields with default format patterns
 * (no explicit format specified).
 */
public class DefaultFormatModel implements Row {

    @FixedField(offset = 1, length = 8)
    public LocalDate localDate;

    @FixedField(offset = 9, length = 6)
    public LocalTime localTime;

    @FixedField(offset = 15, length = 15)
    public LocalDateTime localDateTime;

    @FixedField(offset = 30, length = 8)
    public Date date;

}
