import java.util.*;

class ParseTreeNode {
    String type;
    String value;
    List<ParseTreeNode> children;

    public ParseTreeNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ParseTreeNode child) {
        children.add(child);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildTreeString(sb, "", "");
        return sb.toString();
    }

    private void buildTreeString(StringBuilder sb, String prefix, String childPrefix) {
        sb.append(prefix);
        sb.append(type).append("(").append(value).append(")\n");
        for (int i = 0; i < children.size(); i++) {
            ParseTreeNode child = children.get(i);
            if (i < children.size() - 1) {
                child.buildTreeString(sb, childPrefix + "├── ", childPrefix + "│   ");
            } else {
                child.buildTreeString(sb, childPrefix + "└── ", childPrefix + "    ");
            }
        }
    }
}

class ASTNode {
    String type;
    String value;
    List<ASTNode> children;

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public String toString() {
        return type + "(" + value + ")";
    }
}

public class Parser {
    private List<Token> tokens;
    private int index = 0;
    private ErrorLogger errorLogger;
    private List<String> syntaxErrors = new ArrayList<>();

    public Parser(List<Token> tokens, ErrorLogger errorLogger) {
        this.tokens = tokens;
        this.errorLogger = errorLogger;
    }
    //F10
    public static ParseTreeNode buildParseTree(List<Token> tokens, ErrorLogger errorLogger) {
        Parser parser = new Parser(tokens, errorLogger);
        ParseTreeNode tree;
        if (parser.index < tokens.size() && tokens.get(parser.index).type.equals("KEYWORD_DEF")) {
            tree = parser.parseFunctionTree();
        } else {
            tree = parser.parseScript();
        }
        if (!detectUnmatchedBraces(tree)) {
            System.err.println("Compilation error: unmatched parentheses.");
        }
        return recoverFromSyntaxErrors(tree);
    }

    private ParseTreeNode parseScript() {
        ParseTreeNode scriptNode = new ParseTreeNode("Script", "script");
        while (index < tokens.size()) {
            Token currentToken = tokens.get(index);
    
            // Check if it's a function definition
            if (currentToken.type.equals("KEYWORD_DEF")) {
                scriptNode.addChild(parseFunctionTree());
            } else {
                scriptNode.addChild(parseStatement());
            }
        }
        return scriptNode;
    }

    public List<String> getSyntaxErrors() {
        return syntaxErrors;
    }
    //F26
    private ParseTreeNode parseFunctionTree() {
        System.out.println("Parsing function definition...");
    
        match("KEYWORD", "def");  // Consume 'def'
        Token funcName = match("IDENTIFIER");  // Consume function name
        match("SYMBOL", "(");  // Consume '('
    
        List<String> parameters = new ArrayList<>();
        while (index < tokens.size() && !tokens.get(index).value.equals(")")) {
            Token param = match("IDENTIFIER");
            parameters.add(param.value);
            if (index < tokens.size() && tokens.get(index).value.equals(",")) {
                match("SYMBOL", ",");  // Consume ','
            }
        }
    
        ParseTreeNode funcNode = new ParseTreeNode("Function", funcName.value);
        match("SYMBOL", ")");  // Consume ')'
        match("SYMBOL", ":");  // Consume ':'
    
        // Add function parameters as children
        for (String param : parameters) {
            funcNode.addChild(new ParseTreeNode("Parameter", param));
        }
    
        // ✅ Fix: Ensure that function body is properly consumed
        while (index < tokens.size() && !tokens.get(index).value.equals("def")) {
            try {
                ParseTreeNode stmt = parseStatement();
                if (stmt != null) {
                    funcNode.addChild(stmt);
                }
            } catch (RuntimeException e) {
                System.err.println("Error parsing function body: " + e.getMessage());
    
                // ✅ Fix: Skip tokens to prevent infinite loops
                while (index < tokens.size() && !tokens.get(index).value.equals("def") 
                       && !tokens.get(index).value.equals("\n")) {
                    index++;  // Move past the problematic tokens
                }
    
                if (index < tokens.size()) {
                    index++;  // Move to next token after newline
                }
            }
        }
    
        System.out.println("Function parsed successfully.");
        return funcNode;
    }

