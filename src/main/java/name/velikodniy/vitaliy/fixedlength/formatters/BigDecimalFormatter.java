package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formatter for {@link BigDecimal} values with support for
 * implicit decimal points via {@link FixedField#divide()}.
 *
 * <p>When {@code divide} is set to {@code n}, the raw integer
 * value is divided by 10<sup>n</sup> during parsing and
 * multiplied by 10<sup>n</sup> during formatting. For example,
 * the string {@code "1234"} with {@code divide = 2} produces
 * {@code BigDecimal("12.34")}.
 */
public class BigDecimalFormatter extends Formatter<BigDecimal> {

    /** {@inheritDoc} */
    @Override
    public BigDecimal asObject(String string, FixedField field) {
        BigDecimal result = new BigDecimal("".equals(string) ? "0" : string);
        if (field.divide() != 0 && result.compareTo(BigDecimal.ZERO) != 0) {
            result = result.divide(
                    BigDecimal.TEN.pow(field.divide()),
                    field.divide(),
                    RoundingMode.HALF_UP);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String asString(BigDecimal object, FixedField field) {
        if (object == null) {
            return "0";
        }

        BigDecimal result = object;

        if (field.divide() != 0) {
            result = object.multiply(BigDecimal.TEN.pow(field.divide()));
        }

        return result.toPlainString();
    }
}
