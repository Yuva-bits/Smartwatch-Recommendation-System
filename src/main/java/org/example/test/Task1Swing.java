package org.example.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class Task1Swing extends JFrame {
    private WebDriver driver;
    private Map<String, Integer> searchFrequency;

    // Constructor to initialize the WebDriver and search frequency map
    public Task1Swing() {
        this.driver = new ChromeDriver();
        this.searchFrequency = new TreeMap<>();
        initializeUI();
    }

    // Initialize the Swing UI components
    private void initializeUI() {
        setTitle("Apple Watch Comparator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the window on the screen
        getContentPane().setBackground(new Color(240, 240, 255));  // Light background color

        // Create a panel for the user input area
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.setBackground(new Color(240, 240, 255));  // Match background color

        JLabel searchLabel = new JLabel("Enter Watch Name:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        searchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchLabel.setForeground(new Color(0, 51, 102));  // Dark blue color for label

        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchField.setBackground(new Color(255, 255, 255));  // White background for text field

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.setBackground(new Color(34, 139, 34));  // Green button
        searchButton.setForeground(Color.WHITE);
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setBackground(new Color(255, 69, 0));  // Red exit button
        exitButton.setForeground(Color.WHITE);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultArea.setBackground(new Color(255, 255, 255));  // White background for result area
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(750, 300));

        // Adding components to the input panel
        inputPanel.add(searchLabel);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(searchField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(searchButton);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(exitButton);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(scrollPane);

        // Adding input panel to the frame
        getContentPane().add(inputPanel, BorderLayout.CENTER);

        // Action listener for the search button
        searchButton.addActionListener(e -> {
            String watchName = searchField.getText().trim();
            if (!watchName.isEmpty()) {
                searchAndCompare(watchName, resultArea);
            }
        });

        // Action listener for the exit button
        exitButton.addActionListener(e -> {
            displaySearchFrequencies();
            System.exit(0);
        });
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Task1SwingUI analyzer = new Task1SwingUI();
            analyzer.setVisible(true);
        });
    }

    // Search and compare the given Apple Watch name
    private void searchAndCompare(String watchName, JTextArea resultArea) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            Select select1 = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selector-0"))));
            selectOptionContaining(select1, watchName);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".compare-table")));

            // Extract and display price information
            String priceInfo = extractPriceInfo();
            String comparisonData = extractComparisonData();

            resultArea.setText(""); // Clear previous results
            resultArea.append("<html><body style='font-family: Arial, sans-serif;'>");

            resultArea.append("<h3 style='color: #003366;'>" + watchName + "</h3>");
            resultArea.append("<h4 style='color: #006400;'>Price Information:</h4>");
            resultArea.append("<pre style='font-weight: bold; color: green;'>" + priceInfo + "</pre>");

            resultArea.append("<h4 style='color: #003366;'>Comparison Data:</h4>");
            resultArea.append("<pre style='font-weight: normal; color: #333;'>"+ comparisonData + "</pre>");

            resultArea.append("</body></html>");

            updateSearchFrequency(watchName);

        } catch (Exception e) {
            resultArea.setText("Error occurred while searching: " + e.getMessage());
        }
    }

    // Selects an option from the dropdown that contains the given text
    private void selectOptionContaining(Select select, String text) {
        List<WebElement> options = select.getOptions();
        WebElement bestMatch = null;
        int bestMatchValue = -1;

        for (WebElement option : options) {
            String optionText = option.getText().toLowerCase();
            String searchText = text.toLowerCase();

            if (optionText.equals(searchText)) {
                select.selectByVisibleText(option.getText());
                return;
            }

            if (optionText.contains(searchText)) {
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

    // Extracts comparison data from the webpage
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

    // Updates the search frequency for a given watch name
    private void updateSearchFrequency(String watchName) {
        searchFrequency.put(watchName, searchFrequency.getOrDefault(watchName, 0) + 1);
    }

    // Displays the search frequencies in descending order
    private void displaySearchFrequencies() {
        System.out.println("\nSearch Frequencies:");
        searchFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    // Extracts price information from the webpage
    private String extractPriceInfo() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".first-child.compare-column.template-price")));

        StringBuilder priceInfo = new StringBuilder();
        priceInfo.append("Price Information:\n");
        priceInfo.append("-".repeat(75)).append("\n");

        if (priceElement.findElements(By.cssSelector(".compare-column-price-group")).isEmpty()) {
            String notAvailable = priceElement.findElement(By.cssSelector(".visuallyhidden")).getText();
            priceInfo.append(String.format("%-20s | %-50s%n", "Price", notAvailable));
        } else {
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
}
