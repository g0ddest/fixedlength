package name.velikodniy.vitaliy.fixedlength;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AlignTest {

    @Test
    @DisplayName("RIGHT.make pads on the left")
    void rightMakePads() {
        assertEquals("  abc", Align.RIGHT.make("abc", 5, ' '));
    }

    @Test
    @DisplayName("RIGHT.make truncates when too long")
    void rightMakeTruncates() {
        assertEquals("cde", Align.RIGHT.make("abcde", 3, ' '));
    }

    @Test
    @DisplayName("RIGHT.make handles null input")
    void rightMakeNull() {
        assertNull(Align.RIGHT.make(null, 5, ' '));
    }

    @Test
    @DisplayName("RIGHT.remove strips leading padding")
    void rightRemoveStrips() {
        assertEquals("123", Align.RIGHT.remove("00123", '0'));
    }

    @Test
    @DisplayName("RIGHT.remove returns 0 for all-zero padding")
    void rightRemoveAllZero() {
        assertEquals("0", Align.RIGHT.remove("0000", '0'));
    }

    @Test
    @DisplayName("RIGHT.remove returns empty for all-space padding")
    void rightRemoveAllSpaces() {
        assertEquals("", Align.RIGHT.remove("    ", ' '));
    }

    @Test
    @DisplayName("RIGHT.remove handles null")
    void rightRemoveNull() {
        assertEquals("", Align.RIGHT.remove(null, ' '));
    }

    @Test
    @DisplayName("RIGHT.remove handles empty string")
    void rightRemoveEmpty() {
        assertEquals("", Align.RIGHT.remove("", ' '));
    }

    @Test
    @DisplayName("RIGHT.remove with zero padding on null returns 0")
    void rightRemoveNullZero() {
        assertEquals("0", Align.RIGHT.remove(null, '0'));
    }

    @Test
    @DisplayName("LEFT.make pads on the right")
    void leftMakePads() {
        assertEquals("abc  ", Align.LEFT.make("abc", 5, ' '));
    }

    @Test
    @DisplayName("LEFT.make truncates when too long")
    void leftMakeTruncates() {
        assertEquals("abc", Align.LEFT.make("abcde", 3, ' '));
    }

    @Test
    @DisplayName("LEFT.make handles null input")
    void leftMakeNull() {
        assertNull(Align.LEFT.make(null, 5, ' '));
    }

    @Test
    @DisplayName("LEFT.remove strips trailing padding")
    void leftRemoveStrips() {
        assertEquals("abc", Align.LEFT.remove("abc   ", ' '));
    }

    @Test
    @DisplayName("LEFT.remove handles null")
    void leftRemoveNull() {
        assertEquals("", Align.LEFT.remove(null, ' '));
    }

    @Test
    @DisplayName("LEFT.remove handles empty string")
    void leftRemoveEmpty() {
        assertEquals("", Align.LEFT.remove("", ' '));
    }

    @Test
    @DisplayName("LEFT.remove all padding returns empty")
    void leftRemoveAllPadding() {
        assertEquals("", Align.LEFT.remove("   ", ' '));
    }

    @Test
    @DisplayName("repeat creates correct string")
    void repeatCreates() {
        assertEquals("***", Align.repeat('*', 3));
    }

    @Test
    @DisplayName("repeat with zero returns empty")
    void repeatZero() {
        assertEquals("", Align.repeat('x', 0));
    }

    @Test
    @DisplayName("repeat with negative returns empty")
    void repeatNegative() {
        assertEquals("", Align.repeat('x', -1));
    }

    @Test
    @DisplayName("rightPad pads correctly")
    void rightPadBasic() {
        assertEquals("ab**", Align.rightPad("ab", 4, '*'));
    }

    @Test
    @DisplayName("rightPad returns same when already long enough")
    void rightPadNoChange() {
        assertEquals("abcd", Align.rightPad("abcd", 3, '*'));
    }

    @Test
    @DisplayName("rightPad handles null")
    void rightPadNull() {
        assertNull(Align.rightPad(null, 5, ' '));
    }

    @Test
    @DisplayName("rightPad with string padStr")
    void rightPadStringPad() {
        assertEquals("abxyxy",
                Align.rightPad("ab", 6, "xy"));
    }

    @Test
    @DisplayName("rightPad with string padStr exact fit")
    void rightPadStringExact() {
        assertEquals("abxy",
                Align.rightPad("ab", 4, "xy"));
    }

    @Test
    @DisplayName("rightPad with string padStr partial")
    void rightPadStringPartial() {
        assertEquals("abx",
                Align.rightPad("ab", 3, "xy"));
    }

    @Test
    @DisplayName("rightPad with null padStr defaults to space")
    void rightPadNullPadStr() {
        assertEquals("ab  ",
                Align.rightPad("ab", 4, null));
    }

    @Test
    @DisplayName("rightPad with empty padStr defaults to space")
    void rightPadEmptyPadStr() {
        assertEquals("ab  ",
                Align.rightPad("ab", 4, ""));
    }

    @Test
    @DisplayName("rightPad null input returns null")
    void rightPadNullInput() {
        assertNull(Align.rightPad(null, 5, "x"));
    }

    @Test
    @DisplayName("rightPad no padding needed")
    void rightPadNoPadding() {
        assertEquals("abcde",
                Align.rightPad("abcde", 3, "x"));
    }
}