    private ParseTreeNode parseStatement() {
        if (index >= tokens.size()) {
            return null; // End of file
        }
    
        Token currentToken = tokens.get(index);
        System.out.println("Current token: " + currentToken);
    
        try {
            if (currentToken.type.equals("KEYWORD") && currentToken.value.equals("print")) {
                return parsePrintTree();
            } else if (currentToken.type.equals("IDENTIFIER")) {
                return parseAssignmentTree();
            } else if (currentToken.type.equals("KEYWORD") && currentToken.value.equals("if")) {
                return parseIfStatementTree();
            } else if (currentToken.type.equals("KEYWORD") && currentToken.value.equals("for")) {
                return parseForLoopTree();
            } else if (currentToken.type.equals("KEYWORD") && currentToken.value.equals("while")) {
                return parseWhileLoopTree();
            } else if (currentToken.type.equals("KEYWORD") && currentToken.value.equals("def")) { 
                return parseFunctionTree();  // ✅ Fix: Now `def` will be parsed correctly
            } 
            else {
                throw new RuntimeException("Syntax Error: Unexpected token in statement: " + currentToken);
            }
    
            // Unexpected token - likely a syntax error
        } catch (RuntimeException e) {
            System.err.println("Error parsing statement: " + e.getMessage());
    
            // Skip tokens until the next valid statement (newline or semicolon)
            while (index < tokens.size() && !tokens.get(index).value.equals("\n") && !tokens.get(index).value.equals(";")) {
                index++;
            }
            if (index < tokens.size() && (tokens.get(index).value.equals("\n") || tokens.get(index).value.equals(";"))) {
                index++;
            }
            return new ParseTreeNode("Error", "Error");
        }
    }

    private ParseTreeNode parseForLoopTree() {
        System.out.println("Parsing for loop...");
    
        match("KEYWORD", "for");
        Token varName = match("IDENTIFIER");  // Loop variable (e.g., `i`)
    
        match("KEYWORD", "in");  // Ensure "in" is recognized
        match("KEYWORD", "range");  // Ensure `range` is correctly matched
        match("SYMBOL", "(");  // Consume '('
    
        ParseTreeNode forNode = new ParseTreeNode("ForLoop", "for");
        forNode.addChild(new ParseTreeNode("Variable", varName.value));  // Loop variable
    
        // ✅ Allow IDENTIFIER (variable) or NUMBER inside range()
        Token start = null, end = null, step = null;
    
        if (index < tokens.size() && 
            (tokens.get(index).type.equals("NUMBER") || tokens.get(index).type.equals("IDENTIFIER"))) {
            start = match(tokens.get(index).type);
        } else {
            start = new Token("NUMBER", "0", -1);  // Default start to 0 if omitted
        }
    
        // ✅ Check for an end value
        if (index < tokens.size() && tokens.get(index).value.equals(",")) {
            match("SYMBOL", ",");
            if (index < tokens.size() && 
                (tokens.get(index).type.equals("NUMBER") || tokens.get(index).type.equals("IDENTIFIER"))) {
                end = match(tokens.get(index).type);
            } else {
                errorLogger.logError("Syntax Error: Invalid end value in range()");
            }
        } else {
            end = start;  // If only one value is provided, it’s the end, and start should be 0
            start = new Token("NUMBER", "0", -1);
        }
    
        // ✅ Check for a step value
        if (index < tokens.size() && tokens.get(index).value.equals(",")) {
            match("SYMBOL", ",");
            if (index < tokens.size() && 
                (tokens.get(index).type.equals("NUMBER") || tokens.get(index).type.equals("IDENTIFIER"))) {
                step = match(tokens.get(index).type);
            } else {
                errorLogger.logError("Syntax Error: Invalid step value in range()");
            }
        }
    
        match("SYMBOL", ")");  // Consume ')'
        match("SYMBOL", ":");  // Consume ':'
    
        // ✅ Wrap range values in a proper "Range" node
        ParseTreeNode rangeNode = new ParseTreeNode("Range", "range");
        rangeNode.addChild(new ParseTreeNode("RangeStart", start.value));
        rangeNode.addChild(new ParseTreeNode("RangeEnd", end.value));
        if (step != null) {
            rangeNode.addChild(new ParseTreeNode("RangeStep", step.value));
        }
        forNode.addChild(rangeNode);  // ✅ Attach range info to loop
    
        // ✅ Parse loop body
        while (index < tokens.size()) {
            Token currentToken = tokens.get(index);
            if (currentToken.value.equals("\n")) {
                index++;
                if (index >= tokens.size()) break;
                currentToken = tokens.get(index);
            }
    
            // ✅ Stop parsing if a new function/class starts
            if (currentToken.type.equals("KEYWORD") &&
                (currentToken.value.equals("def") || currentToken.value.equals("class") ||
                 currentToken.value.equals("for") || currentToken.value.equals("while") ||
                 currentToken.value.equals("if"))) {
                break;
            }
    
            forNode.addChild(parseStatement());  // ✅ Continue parsing loop body
        }
    
        System.out.println("For loop parsed successfully.");
        return forNode;
        
    }
    private ParseTreeNode parseWhileLoopTree() {
        match("KEYWORD", "while");
        ParseTreeNode condition = parseExpressionTree();
        match("SYMBOL", ":");

        ParseTreeNode whileNode = new ParseTreeNode("WhileLoop", "while");
        whileNode.addChild(condition);

        while (index < tokens.size() && !tokens.get(index).value.equals("for") && !tokens.get(index).value.equals("while") && !tokens.get(index).value.equals("if")) {
            whileNode.addChild(parseStatement());
        }
        return whileNode;
    }

