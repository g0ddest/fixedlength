package name.velikodniy.vitaliy.fixedlength;

import java.util.Arrays;

/**
 * Alignment of a value within a fixed-length field.
 *
 * <p>Determines how values shorter than the field width are
 * padded and how padding is stripped during parsing.
 *
 * <ul>
 *   <li>{@link #RIGHT} — pads on the left (e.g. numeric fields)
 *   <li>{@link #LEFT}  — pads on the right (e.g. text fields)
 * </ul>
 */
public enum Align {
    RIGHT {
        /**
         * Left-pads {@code data} with {@code paddingChar} to
         * reach {@code length}. If {@code data} is longer than
         * {@code length}, the leftmost characters are truncated.
         */
        public String make(String data, int length, char paddingChar) {
            String result = Align.leftPad(data, length, paddingChar);
            if (data == null) {
                data = "";
            }
            int dataLength = data.length();
            if (dataLength > length) {
                result = Align.substring(data, dataLength - length, dataLength);
            }
            return result;
        }

        /**
         * Strips leading {@code paddingChar} characters from
         * {@code data}. If all characters are padding and the
         * padding character is {@code '0'}, returns {@code "0"}.
         */
        public String remove(String data, char paddingChar) {
            if (data == null || data.isEmpty()) {
                return paddingChar == '0' ? "0" : "";
            }
            int start = 0;
            while (start < data.length()
                    && data.charAt(start) == paddingChar) {
                start++;
            }
            if (start == data.length()) {
                return paddingChar == '0' ? "0" : "";
            }
            return data.substring(start);
        }
    },
    LEFT {
        /**
         * Right-pads {@code data} with {@code paddingChar} to
         * reach {@code length}. If {@code data} is longer than
         * {@code length}, the rightmost characters are truncated.
         */
        public String make(String data, int length, char paddingChar) {
            String result = Align.rightPad(data, length, paddingChar);
            if (data == null) {
                data = "";
            }
            int dataLength = data.length();
            if (dataLength > length) {
                result = Align.substring(data, 0, length);
            }
            return result;
        }

        /**
         * Strips trailing {@code paddingChar} characters from
         * {@code data}.
         */
        public String remove(String data, char paddingChar) {
            if (data == null || data.isEmpty()) {
                return "";
            }
            int end = data.length();
            while (end > 0
                    && data.charAt(end - 1) == paddingChar) {
                end--;
            }
            return data.substring(0, end);
        }
    };

    /**
     * Pads or truncates {@code data} to exactly {@code length}
     * characters using {@code paddingChar}.
     *
     * @param data        the value to pad (may be {@code null})
     * @param length      the target field width
     * @param paddingChar the character used for padding
     * @return a string of exactly {@code length} characters
     */
    public abstract String make(String data, int length, char paddingChar);

    /**
     * Removes padding characters from the aligned side of
     * {@code data}.
     *
     * @param data        the padded value (may be {@code null})
     * @param paddingChar the padding character to strip
     * @return the value with padding removed
     */
    public abstract String remove(String data, char paddingChar);

    private static final int MAX_PAD = 8192;

    private static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }
        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return "";
        }
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    private static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > MAX_PAD) {
            return leftPad(str, size, Character.toString(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    private static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (padStr == null || padStr.isEmpty()) {
            padStr = " ";
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= MAX_PAD) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    /**
     * Right-pads {@code str} with {@code padChar} to reach
     * {@code size} characters. Returns {@code str} unchanged
     * if it is already at least {@code size} characters long.
     *
     * @param str     the string to pad (may be {@code null})
     * @param size    the target length
     * @param padChar the padding character
     * @return the padded string, or {@code null} if input is
     *         {@code null}
     */
    public static String rightPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > MAX_PAD) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(repeat(padChar, pads));
    }

    /**
     * Right-pads {@code str} by repeating {@code padStr} to
     * reach {@code size} characters.
     *
     * @param str    the string to pad (may be {@code null})
     * @param size   the target length
     * @param padStr the padding string
     * @return the padded string, or {@code null} if input is
     *         {@code null}
     */
    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (padStr == null || padStr.isEmpty()) {
            padStr = " ";
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= MAX_PAD) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    /**
     * Creates a string consisting of {@code ch} repeated
     * {@code repeat} times.
     *
     * @param ch     the character to repeat
     * @param repeat the number of repetitions (zero or negative
     *               returns an empty string)
     * @return the repeated string
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }
}
