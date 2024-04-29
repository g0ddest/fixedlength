 # Fixed Length handler for Java
 
[![Maven Central](https://img.shields.io/maven-central/v/name.velikodniy.vitaliy/fixedlength)](https://search.maven.org/artifact/name.velikodniy.vitaliy/fixedlength)
![Gradle Build](https://github.com/g0ddest/fixedlength/workflows/Gradle%20Build/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=g0ddest_fixedlength&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=g0ddest_fixedlength)

This is fast simple zero-dependency library for Java 8+ that aims to parse fixed length (files with entities placed on fixed place in every line) files.

Library was inspired by [Fixed Length File Handler](https://github.com/GuiaBolso/fixed-length-file-handler) and [fixedformat4j](https://github.com/jeyben/fixedformat4j).

One of its advantages is support mixed line types.

It works with `InputStream` so it is more memory efficient than store all file in memory. This is big 
advantage when working with big files.  

## Download

This library is published to Maven Central and to Github packages, so you'll need to configure that in your repositories:

Just ensure that you have 

```groovy
repositories {
    mavenCentral()
}
```

or optionally if you want you can get the package from the Github packages

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
(you need to add property with your username and github token, or put them into system envs).

And then configure dependency:

Maven:
```xml
<dependency>
  <groupId>name.velikodniy.vitaliy</groupId>
  <artifactId>fixedlength</artifactId>
  <version>0.10</version>
  <type>pom</type>
</dependency>
```

Gradle:
```groovy
implementation 'name.velikodniy.vitaliy:fixedlength:0.10'
```

Ivy:
```xml
<dependency org='name.velikodniy.vitaliy' name='fixedlength' rev='0.10'>
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

you need just write down this class (annotated fields also could be pulled from annotated classes):

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
* `ignore` — the parser will ignore the field content if it matches the given regular expression. For example, `"0{8}"` will ignore `"00000000"`

### Generics support

You can also use generics to cast parsed object to desired class.
It is more convenient if you have file with one entity type.

```java
List<Employee> parse = new FixedLength<Employee>()
                .registerLineType(Employee.class);
```

### Ignoring errors

If there is errors on your line format there are two modes that you could skip these errors if you want to:

* `skipErroneousLines` — line with error will not be added to result.
* `skipErroneousFields` — fields with errors will be `null`.

In both cases warnings will be raised in logs.

By default, exception will be raised for entire process.

### Cases to use

In the case if you have 2 different records in one line and there is a split index you can add a method in your entity that should return index of the next record and mark it with annotation `SplitLineAfter`.

For example record

```
HEADERMy Title  26        EmplJoe1      Smith     Developer 07500010012009
```

Number 26 indicates index of the next record.

You can describe it with entity:

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

## Benchmark

There is a benchmark, you can run it with `gradle jmh` command. Also, you can change running parameters of it in file `src/jmh/java/name/velikodniy/vitaliy/fixedlength/benchmark/BenchmarkRunner.java`. 
