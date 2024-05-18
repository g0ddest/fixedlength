package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;

@FixedLine(predicate = EmployeePositionPredicate.class)
public class EmployeePosition {

    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    private String position;

    public String getPosition() {
        return position;
    }
}
