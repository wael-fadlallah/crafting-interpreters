package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens  = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> Keywords;

    static {
        Keywords = new HashMap<>();
        Keywords.put("and", AND);
        Keywords.put("class", CLASS);
        Keywords.put("else", ELSE);
        Keywords.put("false", FALSE);
        Keywords.put("for", FOR);
        Keywords.put("fun", FUN);
        Keywords.put("if", IF);
        Keywords.put("nil", NIL);
        Keywords.put("or", OR);
        Keywords.put("print", PRINT);
        Keywords.put("return", RETURN);
        Keywords.put("super", SUPER);
        Keywords.put("this", THIS);
        Keywords.put("true", TRUE);
        Keywords.put("var", VAR);
        Keywords.put("while", WHILE);
    }

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()){
//          we are at the beginning of the next lexeme
            start = current;
            scanTokens();
        }
        tokens.add(new Token(EOF, "", null, line));

        return tokens;
    }

    private void scanToken(){
        char c = advance();

        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if(match('/')){
//                    a comment goes until the end of the file
                    while (peek() != '\n' && !isAtEnd()) advance();
                }else{
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> {}
            case '\n' -> { line++; }

//            Literals
            case '"' -> {
                string();
            }
            default -> {
                if(isDigit(c)){
                    number();
                }else if (isAlpha(c)){
                 identifier();   
                }else{
                    Lox.error(line, "Unexpected Character.");
                }
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = Keywords.get(text);
        if (type == null) type  = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c<= 'z') || (c >= 'A' && c<= 'Z') || c == '_';
    }

    private void number() {
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())){
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            Lox.error(line, "Unmatched string.");
        }

        advance(); // to include the closing "

//        Trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if (source.charAt(current) != expected) return  false;

        current++;
        return true;
    }

    private char advance() {
//      return the current character then increase the current counter
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }


    private boolean isAtEnd() {
        return current >= source.length();
    }

}
