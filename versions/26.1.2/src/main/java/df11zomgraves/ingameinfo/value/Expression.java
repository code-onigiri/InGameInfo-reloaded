package df11zomgraves.ingameinfo.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import df11zomgraves.ingameinfo.tag.TagRegistry;

/**
 * Evaluates the expression syntax used inside double braces: {{ ... }}.
 *
 * <p>Only expressions inside {{ }} are affected. The classic {tag} syntax is
 * handled by {@link Value#replaceVariables(String)} exactly as before.</p>
 *
 * <p>Supported syntax:</p>
 * <ul>
 *   <li>numbers, quoted strings and bare tag names</li>
 *   <li>+ - * / % with the usual precedence</li>
 *   <li>parentheses and unary + / -</li>
 *   <li>round(x[, digits]), min(...), max(...), abs(x), floor(x), ceil(x)</li>
 *   <li>format("java format", value, ...)</li>
 * </ul>
 *
 * <p>When a tag is unknown or the expression cannot be parsed, the caller
 * leaves the original {{ ... }} text in place. That keeps a broken expression
 * visible in the HUD instead of silently changing the surrounding text.</p>
 */
public final class Expression {
    private static final int CACHE_SIZE = 512;
    private static final Map<String, Optional<Node>> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, Optional<Node>>(CACHE_SIZE, 0.75f, true) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(final Map.Entry<String, Optional<Node>> eldest) {
                    return size() > CACHE_SIZE;
                }
            });

    private Expression() {
    }

    public static String evaluate(final String source) {
        if (source == null) {
            throw new ExpressionException("Expression is null");
        }

        final Optional<Node> cached = CACHE.get(source);
        final Node node;
        if (cached != null) {
            if (!cached.isPresent()) {
                throw new ExpressionException("Invalid expression");
            }
            node = cached.get();
        } else {
            try {
                node = new Parser(source).parse();
                CACHE.put(source, Optional.of(node));
            } catch (final ExpressionException e) {
                CACHE.put(source, Optional.empty());
                throw e;
            }
        }

        try {
            return asString(node.evaluate());
        } catch (final ExpressionException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ExpressionException("Evaluation failed", e);
        }
    }

    private static String asString(final Object value) {
        if (value instanceof Number) {
            return formatNumber(((Number) value).doubleValue());
        }
        return String.valueOf(value);
    }

    private static String formatNumber(final double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        if (value == Math.rint(value) && Math.abs(value) < 9.007199254740992E15) {
            return Long.toString((long) value);
        }
        final BigDecimal decimal = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        return decimal.toPlainString();
    }

    private static double toDouble(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0 : 0.0;
        }
        final String string = String.valueOf(value).trim();
        if (string.isEmpty()) {
            throw new ExpressionException("Expected a number, got an empty string");
        }
        try {
            return Double.parseDouble(string);
        } catch (final NumberFormatException e) {
            throw new ExpressionException("Expected a number, got '" + string + "'", e);
        }
    }

    private static boolean isNumber(final Object value) {
        if (value instanceof Number) {
            return true;
        }
        if (value instanceof Boolean) {
            return false;
        }
        final String string = String.valueOf(value).trim();
        if (string.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(string);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private static long toLong(final Object value) {
        final double number = toDouble(value);
        if (!Double.isFinite(number)) {
            throw new ExpressionException("Expected a finite number");
        }
        return (long) number;
    }

    private static int toInt(final Object value) {
        final long number = toLong(value);
        if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw new ExpressionException("Integer out of range");
        }
        return (int) number;
    }

    private static Object add(final Object left, final Object right) {
        if (isNumber(left) && isNumber(right)) {
            return toDouble(left) + toDouble(right);
        }
        return asString(left) + asString(right);
    }

    private static Object numericBinary(final char operator, final Object left, final Object right) {
        final double a = toDouble(left);
        final double b = toDouble(right);
        switch (operator) {
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                return b == 0.0 ? 0.0 : a / b;
            case '%':
                return b == 0.0 ? 0.0 : a % b;
            default:
                throw new ExpressionException("Unknown operator: " + operator);
        }
    }

    private static Object round(final double value, final int digits) {
        if (digits > 0) {
            return String.format(Locale.ENGLISH, "%." + digits + "f", value);
        }
        final double factor = Math.pow(10, digits);
        return Math.round(value * factor) / factor;
    }

    private static String format(final String pattern, final List<Object> arguments) {
        final Object[] values = arguments.toArray();
        int sequential = 0;
        int previous = -1;
        for (int i = 0; i < pattern.length(); i++) {
            final char c = pattern.charAt(i);
            if (c != '%') {
                continue;
            }
            if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '%') {
                i++;
                continue;
            }

            int j = i + 1;
            int explicit = -1;
            final int digitsStart = j;
            while (j < pattern.length() && Character.isDigit(pattern.charAt(j))) {
                j++;
            }
            if (j < pattern.length() && pattern.charAt(j) == '$' && j > digitsStart) {
                explicit = Integer.parseInt(pattern.substring(digitsStart, j)) - 1;
                j++;
            } else {
                j = digitsStart;
            }
            boolean reusePrevious = false;
            while (j < pattern.length() && "<-+ 0,#(".indexOf(pattern.charAt(j)) >= 0) {
                if (pattern.charAt(j) == '<') {
                    reusePrevious = true;
                }
                j++;
            }
            while (j < pattern.length() && Character.isDigit(pattern.charAt(j))) {
                j++;
            }
            if (j < pattern.length() && pattern.charAt(j) == '.') {
                j++;
                while (j < pattern.length() && Character.isDigit(pattern.charAt(j))) {
                    j++;
                }
            }
            if (j >= pattern.length()) {
                break;
            }

            char conversion = pattern.charAt(j);
            boolean date = false;
            if (conversion == 't' || conversion == 'T') {
                date = true;
                if (j + 1 >= pattern.length()) {
                    break;
                }
                conversion = pattern.charAt(j + 1);
                j++;
            }
            if (conversion == 'n') {
                i = j;
                continue;
            }

            final int index;
            if (reusePrevious) {
                if (previous < 0) {
                    throw new ExpressionException("No previous argument to reuse with '<'");
                }
                index = previous;
            } else if (explicit >= 0) {
                index = explicit;
                previous = index;
            } else {
                index = sequential++;
                previous = index;
            }
            if (index < 0 || index >= values.length) {
                throw new ExpressionException("Not enough arguments for format string");
            }
            values[index] = date ? toLong(values[index]) : coerceFormatArgument(conversion, values[index]);
            i = j;
        }

        try {
            return String.format(Locale.ENGLISH, pattern, values);
        } catch (final RuntimeException e) {
            throw new ExpressionException("Invalid format string", e);
        }
    }

    private static Object coerceFormatArgument(final char conversion, final Object value) {
        switch (conversion) {
            case 'd':
            case 'o':
            case 'x':
            case 'X':
                return toLong(value);
            case 'f':
            case 'e':
            case 'E':
            case 'g':
            case 'G':
            case 'a':
            case 'A':
                return toDouble(value);
            case 'c':
            case 'C':
                if (value instanceof Character) {
                    return value;
                }
                if (value instanceof Number) {
                    return (char) ((Number) value).intValue();
                }
                final String string = asString(value);
                return string.isEmpty() ? '\0' : string.charAt(0);
            default:
                return value;
        }
    }

    private interface Node {
        Object evaluate();
    }

    private static final class LiteralNode implements Node {
        private final Object value;

        private LiteralNode(final Object value) {
            this.value = value;
        }

        @Override
        public Object evaluate() {
            return this.value;
        }
    }

    private static final class TagNode implements Node {
        private final String name;

        private TagNode(final String name) {
            this.name = name;
        }

        @Override
        public Object evaluate() {
            final String value;
            try {
                value = TagRegistry.INSTANCE.getValue(this.name);
            } catch (final RuntimeException e) {
                throw new ExpressionException("Tag '" + this.name + "' failed", e);
            }
            if (value == null) {
                throw new ExpressionException("Unknown tag '" + this.name + "'");
            }
            return value;
        }
    }

    private static final class UnaryNode implements Node {
        private final char operator;
        private final Node operand;

        private UnaryNode(final char operator, final Node operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public Object evaluate() {
            final double value = toDouble(this.operand.evaluate());
            return this.operator == '-' ? -value : value;
        }
    }

    private static final class BinaryNode implements Node {
        private final char operator;
        private final Node left;
        private final Node right;

        private BinaryNode(final char operator, final Node left, final Node right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        @Override
        public Object evaluate() {
            final Object leftValue = this.left.evaluate();
            final Object rightValue = this.right.evaluate();
            if (this.operator == '+') {
                return add(leftValue, rightValue);
            }
            return numericBinary(this.operator, leftValue, rightValue);
        }
    }

    private static final class FunctionNode implements Node {
        private final String name;
        private final List<Node> arguments;

        private FunctionNode(final String name, final List<Node> arguments) {
            this.name = name.toLowerCase(Locale.ENGLISH);
            this.arguments = arguments;
        }

        @Override
        public Object evaluate() {
            switch (this.name) {
                case "round":
                    return evaluateRound();
                case "min":
                    return evaluateMinMax(true);
                case "max":
                    return evaluateMinMax(false);
                case "abs":
                    return Math.abs(oneNumber("abs"));
                case "floor":
                    return Math.floor(oneNumber("floor"));
                case "ceil":
                    return Math.ceil(oneNumber("ceil"));
                case "format":
                    return evaluateFormat();
                default:
                    throw new ExpressionException("Unknown function '" + this.name + "'");
            }
        }

        private Object evaluateRound() {
            if (this.arguments.size() < 1 || this.arguments.size() > 2) {
                throw new ExpressionException("round() expects one or two arguments");
            }
            final double value = toDouble(this.arguments.get(0).evaluate());
            final int digits = this.arguments.size() == 2 ? toInt(this.arguments.get(1).evaluate()) : 0;
            return round(value, digits);
        }

        private Object evaluateMinMax(final boolean minimum) {
            if (this.arguments.isEmpty()) {
                throw new ExpressionException((minimum ? "min" : "max") + "() expects at least one argument");
            }
            double result = toDouble(this.arguments.get(0).evaluate());
            for (int i = 1; i < this.arguments.size(); i++) {
                final double value = toDouble(this.arguments.get(i).evaluate());
                result = minimum ? Math.min(result, value) : Math.max(result, value);
            }
            return result;
        }

        private double oneNumber(final String function) {
            if (this.arguments.size() != 1) {
                throw new ExpressionException(function + "() expects one argument");
            }
            return toDouble(this.arguments.get(0).evaluate());
        }

        private String evaluateFormat() {
            if (this.arguments.isEmpty()) {
                throw new ExpressionException("format() expects a format string");
            }
            final String pattern = asString(this.arguments.get(0).evaluate());
            final List<Object> values = new ArrayList<Object>(this.arguments.size() - 1);
            for (int i = 1; i < this.arguments.size(); i++) {
                values.add(this.arguments.get(i).evaluate());
            }
            return format(pattern, values);
        }
    }

    private static final class Parser {
        private final String source;
        private int position;

        private Parser(final String source) {
            this.source = source;
        }

        private Node parse() {
            final Node node = parseAdditive();
            skipWhitespace();
            if (this.position != this.source.length()) {
                throw error("Unexpected character");
            }
            return node;
        }

        private Node parseAdditive() {
            Node left = parseMultiplicative();
            while (true) {
                skipWhitespace();
                final char c = peek();
                if (c != '+' && c != '-') {
                    return left;
                }
                this.position++;
                left = new BinaryNode(c, left, parseMultiplicative());
            }
        }

        private Node parseMultiplicative() {
            Node left = parseUnary();
            while (true) {
                skipWhitespace();
                final char c = peek();
                if (c != '*' && c != '/' && c != '%') {
                    return left;
                }
                this.position++;
                left = new BinaryNode(c, left, parseUnary());
            }
        }

        private Node parseUnary() {
            skipWhitespace();
            final char c = peek();
            if (c == '+' || c == '-') {
                this.position++;
                return new UnaryNode(c, parseUnary());
            }
            return parsePrimary();
        }

        private Node parsePrimary() {
            skipWhitespace();
            if (this.position >= this.source.length()) {
                throw error("Unexpected end of expression");
            }

            final char c = this.source.charAt(this.position);
            if (c == '(') {
                this.position++;
                final Node node = parseAdditive();
                skipWhitespace();
                expect(')');
                return node;
            }
            if (c == '"' || c == '\'') {
                return new LiteralNode(parseString(c));
            }
            if (Character.isDigit(c) || (c == '.' && this.position + 1 < this.source.length()
                    && Character.isDigit(this.source.charAt(this.position + 1)))) {
                return new LiteralNode(parseNumber());
            }
            if (isIdentifierStart(c)) {
                final String name = parseIdentifier();
                skipWhitespace();
                if (this.position < this.source.length() && this.source.charAt(this.position) == '(') {
                    this.position++;
                    return new FunctionNode(name, parseArguments());
                }
                return new TagNode(name);
            }
            throw error("Unexpected character '" + c + "'");
        }

        private List<Node> parseArguments() {
            final List<Node> arguments = new ArrayList<Node>();
            skipWhitespace();
            if (this.position < this.source.length() && this.source.charAt(this.position) == ')') {
                this.position++;
                return arguments;
            }
            while (true) {
                arguments.add(parseAdditive());
                skipWhitespace();
                if (this.position >= this.source.length()) {
                    throw error("Expected ')'");
                }
                final char c = this.source.charAt(this.position);
                if (c == ',') {
                    this.position++;
                    continue;
                }
                if (c == ')') {
                    this.position++;
                    return arguments;
                }
                throw error("Expected ',' or ')'");
            }
        }

        private String parseIdentifier() {
            final int start = this.position;
            while (this.position < this.source.length() && isIdentifierPart(this.source.charAt(this.position))) {
                this.position++;
            }
            return this.source.substring(start, this.position);
        }

        private double parseNumber() {
            final int start = this.position;
            boolean dot = false;
            boolean exponent = false;
            while (this.position < this.source.length()) {
                final char c = this.source.charAt(this.position);
                if (Character.isDigit(c)) {
                    this.position++;
                    continue;
                }
                if (c == '.' && !dot && !exponent) {
                    dot = true;
                    this.position++;
                    continue;
                }
                if ((c == 'e' || c == 'E') && !exponent) {
                    exponent = true;
                    this.position++;
                    if (this.position < this.source.length()
                            && (this.source.charAt(this.position) == '+' || this.source.charAt(this.position) == '-')) {
                        this.position++;
                    }
                    continue;
                }
                break;
            }
            try {
                return Double.parseDouble(this.source.substring(start, this.position));
            } catch (final NumberFormatException e) {
                throw error("Invalid number");
            }
        }

        private String parseString(final char quote) {
            this.position++;
            final StringBuilder builder = new StringBuilder();
            while (this.position < this.source.length()) {
                final char c = this.source.charAt(this.position++);
                if (c == quote) {
                    return builder.toString();
                }
                if (c != '\\') {
                    builder.append(c);
                    continue;
                }
                if (this.position >= this.source.length()) {
                    throw error("Unterminated escape sequence");
                }
                final char escaped = this.source.charAt(this.position++);
                switch (escaped) {
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '"':
                        builder.append('"');
                        break;
                    case '\'':
                        builder.append('\'');
                        break;
                    case 'u':
                        if (this.position + 4 > this.source.length()) {
                            throw error("Invalid unicode escape");
                        }
                        try {
                            builder.append((char) Integer.parseInt(this.source.substring(this.position, this.position + 4), 16));
                        } catch (final NumberFormatException e) {
                            throw error("Invalid unicode escape");
                        }
                        this.position += 4;
                        break;
                    default:
                        builder.append(escaped);
                        break;
                }
            }
            throw error("Unterminated string");
        }

        private void skipWhitespace() {
            while (this.position < this.source.length() && Character.isWhitespace(this.source.charAt(this.position))) {
                this.position++;
            }
        }

        private char peek() {
            return this.position < this.source.length() ? this.source.charAt(this.position) : '\0';
        }

        private void expect(final char c) {
            if (this.position >= this.source.length() || this.source.charAt(this.position) != c) {
                throw error("Expected '" + c + "'");
            }
            this.position++;
        }

        private ExpressionException error(final String message) {
            return new ExpressionException(message + " at " + this.position + " in '" + this.source + "'");
        }

        private static boolean isIdentifierStart(final char c) {
            return Character.isLetter(c) || c == '_';
        }

        private static boolean isIdentifierPart(final char c) {
            return Character.isLetterOrDigit(c) || c == '_';
        }
    }

    public static class ExpressionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ExpressionException(final String message) {
            super(message);
        }

        public ExpressionException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
