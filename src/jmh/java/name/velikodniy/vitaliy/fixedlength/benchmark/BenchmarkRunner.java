package name.velikodniy.vitaliy.fixedlength.benchmark;

import name.velikodniy.vitaliy.fixedlength.FixedLength;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Fork(3)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class BenchmarkRunner {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Param({"10", "1000", "100000", "1000000"})
    private int n;

    private InputStream stream;

    @Setup
    public void setup() {
        String mixedTypesExample =
                "EmplJoe1      Smith     Developer 07500010012009";
        stream = new FixedLengthBenchStream(mixedTypesExample, n);
    }

    @Benchmark
    public void mixed() {

        List<Object> parse = new FixedLength()
                .registerLineType(EmployeeMixed.class)
                .parse(stream);
    }
}
