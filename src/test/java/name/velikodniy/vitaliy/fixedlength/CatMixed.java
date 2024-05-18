package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

import java.time.LocalDate;

@FixedLine(startsWith = "Cat")
public class CatMixed {

    @FixedField(offset = 4, length = 10, align = Align.LEFT)
    public String name;

    @FixedField(offset = 14, length = 8, format = "yyyyMMdd", ignore = "0{8}")
    public LocalDate birthDate;

}