    private ParseTreeNode parsePrintTree() {
        match("KEYWORD", "print");
        match("SYMBOL", "(");

        ParseTreeNode printNode = new ParseTreeNode("Print", "print");
        if (index < tokens.size()) {
            printNode.addChild(parseExpressionTree()); // ✅ Parse full expressions, not just one token
        }
        if (index < tokens.size() && tokens.get(index).type.equals("STRING")) {
            printNode.addChild(new ParseTreeNode("StringLiteral", tokens.get(index).value));
            match("STRING");
        } else if (index < tokens.size() && tokens.get(index).type.equals("IDENTIFIER")) {
            String error = "Syntax Error: Missing ')' in print statement at line " + tokens.get(index - 1).lineNumber;
            errorLogger.logError(error);
            System.out.println(suggestCorrections(error, tokens.get(index - 1).lineNumber));
            printNode.addChild(new ParseTreeNode("VariableReference", tokens.get(index).value));
            match("IDENTIFIER");
        }

        if (index < tokens.size() && tokens.get(index).value.equals(")")) {
            match("SYMBOL", ")");
        } else {
            System.out.println("Recovered from missing ')'. Added closing parenthesis.");
        }
        return printNode;
    }
    private ParseTreeNode parseListLiteral() {
        match("SYMBOL", "[");
        ParseTreeNode listNode = new ParseTreeNode("List", "list");
        while (index < tokens.size() && !tokens.get(index).value.equals("]")) {
            if (tokens.get(index).type.equals("NUMBER") || tokens.get(index).type.equals("STRING") || tokens.get(index).type.equals("IDENTIFIER")) {
                listNode.addChild(new ParseTreeNode("Value", tokens.get(index).value));
                match(tokens.get(index).type);
            }
            if (index < tokens.size() && tokens.get(index).value.equals(",")) {
                match("SYMBOL", ",");
            }
        }
        match("SYMBOL", "]");
        return listNode;
    }
    private Set<String> declaredVariables = new HashSet<>();
    private ParseTreeNode parseAssignmentTree() {
        System.out.println("parseAssignmentTree: Current token: " + tokens.get(index));
    
        Token varName = match("IDENTIFIER"); // Match the variable name
        match("SYMBOL", "="); // Match the '=' symbol
        
        declaredVariables.add(varName.value);

        if (index >= tokens.size()) {
            System.out.println("Recovered from missing value in assignment. Assigned 'None'.");
            return new ParseTreeNode("Assignment", varName.value);
        }
    
        ParseTreeNode valueNode;
    
        if (tokens.get(index).type.equals("STRING")) { 
            // ✅ Handle string literals correctly
            Token stringToken = match("STRING");
            valueNode = new ParseTreeNode("StringLiteral", stringToken.value);
        } else if (tokens.get(index).value.equals("[")) {  
            // ✅ Handle list assignment
            valueNode = parseListLiteral();
        } else if (tokens.get(index).value.equals("-")) {
            // ✅ Handle negative numbers correctly
            match("SYMBOL", "-");  // Consume the '-' symbol
            
            if (index < tokens.size() && tokens.get(index).type.equals("NUMBER")) {
                Token numberToken = match("NUMBER");
                valueNode = new ParseTreeNode("Value", "-" + numberToken.value);
            } else {
                throw new RuntimeException("Syntax Error: Expected a number after '-' at line " + tokens.get(index).lineNumber);
            }
        } else {
            // ✅ Handle numbers and identifiers properly
            Token valueToken = match(tokens.get(index).type);
            valueNode = new ParseTreeNode("Value", valueToken.value);
        }
    
        // ✅ Construct assignment node
        ParseTreeNode assignNode = new ParseTreeNode("Assignment", varName.value);
        assignNode.addChild(valueNode);
        return assignNode;
    }

