package org.example.WebCrawler;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHighlighterApp extends JFrame {
    private JTextPane textPane;
    private HashMap<String, Pattern> patterns;

    public RegexHighlighterApp() {
        // Set up the main frame
        setTitle("Regex Highlighter Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the text pane with a scroll pane
        textPane = new JTextPane();
        textPane.setEditable(false);  // Disable editing to prevent user modification of highlights
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // Set up the load button
        JButton loadButton = new JButton("Load Document");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAndHighlightText();
            }
        });
        add(loadButton, BorderLayout.SOUTH);

        // Initialize patterns with regex expressions and meaningful names
        patterns = new HashMap<>();
        
        // Postal Code Pattern: Canadian postal code format (e.g., A1A 1A1 or A1A1A1)
        patterns.put("Postal Code", Pattern.compile("([A-Za-z]\\d[A-Za-z]) ?(\\d[A-Za-z]\\d)"));

        /*
        Postal Code (([A-Za-z]\\d[A-Za-z]) ?(\\d[A-Za-z]\\d))
        [A-Za-z]: Matches an uppercase or lowercase letter.
        \\d: Matches a single digit (0�9).
        ([A-Za-z]\\d[A-Za-z]): Matches the first part of a Canadian postal code format, such as A1A.
        ?: Allows an optional space between the first and second parts.
        (\\d[A-Za-z]\\d): Matches the second part of the postal code format, such as 1A1.
        */
        
        // Phone Number Pattern: Supports multiple formats, including optional country code
        patterns.put("Phone Number", Pattern.compile("(\\+1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}"));

        /*
        Phone Number ((\\+1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4})
        (\\+1[-.\\s]?)?: Matches an optional country code (+1) followed by an optional separator (space, dash, or period).
        \\(?\\d{3}\\)?: Matches an area code, optionally enclosed in parentheses.
        [-.\\s]?: Allows an optional separator (space, dash, or period).
        \\d{3}: Matches the next three digits.
        [-.\\s]?: Allows another optional separator.
        \\d{4}: Matches the final four digits of the phone number.
        */
        
        // Email Pattern: Standard email format with alphanumeric characters and certain special characters
        patterns.put("Email", Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"));

        /*
        Email ([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})
        [a-zA-Z0-9._%+-]+: Matches the username part of the email, allowing letters, digits, and certain special characters.
        @: Matches the "@" symbol separating the username and domain.
        [a-zA-Z0-9.-]+: Matches the domain name.
        \\.: Matches the dot before the top-level domain.
        [a-zA-Z]{2,6}: Matches the top-level domain, with 2 to 6 alphabetic characters (e.g., .com, .org).
		*/
        
        // URL Pattern: Matches URLs with optional http(s):// and www prefixes
        patterns.put("URL", Pattern.compile("(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/[\\w-./?%&=]*)?"));

        /*
        URL ((https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/[\\w-./?%&=]*)?)
        	(https?://)?: Optionally matches http:// or https://.
        	(www\\.)?: Optionally matches www..
        	[a-zA-Z0-9.-]+: Matches the main part of the domain.
        	\\.[a-zA-Z]{2,6}: Matches the dot and top-level domain (e.g., .com).
        	(/[\\w-./?%&=]*)?: Optionally matches the URL path with alphanumeric and certain special characters.
        */
        
        // Floating Point Number Pattern: Supports standard and scientific notation (e.g., -123.45, 1.23e+4)
        patterns.put("Floating Point Number", Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?"));

        /*
        Floating Point Number ([+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?)
        [+-]?: Allows an optional sign (plus or minus).
        (\\d+(\\.\\d*)?|\\.\\d+): Matches either a decimal or whole number.
        ([eE][+-]?\\d+)?: Optionally matches scientific notation, like e+4.
        */
        
        // Postal Address Pattern: Matches common street addresses (e.g., 123 Main St, 456 Maple Avenue)
        patterns.put("Postal Address", Pattern.compile("\\d+\\s+[a-zA-Z\\s]+(Street|St|Avenue|Ave|Boulevard|Blvd|Drive|Dr|Road|Rd|Lane|Ln|Way)"));

        /*
        Postal Address (\\d+\\s+[a-zA-Z\\s]+(Street|St|Avenue|Ave|Boulevard|Blvd|Drive|Dr|Road|Rd|Lane|Ln|Way))
        \\d+: Matches the street number.
        \\s+: Matches spaces following the number.
        [a-zA-Z\\s]+: Matches the street name.
        (Street|St|Avenue|Ave|Boulevard|Blvd|Drive|Dr|Road|Rd|Lane|Ln|Way): Matches common street suffixes.
        */
        
        // Page Number Pattern: Matches page numbers like p. 123, pp. 123-125
        patterns.put("Page Number", Pattern.compile("p{1,2}\\.? ?\\d+(-\\d+)?"));

        /*
        Page Number (p{1,2}\\.? ?\\d+(-\\d+)?)
        p{1,2}: Matches p or pp for single or multiple pages.
        \\.?: Matches an optional period.
        ?: Allows an optional space.
        \\d+: Matches the page number.
        (-\\d+)?: Matches a page range if specified.
       */ 
       
        // HTML Tag Pattern: Matches simple HTML tags (e.g., <p>...</p>, <a href="...">...</a>)
        patterns.put("HTML Tag", Pattern.compile("<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>(.*?)</\\1>"));

        /*
        HTML Tag (<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>(.*?)</\\1>)
        <([a-zA-Z][a-zA-Z0-9]*): Matches the opening tag with alphanumeric name.
        \\b[^>]*>: Matches attributes within the tag.
        (.*?): Matches content within the tag.
        </\\1>: Matches the corresponding closing tag.
        */
        
        // Document Keywords Pattern: Matches specific keywords in the document
        patterns.put("Document Keywords", Pattern.compile("\\b(keyword1|keyword2|keyword3)\\b"));

        /*
        Document Keywords (\\b(keyword1|keyword2|keyword3)\\b)
        \\b: Matches a word boundary.
        (keyword1|keyword2|keyword3): Matches any of the specified keywords.
        */
        
        setVisible(true);
    }

    /**
     * Loads the document, highlights patterns, and displays in text pane.
     */
    private void loadAndHighlightText() {
        // Open file chooser to select the document to load
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Read file content into text area
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                // Set the text in the text pane without applying any default styling
                textPane.setText(content.toString());
                
                // Highlight only the patterns in the loaded text
                highlightPatterns(content.toString());
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Highlights patterns in the document using predefined colors.
     */
    private void highlightPatterns(String text) {
        // Define colors for each pattern type
        HashMap<String, Color> colors = new HashMap<>();
        colors.put("Postal Code", Color.MAGENTA);
        colors.put("Phone Number", new Color(135, 206, 250)); // Light blue color for phone numbers
        colors.put("Email", Color.ORANGE);
        colors.put("URL", Color.GREEN);
        colors.put("Floating Point Number", Color.PINK);
        colors.put("Postal Address", Color.YELLOW);
        colors.put("Page Number", new Color(160, 82, 45)); // SaddleBrown
        colors.put("HTML Tag", Color.GRAY);
        colors.put("Document Keywords", Color.LIGHT_GRAY);

        StyledDocument doc = textPane.getStyledDocument();

        // Clear any previous highlights by resetting character attributes to default
        doc.setCharacterAttributes(0, text.length(), new SimpleAttributeSet(), true);

        // Apply highlights for each pattern
        for (String key : patterns.keySet()) {
            Pattern pattern = patterns.get(key);
            Matcher matcher = pattern.matcher(text);

            Color color = colors.get(key); // Get the color associated with the current pattern
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                highlightText(start, end, color); // Apply color only to matched pattern text
            }
        }
    }

    /**
     * Highlights a specific range in the text pane with a unique color for each pattern type.
     */
    private void highlightText(int start, int end, Color color) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setBackground(attributeSet, color); // Set only background color for matched text
        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(start, end - start, attributeSet, false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegexHighlighterApp());
    }
}
