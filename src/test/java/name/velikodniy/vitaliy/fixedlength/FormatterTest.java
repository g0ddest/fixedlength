package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.formatters.Formatter;
import name.velikodniy.vitaliy.fixedlength.formatters.IntegerFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatterTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

    String singleTypeExampleFormatted =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer 000000        ";

    String singleTypeExampleWithNullValues =
            "Joe1                Developer  7500010012009\n" +
            "Joe3      Smith                 950012122008\n" +
            "Joe4                               000000000\n" +
            "          Smith     Developer  6500010012009";

    @Test
    @DisplayName("Simple string format")
    void simpleFormat() {

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(Employee.class);

        List<Row> parse = impl
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(singleTypeExampleFormatted, impl.format(parse));

    }

    @Test
    @DisplayName("Simple string format class hierarchy")
    void simpleFormatInherited() {

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(InheritedEmployee.class);

        List<Row> parse = impl
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(singleTypeExampleFormatted, impl.format(parse));

    }

    @Test
    @DisplayName("Simple string format with null values")
    void simpleFormatWithNullValues() {

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(EmployeeWithFallbackStrings.class);

        List<Row> parse = impl
                .parse(new ByteArrayInputStream(singleTypeExampleWithNullValues.getBytes()));

        assertEquals(singleTypeExampleWithNullValues, impl.format(parse));

    }

    @Test
    @DisplayName("Format null field without fallback fills with padding")
    void formatNullWithoutFallback() {
        Employee emp = new Employee();
        emp.firstName = "Test";
        // lastName, title, salary, hireDate are all null

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(Employee.class);

        String result = impl.format(
                Collections.singletonList(emp));

        // Total width: 10+10+10+6+8 = 44
        assertEquals(44, result.length());
        assertEquals("Test      ", result.substring(0, 10));
    }

    @Test
    @DisplayName("registerFormatter overrides default")
    void registerFormatterOverride() {
        FixedLength<Row> impl = new FixedLength<Row>()
                .registerFormatter(
                        Integer.class, IntegerFormatter.class)
                .registerLineType(Employee.class);

        List<Row> parse = impl.parse(
                new ByteArrayInputStream(
                        singleTypeExample.getBytes()));
        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("registerLineTypes with list")
    @SuppressWarnings("unchecked")
    void registerLineTypesList() {
        List<Class<Row>> types = Arrays.asList(
                (Class<Row>) (Class<?>) Employee.class,
                (Class<Row>) (Class<?>)
                        InheritedEmployee.class);

        FixedLength<Row> impl = new FixedLength<Row>();
        impl.registerLineTypes(types);

        List<Row> parse = impl.parse(
                new ByteArrayInputStream(
                        singleTypeExample.getBytes()));
        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("registerLineTypes with array")
    @SuppressWarnings("unchecked")
    void registerLineTypesArray() {
        Class<Row>[] types = new Class[]{Employee.class};

        FixedLength<Row> impl = new FixedLength<Row>();
        impl.registerLineTypes(types);

        List<Row> parse = impl.parse(
                new ByteArrayInputStream(
                        singleTypeExample.getBytes()));
        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Deprecated stopSkipUnknownLines delegates")
    @SuppressWarnings("deprecation")
    void stopSkipUnknownLinesDelegates() {
        FixedLength<Object> impl = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .stopSkipUnknownLines();

        assertThrows(FixedLengthException.class, () ->
                impl.parse(new ByteArrayInputStream(
                        "UNKNOWN".getBytes())));
    }

    @Test
    @DisplayName("Formatter.instance throws for unknown type")
    void formatterInstanceUnknownType() {
        Map<Class<? extends Serializable>,
                Class<? extends Formatter<? extends Serializable>>>
                formatters = Formatter.getDefaultFormatters();

        assertThrows(FixedLengthException.class, () ->
                Formatter.instance(formatters, Boolean.class));
    }

    @Test
    @DisplayName("Format EmployeeRecord via constructor-based parsing")
    void formatEmployeeRecord() {
        String input = "Joe1      Smith     Developer "
                + "07500010012009";

        FixedLength<EmployeeRecord> impl =
                new FixedLength<EmployeeRecord>()
                        .registerLineType(EmployeeRecord.class);

        List<EmployeeRecord> parse = impl.parse(
                new ByteArrayInputStream(input.getBytes()));

        assertEquals(1, parse.size());
        String result = impl.format(parse);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("Format EmployeeMixed exercises BigDecimal divide")
    void formatWithBigDecimalDivide() {
        // EmployeeMixed: salary field has divide=2
        // This exercises BigDecimalFormatter.asString with
        // divide > 0
        String line =
                "EmplJoe1      Smith     Developer "
                + "07500010012009";

        FixedLength<Object> impl = new FixedLength<>()
                .registerLineType(EmployeeMixed.class);

        List<Object> parse = impl.parse(
                new ByteArrayInputStream(line.getBytes()));
        assertEquals(1, parse.size());

        EmployeeMixed emp = (EmployeeMixed) parse.get(0);
        assertNotNull(emp.salary);

        String result = impl.format(parse);
        assertNotNull(result);
        // Verify the result contains formatted content
        assertEquals("Joe1", result.substring(0, 4));
    }

}