    private ParseTreeNode parseIfStatementTree() {
        match("KEYWORD", "if");
    
        // ✅ Ensure condition is parsed correctly
        ParseTreeNode conditionNode = parseExpressionTree();
        match("SYMBOL", ":");
    
        // ✅ Create the If node
        ParseTreeNode ifNode = new ParseTreeNode("IfStatement", "if");
        ifNode.addChild(conditionNode);
    
        // ✅ Parse if-block body
        while (index < tokens.size()) {
            Token currentToken = tokens.get(index);
    
            // ✅ Stop parsing if we hit elif or else (they are not part of this block)
            if (currentToken.type.equals("KEYWORD") && 
                (currentToken.value.equals("elif") || currentToken.value.equals("else"))) {
                break;
            }
    
            ifNode.addChild(parseStatement());
        }
    
        // ✅ Handle elif cases
        while (index < tokens.size() && tokens.get(index).value.equals("elif")) {
            match("KEYWORD", "elif");
            ParseTreeNode elifCondition = parseExpressionTree();
            match("SYMBOL", ":");
            ParseTreeNode elifNode = new ParseTreeNode("ElifStatement", "elif");
            elifNode.addChild(elifCondition);
    
            // ✅ Parse elif-block body correctly
            while (index < tokens.size()) {
                Token currentToken = tokens.get(index);
    
                // ✅ Stop parsing elif block if we hit another elif or else
                if (currentToken.type.equals("KEYWORD") && 
                    (currentToken.value.equals("elif") || currentToken.value.equals("else"))) {
                    break;
                }
    
                elifNode.addChild(parseStatement());
            }
    
            ifNode.addChild(elifNode);
        }
    
        // ✅ Handle else case
        if (index < tokens.size() && tokens.get(index).value.equals("else")) {
            match("KEYWORD", "else");
            match("SYMBOL", ":");
            ParseTreeNode elseNode = new ParseTreeNode("ElseStatement", "else");
    
            // ✅ Parse else-block body correctly
            while (index < tokens.size()) {
                Token currentToken = tokens.get(index);
    
                // ✅ Stop parsing if we hit a new function or loop declaration
                if (currentToken.type.equals("KEYWORD") && 
                    (currentToken.value.equals("def") || currentToken.value.equals("for") ||
                     currentToken.value.equals("while") || currentToken.value.equals("if"))) {
                    break;
                }
    
                elseNode.addChild(parseStatement());
            }
    
            ifNode.addChild(elseNode);
        }
    
        return ifNode;
    }
    
