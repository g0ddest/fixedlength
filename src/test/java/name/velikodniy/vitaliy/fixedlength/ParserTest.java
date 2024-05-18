package name.velikodniy.vitaliy.fixedlength;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

    String singleTypeWithErrorExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe1      Smith     Developer 07500013012009";

    String mixedTypesExample =
            "EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "CatNoBirthDt 00000000\n" +
            "EmplJoe3      Smith     Developer ";

    String mixedTypesSplitRecordExample =
            "HEADERMy Title  26        EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "EmplJoe3      Smith     Developer ";

    String mixedTypesWrongSplitRecordExample =
            "HEADERMy Title  00        EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "EmplJoe3      Smith     Developer ";

    String mixedTypesCustomDelimiter =
            "EmplJoe1      Smith     Developer 07500010012009@" +
            "CatSnowball  20200103@" +
            "EmplJoe3      Smith     Developer ";

    String mixedTypesCustomExample =
            "EmplJoe1      Smith     Developer 07500010012009\n" +
            "Engineer      POSITION";

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseInheritedOneLineType() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(InheritedEmployee.class)
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(2, parse.size());
        parse.forEach(e -> {
            assertNotNull(((InheritedEmployee) e).firstName);
            assertNotNull(((InheritedEmployee) e).lastName);
        });
    }

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineType() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with default charset and one line type and empty annotation")
    void testParseOneLineTypeEmptyAnnotation() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(EmployeeWithEmptyAnnotation.class)
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with throwing exception when format erroneous fields")
    void testParseThrowsExceptionOnInvalidFormat() throws FixedLengthException {
        assertThrows(DateTimeParseException.class, () ->
                new FixedLength<Row>()
                        .registerLineType(Employee.class)
                        .parse(new ByteArrayInputStream(singleTypeWithErrorExample.getBytes())));
    }

    @Test
    @DisplayName("Parse as input stream with skipping format erroneous fields")
    void testParseWithSkippingErroneousFields() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .skipErroneousFields()
                .parse(new ByteArrayInputStream(singleTypeWithErrorExample.getBytes()));

        assertEquals(2, parse.size());
        assertNull(((Employee) parse.get(1)).hireDate);
    }

    @Test
    @DisplayName("Parse as input stream with skipping format erroneous lines")
    void testParseWithSkippingErroneousLines() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .skipErroneousLines()
                .parse(new ByteArrayInputStream(singleTypeWithErrorExample.getBytes()));

        assertEquals(1, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineTypeUS_ACII() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(Employee.class)
                .usingCharset(StandardCharsets.US_ASCII)
                .parse(
                        new ByteArrayInputStream(singleTypeExample.getBytes(StandardCharsets.US_ASCII)));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type")
    void testParseMixedLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesExample.getBytes()));

        assertEquals(4, parse.size());
        assertThat(parse.get(0), instanceOf(EmployeeMixed.class));
        assertThat(parse.get(1), instanceOf(CatMixed.class));
        assertThat(parse.get(2), instanceOf(CatMixed.class));
        assertThat(parse.get(3), instanceOf(EmployeeMixed.class));
        EmployeeMixed employeeMixed = (EmployeeMixed) parse.get(0);
        assertEquals("Joe1", employeeMixed.firstName);
        assertEquals("Smith", employeeMixed.lastName);
        CatMixed catMixed = (CatMixed) parse.get(1);
        assertEquals(LocalDate.of(2020, 1, 3), catMixed.birthDate);
        catMixed = (CatMixed) parse.get(2);
        assertNull(catMixed.birthDate);
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type with split record")
    void testParseMixedLineTypeSplit() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(HeaderSplit.class)
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesSplitRecordExample.getBytes()));

        assertEquals(4, parse.size());
        assertThat(parse.get(0), instanceOf(HeaderSplit.class));
        assertThat(parse.get(1), instanceOf(EmployeeMixed.class));
        assertThat(parse.get(2), instanceOf(CatMixed.class));
        assertThat(parse.get(3), instanceOf(EmployeeMixed.class));
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type with wrong split record")
    void testParseMixedLineTypeWrongSplit() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(HeaderSplit.class)
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesWrongSplitRecordExample.getBytes()));

        assertEquals(3, parse.size());
        assertThat(parse.get(0), instanceOf(HeaderSplit.class));
        assertThat(parse.get(1), instanceOf(CatMixed.class));
        assertThat(parse.get(2), instanceOf(EmployeeMixed.class));
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type and custom delimiter")
    void testParseMixedLineTypeCustomDelimiter() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .usingLineDelimiter(Pattern.compile("@"))
                .parse(new ByteArrayInputStream(mixedTypesCustomDelimiter.getBytes()));

        assertEquals(3, parse.size());
    }

    @Test
    @DisplayName("Parse as reader with default charset and one line type")
    void testParseReaderWithDefaultCharset() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .parse(new StringReader(singleTypeExample));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type and custom predicate")
    void testParseMixedLineTypeCustomPredicate() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(EmployeePosition.class)
                .parse(new ByteArrayInputStream(mixedTypesCustomExample.getBytes()));

        assertEquals(2, parse.size());
    }
}
