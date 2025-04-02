import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeGenerator {
    private static final Set<String> declaredVariables =new HashSet<>();
    //F16
    public static String generateJavaCode(ASTNode ast) throws IOException {
        StringBuilder javaCode = new StringBuilder();
        declaredVariables.clear(); 
        // Add Java Imports
        javaCode=addJavaImports(javaCode);

        // Class Declaration
        javaCode.append("public class output {\n");
        javaCode.append("    public static void main(String[] args) {\n");

        // Translate AST to Java Code
        translateAST(ast, javaCode);

        // Close main method and class
        javaCode.append("    }\n");
        javaCode.append("}\n");

        return javaCode.toString(); // ✅ Return Java Code as String
    }
    //F17
    private static StringBuilder addJavaImports(StringBuilder javaCode) {
        return javaCode.append("import java.util.*;\n\n");
    }
    private static void translateAST(ASTNode ast, StringBuilder javaCode) {
        if (ast.type.equals("Script")) {
            for (ASTNode child : ast.children) {
                translateAST(child, javaCode);
            }
        } else if (ast.type.equals("Function")) {  
            generateFunction(ast, javaCode);
        } else if (ast.type.equals("ForLoop") || ast.type.equals("WhileLoop")) {
            generateLoops(ast, javaCode);
        } else if (ast.type.equals("IfStatement")) {
            generateIfStatement(ast, javaCode);
        } else if (ast.type.equals("Assignment")) {
            generateAssignment(ast, javaCode);
        } else if (ast.type.equals("Print")) {
            generatePrint(ast, javaCode);
        } else {
            for (ASTNode child : ast.children) {
                translateAST(child, javaCode);
            }
            
            switch (ast.type) {
                case "Comparison":
                case "Operand":
                case "Value":
                case "StringLiteral":
                case "List":
                    break;
                default:
                    System.err.println("⚠ Unrecognized AST node: " + ast.type);
            }
        }
    }
    
    //F18
    private static void generateLoops(ASTNode ast, StringBuilder javaCode) {
        if (ast.type.equals("ForLoop")) {
            generateForLoop(ast, javaCode);
        } else if (ast.type.equals("WhileLoop")) {
            generateWhileLoop(ast, javaCode);
        }
    }

    private static void generateForLoop(ASTNode ast, StringBuilder javaCode) {
        if (ast.children.size() < 2) {
            System.err.println("⚠ Error: ForLoop node has insufficient children.");
            return;
        }

        ASTNode loopVar = ast.children.get(0);
        ASTNode iterable = ast.children.get(1);

        ASTNode rangeStart = null, rangeEnd = null, rangeStep = null;

        for (ASTNode child : iterable.children) {
            switch (child.type) {
                case "RangeStart":
                    rangeStart = child;
                    break;
                case "RangeEnd":
                    rangeEnd = child;
                    break;
                case "RangeStep":
                    rangeStep = child;
                    break;
            }
        }

        if (rangeEnd != null) {
            String start = (rangeStart != null) ? rangeStart.value : "0";
            String end = rangeEnd.value;
            String step = (rangeStep != null) ? rangeStep.value : "1";

            javaCode.append("        for (int ").append(loopVar.value)
                    .append(" = ").append(start).append("; ")
                    .append(loopVar.value).append(" < ").append(end)
                    .append("; ").append(loopVar.value).append(" += ")
                    .append(step).append(") {\n");

            for (int i = 2; i < ast.children.size(); i++) {
                javaCode.append("     ");
                translateAST(ast.children.get(i), javaCode);
            }

            javaCode.append("        }\n");
        } else {
            System.err.println("⚠ Error: Invalid range() arguments.");
        }
    }
    

    private static void generateWhileLoop(ASTNode ast, StringBuilder javaCode) {
        javaCode.append("        while (").append(translateExpression(ast.children.get(0))).append(") {\n");

        for (int i = 1; i < ast.children.size(); i++) {
            translateAST(ast.children.get(i), javaCode);
        }
        javaCode.append("        }\n");
    }
    //F19
    private static void generateFunction(ASTNode ast, StringBuilder javaCode) {
        List<String> parameters = new ArrayList<>();
    
        // ✅ Extract function parameters
        for (ASTNode child : ast.children) {
            if (child.type.equals("Parameter")) {
                parameters.add("Object " + child.value); // Assuming all parameters are Object
            }
        }
    
        if (ast.value.equals("main")) {
            javaCode.append("    public static void main(String[] args) {\n");
        } else {
            javaCode.append("    public static void ").append(ast.value)
                    .append("(").append(String.join(", ", parameters)).append(") {\n");
        }
    
        // ✅ Generate function body
        for (ASTNode child : ast.children) {
            if (!child.type.equals("Parameter")) {  // ✅ Skip parameters in function body
                javaCode.append("        ");
                translateAST(child, javaCode);
            }
        }
    
        javaCode.append("    }\n\n");
    }
    
    //F20
    private static void generateIfStatement(ASTNode ast, StringBuilder javaCode) {
        String condition = translateExpression(ast.children.get(0));
        javaCode.append("        if (").append(condition).append(") {\n");

        for (int i = 1; i < ast.children.size(); i++) {
            ASTNode child = ast.children.get(i);

            if (child.type.equals("ElifStatement")) {  // ✅ Correctly handling `elif`
                String elifCondition = translateExpression(child.children.get(0));
                javaCode.append("        } else if (").append(elifCondition).append(") {\n");
                for (int j = 1; j < child.children.size(); j++) { // ✅ Process elif body
                    javaCode.append("            ");  // ✅ Apply correct indentation
                    translateAST(child.children.get(j), javaCode);
                }
            } else if (child.type.equals("ElseStatement")) {
                javaCode.append("        } else {\n");
                for (ASTNode elseChild : child.children) { // ✅ Process all statements inside else block
                    javaCode.append("            ");  // ✅ Apply correct indentation
                    translateAST(elseChild, javaCode);
                }
            } else {
                javaCode.append("            ");
                translateAST(child, javaCode);
            }
        }

        javaCode.append("        }\n");
    }

    
    //F25
    private static void generateAssignment(ASTNode ast, StringBuilder javaCode) {
        if (ast.children.isEmpty()) {
            System.err.println("⚠ Error: Assignment node has no value.");
            return;
        }

        ASTNode valueNode = ast.children.get(0);
        String variableName = ast.value;
        String inferredType = inferDataType(valueNode);

        String javaType = mapDataTypes(inferredType);
        String javaValue = formatJavaValue(valueNode);

        if (declaredVariables.contains(variableName)) {
            javaCode.append("        ").append(variableName).append(" = ").append(javaValue).append(";\n");
        } else {
            declaredVariables.add(variableName);
            javaCode.append("        ").append(javaType).append(" ").append(variableName)
                    .append(" = ").append(javaValue).append(";\n");
        }
    }

    private static String inferListElementType(ASTNode listNode) {
        if (listNode.children == null || listNode.children.isEmpty()) {
            return "Object"; // Default if list is empty
        }
        return inferDataType(listNode.children.get(0).value); // Basic inference of first element
    }

    private static String inferDataType(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) return "String";
        if (value.equals("true") || value.equals("false")) return "boolean";
    
        // Check if it's a number
        if (value.matches("-?\\d+")) return "int"; // Integer
        if (value.matches("-?\\d+\\.\\d+")) return "double"; // Float
    
        return "Object"; // Default fallback
    }
    //F21
    private static void generatePrint(ASTNode ast, StringBuilder javaCode) {
        if (ast.children.isEmpty()) {
            javaCode.append("        System.out.println();\n");
            return;
        }
    
        ASTNode valueNode = ast.children.get(0);
        
        // Check if it's an expression (operator node)
        if (valueNode.type.equals("Expression")) {
            javaCode.append("        System.out.println(").append(translateExpression(valueNode)).append(");\n");
        } else {
            javaCode.append("        System.out.println(").append(formatJavaValue(valueNode)).append(");\n");
        }
    }
    //F22
    private static String inferDataType(ASTNode valueNode) {
        if (valueNode == null) return "Object"; // Handle null case
    
        switch (valueNode.type) {
            case "StringLiteral":
                return "String"; // Ensure proper type mapping for strings
            case "Value":
                if (valueNode.value.equals("true") || valueNode.value.equals("false")) return "boolean";
                if (valueNode.value.matches("-?\\d+")) return "int"; // Integer
                if (valueNode.value.matches("-?\\d+\\.\\d+")) return "double"; // Float
                return "String"; // Fallback to String if it's enclosed in quotes
            case "List":
                return "list";
            default:
                return "Object"; // Unknown type
        }
    }
    
    private static String formatJavaValue(ASTNode node) {
        if (node == null) return "null";

        switch (node.type) {
            case "StringLiteral":
                return "\"" + node.value + "\"";
            case "Value":
                return node.value;
            case "List":
                return generateListValue(node);
            default:
                return node.value;
        }
    }
    private static String generateListValue(ASTNode listNode) {
    if (listNode.children == null || listNode.children.isEmpty()) {
        return "Arrays.asList()"; // Empty list
    }

    List<String> elements = new ArrayList<>();
    for (ASTNode child : listNode.children) {
        elements.add(formatJavaValue(child)); // Format each element correctly
    }

    return "Arrays.asList(" + String.join(", ", elements) + ")";
}