    //F24
    private ParseTreeNode parseExpressionTree() {
        ParseTreeNode leftOperand;
    
        // ✅ Handle string literals first
        if (tokens.get(index).type.equals("STRING")) {
            leftOperand = new ParseTreeNode("StringLiteral", tokens.get(index).value);
            match("STRING");
        } 
        // ✅ Handle variables or numbers
        else if (tokens.get(index).type.equals("IDENTIFIER") || tokens.get(index).type.equals("NUMBER")) {
            String varName = tokens.get(index).value;
            if (!declaredVariables.contains(varName)) {
                String suggestion = resolveUndefinedVariable(varName);
                String err= "⚠️ Warning: Undefined variable '" + varName + "'. Did you mean '" + suggestion + "'?";
                errorLogger.logSemanticError(err);
            }
            leftOperand = new ParseTreeNode("Operand", tokens.get(index).value);
            match(tokens.get(index).type);
        } 
        else {
            throw new RuntimeException("Syntax Error: Expected identifier, number, or string at line " + tokens.get(index).lineNumber);
        }
    
        // ✅ Check for operators (comparison or arithmetic)
        while (index < tokens.size() && tokens.get(index).type.equals("SYMBOL")) {
            String operator = tokens.get(index).value;
    
            // ✅ Check if the operator is a valid comparison operator
            if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || 
                operator.equals("<=") || operator.equals("==") || operator.equals("!=")) {
                match("SYMBOL");  // Consume operator
    
                // ✅ Parse the right operand
                if (tokens.get(index).type.equals("IDENTIFIER") || tokens.get(index).type.equals("NUMBER")) {
                    ParseTreeNode rightOperand = new ParseTreeNode("Operand", tokens.get(index).value);
                    match(tokens.get(index).type);
    
                    // ✅ Create an Expression Node for the comparison
                    ParseTreeNode operatorNode = new ParseTreeNode("Expression", operator);
                    operatorNode.addChild(leftOperand);
                    operatorNode.addChild(rightOperand);
                    leftOperand = operatorNode;  // Chain comparisons
                } else {
                    throw new RuntimeException("Syntax Error: Expected identifier or number after `" + operator + "` at line " + tokens.get(index).lineNumber);
                }
            } 
            // ✅ Check for string concatenation (`+` operator)
            else if (operator.equals("+")) {
                match("SYMBOL"); 
    
                if (tokens.get(index).type.equals("STRING") || tokens.get(index).type.equals("IDENTIFIER") || tokens.get(index).type.equals("NUMBER")) {
                    ParseTreeNode rightOperand = new ParseTreeNode("Operand", tokens.get(index).value);
                    match(tokens.get(index).type);
    
                    ParseTreeNode operatorNode = new ParseTreeNode("Expression", operator);
                    operatorNode.addChild(leftOperand);
                    operatorNode.addChild(rightOperand);
                    leftOperand = operatorNode;
                } else {
                    throw new RuntimeException("Syntax Error: Expected string, identifier, or number after `+` at line " + tokens.get(index).lineNumber);
                }
            } 
            else {
                break;  // ✅ No valid operator found, exit loop
            }
        }
    
