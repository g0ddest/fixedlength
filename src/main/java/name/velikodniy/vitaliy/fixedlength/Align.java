package name.velikodniy.vitaliy.fixedlength;

public enum Align {
    RIGHT {
        public String remove(String data, char paddingChar) {
            String result = data;
            if (data == null) {
                result = "";
            }
            while (result.startsWith("" + paddingChar)) {
                result = result.substring(1);
            }
            if (paddingChar == '0' && result.isEmpty()) result = "0";
            return result;
        }
    },
    LEFT {
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

    public abstract String remove(String data, char paddingChar);
}
