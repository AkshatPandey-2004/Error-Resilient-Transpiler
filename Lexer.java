import java.util.*;
import java.util.regex.*;

class Token {
    String type;
    String value;
    int lineNumber; // Line number tracking

    public Token(String type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
        this.lineNumber = -1; // Default line number if not provided
    }

    @Override
    public String toString() {
        return type + "('" + value + "') at line " + lineNumber;
    }
}

public class Lexer {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "def", "print", "if", "else", "elif","for", "while", "return", "class", "import", "from",
        "in",  // ✅ Added 'in' so it's not treated as an IDENTIFIER
        "range"
    ));
    
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
    "\\b(def|print|if|else|elif|for|while|return|class|import|from|in|range)\\b" +  // Keywords
    "|([a-zA-Z_][a-zA-Z0-9_]*)" + // Identifiers
    "|(==|!=|<=|>=|<|>|=|\\+|\\-|\\*|\\/|\\(|\\)|\\[|\\]|,|\\{|\\}|:)" + // Operators & Symbols
    "|(-?[0-9]+(\\.[0-9]+)?)" + // Numbers (integers & floats)
    "|(\"(?:\\\\\"|[^\"])*\"|'(?:\\\\'|[^'])*')" // Strings
);

    //F2
    public static List<Token> tokenize(String inputCode, ErrorLogger logger) {
        List<Token> tokens = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        String[] lines = inputCode.split("\n");
        int lineNumber = 1;
    
        for (String line : lines) {
            line = removeComments(line);
            Matcher matcher = TOKEN_PATTERN.matcher(line);
            int lastMatchEnd = 0;
    
            while (matcher.find()) {
                if (matcher.start() > lastMatchEnd) {
                    String invalidToken = line.substring(lastMatchEnd, matcher.start()).trim();
                    if (!invalidToken.isEmpty()) {
                        tokens.add(new Token("INVALID", invalidToken, lineNumber));
                        errors.add("Unrecognized token: " + invalidToken + " at line " + lineNumber);
                    }
                }
            
                if (matcher.group(1) != null) {  
                    tokens.add(new Token("KEYWORD", matcher.group(1), lineNumber));
                } else if (matcher.group(2) != null) {  
                    tokens.add(new Token("IDENTIFIER", matcher.group(2), lineNumber));
                } else if (matcher.group(3) != null) {  
                    tokens.add(new Token("SYMBOL", matcher.group(3), lineNumber));
                } else if (matcher.group(4) != null) {  
                    tokens.add(new Token("NUMBER", matcher.group(4), lineNumber));
                } else if (matcher.group(6) != null) {  
                    tokens.add(handleString(matcher.group(6), lineNumber, logger));
                }
            
                lastMatchEnd = matcher.end();
            }
            
    
            if (lastMatchEnd < line.length()) {
                String invalidToken = line.substring(lastMatchEnd).trim();
                if (!invalidToken.isEmpty()) {
                    tokens.add(new Token("INVALID", invalidToken, lineNumber));
                    errors.add("Unrecognized token: " + invalidToken + " at line " + lineNumber);
                }
            }
    
            lineNumber++;
        }
    
        logTokenErrors(errors, logger);
        return tokens;
    }
    //F27
    public static Token handleString(String rawString, int lineNumber, ErrorLogger logger) {
        if (rawString.length() < 2) { // Invalid string
            logger.logError("Lexical Error: Invalid string at line " + lineNumber);
            return new Token("INVALID", rawString, lineNumber);
        }

        char quoteType = rawString.charAt(0); // Detect if it's a single or double quote
        if (rawString.charAt(rawString.length() - 1) != quoteType) { // Unclosed string
            logger.logError("Lexical Error: Unclosed string at line " + lineNumber);
            return new Token("INVALID", rawString, lineNumber);
        }

        // Extract the content inside the quotes
        String processedString = rawString.substring(1, rawString.length() - 1)
                                      .replace("\\\"", "\"")  // Handle escaped quotes
                                      .replace("\\'", "'");

        return new Token("STRING", processedString, lineNumber);
    }
    

    public static String removeComments(String line) {
        boolean insideString = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' || c == '\'') {
                insideString = !insideString;
            }
            if (c == '#' && !insideString) {
                break; // Ignore rest of the line after #
            }
            sb.append(c);
        }
        return sb.toString().trim();
    }
    //F3
    public static boolean isKeyword(String token) {
        return KEYWORDS.contains(token);
    }
    //F4
    public static boolean isIdentifier(String token) {
        return token.matches("[a-zA-Z_][a-zA-Z0-9_]*") && !isKeyword(token);
    }
    //F5
    public static boolean isNumber(String token) {
        return token.matches("[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?");
    }
    //F6
    public static void logTokenErrors(List<String> errors, ErrorLogger logger) {
        for (String error : errors) {
            logger.logError("Lexical Error: " + error);
        }
    }
    //F7
    public static Queue<Token> generateTokenStream(List<Token> tokens) {
        return new LinkedList<>(tokens);
    }
    //F8
    public static boolean detectInvalidToken(String token) {
        return !(isKeyword(token) || isIdentifier(token) || isNumber(token)) ;
    }
    //F9
    public static List<Token> recoverFromLexErrors(List<Token> tokens, ErrorLogger logger) {
        List<Token> validTokens = new ArrayList<>();
    
        for (Token token : tokens) {
            if (token.type.equals("INVALID")) {
                logger.logError("Recovered from lexical error: Removed invalid token '" + token.value + "' at line " + token.lineNumber);
                continue; // Skip adding invalid tokens
            }
    
            if (token.type.equals("IDENTIFIER") && token.value.matches("^[0-9].*")) {
                logger.logError("Recovered from lexical error: Removed invalid identifier '" + token.value + "' at line " + token.lineNumber);
                continue; // Skip invalid identifiers
            }
    
            validTokens.add(token);
        }
        
        return validTokens;
    }
}
