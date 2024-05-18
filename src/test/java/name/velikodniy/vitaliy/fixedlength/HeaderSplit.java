package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;
import name.velikodniy.vitaliy.fixedlength.annotation.SplitLineAfter;

@FixedLine(startsWith = "HEADER")
public class HeaderSplit {
    @FixedField(offset = 7, length = 10)
    public String title;
    @FixedField(offset = 17, length = 2)
    public int headerLength;

    @SplitLineAfter
    public int getSplitIndex() {
        return headerLength;
    }
}
