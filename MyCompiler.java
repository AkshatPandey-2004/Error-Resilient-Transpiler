import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MyCompiler {
    public static void main(String[] args) {
        try {
            // Check if input.py exists
            Path inputPath = Paths.get("input.py");
            if (!Files.exists(inputPath)) {
                System.err.println("Error: input.py not found!");
                return;
            }
            ErrorLogger logger = new ErrorLogger("error_log.txt");

            // Read input.py
            String code = new String(Files.readAllBytes(inputPath));

            // Step 1: Tokenization
            List<Token> tokens = Lexer.tokenize(code, logger);

            // Step 1.5: Recover from lexical errors
            tokens = Lexer.recoverFromLexErrors(tokens, logger);

            System.out.println("Tokens: " + tokens);

            if (tokens.isEmpty()) {
                System.err.println("Error: No tokens generated. Check input.py.");
                return;
            }

            // Step 2: Build the Parse Tree
            ParseTreeNode parseTree = Parser.buildParseTree(tokens, logger); // Pass the token list directly
            System.out.println("\nParse Tree:\n" + parseTree); // Debugging Output

            // Step 2.5: Recover from syntax errors
            parseTree = Parser.recoverFromSyntaxErrors(parseTree);
            System.out.println("\nParse Tree (After Recovery):\n" + parseTree);

            // Step 3: Convert Parse Tree to AST
            ASTNode ast = Parser.convertParseTreeToAST(parseTree);

            if (ast == null) {
                System.err.println("Parsing failed.");
                return;
            }

            // Step 4: Code Generation
            CodeGenerator.generateJavaCode(ast);

            System.out.println("Compilation successful! output.java generated.");
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
        }

        
    }
    public static void logSyntaxErrors(List<String> errors, ErrorLogger logger) {
            if (!errors.isEmpty()) {
                System.err.println("Syntax Errors:");
                for (String error : errors) {
                    logger.logError(error);
                }
            }
        }
        //F1 readSoiurceFile and Compile 
        public static String readsourcefile(String pythonCode) {
            try {
                ErrorLogger logger = new ErrorLogger("error_log.txt");
    
                // Step 1: Tokenization
                List<Token> tokens = Lexer.tokenize(pythonCode, logger);
                tokens = Lexer.recoverFromLexErrors(tokens, logger);
    
                if (tokens.isEmpty()) {
                    return "Error: No tokens generated. Check your input.";
                }
    
                // Step 2: Parsing
                ParseTreeNode parseTree = Parser.buildParseTree(tokens, logger);
                parseTree = Parser.recoverFromSyntaxErrors(parseTree);
    
                // Step 3: Convert Parse Tree to AST
                ASTNode ast = Parser.convertParseTreeToAST(parseTree);
                if (ast == null) {
                    return "Parsing failed. No AST generated.";
                }
    
                // Step 4: Code Generation
                return CodeGenerator.generateJavaCode(ast); // Now returns Java code as a String
            } catch (Exception e) {
                return "Compilation failed: " + e.getMessage();
            }
        }
}