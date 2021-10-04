package name.velikodniy.vitaliy.fixedlength;

import java.util.Arrays;

public enum Align {
    RIGHT {
        public String make(String data, int length, char paddingChar) {
            String result;
            if (data == null) {
                data = "";
            }
            int dataLength = data.length();
            if (dataLength > length) {
                result = Align.substring(data, dataLength - length, dataLength);
            } else {
                result = Align.leftPad(data, length, paddingChar);
            }
            return result;
        }

        public String remove(String data, char paddingChar) {
            String result = data;
            if (data == null) {
                result = "";
            }
            while (result.startsWith("" + paddingChar)) {
                result = result.substring(1);
            }
            if (paddingChar == '0' && result.isEmpty()) {
                result = "0";
            }
            return result;
        }
    },
    LEFT {
        public String make(String data, int length, char paddingChar) {
            String result;
            if (data == null) {
                data = "";
            }
            int dataLength = data.length();
            if (dataLength > length) {
                result = Align.substring(data, 0, length);
            } else {
                result = Align.rightPad(data, length, paddingChar);
            }
            return result;
        }

        public String remove(String data, char paddingChar) {
            String result = data;
            if (data == null) {
                result = "";
            }
            while (result.endsWith("" + paddingChar)) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
    };

    public abstract String make(String data, int length, char paddingChar);

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
        if (padStr == null || padStr.length() == 0) {
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

    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (padStr == null || padStr.length() == 0) {
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

    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }
}
