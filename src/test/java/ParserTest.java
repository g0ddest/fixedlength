import name.velikodniy.vitaliy.fixedlength.FixedLength;
import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

public class ParserTest {

    String expamle1 =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

    String expamle2 =
            "EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "EmplJoe3      Smith     Developer ";

    @Test
    public void testParseOneLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength()
                .registerLineType(Employee.class)
                .parse(new ByteArrayInputStream(expamle1.getBytes()));

        assert parse.size() == 2;
    }

    @Test
    public void testParseMixedLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(expamle2.getBytes()));

        assert parse.size() == 3;
    }

}
