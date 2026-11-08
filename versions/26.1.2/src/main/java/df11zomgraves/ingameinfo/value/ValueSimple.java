package df11zomgraves.ingameinfo.value;

import java.util.regex.Pattern;

public abstract class ValueSimple extends Value {
    private static final Pattern DOLLAR_COLOR_CODE = Pattern.compile("\\$(?=[0-9a-fk-or])");
    private static final Pattern SECTION_COLOR_CODE = Pattern.compile("\u00a7(?=[0-9a-fk-or])");
    private static final Pattern ESCAPED_CHARS = Pattern.compile("\\\\([<>\\[/\\]\\\\])");
    private static final Pattern UNESCAPED_CHARS = Pattern.compile("([<>\\[/\\]\\\\])");
    private static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    @Override
    public Value setRawValue(final String value, final boolean isText) {
        this.value = DOLLAR_COLOR_CODE.matcher(value).replaceAll("\u00a7");
        if (isText) {
            this.value = ESCAPED_CHARS.matcher(this.value).replaceAll("$1");
        }
        return this;
    }

    @Override
    public String getRawValue(final boolean isText) {
        // Only a section sign that forms a formatting code came from a '$'
        // escape; any other literal section sign is kept as-is so that the
        // load/save round trip does not rewrite it.
        String str = SECTION_COLOR_CODE.matcher(this.value).replaceAll("\\$");
        if (isText) {
            str = UNESCAPED_CHARS.matcher(str).replaceAll("\\\\$1");
        }
        return str;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public boolean isValidSize() {
        return this.values.size() == 0;
    }

    public static class ValueString extends ValueSimple {
        @Override
        public String getType() {
            if (NUMBER.matcher(this.value).matches()) {
                return ValueRegistry.INSTANCE.forClass(ValueNumber.class);
            }
            return super.getType();
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }

    public static class ValueNumber extends ValueSimple {
        @Override
        public String getValue() {
            return this.value;
        }
    }

    public static class ValueVariable extends ValueSimple {
        @Override
        public String getValue() {
            return getVariableValue(this.value);
        }
    }

    public static class ValueInvalid extends ValueComplex {
        @Override
        public boolean isValidSize() {
            return true;
        }

        @Override
        public String getValue() {
            return "";
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }

    public static void register() {
        ValueRegistry.INSTANCE.register(new ValueString().setName("str").setAliases("string"));
        ValueRegistry.INSTANCE.register(new ValueNumber().setName("num").setAliases("number", "int", "integer", "float", "double"));
        ValueRegistry.INSTANCE.register(new ValueVariable().setName("var").setAliases("variable"));
        ValueRegistry.INSTANCE.register(new ValueInvalid().setName("invalid"));
    }
}
