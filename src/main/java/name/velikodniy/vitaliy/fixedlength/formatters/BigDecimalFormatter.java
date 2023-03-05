package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simple formatter for BigDecimal. Works with float numbers.
 * For example number 1234 turns to 12.34 if you specify
 * divide parameter as 2.
 */
public class BigDecimalFormatter extends Formatter<BigDecimal> {
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
