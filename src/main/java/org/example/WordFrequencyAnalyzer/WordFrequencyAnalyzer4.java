package org.example.WordFrequencyAnalyzer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class WordFrequencyAnalyzer4 {
    private WebDriver driver;
    private Map<String, Integer> searchFrequency;

    /**
     * Constructor initializes the WebDriver and search frequency map.
     */
    public WordFrequencyAnalyzer4() {
        this.driver = new ChromeDriver();
        this.searchFrequency = new TreeMap<>();
    }

    /**
     * Main method to run the program.
     */
    public static void main(String[] args) {
        WordFrequencyAnalyzer4 analyzer = new WordFrequencyAnalyzer4();
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

            // Extract and display comparison data
            String comparisonData = extractComparisonData();
            if (!comparisonData.isEmpty()) {
                System.out.println("Features for " + watchName + ":");
                System.out.println("-".repeat(40));
                System.out.print(comparisonData);
                System.out.println("-".repeat(40));
            } else {
                System.out.println("No data found for " + watchName);
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
        for (WebElement option : select.getOptions()) {
            if (option.getText().toLowerCase().contains(text.toLowerCase())) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }
        System.out.println("No matching option found for: " + text);
    }

    /**
     * Extracts comparison data from the webpage.
     * @return A string containing the comparison data.
     */
    private String extractComparisonData() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> allDivs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div.compare-column.template-item-default[role='cell gridcell']")));

        StringBuilder data = new StringBuilder();
        int featureCount = 1;

        // Add table header
        data.append(String.format("%-20s | %-50s%n", "Feature", "Value"));
        data.append("-".repeat(75)).append("\n");

        for (int i = 0; i < allDivs.size(); i += 2) {
            String feature = allDivs.get(i).findElement(By.cssSelector("span")).getText();
            String value = (i + 1 < allDivs.size()) ? allDivs.get(i + 1).findElement(By.cssSelector("span")).getText() : "";

            // Skip if both feature and value are hyphens
            if (feature.equals("—") && value.equals("—")) {
                continue;
            }

            // Replace empty or hyphen value with "N/A"
            value = (value.isEmpty() || value.equals("—")) ? "N/A" : value;

            // Format and append the row
            data.append(String.format("%-20s | %-50s%n", "Feature " + featureCount, value));
            featureCount++;
        }

        return data.toString();
    }
//    private String extractComparisonData(WebDriver driver) {
//        StringBuilder data = new StringBuilder();
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//        // Extract Summary and Finishes sections
//        String[] initialSections = {"Summary", "Finishes"};
//        for (String section : initialSections) {
//            WebElement sectionElement = wait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//div[@role='rowgroup' and contains(@class, 'section-" + section.toLowerCase() + "')]")));
//
//            data.append(section).append("\n");
//
//            List<WebElement> rows = sectionElement.findElements(By.cssSelector("div[role='row']"));
//            for (WebElement row : rows) {
//                // Extract row header
//                List<WebElement> rowHeaders = row.findElements(By.cssSelector("div[role='rowheader'] span"));
//                if (!rowHeaders.isEmpty()) {
//                    String headerText = rowHeaders.get(0).getText().trim();
//                    if (!headerText.isEmpty()) {
//                        data.append(String.format("%-20s | ", headerText));
//                    }
//                }
//
//                // Target the first child with class 'first-child compare-column template-item-default'
//                List<WebElement> firstChildCells = row.findElements(By.cssSelector("div.first-child.compare-column.template-item-default"));
//                if (!firstChildCells.isEmpty()) {
//                    WebElement firstChildCell = firstChildCells.get(0);
//                    String cellText = firstChildCell.getText().trim();
//
//                    // Skip unwanted texts and image cells
//                    if (!cellText.isEmpty() && !cellText.equals("—") && !cellText.equals("Not available") &&
//                            firstChildCell.findElements(By.cssSelector("div.image-icon-wrapper")).isEmpty()) {
//                        continue; // Skip this cell if it contains an image
//                    }
//
//                    // Append valid text data
//                    if (!cellText.isEmpty() && !cellText.equals("—") && !cellText.equals("Not available")) {
//                        data.append(String.format("%-50s", cellText));
//                    }
//                }
//                data.append("\n");
//            }
//            data.append("\n");
//        }
//
//        // Extract data from remaining sections
//        List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup'][class='compare-section']"));
//        for (WebElement section : sections) {
//            WebElement headerElement = section.findElement(By.cssSelector("div[role='rowheader'] span"));
//            String sectionHeading = headerElement.getText().trim();
//
//            // Only print heading if there is valid data
//            boolean hasValidData = false;
//            List<WebElement> rows = section.findElements(By.cssSelector("div[role='row']"));
//
//            for (WebElement row : rows) {
//                // Extract row header
//                List<WebElement> rowHeaders = row.findElements(By.cssSelector("div[role='rowheader'] span"));
//                if (!rowHeaders.isEmpty()) {
//                    String headerText = rowHeaders.get(0).getText().trim();
//                    if (!headerText.isEmpty()) {
//                        data.append(String.format("%-20s | ", headerText));
//                    }
//                }
//
//                // Target the first child with class 'first-child compare-column template-item-default'
//                List<WebElement> cells = row.findElements(By.cssSelector("div.first-child.compare-column.template-item-default"));
//                if (!cells.isEmpty()) {
//                    WebElement cell = cells.get(0);
//                    String cellText = cell.getText().trim();
//
//                    // Skip unwanted texts and image cells
//                    if (!cellText.isEmpty() && !cellText.equals("—") && !cellText.equals("Not available") &&
//                            !cell.findElements(By.cssSelector("div.image-icon-wrapper")).isEmpty()) {
//                        continue; // Skip this cell if it contains an image
//                    }
//
//                    // Append valid text data
//                    if (!cellText.isEmpty() && !cellText.equals("—") && !cellText.equals("Not available")) {
//                        data.append(String.format("%-50s", cellText));
//                        hasValidData = true; // Mark that we have valid data
//                    }
//                }
//                data.append("\n");
//            }
//
//            // Only append heading if there was valid data in this section
//            if (hasValidData) {
//                data.append(sectionHeading).append("\n");
//            }
//
//            data.append("\n");
//        }
//
//        return data.toString();
//    }

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
}