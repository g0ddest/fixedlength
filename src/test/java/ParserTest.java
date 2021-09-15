import name.velikodniy.vitaliy.fixedlength.FixedLength;
import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

class ParserTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
                    "Joe3      Smith     Developer ";

    String mixedTypesExample =
            "EmplJoe1      Smith     Developer 07500010012009\n" +
                    "CatSnowball  20200103\n" +
                    "CatNoBirthDt 00000000\n" +
                    "EmplJoe3      Smith     Developer ";

    String mixedTypesSplitRecordExample =
            "HEADERMy Title  26        EmplJoe1      Smith     Developer 07500010012009\n" +
                    "CatSnowball  20200103\n" +
                    "EmplJoe3      Smith     Developer ";

    String mixedTypesCustomDelimiter =
            "EmplJoe1      Smith     Developer 07500010012009@" +
                    "CatSnowball  20200103@" +
                    "EmplJoe3      Smith     Developer ";

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineType() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assert parse.size() == 2;
    }

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineTypeUS_ACII() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(Employee.class)
                .usingCharset(StandardCharsets.US_ASCII)
                .parse(
                        new ByteArrayInputStream(singleTypeExample.getBytes(StandardCharsets.US_ASCII)));

        assert parse.size() == 2;
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type")
    void testParseMixedLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesExample.getBytes()));

        assert parse.size() == 4;
        assert parse.get(0) instanceof EmployeeMixed;
        assert parse.get(1) instanceof CatMixed;
        assert parse.get(2) instanceof CatMixed;
        assert parse.get(3) instanceof EmployeeMixed;
        EmployeeMixed employeeMixed = (EmployeeMixed) parse.get(0);
        assert "Joe1".equals(employeeMixed.firstName);
        assert "Smith".equals(employeeMixed.lastName);
        CatMixed catMixed = (CatMixed) parse.get(1);
        assert LocalDate.of(2020, 1, 3).equals(catMixed.birthDate);
        catMixed = (CatMixed) parse.get(2);
        assert catMixed.birthDate == null;
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type with split record")
    void testParseMixedLineTypeSplit() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(HeaderSplit.class)
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesSplitRecordExample.getBytes()));

        assert parse.size() == 4;
        assert parse.get(0) instanceof HeaderSplit;
        assert parse.get(1) instanceof EmployeeMixed;
        assert parse.get(2) instanceof CatMixed;
        assert parse.get(3) instanceof EmployeeMixed;
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type and custom delimiter")
    void testParseMixedLineTypeCustomDelimiter() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .usingLineDelimiter(Pattern.compile("@"))
                .parse(new ByteArrayInputStream(mixedTypesCustomDelimiter.getBytes()));

        assert parse.size() == 3;

    }
}
