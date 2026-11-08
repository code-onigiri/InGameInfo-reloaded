package df11zomgraves.ingameinfo.parser.text;

import static df11zomgraves.ingameinfo.parser.text.Token.TokenType;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.regex.Matcher;

public class Tokenizer {
    private final Queue<Token> tokens;

    public Tokenizer() {
        this.tokens = new LinkedList<Token>();
    }

    public void tokenize(String str) throws Exception {
        Location start = new Location(0, 0);
        final Location end = new Location(0, 0);
        boolean match;

        this.tokens.clear();

        str = str.trim();

        boolean atLineStart = true;
        while (str.length() > 0) {
            if (atLineStart) {
                // Indentation is formatting, not content: skip blanks at the
                // start of a line instead of folding them into STRING lexems.
                int indent = 0;
                while (indent < str.length() && (str.charAt(indent) == ' ' || str.charAt(indent) == '\t' || str.charAt(indent) == '\r')) {
                    indent++;
                }
                if (indent > 0) {
                    str = str.substring(indent);
                    start.setColumn(start.getColumn() + indent);
                    end.setRow(start.getRow());
                    end.setColumn(start.getColumn());
                    if (str.length() == 0) {
                        break;
                    }
                }
            }

            match = false;
            for (final TokenType tokenType : TokenType.values()) {
                if (tokenType.equals(TokenType.EOF)) {
                    // EOF is only the synthetic end marker appended below; a
                    // literal '$' in the input is ordinary STRING content.
                    continue;
                }
                final Matcher matcher = tokenType.getPattern().matcher(str);
                if (matcher.find()) {
                    match = true;
                    final String lexem = matcher.group();
                    str = matcher.replaceFirst("");

                    if (tokenType.equals(TokenType.NEWLINE)) {
                        final int lines = lexem.length() - lexem.replace("\n", "").length();
                        end.setRow(start.getRow() + lines);
                        end.setColumn(0);
                    } else {
                        end.setColumn(start.getColumn() + lexem.length());
                    }

                    this.tokens.add(new Token(lexem, start.clone(), end.clone(), tokenType));
                    start = end.clone();
                    atLineStart = tokenType.equals(TokenType.NEWLINE);
                    break;
                }
            }

            if (!match) {
                throw new InvalidTokenException(String.format(Locale.ENGLISH, "Invalid token at %s!", start));
            }
        }

        this.tokens.add(new Token("", start.clone(), end.clone(), TokenType.EOF));
    }

    public Token nextToken() {
        return this.tokens.remove();
    }
}
