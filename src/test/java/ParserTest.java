import name.velikodniy.vitaliy.fixedlength.FixedLength;
import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

class ParserTest {

    String example1 =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

    String example2 =
            "EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "EmplJoe3      Smith     Developer ";

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength()
                .registerLineType(Employee.class)
                .parse(new ByteArrayInputStream(example1.getBytes()));

        assert parse.size() == 2;
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type")
    void testParseMixedLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(example2.getBytes()));

        assert parse.size() == 3;
        assert parse.get(0) instanceof EmployeeMixed;
        assert parse.get(1) instanceof CatMixed;
        assert parse.get(2) instanceof EmployeeMixed;
        EmployeeMixed employeeMixed = (EmployeeMixed) parse.get(0);
        assert "Joe1".equals(employeeMixed.firstName);
        assert "Smith".equals(employeeMixed.lastName);
        CatMixed catMixed = (CatMixed) parse.get(1);
        assert LocalDate.of(2020, 1, 3).equals(catMixed.birthDate);
    }

}
