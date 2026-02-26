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
 * Fluent builder and processor for fixed-length (positional)
 * flat files.
 *
 * <p>Typical usage:
 * <pre>{@code
 * List<MyRecord> records = new FixedLength<MyRecord>()
 *         .registerLineType(MyRecord.class)
 *         .parse(inputStream);
 * }</pre>
 *
 * <p>Supports both parsing (text to objects) and formatting
 * (objects to text). Multiple line types can be registered for
 * mixed-format files. Custom formatters can be registered for
 * user-defined types.
 *
 * <p><strong>Thread safety:</strong> instances of this class are
 * <em>not</em> thread-safe. Create a separate instance for each
 * thread, or synchronize access externally.
 *
 * @param <T> the common supertype of all registered line types
 */
public class FixedLength<T> {

    private static final Logger LOGGER =
            Logger.getLogger(FixedLength.class.getName());

    private final Map<
            Class<? extends Serializable>,
            Class<? extends Formatter<? extends Serializable>>
            > formatters = Formatter.getDefaultFormatters();
    private final Map<
            Class<? extends Predicate<String>>,
            Predicate<String>> predicates = new HashMap<>();
    private final List<FixedFormatLine<? extends T>> lineTypes =
            new ArrayList<>();
    private boolean skipUnknownLines = true;
    private boolean skipErroneousFields = false;
    private boolean skipErroneousLines = false;
    private Charset charset = Charset.defaultCharset();
    private String delimiterString = "\n";
    private Pattern delimiter = Pattern.compile(delimiterString);

    private FixedFormatLine<T> classToLineDesc(
            final Class<? extends T> clazz) {
        FixedFormatLine<T> fixedFormatLine = new FixedFormatLine<>();
        fixedFormatLine.setClazz(clazz);
        FixedLine annotation =
                clazz.getDeclaredAnnotation(FixedLine.class);
        if (annotation != null) {
            fixedFormatLine.setStartsWith(annotation.startsWith());
            fixedFormatLine.setPredicate(annotation.predicate());
        }
        for (Field field : getAllFields(clazz)) {
            FixedField fieldAnnotation =
                    field.getDeclaredAnnotation(FixedField.class);
            if (fieldAnnotation == null) {
                continue;
            }
            fixedFormatLine.getFixedFormatFields()
                    .add(new FixedFormatField(field, fieldAnnotation));
        }

        validateFields(fixedFormatLine, clazz);

        for (Method method : clazz.getMethods()) {
            SplitLineAfter splitLineAfter =
                    method.getDeclaredAnnotation(SplitLineAfter.class);
            if (splitLineAfter == null) {
                continue;
            }
            fixedFormatLine.setSplitAfterMethod(method);
        }
        return fixedFormatLine;
    }

    private void validateFields(
            FixedFormatLine<T> line, Class<?> clazz) {
        for (FixedFormatField fff : line.getFixedFormatFields()) {
            FixedField fa = fff.getFixedFieldAnnotation();
            if (fa.offset() < 1) {
                throw new FixedLengthException(String.format(
                        "Field '%s' in %s has invalid offset "
                                + "%d (must be >= 1)",
                        fff.getField().getName(),
                        clazz.getName(),
                        fa.offset()));
            }
            if (fa.length() <= 0) {
                throw new FixedLengthException(String.format(
                        "Field '%s' in %s has invalid length "
                                + "%d (must be > 0)",
                        fff.getField().getName(),
                        clazz.getName(),
                        fa.length()));
            }
        }
    }

    List<Field> getAllFields(final Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        List<Field> result =
                new ArrayList<>(getAllFields(clazz.getSuperclass()));
        List<Field> filteredFields =
                Arrays.stream(clazz.getDeclaredFields())
                        .collect(Collectors.toList());
        result.addAll(filteredFields);
        return result;
    }

