package df11zomgraves.ingameinfo.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.gui.overlay.Info;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;

public abstract class Value {
	protected static final Minecraft minecraft = Minecraft.getInstance();
    private static final Pattern PATTERN = Pattern.compile("\\{([a-z0-9]+)\\}", Pattern.CASE_INSENSITIVE);
    private static final Matcher MATCHER = PATTERN.matcher("");
    protected static List<Info> info;

    private String name = null;
    private String[] aliases = new String[0];
    protected String value = "";
    public final List<Value> values = new ArrayList<Value>();

    public Value setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Value setAliases(final String... aliases) {
        this.aliases = aliases;
        return this;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public String getType() {
        return ValueRegistry.INSTANCE.forClass(getClass());
    }

    public Value setRawValue(final String value, final boolean isText) {
        this.value = "";
        return this;
    }

    public String getRawValue(final boolean isText) {
        return this.value;
    }

    protected String replaceVariables(String str) {
        final int firstOpen = str.indexOf("{{");
        if (firstOpen < 0) {
            return replaceTags(str);
        }

        final StringBuilder builder = new StringBuilder(str.length());
        int index = 0;
        while (index < str.length()) {
            final int open = str.indexOf("{{", index);
            if (open < 0) {
                builder.append(replaceTags(str.substring(index)));
                break;
            }

            builder.append(replaceTags(str.substring(index, open)));
            final int close = findExpressionEnd(str, open + 2);
            if (close < 0) {
                builder.append(replaceTags(str.substring(open)));
                break;
            }

            final String expression = str.substring(open + 2, close);
            try {
                builder.append(Expression.evaluate(expression));
            } catch (final RuntimeException e) {
                // Keep the original text visible when an expression is invalid.
                builder.append(str, open, close + 2);
            }
            index = close + 2;
        }
        return builder.toString();
    }

    private String replaceTags(String str) {
        MATCHER.reset(str);

        while (MATCHER.find()) {
            str = str.replace(MATCHER.group(0), getVariableValue(MATCHER.group(1)));
        }

        return str;
    }

    private static int findExpressionEnd(final String str, final int from) {
        boolean inString = false;
        boolean escaped = false;
        char quote = 0;
        for (int i = from; i < str.length() - 1; i++) {
            final char c = str.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == quote) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                quote = c;
            } else if (c == '}' && str.charAt(i + 1) == '}') {
                return i;
            }
        }
        return -1;
    }

    public abstract boolean isSimple();

    public abstract boolean isValidSize();

    public abstract String getValue();

    public boolean isValid() {
        return true;
    }

    protected String getValue(final int index) {
        return this.values.get(index).getReplacedValue();
    }

    protected int getIntValue() {
        return Integer.parseInt(getReplacedValue());
    }

    protected int getIntValue(final int index) {
        return Integer.parseInt(getValue(index));
    }

    protected double getDoubleValue() {
        return Double.parseDouble(getReplacedValue());
    }

    protected double getDoubleValue(final int index) {
        return Double.parseDouble(getValue(index));
    }

    protected boolean getBooleanValue() {
        return Boolean.parseBoolean(getReplacedValue());
    }

    protected boolean getBooleanValue(final int index) {
        return Boolean.parseBoolean(getValue(index));
    }

    protected String getVariableValue(final String var) {
        try {
            final String value = TagRegistry.INSTANCE.getValue(var);
            if (value != null) {
                return value;
            }
        } catch (final Exception e) {
        	InGameInfoXML.logger.debug("Failed to get value!", e);
            return "null";
        }

        return "{" + var + "}";
    }

    public String getReplacedValue() {
        return replaceVariables(getValue());
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "[%s] '%s'", getClass(), this.value);
    }

    public static void setInfo(final List<Info> info) {
        Value.info = info;
    }

    public static Value fromString(final String str) {
        return ValueRegistry.INSTANCE.forName(str);
    }

    public static String toString(final Value value) {
        return ValueRegistry.INSTANCE.forClass(value.getClass());
    }
}
