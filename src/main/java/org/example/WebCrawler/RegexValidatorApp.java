package org.example.WebCrawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

public class RegexValidatorApp extends JFrame {
    public RegexValidatorApp() {
        // Set up the main frame
        setTitle("Regex Validator Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with padding for a cleaner look
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Adding fields with labels, hints, and validation
        addRegexField(mainPanel, "Postal Code (Canada):",
                "Formats: A1A 1A1, A1A1A1", "([A-Za-z]\\d[A-Za-z]) ?(\\d[A-Za-z]\\d)");

        addRegexField(mainPanel, "Phone Number (Canada):",
                "Formats: +1 234-567-8901, (234) 567-8901, 234-567-8901", "(\\+1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");

        addRegexField(mainPanel, "Email Address:",
                "Format: username@domain.com", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

        addRegexField(mainPanel, "URL:",
                "Formats: http(s)://www.example.com, www.example.com", "(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/[\\w-./?%&=]*)?");

        addRegexField(mainPanel, "Program Identifier:",
                "Format: variable_name or variableName", "[a-zA-Z_][a-zA-Z0-9_]*");

        addRegexField(mainPanel, "Arithmetic Expression:",
                "Format: 1 + 2 - 3 * 4 / 5", "\\d+(\\s*[-+*/]\\s*\\d+)+");

        addRegexField(mainPanel, "Floating Point Number:",
                "Formats: 123.456, -123.45e+6", "[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");

        addRegexField(mainPanel, "Postal Address:",
                "Formats: 123 Main St, 456 Maple Ave", "\\d+\\s+[a-zA-Z\\s]+(Street|St|Avenue|Ave|Boulevard|Blvd|Drive|Dr|Road|Rd|Lane|Ln|Way)");

        addRegexField(mainPanel, "Page Number:",
                "Formats: p. 123, pp. 123-125", "p{1,2}\\.? ?\\d+(-\\d+)?");

        addRegexField(mainPanel, "HTML Tag:",
                "Format: <tag>...</tag>", "<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>(.*?)</\\1>");

        addRegexField(mainPanel, "Document Keywords:",
                "Enter words separated by spaces, commas, or other punctuation", "\\b(keyword1|keyword2|keyword3)\\b");

        // Add the main panel to the frame with a scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);
        setVisible(true);
    }

    /**
     * We add a regex field to the main panel with a label, text field, and hint.
     * Examples:
     * panel      The main panel to which the field should be added.
     * labelText  The label text describing the type of input.
     * hintText   The hint text showing the expected formats.
     * regex      The regex pattern to validate the input.
     */
    private void addRegexField(JPanel panel, String labelText, String hintText, String regex) {
        // Panel to hold each field section for better styling
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Label for the field
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        fieldPanel.add(label, BorderLayout.NORTH);

        // Text field for user input
        JTextField textField = new JTextField(30);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                textField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        fieldPanel.add(textField, BorderLayout.CENTER);

        // Hint label
        JLabel hintLabel = new JLabel(hintText);
        hintLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(100, 100, 100)); // Gray color for the hint
        fieldPanel.add(hintLabel, BorderLayout.SOUTH);

        // Real-time validation
        textField.addKeyListener(new ValidationListener(textField, regex));

        // Add the field panel to the main panel
        panel.add(fieldPanel);
    }

    /**
     * Inner class to handle real-time validation of each input field.
     * Changes the field's background color based on validation result.
     */
    private class ValidationListener extends KeyAdapter {
        private JTextField textField;
        private String regexPattern;

        public ValidationListener(JTextField textField, String regexPattern) {
            this.textField = textField;
            this.regexPattern = regexPattern;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Pattern pattern = Pattern.compile(regexPattern);
            if (pattern.matcher(textField.getText()).matches()) {
                textField.setBackground(new Color(210, 255, 210)); // Light green for valid input
            } else {
                textField.setBackground(new Color(255, 210, 210)); // Light red for invalid input
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegexValidatorApp());
    }
}