    /**
     * Registers a line type for parsing and formatting.
     *
     * <p>The class must have fields annotated with
     * {@link FixedField}. Optionally, the class itself can be
     * annotated with {@link FixedLine} to specify line-matching
     * criteria for mixed-format files.
     *
     * <p>Can be called multiple times to register different line
     * types.
     *
     * @param lineClass the annotated entity class to register
     * @return this instance for method chaining
     * @throws FixedLengthException if any {@link FixedField}
     *         annotation has invalid offset or length values
     */
    public FixedLength<T> registerLineType(
            final Class<? extends T> lineClass) {
        lineTypes.add(classToLineDesc(lineClass));
        return this;
    }

    /**
     * Registers a custom formatter for the given type.
     *
     * <p>Custom formatters override built-in formatters for the
     * same type. The registration applies only to this
     * {@code FixedLength} instance.
     *
     * @param typeClass      the type to be formatted
     * @param formatterClass the formatter class to use
     * @return this instance for method chaining
     */
    public FixedLength<T> registerFormatter(
            final Class<? extends Serializable> typeClass,
            final Class<? extends Formatter<? extends Serializable>>
                    formatterClass) {
        formatters.put(typeClass, formatterClass);
        return this;
    }

    /**
     * Configures this parser to throw a
     * {@link FixedLengthException} when a line does not match
     * any registered line type.
     *
     * <p>By default, unknown lines are silently skipped.
     *
     * @return this instance for method chaining
     */
    public FixedLength<T> failOnUnknownLines() {
        skipUnknownLines = false;
        return this;
    }

    /**
     * Configures this parser to throw on unknown lines.
     *
     * @return this instance for method chaining
     * @deprecated Use {@link #failOnUnknownLines()} instead
     *             for a clearer method name.
     */
    @Deprecated
    public FixedLength<T> stopSkipUnknownLines() {
        return failOnUnknownLines();
    }

    /**
     * Configures this parser to set fields to {@code null} when
     * a parsing error occurs on an individual field, instead of
     * throwing an exception.
     *
     * @return this instance for method chaining
     */
    public FixedLength<T> skipErroneousFields() {
        skipErroneousFields = true;
        return this;
    }

    /**
     * Configures this parser to skip entire lines that cause
     * parsing errors, instead of throwing an exception.
     *
     * @return this instance for method chaining
     */
    public FixedLength<T> skipErroneousLines() {
        skipErroneousLines = true;
        return this;
    }

    /**
     * Registers multiple line types at once from a list.
     *
     * <p>Equivalent to calling {@link #registerLineType} for
     * each class in the list.
     *
     * @param lineClasses the entity classes to register
     * @return this instance for method chaining
     */
    public FixedLength<T> registerLineTypes(
            final List<Class<T>> lineClasses) {
        lineTypes.addAll(
                lineClasses.stream()
                        .map(this::classToLineDesc)
                        .collect(Collectors.toList())
        );
        return this;
    }

    /**
     * Registers multiple line types at once from an array.
     *
     * <p>Equivalent to calling {@link #registerLineType} for
     * each class in the array.
     *
     * @param lineClasses the entity classes to register
     * @return this instance for method chaining
     */
    public FixedLength<T> registerLineTypes(
            final Class<T>[] lineClasses) {
        registerLineTypes(Arrays.asList(lineClasses));
        return this;
    }

    /**
     * Sets the character encoding used when reading from an
     * {@link InputStream}.
     *
     * <p>If not specified, {@link Charset#defaultCharset()} is
     * used.
     *
     * @param charset the charset to use (must not be {@code null})
     * @return this instance for method chaining
     * @throws NullPointerException if {@code charset} is
     *         {@code null}
     */
    public FixedLength<T> usingCharset(Charset charset) {
        this.charset = requireNonNull(
                charset, "Charset can't be null");
        return this;
    }

