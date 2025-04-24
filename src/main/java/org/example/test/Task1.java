package org.example.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class Task1 {
    private WebDriver driver;
    private Map<String, Integer> searchFrequency;

    /**
     * Constructor initializes the WebDriver and search frequency map.
     */
    public Task1() {
        this.driver = new ChromeDriver();
        this.searchFrequency = new TreeMap<>();
    }

    /**
     * Main method to run the program.
     */
    public static void main(String[] args) {
        Task1 analyzer = new Task1();
        analyzer.run();
    }

    /**
     * Runs the main program loop.
     */
    public void run() {
        try {
            driver.get("https://www.apple.com/watch/compare/");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Enter a watch name to search (or 'exit' to quit):");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                searchAndCompare(input);
            }

            displaySearchFrequencies();
        } finally {
            driver.quit();
        }
    }

    /**
     * Searches for a watch and compares it with another.
     * @param watchName The name of the watch to search for.
     */
    private void searchAndCompare(String watchName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Select the first dropdown
            Select select1 = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selector-0"))));
            selectOptionContaining(select1, watchName);

            // Wait for the comparison table to be present
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".compare-table")));

            // Extract and display price information
            String priceInfo = extractPriceInfo();
            System.out.println(priceInfo);

            // Extract and display comparison data
            String comparisonData = extractComparisonData();
            if (!comparisonData.isEmpty()) {
                System.out.println("Features for " + watchName + ":");
                System.out.println("-".repeat(75));
                System.out.print(comparisonData);
            } else {
                System.out.println("No feature data found for " + watchName);
            }

            updateSearchFrequency(watchName);

        } catch (Exception e) {
            System.out.println("Error occurred while searching: " + e.getMessage());
        }
    }

    /**
     * Selects an option from a dropdown that contains the given text.
     * @param select The Select object representing the dropdown.
     * @param text The text to search for in the options.
     */
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

    /**
     * Extracts comparison data from the webpage.
     * @return A string containing the comparison data.
     */
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

    /**
     * Updates the search frequency for a given watch name.
     * @param watchName The name of the watch to update frequency for.
     */
    private void updateSearchFrequency(String watchName) {
        searchFrequency.put(watchName, searchFrequency.getOrDefault(watchName, 0) + 1);
    }

    /**
     * Displays the search frequencies in descending order.
     */
    private void displaySearchFrequencies() {
        System.out.println("\nSearch Frequencies:");
        searchFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

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
}