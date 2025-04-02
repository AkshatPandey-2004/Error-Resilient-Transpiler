import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;


public class ErrorLogger {
    private PrintWriter writer;

    public ErrorLogger(String logFilePath) {
        try {
            this.writer = new PrintWriter(new FileWriter(logFilePath, true)); // true for append
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
        }
    }
    //F15
    public void logSyntaxError(int lineNumber, String message) {
        String formattedMessage = "Syntax Error at line " + lineNumber + ": " + message;
        System.err.println(formattedMessage); // Print error to console
        logError(formattedMessage); // Log the error using existing logError method
    }
    //F28
    public void logSemanticError( String message) {
        String formattedMessage = "Semantic Error:" + message;
        System.err.println(formattedMessage); // Print error to console
        logError(formattedMessage); // Log the error in the file
    }

    public void logError(String message) {
        if (writer != null) {
            writer.println("------ Error Entry ------");
            writer.println("Timestamp: " + new Date()); // Add timestamp for better tracking
            writer.println(message);
            writer.println("-------------------------");
            writer.flush(); // Ensure immediate write
        }
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}