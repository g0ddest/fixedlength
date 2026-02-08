package name.velikodniy.vitaliy.fixedlength;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatterTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

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

        assertEquals(singleTypeExample, impl.format(parse));

    }

    @Test
    @DisplayName("Simple string format class hierarchy")
    void simpleFormatInherited() {

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(InheritedEmployee.class);

        List<Row> parse = impl
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(singleTypeExample, impl.format(parse));

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
    
}
