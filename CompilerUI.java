import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CompilerUI {
    private JTextArea pythonCodeArea, javaCodeArea, consoleArea, errorLogArea;

    public CompilerUI() {
        // Create main frame
        JFrame frame = new JFrame("🚀 Python to Java Compiler");
        frame.setSize(950, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center window
        frame.setResizable(true);  // Enable resizing
    
        // 🎨 Dark theme colors
        Color bgColor = new Color(30, 34, 45);  // Dark background
        Color textColor = Color.WHITE;          // White text
        Color outputColor = new Color(80, 200, 120); // Green text for output
        Color buttonColor = new Color(128, 0, 128); // Purple button color
        Color borderColor = new Color(100, 100, 120); // Border
    
        // 🌟 Main Panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // 📌 Labels
        JLabel headerLabel = new JLabel("Error-Resilient Python to Java Transpiler", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Slightly smaller font
    
        // Make sure header label is centered in its container
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        JLabel inputLabel = new JLabel("📥 Input Python Code");
        JLabel outputLabel = new JLabel("📤 Converted Java Code");
    
        inputLabel.setForeground(Color.WHITE);
        outputLabel.setForeground(outputColor);
    
        // 📝 Text Areas (Code Editor Style)
        pythonCodeArea = createTextArea(bgColor, textColor, 14);
        javaCodeArea = createTextArea(bgColor, outputColor, 16);
        javaCodeArea.setEditable(false);
    
        // 🎚 Scroll Panes with modern styling
        JScrollPane inputScroll = createScrollPane(pythonCodeArea);
        JScrollPane outputScroll = createScrollPane(javaCodeArea);
    
        // 📝 Error Log Area (New area for showing error log content)
        errorLogArea = createTextArea(bgColor, Color.RED, 12);
        errorLogArea.setEditable(false);
        JScrollPane errorLogScroll = createScrollPane(errorLogArea);
    
        // 🔘 Buttons
        JButton convertButton = createButton("Convert");
        JButton manualButton = createButton("Manual");
        JButton codeButton = createButton("Code");
    
        // 📌 Code Panels
        JPanel codePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        codePanel.setBackground(bgColor);
        JPanel inputPanel = createPanel(bgColor, inputLabel, inputScroll);
        inputPanel.setPreferredSize(new Dimension(350, 0)); 
        JPanel outputPanel = createPanel(bgColor, outputLabel, outputScroll);
    
        codePanel.add(inputPanel);
        codePanel.add(outputPanel);
    
        // 📌 Button Panel (Now positioned directly below the header)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(bgColor);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center buttons
        buttonPanel.add(convertButton);
        buttonPanel.add(manualButton);
        buttonPanel.add(codeButton);
    
        // 📌 Error Log Panel
        JPanel errorLogPanel = new JPanel(new BorderLayout());
        errorLogPanel.setBackground(bgColor);
        JLabel errorLogLabel = new JLabel("📋 Error Log", JLabel.CENTER);
        errorLogLabel.setForeground(Color.RED);  // Set the label text to red
        errorLogPanel.add(errorLogLabel, BorderLayout.NORTH);
        errorLogPanel.add(errorLogScroll, BorderLayout.CENTER);
        errorLogPanel.setPreferredSize(new Dimension(400, 0));  // Make the error log panel wider
    
        // 🚀 Add Components to Panel
        JPanel topPanel = new JPanel(new BorderLayout()); // Use BorderLayout
        topPanel.setBackground(bgColor);
        topPanel.add(headerLabel, BorderLayout.CENTER);  // Add the header at the center
        topPanel.add(buttonPanel, BorderLayout.SOUTH);  // Add the buttons below the header
    
        panel.add(topPanel, BorderLayout.NORTH); // Add stacked components at the top
        panel.add(codePanel, BorderLayout.CENTER);  // Add the code panel in the center
        panel.add(errorLogPanel, BorderLayout.EAST); // Place error log panel on the right
    
        frame.add(panel);
        frame.setVisible(true);
    
        // 🔹 Set Focus on Python Input Area
        SwingUtilities.invokeLater(() -> pythonCodeArea.requestFocus());
    
        // 🎯 Button Actions
        convertButton.addActionListener(e -> convertCode());
        manualButton.addActionListener(e -> openManual());
        codeButton.addActionListener(e -> openGitHub());
    }
    

    // ✅ Creates a modern text area
    private JTextArea createTextArea(Color bg, Color fg, int fontSize) {
        JTextArea textArea = new JTextArea();
        textArea.setBackground(bg);
        textArea.setForeground(fg);
        textArea.setCaretColor(Color.WHITE);
        textArea.setFont(new Font("Consolas", Font.PLAIN, fontSize));
        textArea.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        return textArea;
    }

    // ✅ Creates a scroll pane with custom styling
    private JScrollPane createScrollPane(JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120)));
        return scrollPane;
    }

    // ✅ Creates a labeled panel
    private JPanel createPanel(Color bg, JLabel label, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ✅ Creates a modern button
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Larger font size for bigger button
        button.setBackground(new Color(128, 0, 128)); // Purple color
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40)); // Larger size

        // Hover Effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(160, 0, 160)); // Slightly lighter purple on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(128, 0, 128)); // Purple color on exit
            }
        });

        return button;
    }

    // 🔥 Convert Code (Calls Compiler)
    private void convertCode() {
        clearErrorLogFile();
        String pythonCode = pythonCodeArea.getText();
        String javaCode = MyCompiler.readsourcefile(pythonCode);  // Call compiler
        javaCodeArea.setText(javaCode);

        // Reset the error log and read it from the errorlog.txt file
        errorLogArea.setText("");  // Clear previous error logs
        readErrorLog();
    }

    private void clearErrorLogFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("error_log.txt"))) {
            // Writing nothing will clear the file
            writer.write("");
        } catch (IOException e) {
            errorLogArea.setText("Error clearing the error log file.");
        }
    }

    // 📜 Reads the errorlog.txt file and updates the errorLogArea
    private void readErrorLog() {
        try (BufferedReader reader = new BufferedReader(new FileReader("error_log.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errorLogArea.append(line + "\n");
            }
        } catch (IOException e) {
            errorLogArea.setText("Error reading error log file.");
        }
    }

    // Open Manual PDF
    private void openManual() {
        try {
            File manual = new File("user_manual.pdf");
            if (manual.exists()) {
                Desktop.getDesktop().open(manual);
            } else {
                JOptionPane.showMessageDialog(null, "Manual PDF not found.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error opening manual: " + e.getMessage());
        }
    }

    // Open GitHub Link
    private void openGitHub() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/AkshatPandey-2004/Error-Resilient-Transpiler"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error opening GitHub: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new CompilerUI();
    }
}
