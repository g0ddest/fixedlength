import name.velikodniy.vitaliy.fixedlength.FixedLength;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatterTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

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
    
}
