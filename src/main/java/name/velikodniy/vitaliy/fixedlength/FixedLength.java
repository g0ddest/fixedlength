package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;
import name.velikodniy.vitaliy.fixedlength.annotation.SplitLineAfter;
import name.velikodniy.vitaliy.fixedlength.formatters.Formatter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * Class for fixed line processing and registering classes to process.
 * @param <T>
 */
public class FixedLength<T> {

    private static final Logger LOGGER = Logger.getLogger(FixedLength.class.getName());

    private static final Map<
            Class<? extends Serializable>,
            Class<? extends Formatter<? extends Serializable>>
            > FORMATTERS
            = Formatter.getDefaultFormatters();
    private final Map<Class<? extends Predicate<String>>, Predicate<String>> predicates = new HashMap<>();
    private final List<FixedFormatLine<? extends T>> lineTypes = new ArrayList<>();
    private boolean skipUnknownLines = true;
    private boolean skipErroneousFields = false;
    private boolean skipErroneousLines = false;
    private Charset charset = Charset.defaultCharset();
    private String delimiterString = "\n";
    private Pattern delimiter = Pattern.compile(delimiterString);

    private FixedFormatLine<T> classToLineDesc(final Class<? extends T> clazz) {
        FixedFormatLine<T> fixedFormatLine = new FixedFormatLine<>();
        fixedFormatLine.clazz = clazz;
        FixedLine annotation = clazz.getDeclaredAnnotation(FixedLine.class);
        if (annotation != null) {
            fixedFormatLine.setStartsWith(annotation.startsWith());
            fixedFormatLine.predicate = annotation.predicate();
        }
        for (Field field : getAllFields(clazz)) {
            FixedField fieldAnnotation = field.getDeclaredAnnotation(FixedField.class);
            if (fieldAnnotation == null) {
                continue;
            }
            fixedFormatLine.fixedFormatFields.add(new FixedFormatField(field, fieldAnnotation));
        }

        for (Method method : clazz.getMethods()) {
            SplitLineAfter splitLineAfter = method.getDeclaredAnnotation(SplitLineAfter.class);
            if (splitLineAfter == null) {
                continue;
            }
            fixedFormatLine.splitAfterMethod = method;
        }
        return fixedFormatLine;
    }

