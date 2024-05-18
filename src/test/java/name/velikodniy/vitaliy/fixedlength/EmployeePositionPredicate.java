package name.velikodniy.vitaliy.fixedlength;

import java.util.function.Predicate;

public class EmployeePositionPredicate implements Predicate<String> {

    @Override
    public boolean test(String s) {
        return s.contains("POSITION");
    }
}