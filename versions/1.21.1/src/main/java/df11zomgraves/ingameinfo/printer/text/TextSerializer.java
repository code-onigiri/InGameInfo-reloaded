package df11zomgraves.ingameinfo.printer.text;

import df11zomgraves.ingameinfo.value.Value;
import df11zomgraves.ingameinfo.value.ValueComplex;
import df11zomgraves.ingameinfo.value.ValueLogic;
import df11zomgraves.ingameinfo.value.ValueSimple;

import java.util.List;

/** Serializes parsed values back to the text configuration syntax. */
public final class TextSerializer {
    private TextSerializer() {
    }

    public static String serialize(final List<Value> values) {
        final StringBuilder builder = new StringBuilder();
        for (final Value value : values) {
            builder.append(serializeValue(value));
        }
        return builder.toString();
    }

    public static String serializeValue(final Value value) {
        final List<Value> values = value.values;
        final int size = values.size();
        final String type = value.getType();

        if (!value.isValidSize()) {
            return "";
        }

        if (value.isSimple()) {
            final String str = value.getRawValue(true);
            return value.getClass() == ValueSimple.ValueVariable.class ? "<" + str + ">" : str;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append('<').append(type).append('[');
        builder.append(serializeValue(values.get(0)));
        if (value.getClass() == ValueLogic.ValueIf.class) {
            builder.append('[').append(serializeValue(values.get(1)));
            if (size == 3) {
                builder.append('/').append(serializeValue(values.get(2)));
            }
            builder.append(']');
        } else if (value.getClass() == ValueComplex.ValueMin.class || value.getClass() == ValueComplex.ValueMax.class) {
            builder.append('/').append(serializeValue(values.get(1)));
            if (size == 4) {
                builder.append('[').append(serializeValue(values.get(2)))
                        .append('/').append(serializeValue(values.get(3))).append(']');
            }
        } else {
            for (int i = 1; i < size; i++) {
                builder.append('/').append(serializeValue(values.get(i)));
            }
        }
        return builder.append("]>").toString();
    }
}
