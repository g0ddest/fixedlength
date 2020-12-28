package name.velikodniy.vitaliy.fixedlength;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedLine;
import name.velikodniy.vitaliy.fixedlength.formatters.Formatter;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class FixedLength {

    private static Map<Class<? extends Serializable>, Class<? extends name.velikodniy.vitaliy.fixedlength.formatters.Formatter>> formatters = name.velikodniy.vitaliy.fixedlength.formatters.Formatter.defaultFormatters;
    private String delimiter = "\\n";
    private List<FixedFormatLine> lineTypes = new ArrayList<>();
    private boolean skipUnknownLines = true;

    private static FixedFormatLine classToLineDesc(Class clazz) {
        FixedFormatLine fixedFormatLine = new FixedFormatLine();
        fixedFormatLine.clazz = clazz;
        FixedLine annotation = (FixedLine) clazz.getDeclaredAnnotation(FixedLine.class);
        if (annotation != null) {
            fixedFormatLine.startsWith = annotation.startsWith();
        }
        return fixedFormatLine;
    }

    public FixedLength registerLineType(Class lineClass) {
        lineTypes.add(classToLineDesc(lineClass));
        return this;
    }

    public FixedLength registerFormatter(Class<? extends Serializable> typeClass, Class<? extends name.velikodniy.vitaliy.fixedlength.formatters.Formatter> formatterClass) {
        formatters.put(typeClass, formatterClass);
        return this;
    }

    public FixedLength stopSkipUnknownLines() {
        skipUnknownLines = false;
        return this;
    }

    public FixedLength registerLineTypes(List<Class> lineClasses) {
        lineTypes.addAll(
                lineClasses.stream().map(c -> classToLineDesc(c)).collect(Collectors.toList())
        );
        return this;
    }

    public FixedLength registerLineTypes(Class[] lineClasses) {
        registerLineTypes(Arrays.asList(lineClasses));
        return this;
    }

    public FixedFormatLine fixedFormatLine(String line) {
        if (lineTypes.size() == 1) {
            if (lineTypes.get(0).startsWith == null)
                return lineTypes.get(0);
            else if (line.startsWith(lineTypes.get(0).startsWith))
                return lineTypes.get(0);
            else return null;
        }
        for (FixedFormatLine lineType : lineTypes) {
            if (lineType.startsWith != null && line.startsWith(lineType.startsWith)) {
                return lineType;
            }
        }
        return null;
    }

    private Object lineToObject(Class clazz, String line) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, FixedLengthException {

        Object o = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getFields()) {
            FixedField fieldAnnotation = field.getDeclaredAnnotation(FixedField.class);
            if (fieldAnnotation != null) {
                if (fieldAnnotation.offset() - 1 + fieldAnnotation.length() <= line.length()) {
                    String str = fieldAnnotation.align().remove(line.substring(
                            fieldAnnotation.offset() - 1,
                            fieldAnnotation.offset() - 1 + fieldAnnotation.length()
                    ), fieldAnnotation.padding());

                    if (str != null && !str.trim().isEmpty()) {
                        name.velikodniy.vitaliy.fixedlength.formatters.Formatter formatter = Formatter.instance(formatters, field.getType());
                        field.set(o, formatter.asObject(str, fieldAnnotation));
                    }
                }
            }
        }

        return o;
    }

    public List<Object> parse(InputStream stream) throws FixedLengthException {

        if (lineTypes.isEmpty()) throw new FixedLengthException("Specify at least one line type");
        List<Object> result = new ArrayList<>();

        Scanner scan = new Scanner(stream);

        while (scan.hasNext()) {
            String line = scan.nextLine();
            FixedFormatLine fixedFormatLine = fixedFormatLine(line);
            if (fixedFormatLine != null) {
                try {
                    result.add(lineToObject(fixedFormatLine.clazz, line));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    throw new FixedLengthException("No empty constructor in class");
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    throw new FixedLengthException("Access to field failed", e);
                }
            } else if (!skipUnknownLines) throw new FixedLengthException("Find unknown line");
        }

        return result;
    }

    private static class FixedFormatLine {
        public String startsWith = null;
        public Class clazz;
    }

}