    List<Field> getAllFields(final Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toList());
        result.addAll(filteredFields);
        return result;
    }

    /**
     * Register here type that will be processed in serialization, or deserialization process.
     * Could be called more than once.
     * @param lineClass class for entity to be registered
     * @return instance of FixedLength
     */
    public FixedLength<T> registerLineType(final Class<? extends T> lineClass) {
        lineTypes.add(classToLineDesc(lineClass));
        return this;
    }

    /**
     * Add formatter to work with class types.
     *
     * @param typeClass      type that should be formatter
     * @param formatterClass formatter to pass through
     * @return instance of FixedLength
     */
    public FixedLength<T> registerFormatter(
            final Class<? extends Serializable> typeClass,
            final Class<? extends Formatter<? extends Serializable>> formatterClass) {
        FORMATTERS.put(typeClass, formatterClass);
        return this;
    }

    /**
     * In case of unknown line, the one will be skipped with no exception thrown
     * @return instance of FixedLength
     */
    public FixedLength<T> stopSkipUnknownLines() {
        skipUnknownLines = false;
        return this;
    }

    /**
     * In case of error field in parsing the line, the one will be skipped with no exception thrown
     * @return instance of FixedLength
     */
    public FixedLength<T> skipErroneousFields() {
        skipErroneousFields = true;
        return this;
    }

    /**
     * In case of error line while parsing, the one will be skipped with no exception thrown
     * @return instance of FixedLength
     */
    public FixedLength<T> skipErroneousLines() {
        skipErroneousLines = true;
        return this;
    }

    /**
     * In case you have a mixed fixed length file with different types in it,
     * you could register more than one line type in array instead of calling registerLineType multiple times.
     * @param lineClasses class for entity to be registered
     * @return instance of FixedLength
     */
    public FixedLength<T> registerLineTypes(final List<Class<T>> lineClasses) {
        lineTypes.addAll(
                lineClasses.stream()
                        .map(this::classToLineDesc)
                        .collect(Collectors.toList())
        );
        return this;
    }

    /**
     * In case you have a mixed fixed length file with different types in it,
     * you could register more than one line type in array instead of calling registerLineType multiple times.
     * @param lineClasses class for entity to be registered
     * @return instance of FixedLength
     */
    public FixedLength<T> registerLineTypes(final Class<T>[] lineClasses) {
        registerLineTypes(Arrays.asList(lineClasses));
        return this;
    }

    /**
     * Specifies charset of a file, in case of no provided Charset.defaultCharset() will be used
     * @param charset Charset of current file
     * @return instance of FixedLength
     */
    public FixedLength<T> usingCharset(Charset charset) {
        this.charset = requireNonNull(charset, "Charset can't be null");
        return this;
    }

    /**
     * Delimiter between fixed line entity records could be specified as a regexp pattern.
     * By default, it is linefeed LF \n
     * @param pattern regexp pattern how to break lines
     * @return instance of FixedLength
     */
    public FixedLength<T> usingLineDelimiter(Pattern pattern) {
        this.delimiter = requireNonNull(pattern, "Line delimiter pattern can't be null");
        return this;
    }

    /**
     * Delimiter between fixed line entity records. By default, it is linefeed LF \n (\u000a)
     * @param delimiterString string that points the end of a line
     * @return instance of FixedLength
     */
    public FixedLength<T> usingLineDelimiter(String delimiterString) {
        this.delimiterString = requireNonNull(
                delimiterString,
                "Delimiter can't be null");
        this.delimiter = Pattern.compile("delimiterString");
        return this;
    }

    private Predicate<String> getPredicate(Class<? extends Predicate<String>> clazz) {
        if (predicates.containsKey(clazz)) {
            return predicates.get(clazz);
        } else {
            Predicate<String> predicate;
            try {
                predicate = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                     | NoSuchMethodException e) {
                throw new FixedLengthException("Cannot init predicate, it should have empty constructor", e);
            }
            predicates.put(clazz, predicate);
            return predicate;
        }
    }

    private FixedFormatRecord fixedFormatLine(String line) {
        for (FixedFormatLine<? extends T> lineType : lineTypes) {
            if (
                    lineType.getStartsWith()
                            .map(line::startsWith)
                            .orElse(true)
                    &&
                    lineType.getPredicate()
                            .map(this::getPredicate)
                            .map(p -> p.test(line))
                            .orElse(true)
            ) {
                return new FixedFormatRecord(line, lineType);
            }
        }
        if (!skipUnknownLines) {
            throw new FixedLengthException("Find unknown line:\n " + line);
        }
        return null;
    }

    private T lineToObject(FixedFormatRecord fixedFormatRecord) {
        Class<? extends T> clazz = fixedFormatRecord.fixedFormatLine.clazz;
        String line = fixedFormatRecord.rawLine;
        T lineAsObject = null;
        boolean useEmptyConstructor = true;
        try {
            lineAsObject = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            LOGGER.fine("No empty constructor in class");
            useEmptyConstructor = false;
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException e) {
            throw new FixedLengthException(
                    "Unable to instantiate " + clazz.getName(), e
            );
        }

        Object[] args = new Object[fixedFormatRecord.fixedFormatLine.fixedFormatFields.size()];
        int index = 0;
        for (FixedFormatField fixedFormatField : fixedFormatRecord.fixedFormatLine.fixedFormatFields) {
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
            if (acceptFieldContent(str, fieldAnnotation)) {
                if (useEmptyConstructor) {
                    fillField(field, lineAsObject, str, fieldAnnotation);
                } else {
                    args[index++] = Formatter.instance(FORMATTERS, field.getType()).asObject(str, fieldAnnotation);
                }
            }
        }
        if (!useEmptyConstructor) {
            try {
                if (clazz.getDeclaredConstructors().length != 1) {
                    throw new FixedLengthException("There should be only one matching constructor");
                }
                lineAsObject = (T) clazz.getDeclaredConstructors()[0].newInstance(args);
            } catch (IllegalAccessException
                     | InstantiationException
                     | InvocationTargetException e) {
                throw new FixedLengthException("Unable to instantiate " + clazz.getName(), e);
            }
        }
        return lineAsObject;
    }

    private void fillField(Field field, T lineAsObject, String str, FixedField fieldAnnotation) {
        field.setAccessible(true);

        try {
            field.set(
                    lineAsObject,
                    Formatter.instance(FORMATTERS, field.getType()).asObject(str, fieldAnnotation)
            );
        } catch (IllegalAccessException e) {
            throw new FixedLengthException("Access to field failed", e);
        } catch (Exception e) {
            if (e instanceof FixedLengthException) {
                throw e;
            }
            if (!skipErroneousFields) {
                throw e;
            }
            LOGGER.warning(String.format(
                    "Skipping field of type %s with error in value %s",
                    field.getType(),
                    str
            ));
        }
    }

    private boolean acceptFieldContent(String content, FixedField fieldAnnotation) {
        if (content == null) {
            return false;
        }
        if (content.trim().isEmpty() && !fieldAnnotation.allowEmptyStrings()) {
            return false;
        }
        if (fieldAnnotation.ignore().isEmpty()) {
            // No ignore content defined, accepting
            return true;
        }
        // Ignore content defined: accepting if not matching ignore regular expression
        Pattern pattern = Pattern.compile(fieldAnnotation.ignore());
        return !pattern.matcher(content).matches();
    }

    private List<T> lineToObjects(FixedFormatRecord fixedFormatRecord) {
        try {
            T lineAsObject = this.lineToObject(fixedFormatRecord);
            Method splitMethod = fixedFormatRecord.fixedFormatLine.splitAfterMethod;
            if (splitMethod == null) {
                return Collections.singletonList(lineAsObject);
            }
            int splitIndex;
            try {
                splitIndex = (Integer) splitMethod.invoke(lineAsObject);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new FixedLengthException("Access to method failed", e);
            }
            if (splitIndex <= 0 || splitIndex >= fixedFormatRecord.rawLine.length()) {
                return Collections.singletonList(lineAsObject);
            }
            String subRawLine = fixedFormatRecord.rawLine.substring(splitIndex);
            FixedFormatRecord subRecord = this.fixedFormatLine(subRawLine);
            if (subRecord == null) {
                return Collections.singletonList(lineAsObject);
            }
            List<T> lineAsObjects = new ArrayList<>();
            lineAsObjects.add(lineAsObject);
            lineAsObjects.addAll(lineToObjects(subRecord));
            return lineAsObjects;
        } catch (Exception e) {
            if (e instanceof FixedLengthException) {
                throw e;
            }
            if (!skipErroneousLines) {
                throw e;
            }
            LOGGER.warning("Skipping line with error");
            return Collections.emptyList();
        }
    }

    /**
     * Parses a fixed length file into a List
     * @param stream InputStream of a fixed length file
     * @return List of parsed objects
     * @throws FixedLengthException in case of parsing errors
     */
    public List<T> parse(InputStream stream) throws FixedLengthException {
        return this.parseAsStream(stream).collect(Collectors.toList());
    }

    /**
     * Parses a fixed length file into a List
     * @param reader Reader of a fixed length file
     * @return List of parsed objects
     * @throws FixedLengthException in case of parsing errors
     */
    public List<T> parse(Reader reader) throws FixedLengthException {
        return parseAsStream(reader).collect(Collectors.toList());
    }

    /**
     * Parses a fixed length file into a stream
     * @param inputStream InputStream of a fixed length file
     * @return Stream of parsed objects
     * @throws FixedLengthException in case of parsing errors
     */
    public Stream<T> parseAsStream(InputStream inputStream)
            throws FixedLengthException {
        Stream<String> lines = StreamSupport.stream(
                        Spliterators.spliterator(
                                new Scanner(inputStream, this.charset.name()).useDelimiter(this.delimiter),
                                Long.MAX_VALUE,
                                Spliterator.ORDERED | Spliterator.NONNULL
                        ), false);

        return parseAsStream(lines);
    }

    /**
     * Parses a fixed length file into a stream
     * @param reader Reader of a fixed length file
     * @return Stream of parsed objects
     * @throws FixedLengthException in case of parsing errors
     */
    public Stream<T> parseAsStream(Reader reader) throws FixedLengthException {
        return parseAsStream(new BufferedReader(reader).lines());
    }

    private Stream<T> parseAsStream(Stream<String> lines) throws FixedLengthException {
        if (lineTypes.isEmpty()) {
            throw new FixedLengthException(
                    "Specify at least one line type"
            );
        }

        return lines.map(this::fixedFormatLine)
                .filter(Objects::nonNull)
                .flatMap(fixedFormatRecord -> lineToObjects(fixedFormatRecord).stream());
    }

    /**
     * Builds a fixed length String
     * @param lines lines to be serialized
     * @return String of a fixed length format
     */
    public String format(List<T> lines) {

        final StringBuilder builder = new StringBuilder();

        long currentLine = 1;

        for (T line : lines) {

            getAllFields(line.getClass())
                    .stream()
                    .filter(
                            f ->
                                    f.getAnnotation(FixedField.class) != null
                    )
                    .sorted(Comparator.comparingInt(f -> f.getAnnotation(FixedField.class).offset()))
                    .forEach(f -> {
                        FixedField fixedFieldAnnotation = f.getAnnotation(FixedField.class);

                        Formatter<T> formatter = (Formatter<T>) Formatter.instance(FORMATTERS, f.getType());

                        f.setAccessible(true);

                        T value;
                        try {
                            value = (T) f.get(line);
                        } catch (IllegalAccessException e) {
                            throw new FixedLengthException(e.getMessage(), e);
                        }

                        if (value != null) {
                            builder.append(
                                    fixedFieldAnnotation.align().make(
                                            formatter.asString(value, fixedFieldAnnotation),
                                            fixedFieldAnnotation.length(),
                                            fixedFieldAnnotation.padding())
                            );
                        } else if (!fixedFieldAnnotation.fallbackStringForNullValue().isEmpty()) {
                            if (fixedFieldAnnotation.fallbackStringForNullValue().length()
                                    > fixedFieldAnnotation.length()) {
                                throw new FixedLengthException(String.format(
                                        "Fallback string for null value is too long for field %s in class %s. "
                                                + "Please check the annotation parameters.",
                                        f.getName(), line.getClass().getName()
                                ));
                            }
                            String paddedFallbackString = fixedFieldAnnotation.align().make(
                                    fixedFieldAnnotation.fallbackStringForNullValue(),
                                    fixedFieldAnnotation.length(),
                                    fixedFieldAnnotation.padding()
                            );
                            builder.append(paddedFallbackString);
                        }
                    });

            if (lines.size() != currentLine++) {
                builder.append(this.delimiterString);
            }

        }

        return builder.toString();
    }

    private final class FixedFormatRecord {
        private final String rawLine;
        private final FixedFormatLine<? extends T> fixedFormatLine;

        private FixedFormatRecord(
                final String rawLine,
                final FixedFormatLine<? extends T> fixedFormatLine) {
            this.rawLine = rawLine;
            this.fixedFormatLine = fixedFormatLine;
        }
    }

    private static class FixedFormatLine<T> {
        private String startsWith = null;
        private Class<? extends Predicate<String>> predicate;
        private Class<? extends T> clazz;
        private final List<FixedFormatField> fixedFormatFields = new ArrayList<>();
        private Method splitAfterMethod;

        public Optional<String> getStartsWith() {
            return Optional.ofNullable(startsWith).flatMap(s -> s.isEmpty() ? Optional.empty() : Optional.of(s));
        }

        public Optional<Class<? extends Predicate<String>>> getPredicate() {
            return Optional.ofNullable(predicate);
        }

        public void setStartsWith(String startsWith) {
            this.startsWith = startsWith;
        }

        public void setPredicate(Class<? extends Predicate<String>> predicate) {
            this.predicate = predicate;
        }

        public Class<? extends T> getClazz() {
            return clazz;
        }

        public void setClazz(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

    private static final class FixedFormatField {
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
