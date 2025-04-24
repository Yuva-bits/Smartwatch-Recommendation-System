package org.example.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class Task1SwingUI extends JFrame {
    private WebDriver driver;
    private Map<String, Integer> searchFrequency;
    private JTextField searchField;
    private JEditorPane resultArea;
    private JButton searchButton;
    private JButton exitButton;
    private TrieNode root;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;


    // TrieNode class for autocompletion functionality
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
    }

    // Constructor to initialize the WebDriver and UI components
    public Task1SwingUI() {
        this.driver = new ChromeDriver();
        this.searchFrequency = new TreeMap<>();
        initializeUI();
    }

    // Initialize the user interface
    private void initializeUI() {
        setTitle("Apple Watch Comparator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color backgroundColor = new Color(230, 240, 255); // Light blue background
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Search panel for user input
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        exitButton = new JButton("Exit");
        JButton frequencyButton = new JButton("Show Search Frequencies"); // New button for showing frequencies
        searchPanel.add(new JLabel("Enter watch name: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(exitButton);
        searchPanel.add(frequencyButton); // Add to panel

        // Suggestion list for autocomplete
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        JScrollPane suggestionScrollPane = new JScrollPane(suggestionList);
        suggestionScrollPane.setPreferredSize(new Dimension(200, 50));
        searchPanel.add(suggestionScrollPane);

        // Initialize the Trie for autocomplete
        root = new TrieNode();
        initializeTrie();

        // Add a document listener to update the suggestions dynamically
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestions();
            }
        });

        // Handle selection from the suggestion list
        suggestionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    searchField.setText(selected);
                }
            }
        });

        // Result area to display comparison results
        resultArea = new JEditorPane();
        resultArea.setEditable(false);
        resultArea.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane(resultArea);

        HTMLEditorKit kit = new HTMLEditorKit();
        resultArea.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Arial, sans-serif; margin: 20px; }");
        styleSheet.addRule("h2 { color: #333366; }");
        styleSheet.addRule(".price { font-weight: bold; color: #006600; }");
        styleSheet.addRule(".feature { margin-bottom: 5px; }");
        styleSheet.addRule("table { width: 100%; border-collapse: collapse; }");
        styleSheet.addRule("td { padding: 5px; border: 1px solid #ddd; }");

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Action listeners for buttons
        searchButton.addActionListener(e -> searchAndCompare(searchField.getText().trim()));
        exitButton.addActionListener(e -> exitApplication());
        frequencyButton.addActionListener(e -> showSearchFrequencies()); // Action listener for frequency button

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    // Show the search frequencies in a dialog
    private void showSearchFrequencies() {
        // Create a dialog to display search frequencies
        JDialog frequencyDialog = new JDialog(this, "Search Frequencies", true);
        frequencyDialog.setSize(400, 300);
        frequencyDialog.setLocationRelativeTo(this);

        // Create a panel to hold the frequency list
        JPanel frequencyPanel = new JPanel();
        frequencyPanel.setLayout(new BoxLayout(frequencyPanel, BoxLayout.Y_AXIS));

        // Sort frequencies by count in descending order
        searchFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String watchName = entry.getKey();
                    Integer frequency = entry.getValue();
                    JLabel frequencyLabel = new JLabel(watchName + ": " + frequency + " searches");
                    frequencyPanel.add(frequencyLabel);
                });

        // Add a close button to the dialog
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> frequencyDialog.dispose());
        frequencyPanel.add(closeButton);

        frequencyDialog.add(frequencyPanel);
        frequencyDialog.setVisible(true);
    }


    // Initialize Trie with watch names for autocomplete
    private void initializeTrie() {
        String[] watches = {
                "Apple Watch Series 1", "Apple Watch Series 2", "Apple Watch Series 3",
                "Apple Watch Series 4", "Apple Watch Series 5", "Apple Watch Series 6",
                "Apple Watch Series 7", "Apple Watch Series 8", "Apple Watch Series 9",
                "Apple Watch Series 10", "Apple Watch Ultra", "Apple Watch Ultra 2",
                "Apple Watch SE (1st generation)", "Apple Watch SE (2nd generation)"
        };
        for (String watch : watches) {
            insert(watch.toLowerCase());
        }
    }

    // Insert a word into the Trie
    private void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
    }

    // Search for words with the given prefix in the Trie
    private List<String> search(String prefix) {
        List<String> result = new ArrayList<>();
        TrieNode current = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            if (!current.children.containsKey(c)) {
                return result;
            }
            current = current.children.get(c);
        }
        collectWords(current, prefix, result);
        return result;
    }

    // Collect all words from the Trie given a node and prefix
    private void collectWords(TrieNode node, String prefix, List<String> result) {
        if (node.isEndOfWord) {
            result.add(prefix);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            collectWords(entry.getValue(), prefix + entry.getKey(), result);
        }
    }

    // Update the suggestions list based on the current text in the search field
    private void updateSuggestions() {
        SwingUtilities.invokeLater(() -> {
            String prefix = searchField.getText();
            List<String> suggestions = search(prefix);
            listModel.clear();
            for (String suggestion : suggestions) {
                listModel.addElement(suggestion);
            }
        });
    }

    // Perform search and display results in the result area
    private void searchAndCompare(String watchName) {
        if (watchName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a watch name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedSuggestion = suggestionList.getSelectedValue();
        if (selectedSuggestion != null) {
            watchName = selectedSuggestion;
        }

        String finalWatchName = watchName;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    driver.get("https://www.apple.com/watch/compare/");
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                    Select select1 = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selector-0"))));
                    selectOptionContaining(select1, finalWatchName);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".compare-table")));
                    String priceInfo = extractPriceInfo();
                    String comparisonData = extractComparisonData();
                    updateSearchFrequency(finalWatchName);
                    return formatHTMLResult(finalWatchName, priceInfo, comparisonData);
                } catch (Exception e) {
                    return "<html><body><h2>Error occurred while searching:</h2><p>" + e.getMessage() + "</p></body></html>";
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    resultArea.setText(result);
                    resultArea.setCaretPosition(0);
                } catch (Exception e) {
                    resultArea.setText("<html><body><h2>An error occurred:</h2><p>" + e.getMessage() + "</p></body></html>");
                }
            }
        }.execute();
    }

    // Format the comparison result into HTML format
    private String formatHTMLResult(String watchName, String priceInfo, String comparisonData) {
        StringBuilder html = new StringBuilder("<html><body style='background-color: #E6F0FF;'>");
        html.append("<h2>").append(watchName).append("</h2>");
        html.append("<h3>Price Information</h3>");
        html.append("<table>");
        for (String line : priceInfo.split("\n")) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                html.append("<tr><td>").append(parts[0].trim()).append("</td>");
                html.append("<td class='price'>").append(parts[1].trim()).append("</td></tr>");
            }
        }
        html.append("</table>");
        html.append("<h3>Features</h3>");
        html.append("<table>");
        for (String line : comparisonData.split("\n")) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                html.append("<tr><td>").append(parts[0].trim()).append("</td>");
                html.append("<td class='feature'>").append(parts[1].trim()).append("</td></tr>");
            }
        }
        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }

    // Select option containing the given text in the dropdown
    private void selectOptionContaining(Select select, String text) {
        List<WebElement> options = select.getOptions();
        WebElement bestMatch = null;
        int bestMatchValue = -1;

        for (WebElement option : options) {
            String optionText = option.getText().toLowerCase();
            String searchText = text.toLowerCase();

            if (optionText.equals(searchText)) {
                // Exact match found
                select.selectByVisibleText(option.getText());
                return;
            }

            if (optionText.contains(searchText)) {
                // Extract the series number
                int seriesNumber = extractSeriesNumber(optionText);
                int searchSeriesNumber = extractSeriesNumber(searchText);

                if (seriesNumber == searchSeriesNumber && (bestMatch == null || seriesNumber > bestMatchValue)) {
                    bestMatch = option;
                    bestMatchValue = seriesNumber;
                }
            }
        }

        if (bestMatch != null) {
            select.selectByVisibleText(bestMatch.getText());
        } else {
            System.out.println("No matching option found for: " + text);
        }
    }

    private int extractSeriesNumber(String text) {
        String[] parts = text.split("series");
        if (parts.length > 1) {
            try {
                return Integer.parseInt(parts[1].trim().split("\\s+")[0]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    // Extract comparison data from the webpage
    private String extractComparisonData() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> allDivs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div.first-child.compare-column.template-item-default[role='cell gridcell']")));

        StringBuilder data = new StringBuilder();
        int featureCount = 1;

        for (WebElement div : allDivs) {
            try {
                String feature = div.getText().trim();

                // Skip if feature is empty, just a hyphen, or contains "Not available"
                if (feature.isEmpty() || feature.equals("—") || feature.toLowerCase().contains("not available")) {
                    continue;
                }

                // Replace newlines with spaces and remove extra whitespace
                feature = feature.replaceAll("\\s+", " ").trim();

                data.append(String.format("%-20s | %-50s%n", "Feature " + featureCount, feature));
                featureCount++;
            } catch (Exception e) {
                System.out.println("Error processing a feature: " + e.getMessage());
            }
        }

        return data.toString();
    }

    // Update the search frequency of a given watch name
    private void updateSearchFrequency(String watchName) {
        searchFrequency.put(watchName, searchFrequency.getOrDefault(watchName, 0) + 1);
    }

    // Exit the application and display search frequencies
    private void exitApplication() {
        displaySearchFrequencies();
        driver.quit();
        System.exit(0);
    }

    // Display the search frequencies sorted by frequency in descending order
    private void displaySearchFrequencies() {
        System.out.println("\nSearch Frequencies:");
        searchFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    // Extract price information from the webpage
    private String extractPriceInfo() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".first-child.compare-column.template-price")));

        StringBuilder priceInfo = new StringBuilder();
        priceInfo.append("Price Information:\n");
        priceInfo.append("-".repeat(75)).append("\n");

        if (priceElement.findElements(By.cssSelector(".compare-column-price-group")).isEmpty()) {
            // Case: No price information available
            String notAvailable = priceElement.findElement(By.cssSelector(".visuallyhidden")).getText();
            priceInfo.append(String.format("%-20s | %-50s%n", "Price", notAvailable));
        } else {
            // Case: Price information is available
            WebElement priceGroup = priceElement.findElement(By.cssSelector(".compare-column-price-group"));
            List<WebElement> priceElements = priceGroup.findElements(By.cssSelector(".template-item-default, .template-dynamic-price"));

            for (int i = 0; i < priceElements.size(); i += 2) {
                WebElement typeElement = priceElements.get(i);
                WebElement priceValueElement = priceElements.get(i + 1);

                String type = typeElement.findElement(By.cssSelector("span")).getText();
                String price = priceValueElement.findElement(By.cssSelector("span")).getText();

                priceInfo.append(String.format("%-20s | %-50s%n", type, price));
            }
        }

        return priceInfo.toString();
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Task1SwingUI analyzer = new Task1SwingUI();
            analyzer.setVisible(true);
        });
    }
}
