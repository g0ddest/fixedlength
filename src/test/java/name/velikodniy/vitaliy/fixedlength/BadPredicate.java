package name.velikodniy.vitaliy.fixedlength;

import java.util.function.Predicate;

/**
 * A predicate that cannot be instantiated (no public no-arg
 * constructor), used to test error handling.
 */
public class BadPredicate implements Predicate<String> {

    private BadPredicate() {
        // private constructor — cannot be instantiated by
        // FixedLength
    }

    @Override
    public boolean test(String s) {
        return true;
    }
}