    /**
     * Sets the line delimiter as a regular expression pattern.
     *
     * <p>Defaults to {@code \n} (line feed).
     *
     * @param pattern the compiled regex pattern for splitting
     *                lines (must not be {@code null})
     * @return this instance for method chaining
     * @throws NullPointerException if {@code pattern} is
     *         {@code null}
     */
    public FixedLength<T> usingLineDelimiter(Pattern pattern) {
        this.delimiter = requireNonNull(
                pattern, "Line delimiter pattern can't be null");
        return this;
    }

    /**
     * Sets the line delimiter as a literal string.
     *
     * <p>The string is treated as a literal (not a regex).
     * Defaults to {@code "\n"} (line feed).
     *
     * @param delimiterString the literal delimiter string
     *                        (must not be {@code null})
     * @return this instance for method chaining
     * @throws NullPointerException if {@code delimiterString}
     *         is {@code null}
     */
    public FixedLength<T> usingLineDelimiter(
            String delimiterString) {
        this.delimiterString = requireNonNull(
                delimiterString,
                "Delimiter can't be null");
        this.delimiter = Pattern.compile(
                Pattern.quote(delimiterString));
        return this;
    }

    private Predicate<String> getPredicate(
            Class<? extends Predicate<String>> clazz) {
        if (predicates.containsKey(clazz)) {
            return predicates.get(clazz);
        } else {
            Predicate<String> predicate;
            try {
                predicate =
                        clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException
                     | IllegalAccessException
                     | InvocationTargetException
                     | NoSuchMethodException e) {
                throw new FixedLengthException(
                        "Cannot instantiate predicate; "
                                + "it must have a public "
                                + "no-argument constructor", e);
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
            throw new FixedLengthException(
                    "Unknown line found:\n " + line);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private T lineToObject(FixedFormatRecord fixedFormatRecord) {
        Class<? extends T> clazz =
                fixedFormatRecord.getFixedFormatLine().getClazz();
        String line = fixedFormatRecord.getRawLine();
        T lineAsObject = null;
        boolean useEmptyConstructor = true;
        try {
            lineAsObject =
                    clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            LOGGER.fine("No empty constructor in class");
            useEmptyConstructor = false;
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException e) {
            throw new FixedLengthException(
                    "Unable to instantiate "
                            + clazz.getName(), e);
        }

        List<FixedFormatField> fields =
                fixedFormatRecord.getFixedFormatLine()
                        .getFixedFormatFields();
        Object[] args = new Object[fields.size()];
        parseFields(
                fields, line, useEmptyConstructor,
                lineAsObject, args);
        if (!useEmptyConstructor) {
            lineAsObject = newInstanceViaConstructor(
                    clazz, args);
        }
        return lineAsObject;
    }

    private void parseFields(
            List<FixedFormatField> fields, String line,
            boolean useEmptyConstructor, T lineAsObject,
            Object[] args) {
        int argIndex = 0;
        for (FixedFormatField fixedFormatField : fields) {
            FixedField fieldAnnotation =
                    fixedFormatField.getFixedFieldAnnotation();
            Field field = fixedFormatField.getField();
            int startOfFieldIndex =
                    fieldAnnotation.offset() - 1;
            int endOfFieldIndex =
                    startOfFieldIndex + fieldAnnotation.length();
            if (endOfFieldIndex > line.length()) {
                continue;
            }
            String str = fieldAnnotation.align().remove(
                    line.substring(
                            startOfFieldIndex,
                            endOfFieldIndex),
                    fieldAnnotation.padding());
            if (acceptFieldContent(str, fixedFormatField)) {
                if (useEmptyConstructor) {
                    fillField(field, lineAsObject, str,
                            fieldAnnotation);
                } else {
                    args[argIndex++] = Formatter
                            .instance(formatters,
                                    field.getType())
                            .asObject(str, fieldAnnotation);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private T newInstanceViaConstructor(
            Class<? extends T> clazz, Object[] args) {
        try {
            if (clazz.getDeclaredConstructors().length != 1) {
                throw new FixedLengthException(
                        "There should be only one "
                                + "matching constructor");
            }
            // Constructor args are populated in field
            // declaration order, which must match the
            // constructor parameter order for record-style
            // classes.
            return (T) clazz.getDeclaredConstructors()[0]
                    .newInstance(args);
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException e) {
            throw new FixedLengthException(
                    "Unable to instantiate "
                            + clazz.getName(), e);
        }
    }

    private void fillField(
            Field field, T lineAsObject,
            String str, FixedField fieldAnnotation) {
        field.setAccessible(true);

        try {
            field.set(
                    lineAsObject,
                    Formatter
                            .instance(formatters, field.getType())
                            .asObject(str, fieldAnnotation)
            );
        } catch (IllegalAccessException e) {
            throw new FixedLengthException(
                    "Access to field failed", e);
        } catch (Exception e) {
            if (e instanceof FixedLengthException) {
                throw e;
            }
            if (!skipErroneousFields) {
                throw e;
            }
            LOGGER.warning(String.format(
                    "Skipping field of type %s with error "
                            + "in value %s",
                    field.getType(),
                    str
            ));
        }
    }

    private boolean acceptFieldContent(
            String content,
            FixedFormatField fixedFormatField) {
        FixedField fieldAnnotation =
                fixedFormatField.getFixedFieldAnnotation();
        if (content == null) {
            return false;
        }
        if (content.trim().isEmpty()
                && !fieldAnnotation.allowEmptyStrings()) {
            return false;
        }
        if (fieldAnnotation.ignore().isEmpty()) {
            return true;
        }
        Pattern pattern = fixedFormatField.getIgnorePattern();
        return !pattern.matcher(content).matches();
    }

    private List<T> lineToObjects(
            FixedFormatRecord fixedFormatRecord) {
        try {
            T lineAsObject =
                    this.lineToObject(fixedFormatRecord);
            Method splitMethod = fixedFormatRecord
                    .getFixedFormatLine().getSplitAfterMethod();
            if (splitMethod == null) {
                return Collections.singletonList(lineAsObject);
            }
            int splitIndex;
            try {
                splitIndex =
                        (Integer) splitMethod.invoke(lineAsObject);
            } catch (IllegalAccessException
                     | InvocationTargetException e) {
                throw new FixedLengthException(
                        "Access to method failed", e);
            }
            if (splitIndex <= 0
                    || splitIndex
                    >= fixedFormatRecord.getRawLine().length()) {
                return Collections.singletonList(lineAsObject);
            }
            String subRawLine = fixedFormatRecord
                    .getRawLine().substring(splitIndex);
            FixedFormatRecord subRecord =
                    this.fixedFormatLine(subRawLine);
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
     * Parses a fixed-length file into a {@link List}.
     *
     * @param stream an {@link InputStream} of the fixed-length
     *               file
     * @return a list of parsed objects
     * @throws FixedLengthException if no line types are
     *         registered, or if a parsing error occurs
     */
    public List<T> parse(InputStream stream)
            throws FixedLengthException {
        try (Stream<T> s = this.parseAsStream(stream)) {
            return s.collect(Collectors.toList());
        }
    }

    /**
     * Parses a fixed-length file into a {@link List}.
     *
     * @param reader a {@link Reader} of the fixed-length file
     * @return a list of parsed objects
     * @throws FixedLengthException if no line types are
     *         registered, or if a parsing error occurs
     */
    public List<T> parse(Reader reader)
            throws FixedLengthException {
        try (Stream<T> s = parseAsStream(reader)) {
            return s.collect(Collectors.toList());
        }
    }

    /**
     * Parses a fixed-length file into a {@link Stream}.
     *
     * <p>The returned stream wraps a {@link Scanner}. Callers
     * should close the stream (e.g. via try-with-resources) to
     * release the underlying resources.
     *
     * @param inputStream an {@link InputStream} of the
     *                    fixed-length file
     * @return a stream of parsed objects
     * @throws FixedLengthException if no line types are
     *         registered
     */
    public Stream<T> parseAsStream(InputStream inputStream)
            throws FixedLengthException {
        Scanner scanner = new Scanner(
                inputStream, this.charset.name())
                .useDelimiter(this.delimiter);
        Stream<String> lines = StreamSupport.stream(
                Spliterators.spliterator(
                        scanner,
                        Long.MAX_VALUE,
                        Spliterator.ORDERED
                                | Spliterator.NONNULL
                ), false);

        return parseAsStream(lines).onClose(scanner::close);
    }

    /**
     * Parses a fixed-length file into a {@link Stream}.
     *
     * @param reader a {@link Reader} of the fixed-length file
     * @return a stream of parsed objects
     * @throws FixedLengthException if no line types are
     *         registered
     */
    public Stream<T> parseAsStream(Reader reader)
            throws FixedLengthException {
        BufferedReader buffered = new BufferedReader(reader);
        return parseAsStream(buffered.lines())
                .onClose(() -> {
                    try {
                        buffered.close();
                    } catch (java.io.IOException ignored) {
                        // best-effort close
                    }
                });
    }

    private Stream<T> parseAsStream(Stream<String> lines)
            throws FixedLengthException {
        if (lineTypes.isEmpty()) {
            throw new FixedLengthException(
                    "Specify at least one line type"
            );
        }

        return lines.map(this::fixedFormatLine)
                .filter(Objects::nonNull)
                .flatMap(fixedFormatRecord ->
                        lineToObjects(fixedFormatRecord)
                                .stream());
    }

    /**
     * Serializes a list of objects into a fixed-length format
     * string.
     *
     * <p>Fields with {@code null} values are filled with the
     * padding character (preserving field positions), unless a
     * {@link FixedField#fallbackStringForNullValue()} is
     * specified, in which case that value is used instead.
     *
     * @param lines the objects to serialize
     * @return the formatted fixed-length string
     * @throws FixedLengthException if field formatting fails
     */
    public String format(List<T> lines) {

        final StringBuilder builder = new StringBuilder();

        long currentLine = 1;

        for (T line : lines) {

            getAllFields(line.getClass())
                    .stream()
                    .filter(f ->
                            f.getAnnotation(FixedField.class)
                                    != null)
                    .sorted(Comparator.comparingInt(f ->
                            f.getAnnotation(FixedField.class)
                                    .offset()))
                    .forEach(f -> appendFormattedField(
                            builder, f, line));

            if (lines.size() != currentLine++) {
                builder.append(this.delimiterString);
            }

        }

        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendFormattedField(
            StringBuilder builder, Field f, T line) {
        FixedField ann =
                f.getAnnotation(FixedField.class);
        Formatter<T> formatter = (Formatter<T>)
                Formatter.instance(formatters, f.getType());

        T value = getFieldValue(f, line);

        if (value != null) {
            builder.append(ann.align().make(
                    formatter.asString(value, ann),
                    ann.length(),
                    ann.padding()));
        } else if (!ann.fallbackStringForNullValue()
                .isEmpty()) {
            appendFallbackValue(builder, f, line, ann);
        } else {
            builder.append(Align.repeat(
                    ann.padding(), ann.length()));
        }
    }

    @SuppressWarnings("unchecked")
    private T getFieldValue(Field f, T line) {
        try {
            f.setAccessible(true);
            return (T) f.get(line);
        } catch (IllegalAccessException | SecurityException e) {
            // Field not accessible directly (e.g. Java records
            // or module restrictions); try public accessor method
            try {
                Method accessor =
                        line.getClass().getMethod(f.getName());
                return (T) accessor.invoke(line);
            } catch (NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException ex) {
                throw new FixedLengthException(
                        "Cannot access field "
                                + f.getName(), ex);
            }
        }
    }

    private void appendFallbackValue(
            StringBuilder builder, Field f,
            T line, FixedField ann) {
        if (ann.fallbackStringForNullValue().length()
                > ann.length()) {
            throw new FixedLengthException(String.format(
                    "Fallback string for null value is "
                            + "too long for field %s in "
                            + "class %s. Please check the "
                            + "annotation parameters.",
                    f.getName(),
                    line.getClass().getName()));
        }
        String paddedFallbackString = ann.align().make(
                ann.fallbackStringForNullValue(),
                ann.length(),
                ann.padding());
        builder.append(paddedFallbackString);
    }

    /**
     * Holds a raw line together with its matched format
     * descriptor.
     */
    private final class FixedFormatRecord {
        private final String rawLine;
        private final FixedFormatLine<? extends T> fixedFormatLine;

        private FixedFormatRecord(
                final String rawLine,
                final FixedFormatLine<? extends T>
                        fixedFormatLine) {
            this.rawLine = rawLine;
            this.fixedFormatLine = fixedFormatLine;
        }

        String getRawLine() {
            return rawLine;
        }

        @SuppressWarnings("java:S1452")
        FixedFormatLine<? extends T> getFixedFormatLine() {
            return fixedFormatLine;
        }
    }

    /**
     * Describes the format of a single line type, including
     * the target class, matching criteria, fields, and optional
     * split method.
     *
     * @param <T> the line entity type
     */
    private static class FixedFormatLine<T> {
        private String startsWith;
        private Class<? extends Predicate<String>> predicate;
        private Class<? extends T> clazz;
        private final List<FixedFormatField> fixedFormatFields =
                new ArrayList<>();
        private Method splitAfterMethod;

        /**
         * Returns the {@code startsWith} prefix if explicitly
         * set to a non-empty value.
         *
         * @return optional prefix string
         */
        Optional<String> getStartsWith() {
            return Optional.ofNullable(startsWith)
                    .flatMap(s -> s.isEmpty()
                            ? Optional.empty()
                            : Optional.of(s));
        }

        /**
         * Returns the predicate class if a custom one was
         * specified. Returns {@link Optional#empty()} when the
         * default (always-true) predicate is in effect.
         *
         * @return optional predicate class
         */
        Optional<Class<? extends Predicate<String>>>
                getPredicate() {
            if (predicate == null
                    || FixedLine.DefaultPredicate.class
                    .equals(predicate)) {
                return Optional.empty();
            }
            return Optional.of(predicate);
        }

        void setStartsWith(String startsWith) {
            this.startsWith = startsWith;
        }

        void setPredicate(
                Class<? extends Predicate<String>> predicate) {
            this.predicate = predicate;
        }

        Class<? extends T> getClazz() {
            return clazz;
        }

        void setClazz(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        List<FixedFormatField> getFixedFormatFields() {
            return fixedFormatFields;
        }

        Method getSplitAfterMethod() {
            return splitAfterMethod;
        }

        void setSplitAfterMethod(Method method) {
            this.splitAfterMethod = method;
        }
    }

    /**
     * Wraps a {@link Field} together with its
     * {@link FixedField} annotation and provides a cached
     * compiled {@link Pattern} for the {@code ignore} attribute.
     */
    private static final class FixedFormatField {
        private final Field field;
        private final FixedField fixedFieldAnnotation;
        private Pattern ignorePattern;

        private FixedFormatField(
                Field field, FixedField fixedField) {
            this.field = field;
            this.fixedFieldAnnotation = fixedField;
        }

        Field getField() {
            return field;
        }

        FixedField getFixedFieldAnnotation() {
            return fixedFieldAnnotation;
        }

        /**
         * Returns a cached compiled {@link Pattern} for the
         * {@link FixedField#ignore()} regex, or {@code null}
         * if no ignore regex is defined.
         *
         * @return the compiled pattern, or {@code null}
         */
        Pattern getIgnorePattern() {
            if (ignorePattern == null
                    && !fixedFieldAnnotation.ignore()
                    .isEmpty()) {
                ignorePattern = Pattern.compile(
                        fixedFieldAnnotation.ignore());
            }
            return ignorePattern;
        }
    }

}
