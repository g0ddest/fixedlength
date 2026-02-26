# Fixed Length handler for Java
 
[![Maven Central](https://img.shields.io/maven-central/v/name.velikodniy.vitaliy/fixedlength)](https://search.maven.org/artifact/name.velikodniy.vitaliy/fixedlength)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=g0ddest_fixedlength&metric=coverage)](https://sonarcloud.io/summary/new_code?id=g0ddest_fixedlength)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=g0ddest_fixedlength&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=g0ddest_fixedlength)
[![javadoc](https://javadoc.io/badge2/name.velikodniy.vitaliy/fixedlength/javadoc.svg)](https://javadoc.io/doc/name.velikodniy.vitaliy/fixedlength)

This is a fast, simple, zero-dependency library for Java 8+ that parses and formats fixed-length files (files where each field occupies a fixed position in every line).

The library was inspired by [Fixed Length File Handler](https://github.com/GuiaBolso/fixed-length-file-handler) and [fixedformat4j](https://github.com/jeyben/fixedformat4j).

One of its advantages is support for mixed line types.

It works with `InputStream`, so it is more memory-efficient than storing the entire file in memory. This is a big advantage when working with large files.

## Download

This library is published to Maven Central and to GitHub Packages.

Just ensure that you have

```groovy
repositories {
    mavenCentral()
}
```

Optionally, you can get the package from GitHub Packages:

Gradle:
```groovy
repositories {
    mavenCentral()
    maven {
        url "https://maven.pkg.github.com/g0ddest/fixedlength"
        credentials {
             username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
             password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}
```
(you need to add a property with your username and GitHub token, or set them as environment variables).

And then configure the dependency:

Maven:
```xml
<dependency>
  <groupId>name.velikodniy.vitaliy</groupId>
  <artifactId>fixedlength</artifactId>
  <version>0.14</version>
  <type>pom</type>
</dependency>
```

Gradle:
```groovy
implementation 'name.velikodniy.vitaliy:fixedlength:0.14'
```

Ivy:
```xml
<dependency org='name.velikodniy.vitaliy' name='fixedlength' rev='0.14'>
  <artifact name='fixedlength' ext='pom' ></artifact>
</dependency>
```

## Usage
### Basic usage
For example, you can transform these lines into 2 different kinds of objects:

```
EmplJoe1      Smith     Developer 07500010012009
CatSnowball  20200103
EmplJoe3      Smith     Developer 
```

This is common when processing data in legacy systems.

You just need to write a class with the field structure and annotate each field that you want to map to your file.

To parse this simple file

```
Joe1      Smith     
Joe3      Smith     
```

you just need to write this class (annotated fields can also be inherited from parent classes):

```java
public class Employee {
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 10, length = 10, align = Align.LEFT)
    public String lastName;
}
```

and run the parser:

```java
List<Object> parse = new FixedLength()
    .registerLineType(Employee.class)
    .parse(fileStream);
```

### Mixed line types
If there are multiple line types in your file and they start with different strings, you can register different line types.

To do this, add an annotation to your class:

```java
@FixedLine(startsWith = "Empl")
```

So you can parse this file:

```
EmplJoe1      Smith     
CatSnowball  
EmplJoe3      Smith     
```

with these classes:

```java
@FixedLine(startsWith = "Empl")
public class EmployeeMixed {

    @FixedField(offset = 5, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 15, length = 10, align = Align.LEFT)
    public String lastName;
}
```

(fields can be final as well).

```java
@FixedLine(startsWith = "Cat")
public class CatMixed {

    @FixedField(offset = 4, length = 10, align = Align.LEFT)
    public String name;

    @FixedField(offset = 14, length = 8, format = "yyyyMMdd")
    public LocalDate birthDate;

}
```

and run the parser like this:

```java
List<Object> parse = new FixedLength()
    .registerLineType(EmployeeMixed.class)
    .registerLineType(CatMixed.class)
    .parse(fileStream);
```

### Custom formatters
If you need to use a custom class or type in the parser, you can add your own formatter like this:

```java
public class StringFormatter extends Formatter<String> {
    @Override
    public String asObject(String string, FixedField field) {
        return string;
    }
}
```

and register it with the `registerFormatter` method on a `FixedLength` instance.

### Annotation parameters
Here are all the attributes of the `FixedField` annotation:
* `offset` — the position where this field starts. The line starts at offset 1.
* `length` — the length of the field in characters.
* `align` — which side the content is justified to. Used together with padding.
* `padding` — the filler character, trimmed based on alignment. For example, `" 1"` becomes `"1"`.
* `format` — a parameter passed to the formatter. For example, a date format pattern.
* `divide` — for numeric fields, automatically divides the value by 10^n, where n is the value of this parameter.
* `ignore` — the parser will ignore the field content if it matches the given regular expression. For example, `"0{8}"` will ignore `"00000000"`
* `allowEmptyStrings` — the parser will keep empty strings instead of replacing them with `null`
* `fallbackStringForNullValue` — when formatting an object back to a fixed length string, the formatter will replace a `null` value for this field with the given fallback string. If the fallback string is shorter than the field length, it will be padded according to the specified alignment and padding character.

### Generics support

You can also use generics to cast parsed objects to the desired class.
This is more convenient when you have a file with a single entity type.

```java
List<Employee> parse = new FixedLength<Employee>()
                .registerLineType(Employee.class);
```

### Ignoring errors

If there are errors in your data, two modes allow you to skip them:

* `skipErroneousLines` — a line with an error will not be added to the result.
* `skipErroneousFields` — fields with errors will be `null`.

In both cases, warnings will be logged.

By default, an exception is thrown on the first error.

### Splitting lines

If you have 2 different records in one line and there is a split index, you can add a method to your entity that returns the index of the next record and mark it with the `SplitLineAfter` annotation.

For example, the record

```
HEADERMy Title  26        EmplJoe1      Smith     Developer 07500010012009
```

The number 26 indicates the index of the next record.

You can describe it with this entity:

```java
@FixedLine(startsWith = "HEADER")
public class HeaderSplit {
    @FixedField(offset = 7, length = 10)
    public String title;
    @FixedField(offset = 17, length = 2)
    public int headerLength;

    @SplitLineAfter
    public int getSplitIndex() {
        return headerLength;
    }
}
```

### Custom rules for mixed lines

The `startsWith` parameter provides an easy way to identify the class to deserialize, but sometimes it is not enough. For more complex cases, use the `predicate` parameter in the `FixedLine` annotation. Just implement `Predicate<String>` and pass the class reference in the annotation.

```java
@FixedLine(predicate = EmployeePositionPredicate.class)
```

This class will be initialized just once and cached. 

### Handling empty fields during formatting

When formatting an object back to a fixed-length string, `null` fields are filled with the padding character by default, preserving positional alignment.

If you need a specific value instead of padding, use the `fallbackStringForNullValue` parameter in the `FixedField` annotation. If the fallback string is shorter than the field length, it will be padded according to the specified alignment and padding character.

Let's say we have a class defined as follows:
```java
public class Employee {
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 10, length = 10, align = Align.LEFT)
    public String lastName;

    @FixedField(offset = 20, length = 10, align = Align.LEFT)
    public String role;

    @FixedField(offset = 30, length = 8, align = Align.LEFT, ignore = "0{8}")
    public LocalDate joinDate;
}
```

When parsing the following lines, there will be two `null` values for the 2nd line: `lastName` and `joinDate`:
```
Joe1      Smith     Developer 12122009
Joe3                Tester    00000000
```

By default, formatting the 2nd line back produces padding for null fields:
```
Joe3                Tester
```

To use a meaningful fallback value (e.g. `"00000000"` for dates), specify it explicitly:
```java
public class Employee {
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 10, length = 10, align = Align.LEFT)
    public String lastName;

    @FixedField(offset = 20, length = 10, align = Align.LEFT)
    public String role;

    @FixedField(offset = 30, length = 8, align = Align.LEFT, ignore = "0{8}", fallbackStringForNullValue = "00000000")
    public LocalDate joinDate;
}
```

Now formatting produces:
```
Joe3                Tester    00000000
```

## Java records support

There is experimental support for Java 14+ records without breaking Java 8 compatibility.

Just annotate the record's constructor as follows:

```java
record Employee (
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    String firstName,

    @FixedField(offset = 10, length = 10, align = Align.LEFT)
    String lastName
){}
```

and it works the same way as an annotated class.

## Benchmark

There is a benchmark that you can run with the `gradle jmh` command. You can change its parameters in `src/jmh/java/name/velikodniy/vitaliy/fixedlength/benchmark/BenchmarkRunner.java`.
