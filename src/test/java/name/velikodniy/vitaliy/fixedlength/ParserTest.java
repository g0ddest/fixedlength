package name.velikodniy.vitaliy.fixedlength;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
    @DisplayName("Parse as input stream with default charset and one line type with record emulation class")
    void testParseInheritedOneLineTypeRecord() throws FixedLengthException {
        List<EmployeeRecord> parse = new FixedLength<EmployeeRecord>()
                .registerLineType(EmployeeRecord.class)
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(2, parse.size());
        parse.forEach(e -> {
            assertNotNull(e.firstName);
            assertNotNull(e.lastName);
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

    @Test
    @DisplayName("Parse object with whitespaces preventing it to cast as null")
    void testParseWhitespaceNonNull() throws FixedLengthException {

        List<StringHolder> holders = new FixedLength<StringHolder>()
                .registerLineType(StringHolder.class)
                .parse(new ByteArrayInputStream("   some text to drop".getBytes()));

        assertEquals(1, holders.size());
        assertEquals("   ", holders.get(0).value);
    }

    @Test
    @DisplayName("Parse with string line delimiter")
    void testParseWithStringLineDelimiter() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .usingLineDelimiter("@")
                .parse(new ByteArrayInputStream(
                        mixedTypesCustomDelimiter.getBytes()));

        assertEquals(3, parse.size());
    }

    @Test
    @DisplayName("Parse with regex-special-char string delimiter")
    void testParseWithSpecialCharStringDelimiter() throws FixedLengthException {
        String data =
                "EmplJoe1      Smith     Developer 07500010012009|+|"
                        + "CatSnowball  20200103";
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .usingLineDelimiter("|+|")
                .parse(new ByteArrayInputStream(data.getBytes()));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as stream returns correct element count")
    void testParseAsStream() throws FixedLengthException {
        long count = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .parseAsStream(new ByteArrayInputStream(
                        singleTypeExample.getBytes()))
                .count();

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Parse as stream from reader")
    void testParseAsStreamReader() throws FixedLengthException {
        Stream<Row> stream = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .parseAsStream(new StringReader(singleTypeExample));

        assertEquals(2, stream.count());
    }

    @Test
    @DisplayName("failOnUnknownLines throws on unrecognized line")
    void testFailOnUnknownLines() {
        FixedLength<Object> impl = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .failOnUnknownLines();

        assertThrows(FixedLengthException.class, () ->
                impl.parse(new ByteArrayInputStream(
                        "UNKNOWN LINE CONTENT".getBytes())));
    }

    @Test
    @DisplayName("Register line type with invalid offset throws")
    void testInvalidOffsetThrows() {
        FixedLength<Object> impl = new FixedLength<>();

        assertThrows(FixedLengthException.class, () ->
                impl.registerLineType(BadOffsetModel.class));
    }

    @Test
    @DisplayName("Register line type with invalid length throws")
    void testInvalidLengthThrows() {
        FixedLength<Object> impl = new FixedLength<>();

        assertThrows(FixedLengthException.class, () ->
                impl.registerLineType(BadLengthModel.class));
    }

    @Test
    @DisplayName("Parse all supported types: Long, Short, Date, LocalTime, LocalDateTime")
    void testParseAllTypes() throws FixedLengthException {
        String input =
                "Alice     " // offset 1, length 10
                + "001234"   // offset 11, length 6
                + "0056"     // offset 17, length 4
                + "20200115" // offset 21, length 8
                + "143025"   // offset 29, length 6
                + "01152020 143025"; // offset 35, length 15

        List<AllTypesModel> parse = new FixedLength<AllTypesModel>()
                .registerLineType(AllTypesModel.class)
                .parse(new ByteArrayInputStream(input.getBytes()));

        assertEquals(1, parse.size());
        AllTypesModel m = parse.get(0);
        assertEquals("Alice", m.name);
        assertEquals(1234L, m.longVal);
        assertEquals((short) 56, m.shortVal);
        assertNotNull(m.dateVal);
        assertEquals(LocalTime.of(14, 30, 25), m.timeVal);
        assertEquals(
                LocalDateTime.of(2020, 1, 15, 14, 30, 25),
                m.dateTimeVal);
    }

    @Test
    @DisplayName("Format all supported types round-trip")
    void testFormatAllTypes() throws FixedLengthException {
        String input =
                "Alice     " // offset 1, length 10
                + "001234"   // offset 11, length 6
                + "0056"     // offset 17, length 4
                + "20200115" // offset 21, length 8
                + "143025"   // offset 29, length 6
                + "01152020 143025"; // offset 35, length 15

        FixedLength<AllTypesModel> impl =
                new FixedLength<AllTypesModel>()
                        .registerLineType(AllTypesModel.class);
        List<AllTypesModel> parsed = impl.parse(
                new ByteArrayInputStream(input.getBytes()));

        String formatted = impl.format(parsed);
        assertEquals(input, formatted);
    }

    @Test
    @DisplayName("Fallback string longer than field throws")
    void testFallbackTooLongThrows() {
        BadFallbackModel model = new BadFallbackModel();
        // value is null, fallback is "TOOLONG" (7 chars > 5)

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(BadFallbackModel.class);

        assertThrows(FixedLengthException.class, () ->
                impl.format(Collections.singletonList(model)));
    }

    @Test
    @DisplayName("parseAsStream(Reader) closes BufferedReader")
    void testParseAsStreamReaderCloses() {
        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(Employee.class);

        try (Stream<Row> stream = impl.parseAsStream(
                new StringReader(singleTypeExample))) {
            assertEquals(2, stream.count());
        }
    }

    @Test
    @DisplayName("Parse without registered line types throws")
    void testParseWithoutLineTypesThrows() {
        FixedLength<Object> impl = new FixedLength<>();

        assertThrows(FixedLengthException.class, () ->
                impl.parse(new ByteArrayInputStream(
                        "test".getBytes())));
    }

    @Test
    @DisplayName("Bad predicate class throws on parse")
    void testBadPredicateThrows() {
        FixedLength<Object> impl = new FixedLength<>()
                .registerLineType(BadPredicateModel.class);

        assertThrows(FixedLengthException.class, () ->
                impl.parse(new ByteArrayInputStream(
                        "hello".getBytes())));
    }

    @Test
    @DisplayName("FixedLengthException preserves cause")
    void testExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root");
        FixedLengthException ex =
                new FixedLengthException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("FixedLengthException no-arg constructor")
    void testExceptionNoArg() {
        FixedLengthException ex = new FixedLengthException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("parseAsStream(InputStream) closes scanner on stream close")
    void testParseAsStreamInputStreamCloses() {
        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(Employee.class);

        try (Stream<Row> stream = impl.parseAsStream(
                new ByteArrayInputStream(
                        singleTypeExample.getBytes()))) {
            assertEquals(2, stream.count());
        }
    }

    @Test
    @DisplayName("Parse with default date/time formats")
    void testParseDefaultFormats() throws FixedLengthException {
        // Default formats: LocalDate=yyyyMMdd, LocalTime=HHmmss,
        // LocalDateTime=MMddyyyy HHmmss, Date=yyyyMMdd
        String input =
                "20200115" // LocalDate (offset 1, len 8)
                + "143025"   // LocalTime (offset 9, len 6)
                + "01152020 143025" // LocalDateTime (offset 15, len 15)
                + "20200115"; // Date (offset 30, len 8)

        FixedLength<DefaultFormatModel> impl =
                new FixedLength<DefaultFormatModel>()
                        .registerLineType(DefaultFormatModel.class);

        List<DefaultFormatModel> parse = impl.parse(
                new ByteArrayInputStream(input.getBytes()));
        assertEquals(1, parse.size());

        DefaultFormatModel m = parse.get(0);
        assertEquals(
                LocalDate.of(2020, 1, 15), m.localDate);
        assertEquals(
                LocalTime.of(14, 30, 25), m.localTime);
        assertEquals(
                LocalDateTime.of(2020, 1, 15, 14, 30, 25),
                m.localDateTime);
        assertNotNull(m.date);

        // Round-trip format
        String result = impl.format(parse);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("Format HeaderSplit exercises IntegerFormatter.asString")
    void testFormatHeaderSplit() {
        String line = "HEADERMy Title  26        "
                + "EmplJoe1      Smith     "
                + "Developer 07500010012009";

        FixedLength<Object> impl = new FixedLength<>()
                .registerLineType(HeaderSplit.class)
                .registerLineType(EmployeeMixed.class);

        List<Object> parse = impl.parse(
                new ByteArrayInputStream(line.getBytes()));

        // HeaderSplit + EmployeeMixed from split
        assertEquals(2, parse.size());

        // Format the HeaderSplit object to cover
        // IntegerFormatter.asString
        String result = impl.format(parse);
        assertNotNull(result);
    }
}