private static String translateExpression(ASTNode node) {
    if (node.type.equals("Expression")) {
        return translateExpression(node.children.get(0)) + " " + node.value + " " + translateExpression(node.children.get(1));
    }
    if (node.type.equals("StringLiteral")) {
        return "\"" + node.value + "\"";
    }
    return node.value;
}
    //F23
    private static String mapDataTypes(String pythonType) {
        switch (pythonType) {
            case "int": return "int";
            case "double": return "double";
            case "String": return "String"; // Fix: Ensure String is properly mapped
            case "boolean": return "boolean";
            case "list": return "List<Object>"; // Default generic list
            default: return "Object"; // Unknown types
        }
    }

    @SuppressWarnings("unused")
    private static String mapDataTypes(String pythonType, String elementPythonType) {
        if (pythonType == null || pythonType.trim().isEmpty()) {
            return "Object";
        }

        pythonType = pythonType.trim().toLowerCase();

        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("int", "int");
        typeMap.put("float", "double");
        typeMap.put("str", "String");
        typeMap.put("bool", "boolean");
        typeMap.put("bytes", "byte[]"); // Added bytes mapping

        if (typeMap.containsKey(pythonType)) {
            return typeMap.get(pythonType);
        }

        if (pythonType.equals("list")) {
            String elementType = (elementPythonType != null) ? mapDataTypes(elementPythonType) : "Object";
            return "List<" + elementType + ">";
        }

        return "Object";
    }
}