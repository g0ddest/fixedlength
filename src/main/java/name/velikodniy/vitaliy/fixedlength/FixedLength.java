package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;
import name.velikodniy.vitaliy.fixedlength.annotation.SplitLineAfter;
import name.velikodniy.vitaliy.fixedlength.formatters.Formatter;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ALL")
public class FixedLength {

    private static Map<
                    Class<? extends Serializable>,
                    Class<? extends Formatter>
                    > formatters
            = Formatter.getDefaultFormatters();
    private List<FixedFormatLine> lineTypes = new ArrayList<>();
    private boolean skipUnknownLines = true;
    private Charset charset = Charset.defaultCharset();
    private Pattern delimiter = Pattern.compile("\n");

    private static FixedFormatLine classToLineDesc(final Class clazz) {
        FixedFormatLine fixedFormatLine = new FixedFormatLine();
        fixedFormatLine.clazz = clazz;
        FixedLine annotation =
                (FixedLine) clazz.getDeclaredAnnotation(FixedLine.class);
        if (annotation != null) {
            fixedFormatLine.startsWith = annotation.startsWith();
        }
        for (Field field : clazz.getFields()) {
          FixedField fieldAnnotation = field.getDeclaredAnnotation(FixedField.class);
          if (fieldAnnotation == null) {
            continue;
          }
          fixedFormatLine.fixedFormatFields.add(new FixedFormatField(field, fieldAnnotation));
        }

        for(Method method : clazz.getMethods()) {
          SplitLineAfter splitLineAfter = method.getDeclaredAnnotation(SplitLineAfter.class);
          if(splitLineAfter == null) {
            continue;
          }
          fixedFormatLine.splitAfterMethod = method;
        }
        return fixedFormatLine;
    }

    public FixedLength registerLineType(final Class lineClass) {
        lineTypes.add(classToLineDesc(lineClass));
        return this;
    }

  /**
   * Add formatter to work with class types.
   * @param typeClass type that should be formatter
   * @param formatterClass formatter to pass through
   * @return instance of FixedLength
   */
    public FixedLength registerFormatter(
            final Class<? extends Serializable> typeClass,
            final Class<? extends Formatter> formatterClass) {
        formatters.put(typeClass, formatterClass);
        return this;
    }

    public FixedLength stopSkipUnknownLines() {
        skipUnknownLines = false;
        return this;
    }

    public FixedLength registerLineTypes(final List<Class> lineClasses) {
        lineTypes.addAll(
                lineClasses.stream()
                        .map(c -> classToLineDesc(c))
                        .collect(Collectors.toList())
        );
        return this;
    }

    public FixedLength registerLineTypes(final Class[] lineClasses) {
        registerLineTypes(Arrays.asList(lineClasses));
        return this;
    }

    public FixedLength usingCharset(Charset charset) {
      this.charset = requireNonNull(charset, "Charset can't be null");
      return this;
    }

    public FixedLength usingLineDelimiter(Pattern pattern) {
      this.delimiter = requireNonNull(pattern, "Line delimiter pattern can't be  null");
      return this;
    }

    private FixedFormatRecord fixedFormatLine(String line) {
        if (lineTypes.size() == 1) {
            if (lineTypes.get(0).startsWith == null) {
              return new FixedFormatRecord(line, lineTypes.get(0));
            } else if (line.startsWith(lineTypes.get(0).startsWith)) {
              return new FixedFormatRecord(line, lineTypes.get(0));
            } else {
              return null;
            }
        }
        for (FixedFormatLine lineType : lineTypes) {
            if (
                    lineType.startsWith != null
                    &&
                    line.startsWith(lineType.startsWith)
            ) {
                return new FixedFormatRecord(line, lineType);
            }
        }
        if (!skipUnknownLines) {
            throw new FixedLengthException("Find unknown line:\n " + line);
        }
        return null;
    }

