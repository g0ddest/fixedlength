package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;
import name.velikodniy.vitaliy.fixedlength.formatters.Formatter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public class FixedLength {

    private static Map<
                    Class<? extends Serializable>,
                    Class<? extends Formatter>
                    > formatters
            = Formatter.getDefaultFormatters();
    private List<FixedFormatLine> lineTypes = new ArrayList<>();
    private boolean skipUnknownLines = true;

    private static FixedFormatLine classToLineDesc(final Class clazz) {
        FixedFormatLine fixedFormatLine = new FixedFormatLine();
        fixedFormatLine.clazz = clazz;
        FixedLine annotation =
                (FixedLine) clazz.getDeclaredAnnotation(FixedLine.class);
        if (annotation != null) {
            fixedFormatLine.startsWith = annotation.startsWith();
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


        for (Field field : clazz.getFields()) {
            FixedField fieldAnnotation = field.getDeclaredAnnotation(FixedField.class);
            if (fieldAnnotation == null) {
                continue;
            }
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

    public List<Object> parse(InputStream stream) throws FixedLengthException {
        return this.parseAsStream(
                  new BufferedReader(new InputStreamReader(stream))
                ).collect(Collectors.toList());
    }

    public Stream<Object> parseAsStream(BufferedReader reader)
            throws FixedLengthException {
        if (lineTypes.isEmpty()) {
            throw new FixedLengthException(
                  "Specify at least one line type"
            );
        }
        return reader.lines()
                .map(this::fixedFormatLine)
                .filter(Objects::nonNull)
                .map(this::lineToObject);
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

}
