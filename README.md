 # Fixed Length handler for Java
 
 ![Gradle Build](https://github.com/g0ddest/fixedlength/workflows/Gradle%20Build/badge.svg?branch=master)

This is fast simple zero-dependency library for Java 8+ that aims to parse fixed length files.

Library was inspired by [Fixed Length File Handler](https://github.com/GuiaBolso/fixed-length-file-handler) and [fixedformat4j](https://github.com/jeyben/fixedformat4j).

One of its advantages is support mixed line types.

It works with `InputStream` so it is more memory efficient than store all file in memory. This is big advantage when working with big files.  

## Download

This library is published to Bintray jcenter, so you'll need to configure that in your repositories:

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

And then configure dependency:

Maven:
```xml
<dependency>
  <groupId>name.velikodniy.vitaliy</groupId>
  <artifactId>fixedlength</artifactId>
  <version>0.2</version>
  <type>pom</type>
</dependency>
```

Gradle:
```groovy
implementation 'name.velikodniy.vitaliy:fixedlength:0.2'
```

Ivy:
```xml
<dependency org='name.velikodniy.vitaliy' name='fixedlength' rev='0.2'>
  <artifact name='fixedlength' ext='pom' ></artifact>
</dependency>
```

## Usage

For example, you can transform this lines to 2 different kind of objects:

```
EmplJoe1      Smith     Developer 07500010012009
CatSnowball  20200103
EmplJoe3      Smith     Developer 
```

It's usual when processing data in some legacy systems.

You just need to write class with field structure and annotate each field that you want to connect with your file.

To parse this simple file

```
Joe1      Smith     
Joe3      Smith     
```

you need just write down this class:

```java
public class Employee {
    @FixedField(offset = 1, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 10, length = 10, align = Align.LEFT)
    public String lastName;
}
```

and run parser:

```java
List<Object> parse = new FixedLength()
    .registerLineType(Employee.class)
    .parse(fileStream);
```

If there are few line types in your file and they starts with different string you can register different line types.

To do this you should add annotation to your class:

```java
@FixedLine(startsWith = "Empl")
```

So you can parse this file:

```
EmplJoe1      Smith     
CatSnowball  
EmplJoe3      Smith     
```

with this files:

```java
@FixedLine(startsWith = "Empl")
public class EmployeeMixed {

    @FixedField(offset = 5, length = 10, align = Align.LEFT)
    public String firstName;

    @FixedField(offset = 15, length = 10, align = Align.LEFT)
    public String lastName;
}
```

```java
@FixedLine(startsWith = "Cat")
public class CatMixed {

    @FixedField(offset = 4, length = 10, align = Align.LEFT)
    public String name;

    @FixedField(offset = 14, length = 8, format = "yyyyMMdd")
    public LocalDate birthDate;

}
```

and run parser like that:

```java
List<Object> parse = new FixedLength()
    .registerLineType(EmployeeMixed.class)
    .registerLineType(CatMixed.class)
    .parse(fileStream);
```

If you need to use a custom class or type in parser you can add your own formatter like this:

```java
public class StringFormatter extends Formatter<String> {
    @Override
    public String asObject(String string, FixedField field) {
        return string;
    }
}
```

and register it with `registerFormatter` method on `FixedLength` instance.

There are all fields in `FixedField` annotation:
* `offset` —  position on which this fields starts. Line starts with offset 1.
* `length` — length of the field
* `align` — on which side the content is justified. It works with padding.
* `padding` — based on align trimming filler symbols. For example `" 1"` becomes `"1"`.
* `format` — parameters that goes to formatter. For example, it can be date format.
* `divide` — for number fields you can automatically divide the value on 10^n where n is value of this parameter.
