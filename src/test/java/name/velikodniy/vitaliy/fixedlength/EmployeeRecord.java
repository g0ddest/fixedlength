package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * this class emulates built java 14+'s record as follows
 *  record EmployeeRecord(
 *         @FixedField(offset = 1, length = 10, align = Align.LEFT)
 *         String firstName,
 *         @FixedField(offset = 11, length = 10, align = Align.LEFT)
 *         String lastName,
 *         @FixedField(offset = 21, length = 10, align = Align.LEFT)
 *         String title,
 *         @FixedField(offset = 31, length = 6, padding = '0')
 *         BigDecimal salary,
 *         @FixedField(offset = 37, length = 8, format = "MMddyyyy")
 *         LocalDate hireDate
 * ){}
 */
class EmployeeRecord{
        @FixedField(offset = 1, length = 10, align = Align.LEFT)
        public String firstName;
        @FixedField(offset = 11, length = 10, align = Align.LEFT)
        public String lastName;
        @FixedField(offset = 21, length = 10, align = Align.LEFT)
        public String title;
        @FixedField(offset = 31, length = 6, padding = '0')
        public BigDecimal salary;
        @FixedField(offset = 37, length = 8, format = "MMddyyyy")
        public LocalDate hireDate;

        public EmployeeRecord(String firstName, String lastName, String title, BigDecimal salary, LocalDate hireDate) {
                this.firstName = firstName;
                this.lastName = lastName;
                this.title = title;
                this.salary = salary;
                this.hireDate = hireDate;
        }
}
