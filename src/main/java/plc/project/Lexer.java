package plc.project;

import java.util.List;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the invalid character.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();

        while (chars.has(0)) {
            if (peek("[ \t\r\n]")) {
                chars.advance();
            } else {
                tokens.add(lexToken());
            }
        }

        return tokens;
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        if (peek("[a-zA-Z_][A-Za-z0-9_]*]")) {
            return lexIdentifier();
        } else if (peek("[0-9]+(\\.?(?:[0-9]))*")) {
            return lexNumber();
        } else if (peek("[A-Za-z ] | \\[brnt\"'\\]")) {
            return lexCharacter();
        } else if (peek("[A-Za-z....]+ | \\[brnt\"'\\]")) {
            return lexString();
        } else if (peek("=|==|!=|>=|<=|&&|\\|\\||\\(|\\)|;")) {
            return lexOperator();
        } else {
            chars.advance();
            return null;
            //random edge case idk
        }

        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexIdentifier() {
        int startIndex = chars.index;
        if (!peek("[A-Za-z]")) {
            throw new ParseException("Identifer doesn't start with character", chars.index);
        }
        StringBuilder literalBuilder = new StringBuilder();
        literalBuilder.append(chars.get(0));
        chars.advance();

        while (peek("[A-Za-z0-9]")) {
            literalBuilder.append(chars.get(0));
            chars.advance();
        }

        String literal = literalBuilder.toString();

        return new Token(Token.Type.IDENTIFIER, literal, startIndex);
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexNumber() {
        int startIndex = chars.index;
        boolean decimal = false;
        StringBuilder literalBuilder = new StringBuilder();

        // Optional sign
        if (peek("[-]")) {
            literalBuilder.append(chars.get(0));
            chars.advance();
        }

        // Read integer part
        if (peek("[1-9]")) {
            literalBuilder.append(chars.get(0));
            chars.advance();
            while (peek("[0-9]")) {
                literalBuilder.append(chars.get(0));
                chars.advance();
            }
        } else if (peek("0")) {
            literalBuilder.append(chars.get(0));
            chars.advance();
        } else {
            throw new ParseException("Invalid number format", startIndex);
        }

        // Check if there is a decimal point
        if (peek("\\.")) {
            decimal = true;
            literalBuilder.append(chars.get(0));
            chars.advance();

            // Read fractional part
            if (peek("[0-9]")) {
                literalBuilder.append(chars.get(0));
                chars.advance();
                while (peek("[0-9]")) {
                    literalBuilder.append(chars.get(0));
                    chars.advance();
                }
            } else {
                throw new ParseException("Invalid decimal format", startIndex);
            }
        }

        String literal = literalBuilder.toString();

        Token.Type type;
        type = Token.Type.INTEGER;
        if(decimal) {
            type = Token.Type.DECIMAL;
        }

        return new Token(type, literal, startIndex);
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexCharacter() {
        int startIndex = chars.index;
        StringBuilder literalBuilder = new StringBuilder();

        if (!match("'")) {
            throw new ParseException("Expected opening quote for character literal", startIndex);
        }

        if (peek("\\\\") || peek("[^']")) {
            if (peek("\\")) { // Handle escape sequences
                lexEscape();
            } else {
                literalBuilder.append(chars.get(0));
                chars.advance();
            }
        } else {
            throw new ParseException("Invalid character literal content", startIndex);
        }

        if (literalBuilder.length() != 1) {
            throw new ParseException("Character literal must contain exactly one character", startIndex);
        }

        if (!match("'")) {
            throw new ParseException("Expected closing quote for character literal", startIndex);
        }

        String literal = literalBuilder.toString();

        return new Token(Token.Type.CHARACTER, "'" + literal + "'", startIndex);
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexString() {
        int startIndex = chars.index;
        StringBuilder literalBuilder = new StringBuilder();

        if (!match("\"")) {
            throw new ParseException("Expected opening quote for string literal", startIndex);
        }

        while (!peek("\"")) {
            if (!chars.has(0)) {
                throw new ParseException("Unterminated string literal", startIndex);
            }

            if (match("\\")) {
                lexEscape();
                literalBuilder.append(chars.get(0));
                chars.advance();
            } else {
                literalBuilder.append(chars.get(0));
                chars.advance();
            }
        }

        if (!match("\"")) {
            throw new ParseException("Expected closing quote for string literal", startIndex);
        }

        return new Token(Token.Type.STRING, "\"" + literalBuilder.toString() + "\"", startIndex);
        // throw new UnsupportedOperationException(); //TODO
    }

    public void lexEscape() {
        if (!chars.has(0)) {
            throw new ParseException("Unterminated escape sequence", chars.index);
        }
        //idk what i'm actually supposed to do here
        char nextChar = chars.get(0);
        switch (nextChar) {
            case 'n':
                chars.advance();  // '\n' -> newline
                break;
            case 't':
                chars.advance();  // '\t' -> tab
                break;
            case 'r':
                chars.advance();  // '\r' -> carriage return
                break;
            case '\\':
                chars.advance();  // '\\' -> backslash
                break;
            case '\"':
                chars.advance();  // '\"' -> double quote
                break;
            case '\'':
                chars.advance();  // '\'' -> single quote (for characters)
                break;
            default:
                throw new ParseException("Invalid escape sequence: \\" + nextChar, chars.index);
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexOperator() {
        StringBuilder literalBuilder = new StringBuilder();
        int startIndex = chars.index;

        if (match("==") || match("!=") || match(">=") || match("<=") || match("&&") || match("||")) {
            literalBuilder.append(chars.get(0));
            literalBuilder.append(chars.get(1));
            return new Token(Token.Type.OPERATOR, literalBuilder.toString(), startIndex);
        }

        if (match("=") || match(">") || match("<") || match("+") || match("-") || match("*") || match("/") || match("!") || match("(") || match(")") || match(";")) {
            literalBuilder.append(chars.get(0));
            return new Token(Token.Type.OPERATOR, literalBuilder.toString(), startIndex);
        }

        throw new ParseException("Invalid operator", chars.index);
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for(int i = 0; i < patterns.length; i++) {
            if(!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if(peek){
            for(int i = 0; i < patterns.length; i++){
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