    private Object lineToObject(FixedFormatRecord record) {
      Class clazz = record.fixedFormatLine.clazz;
      String line = record.rawLine;
      Object lineAsObject;
      try {
        lineAsObject = clazz.getDeclaredConstructor().newInstance();
      } catch (NoSuchMethodException e) {
        throw new FixedLengthException("No empty constructor in class", e);
      } catch (IllegalAccessException
          | InstantiationException
          | InvocationTargetException e) {
        throw new FixedLengthException(
            "Unable to instanciate " + clazz.getName(), e
        );
      }


      for (FixedFormatField fixedFormatField : record.fixedFormatLine.fixedFormatFields) {
        FixedField fieldAnnotation = fixedFormatField.getFixedFieldAnnotation();
        Field field = fixedFormatField.getField();
        int startOfFieldIndex = fieldAnnotation.offset() - 1;
        int endOfFieldIndex = startOfFieldIndex + fieldAnnotation.length();
        if (endOfFieldIndex > line.length()) {
          continue;
        }
        String str = fieldAnnotation.align().remove(line.substring(
            startOfFieldIndex,
            endOfFieldIndex
        ), fieldAnnotation.padding());

        if (str != null && !str.trim().isEmpty()) {
          Formatter formatter = Formatter.instance(formatters, field.getType());
          try {
            field.set(lineAsObject, formatter.asObject(str, fieldAnnotation));
          } catch (IllegalAccessException e) {
            throw new FixedLengthException("Access to field failed", e);
          }
        }
      }
      return lineAsObject;
    }

    private List<Object> lineToObjects(FixedFormatRecord record) {
        Object lineAsObject = this.lineToObject(record);
        Method splitMethod = record.fixedFormatLine.splitAfterMethod;
        if(splitMethod == null) {
          return Collections.singletonList(lineAsObject);
        }
        int splitIndex;
        try {
          splitIndex = (Integer) splitMethod.invoke(lineAsObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new FixedLengthException("Access to method failed", e);
        }
        if(splitIndex >= record.rawLine.length()) {
          return Collections.singletonList(lineAsObject);
        }
        String subRawLine = record.rawLine.substring(splitIndex);
        FixedFormatRecord subRecord = this.fixedFormatLine(subRawLine);
        if (subRecord == null) {
          return Collections.singletonList(lineAsObject);
        }
        List<Object> lineAsObjects = new ArrayList<>();
        lineAsObjects.add(lineAsObject);
        lineAsObjects.addAll(lineToObjects(subRecord));
        return lineAsObjects;
    }

    public List<Object> parse(InputStream stream) throws FixedLengthException {
        return this.parseAsStream(stream).collect(Collectors.toList());
    }

    public Stream<Object> parseAsStream(InputStream inputStream)
            throws FixedLengthException {
        if (lineTypes.isEmpty()) {
            throw new FixedLengthException(
                  "Specify at least one line type"
            );
        }

        Scanner scanner = new Scanner(inputStream, this.charset.name())
                .useDelimiter(this.delimiter);
            return StreamSupport.stream(
                Spliterators.spliterator(
                        scanner,
                        Long.MAX_VALUE,
                        Spliterator.ORDERED | Spliterator.NONNULL
                ), false)
                    .map(this::fixedFormatLine)
                    .filter(Objects::nonNull)
                .flatMap(fixedFormatRecord -> lineToObjects(fixedFormatRecord).stream());
    }

    private final class FixedFormatRecord {
        private final String rawLine;
        private final FixedFormatLine fixedFormatLine;

        private FixedFormatRecord(
                final String rawLine,
                final FixedFormatLine fixedFormatLine) {
            this.rawLine = rawLine;
            this.fixedFormatLine = fixedFormatLine;
        }
    }

    private static class FixedFormatLine {
      private String startsWith = null;
      private Class clazz;
      private final List<FixedFormatField> fixedFormatFields = new ArrayList<>();
      private Method splitAfterMethod;

      public String getStartsWith() {
        return startsWith;
      }

      public void setStartsWith(String startsWith) {
        this.startsWith = startsWith;
      }

      public Class getClazz() {
        return clazz;
      }

      public void setClazz(Class clazz) {
        this.clazz = clazz;
      }
    }

    private static class FixedFormatField {
      private final Field field;
      private final FixedField fixedFieldAnnotation;

      private FixedFormatField(Field field, FixedField fixedField) {
        this.field = field;
        this.fixedFieldAnnotation = fixedField;
      }

      public Field getField() {
        return field;
      }

      public FixedField getFixedFieldAnnotation() {
        return fixedFieldAnnotation;
      }
    }

}