        return leftOperand;
    }
    //F27
    private String resolveUndefinedVariable(String unknownVar) {
        String closestMatch = null;
        int minDistance = Integer.MAX_VALUE;
    
        for (String declaredVar : declaredVariables) {
            int distance = levenshteinDistance(unknownVar, declaredVar);
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = declaredVar;
            }
        }
        
        return (closestMatch != null) ? closestMatch : "No suggestion available";
    }
    
    // ✅ Helper function to calculate Levenshtein distance
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
    
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } 
                else if (j == 0) {
                    dp[i][j] = i;
                } 
                else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1), 
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private Token match(String expectedType, String... expectedValues) {
        if (index < tokens.size()) {
            Token currentToken = tokens.get(index);
            System.out.println("Parsing Token: " + currentToken);
    
            if (currentToken.type.equals(expectedType)) {
                if (expectedValues.length == 0 || Arrays.asList(expectedValues).contains(currentToken.value)) {
                    index++;
                    return currentToken;
                } else {
                    String error = "Syntax Error: Expected " + Arrays.toString(expectedValues) + ", but found " + currentToken.value;
                    errorLogger.logError(error);
                    System.out.println(suggestCorrections(error, currentToken.lineNumber));
                    // Do not increment the index here
                    return currentToken;
                }
            } else {
                String error = "Syntax Error: Expected " + expectedType + ", but found " + currentToken.type + " (" + currentToken.value + ")";
                errorLogger.logError(error);
                System.out.println(suggestCorrections(error, currentToken.lineNumber));
                // Do not increment the index here
                return currentToken;
            }
        }
        String error = "Syntax Error: Unexpected end of tokens. Expected " + expectedType;
        errorLogger.logError(error);
        System.out.println(suggestCorrections(error, -1));
        return null;
    }
    //F11
    public static ParseTreeNode recoverFromSyntaxErrors(ParseTreeNode tree) {
        Queue<ParseTreeNode> queue = new LinkedList<>();
        queue.add(tree);

        while (!queue.isEmpty()) {
            ParseTreeNode node = queue.poll();

            if (node.type.equals("Function")) {
                boolean colonFound = false;
                for (ParseTreeNode child : node.children) {
                    if (child.type.equals("Symbol") && child.value.equals(":")) {
                        colonFound = true;
                        break;
                    }
                }
                if (!colonFound) {
                    System.out.println("Recovered from missing ':'. Added ':' to function definition.");
                    node.children.add(new ParseTreeNode("Symbol", ":"));
                }
            }

            if (node.type.equals("Print")) {
                boolean closingParenFound = false;
                for (ParseTreeNode child : node.children) {
                    if (child.type.equals("Symbol") && child.value.equals(")")) {
                        closingParenFound = true;
                        break;
                    }
                }
                if (!closingParenFound) {
                    System.out.println("Recovered from missing ')'. Added closing parenthesis.");
                    node.children.add(new ParseTreeNode("Symbol", ")"));
                }
            }
            queue.addAll(node.children);
        }
        return tree;
    }
    //F12
    public static ASTNode convertParseTreeToAST(ParseTreeNode root) {
        if (root == null) {
            return null;
        }
        ASTNode astRoot = new ASTNode(root.type, root.value);

        if (root.type.equals("ForLoop")) {
            ASTNode forNode = new ASTNode("ForLoop", "for");
            for (ParseTreeNode child : root.children) {
                forNode.addChild(convertParseTreeToAST(child));
            }
            astRoot = forNode;
        } else if (root.type.equals("WhileLoop")) {
            ASTNode whileNode = new ASTNode("WhileLoop", "while");
            for (ParseTreeNode child : root.children) {
                whileNode.addChild(convertParseTreeToAST(child));
            }
            astRoot = whileNode;
        } else if (root.type.equals("IfStatement")) {
            ASTNode ifNode = new ASTNode("IfStatement", "if");
            ifNode.addChild(convertParseTreeToAST(root.children.get(0)));
            for (int i = 1; i < root.children.size(); i++) {
                ifNode.addChild(convertParseTreeToAST(root.children.get(i)));
            }
            astRoot = ifNode;
        } else if (root.type.equals("Operator")) {
            ASTNode operatorNode = new ASTNode("Comparison", root.value);
            operatorNode.addChild(convertParseTreeToAST(root.children.get(0)));
            operatorNode.addChild(convertParseTreeToAST(root.children.get(1)));
            astRoot = operatorNode;
        } else if (root.type.equals("StringLiteral") || root.type.equals("Operand") || root.type.equals("Value") || root.type.equals("Variable") || root.type.equals("RangeEnd")) {
            astRoot = new ASTNode(root.type, root.value);
        } else if (root.type.equals("Print")) {
            ASTNode printNode = new ASTNode(root.type, root.value);
            for (ParseTreeNode child : root.children) {
                if (!child.type.equals("Symbol")) {
                    printNode.addChild(convertParseTreeToAST(child));
                }
            }
            astRoot = printNode;
        } else {
            for (ParseTreeNode child : root.children) {
                ASTNode astChild = convertParseTreeToAST(child);
                if (astChild != null) {
                    astRoot.addChild(astChild);
                }
            }
        }
        return astRoot;
    }
    //F13
    public static boolean detectUnmatchedBraces(ParseTreeNode tree) {
        int openParens = 0;
        int closeParens = 0;
        Queue<ParseTreeNode> queue = new LinkedList<>();
        queue.add(tree);

        while (!queue.isEmpty()) {
            ParseTreeNode node = queue.poll();

            if (node.type.equals("Symbol")) {
                if (node.value.equals("(")) {
                    openParens++;
                } else if (node.value.equals(")")) {
                    closeParens++;
                }
            }
            queue.addAll(node.children);
        }

        if (openParens != closeParens) {
            System.err.println("Syntax Error: Unmatched parentheses. Expected '(' = " + openParens + ", ')' = " + closeParens);
            return false;
        }
        return true;
    }
    //F14
    public static String suggestCorrections(String error, int lineNumber) {
        if (error.contains("Unexpected token KEYWORD_DEF")) {
            return "Missing ':' at line " + lineNumber;
        }
        if (error.contains("Unexpected token return")) {
            return "Missing ':' at line " + (lineNumber - 1);
        }
        if (error.contains("Unexpected token for")) {
            return "Missing ':' at line " + lineNumber;
        }
        if (error.contains("Unexpected token if")) {
            return "Missing ':' at line " + lineNumber;
        }
        if (error.contains("Unexpected token print")) {
            return "Missing '(' at line " + lineNumber;
        }
        if (error.contains("Unmatched parentheses")) {
            return "Missing ')' at line " + lineNumber;
        }
        return "Syntax Error at line " + lineNumber;
    }
}