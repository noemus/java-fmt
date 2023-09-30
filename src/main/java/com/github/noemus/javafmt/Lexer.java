package com.github.noemus.javafmt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *  Tokens:
 *  WS - SPACE, TAB, other special whitespace chars except NL
 *  NL - CR, LF, CRLF
 *  SYMBOL - ~!-+,./*[]{}()&^%|<>?:;
 *  WORD - java identifier or keyword
 *  LINE_COMMENT
 *  BLOCK_COMMENT
 *  JAVADOC
 *  STRING_LITERAL
 *  MULTILINE_STRING
 *  CHAR_LITERAL
 *  NUMBER
 * <p>
 *  Fragments:
 *  EMPTY_LINE - NL after NL followed by WS*
 *  IMPORTS
 *  PACKAGE_DECL
 *  PARAM_LIST - method/constructor params, record params, (paramValue1, paramValue2, ...)
 *  PARAM_DECL_LIST - method/constructor decl, record decl, (type paramName1, type paramName2, ...)
 *  ANNOTATION_PARAMS - (value), ({val1, val2}), (param1 = value1, ...)
 *  BLOCK - method, class body, try/catch/finally body, for/while body, etc. - { statement1; statement2; ...}
 *  ANNOTATION - @annotation_name ANNOTATION_PARAMS
 *  TYPE DECL - class, interface, record, enum
 *  MEMBER_DECL - METHOD_DECL, CTOR_DECL, FIELD_DECL, Inner type decl
 *  METHOD_DECL - modifiers
 *  STATEMENT - all java statements and expressions
 *  EXPRESSION - arithmetic, string, switch expression, ternary, etc.
 */
class Lexer {
    private final char[] chars;

    private int line = 0;
    private int column = 0;
    private int offset = 0;

    private Lexer(char[] chars) {
        this.chars = chars;
    }

    static Lexer from(Path javaFile) {
        try {
            return new Lexer(Files.readString(javaFile, UTF_8).toCharArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    Stream<Token> tokens() {
        return Stream.generate(this::nextToken).takeWhile(Objects::nonNull);
    }

    private Token nextToken() {
        if (offset >= chars.length) {
            return null;
        }
        return parseToken();
    }

    private Token parseToken() {
//        switch (peek()) {
//            case '\r', '\n' -> parseNewLine();
//            case ''
//        }
        return null;
    }

    private Token parseNewLine() {
        return null;
    }

    private char peek() {
        return chars[offset];
    }

    private char next() {
        return chars[offset++];
    }

    private Token token(int len) {
        var token = new Token(line, column, chars, column, len);
        column += len;
        return token;
    }

    record Token(int line, int column, char[] content, int offset, int length) implements Fragment {
        private static final char[] NL_BYTES = System.lineSeparator().toCharArray();

        static Token newLine(int line, int column) {
            return new Token(line, column, NL_BYTES, 0, NL_BYTES.length);
        }

        @Override
        public String text() {
            return ""; //new String(content, offset, length, UTF_8);
        }

        @Override
        public Stream<FormattingError> check() {
            return Stream.empty();
        }
    }

    interface Accumulator {
        Stream<Token> advance(byte c);
    }

    interface Fragment {
        int line();
        int column();
        String text();
        Stream<FormattingError> check();
    }

    record FormattingError(String message) {}
}
