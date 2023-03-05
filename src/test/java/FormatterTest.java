import name.velikodniy.vitaliy.fixedlength.FixedLength;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

public class FormatterTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

    @Test
    @DisplayName("Simple string format")
    public void simpleFormat() {

        FixedLength<Row> impl = new FixedLength<Row>()
                .registerLineType(Employee.class);

        List<Row> parse = impl
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assert singleTypeExample.equals(impl.format(parse));

    }

}
