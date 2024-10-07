package plc.project;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling those functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        try {
            List<Ast.Field> fields = new ArrayList<>();
            List<Ast.Method> methods = new ArrayList<>();

            while (tokens.has(0)) {
                if (match("LET")) {
                    fields.add(parseField());
                } else if (match("DEF")) {
                    methods.add(parseMethod());
                }
            }
            return new Ast.Source(fields, methods);
        }
        catch (ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        try {
            String val = tokens.get(-1).getLiteral();
            Optional<String> type = Optional.empty();
            if (match(":")) {
                if (match(Token.Type.IDENTIFIER)) {
                    type = Optional.of(tokens.get(-1).getLiteral());
                }
            }
            match("=");
            Ast.Expression value = parseExpression();
            match(";");

            return new Ast.Field(val, type.isPresent(), Optional.of(value));
        } catch (ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, for, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        try {
//            if (match("LET")) {
//                return parseDeclarationStatement();
//            } else if (match("IF")) {
//                return parseIfStatement();
//            } else if (match("FOR")) {
//                return parseForStatement();
//            } else if (match("WHILE")) {
//                return parseWhileStatement();
//            } else if (match("RETURN")) {
//                return parseReturnStatement();
//            } else {
                Ast.Expression leftSide = parseExpression();
                if (!match("=")) {
                    if (!match(";")) {
                        throw new ParseException("Expected semicolon", tokens.get(-1).getIndex());
                    }
                    return new Ast.Statement.Expression(leftSide);
                }

                Ast.Expression rightSide = parseExpression();

                if (!match(";")) {
                    throw new ParseException("Expected semicolon", tokens.get(-1).getIndex());
                }
                return new Ast.Statement.Assignment(leftSide, rightSide);
//            }

        } catch (ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */

    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        if(match(Token.Type.IDENTIFIER)) {
            String identifier = tokens.get(-1).getLiteral();

            if(match("=")){
                Ast.Expression expression = parseExpression();
                if(match(";")){
                    return new Ast.Statement.Declaration(identifier, Optional.of(expression));
                }
            }else{
                if(match("=")){
                    return new Ast.Statement.Declaration(identifier, Optional.empty());
                }
            }
        } else {
            throw new ParseException("Invalid Identifier", tokens.index);
        }

        throw new ParseException("Expected semicolon", tokens.index);
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Statement.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        try {
            return parseLogicalExpression();
        } catch (ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException {
        try {
            Ast.Expression astResult = parseEqualityExpression();
            while (match("&&") || match("||")) { // right
                String op = tokens.get(-1).getLiteral();
                Ast.Expression rightExpression = parseEqualityExpression();
                astResult = new Ast.Expression.Binary(op, astResult, rightExpression);
            }
            return astResult;

        } catch(ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseEqualityExpression() throws ParseException {
        try {
            Ast.Expression astResult = parseAdditiveExpression();
            while (match("!=") || match("==") || match(">=") || match(">") || match("<=") || match("<")) {
                String op = tokens.get(-1).getLiteral();
                Ast.Expression rightExpression = parseEqualityExpression();
                astResult = new Ast.Expression.Binary(op, astResult, rightExpression);
            }
            return astResult;
        } catch(ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        try {
            Ast.Expression astResult = parseMultiplicativeExpression();

            while (match("+") || match("-")) {
                String op = tokens.get(-1).getLiteral();
                Ast.Expression rightExpression = parseMultiplicativeExpression();
                astResult = new Ast.Expression.Binary(op, astResult, rightExpression);
            }
            return astResult;

        } catch(ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        try {
            Ast.Expression astResult = parseSecondaryExpression();

            while (match("/") || match("*")) { // right
                String op = tokens.get(-1).getLiteral();
                Ast.Expression rightExpression = parseSecondaryExpression();
                astResult = new Ast.Expression.Binary(op, astResult, rightExpression);
            }
            return astResult;
        } catch(ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expression parseSecondaryExpression() throws ParseException {
        try {
            Ast.Expression initialExpression = parsePrimaryExpression();

            while (match(".")) {
                if (!match(Token.Type.IDENTIFIER)) {
                    throw new ParseException("Invalid Identifier", tokens.get(0).getIndex());
                }
                // Identifier found
                String lit = tokens.get(-1).getLiteral();
                if (!match("(")) { // No expression after
                    initialExpression = new Ast.Expression.Access(Optional.of(initialExpression), lit);
                } else {
                    // Found '('
                    List<Ast.Expression> inputs = new ArrayList<>();
                    if(!match(")")) { // Found expression after
                        inputs.add(parseExpression());
                        while (match(",")) {
                            inputs.add(parseExpression());
                        }
                        if(!match(")")) { // Check closing parentheses
                            throw new ParseException("Invalid function closing parentheses not found", tokens.get(0).getIndex());
                        }
                    }
                    initialExpression = new Ast.Expression.Function(Optional.of(initialExpression), lit, inputs);
                }
            }
            return initialExpression;

        } catch (ParseException e) {
            throw new ParseException(e.getMessage(), e.getIndex());
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if (match("NIL")) {
            return new Ast.Expression.Literal(null);
        }
        else if (match("TRUE")) {
            return new Ast.Expression.Literal(true);
        }
        else if (match("FALSE")) {
            return new Ast.Expression.Literal(false);
        }
        else if (match(Token.Type.INTEGER)) { // INTEGER LITERAL FOUND
            return new Ast.Expression.Literal(new BigInteger(tokens.get(-1).getLiteral()));
        }
        else if (match(Token.Type.DECIMAL)) { // DECIMAL LITERAL FOUND
            return new Ast.Expression.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
        }
        else if (match(Token.Type.CHARACTER)) { // CHARACTER LITERAL FOUND
            String str = tokens.get(-1).getLiteral();
            return new Ast.Expression.Literal(str.charAt(1));
        }
        else if (match(Token.Type.STRING)) { // STRING LITERAL FOUND
            String str = tokens.get(-1).getLiteral();
            str = str.substring(1, str.length() - 1);
            if(str.contains("\\")) {
                str = str.replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\b", "\b")
                        .replace("\\r", "\r")
                        .replace("\\'", "'")
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"");
            }
            return new Ast.Expression.Literal(str);
        }
        else if (match(Token.Type.IDENTIFIER)) { // IDENTIFIER FOUND
            String val = tokens.get(-1).getLiteral();
            if (!match("(")) { // no expression after identifier
                return new Ast.Expression.Access(Optional.empty(), val);
            }
            else { // expression after identifier
                if (!match(")")) { // expression arguments found
                    Ast.Expression initalExpr = parseExpression();
                    List<Ast.Expression> inputs = new ArrayList<>();
                    inputs.add(initalExpr);

                    while (match(",")) {
                        inputs.add(parseExpression());
                    }

                    if (match(")")) { // Check closing parentheses
                        return new Ast.Expression.Function(Optional.empty(), val, inputs);
                    } else {
                        throw new ParseException("Closing parentheses expected", tokens.get(-1).getIndex());
                    }
                } else {
                    if (!tokens.get(-1).getLiteral().equals(")")) {
                        throw new ParseException("Closing parentheses expected", tokens.get(-1).getIndex());
                    } else {
                        return new Ast.Expression.Function(Optional.empty(), val, Collections.emptyList());
                    }
                }


            }

        } else if (match("(")) {
            Ast.Expression expression = parseExpression();
            if (!match(")")) {
                throw new ParseException("Expected closing parenthesis", tokens.get(-1).getIndex());
            }
            return new Ast.Expression.Group(expression);
        } else {
            throw new ParseException("Invalid Primary Expression", tokens.get(-1).getIndex());
            // TODO: handle storing the actual character index instead of I
        }
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for(int i = 0; i < patterns.length; i++){
            if(!tokens.has(i)){
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if(patterns[i] != tokens.get(i).getType()){
                    return false;
                }
            } else if (patterns[i] instanceof String){
                if(!patterns[i].equals(tokens.get(i).getLiteral())){
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
//        throw new UnsupportedOperationException(); //TODO (in lecture)
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);

        if(peek){
            for(int i = 0; i< patterns.length; i++){
                tokens.advance();
            }
        }
        return peek;
//        throw new UnsupportedOperationException(); //TODO (in lecture)
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
