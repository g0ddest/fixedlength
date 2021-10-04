package name.velikodniy.vitaliy.fixedlength.benchmark;

import java.io.InputStream;

public class FixedLengthBenchStream extends InputStream {

    private final byte[] string;
    private final int times;
    private int position = 0;
    private int currentIteration = 0;

    public FixedLengthBenchStream(String string, int times) {
        this.string = (string + "\n").getBytes();
        this.times = times;
    }

    @Override
    public int read() {
        if (position > string.length - 1 && currentIteration >= times - 1) {
            return -1;
        }
        if (position > string.length - 1) {
            currentIteration++;
            position = 0;
        }
        return string[position++];
    }
}
