package org.example.WordFrequencyAnalyzer;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class WordFrequencyAnalyzer3 {
    WebDriver driver = new ChromeDriver();
    private TreeMap<String, Integer> wordFrequency;
    private TreeMap<String, Integer> searchLog;

    public WordFrequencyAnalyzer3(WebDriver driver) {
        this.driver = driver;
        this.wordFrequency = new TreeMap<>();
        this.searchLog = new TreeMap<>();
    }

    public void analyzeWordFrequency() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Increased timeout

        try {
            // Wait for the page to load initially
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".compare-table")));

            // Randomly select options from dropdown menus
            Select select1 = new Select(driver.findElement(By.id("selector-0")));
            Select select2 = new Select(driver.findElement(By.id("selector-1")));

            List<WebElement> options1 = select1.getOptions();
            List<WebElement> options2 = select2.getOptions();

            int randomIndex1 = new Random().nextInt(options1.size());
            int randomIndex2 = new Random().nextInt(options2.size());

            // Get the initial content of the comparison table
            String initialContent = driver.findElement(By.cssSelector("div.compare-column.template-item-default[role='cell gridcell']")).getText();

            // Select new options
            select1.selectByIndex(randomIndex1);
            select2.selectByIndex(randomIndex2);

            // Wait for the content to change
            wait.until((WebDriver d) -> {
                String newContent = d.findElement(By.cssSelector("div.compare-column.template-item-default[role='cell gridcell']")).getText();
                return !newContent.equals(initialContent);
            });

            // Get the selected watch names
            String watchName1 = select1.getFirstSelectedOption().getText();
            String watchName2 = select2.getFirstSelectedOption().getText();

            String pageText = extractTextFromPage();
            List<String> words = preprocessText(pageText);
            countWordFrequencies(words);
            simulateSearches();
            printAndSaveTopSearches(10, watchName1, watchName2);
        } catch (Exception e) {
            System.out.println("An error occurred during word frequency analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractTextFromPage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        StringBuilder textBuilder = new StringBuilder();

        try {
            List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".compare-table *")));
            for (WebElement element : elements) {
                try {
                    String text = wait.until(ExpectedConditions.visibilityOf(element)).getText().trim();
                    if (!text.isEmpty()) {
                        textBuilder.append(text).append(" ");
                    }
                } catch (StaleElementReferenceException | TimeoutException e) {
                    // If the element is stale or times out, just continue to the next element
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting text from page: " + e.getMessage());
        }

        return textBuilder.toString();
    }

    private List<String> preprocessText(String text) {
        text = text.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String[] words = text.split("\\s+");
        List<String> processedWords = new ArrayList<>();
        Set<String> stopWords = new HashSet<>(Arrays.asList("the", "a", "an", "and", "or", "but"));
        for (String word : words) {
            if (!stopWords.contains(word) && !word.isEmpty()) {
                processedWords.add(word);
            }
        }
        return processedWords;
    }

    private void countWordFrequencies(List<String> words) {
        for (String word : words) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }
    }

    private void simulateSearches() {
        String[] searchQueries = {"apple watch", "series", "ultra", "se", "watchos", "fitness"};
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            String query = searchQueries[random.nextInt(searchQueries.length)];
            logSearch(query);
        }
    }

    private void logSearch(String query) {
        searchLog.put(query, searchLog.getOrDefault(query, 0) + 1);
    }

    private void printAndSaveTopSearches(int n, String watchName1, String watchName2) {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(searchLog.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        System.out.println("Top " + n + " most frequent searches for " + watchName1 + " vs " + watchName2 + ":");
        try (FileWriter csvWriter = new FileWriter("top_searches_" + watchName1 + "_vs_" + watchName2 + ".csv")) {
            csvWriter.append("Comparison: " + watchName1 + " vs " + watchName2 + "\n");
            csvWriter.append("Search Query,Frequency\n");
            for (int i = 0; i < Math.min(n, sortedEntries.size()); i++) {
                Map.Entry<String, Integer> entry = sortedEntries.get(i);
                System.out.println(entry.getKey() + ": " + entry.getValue());
                csvWriter.append(String.format("%s,%d\n", entry.getKey(), entry.getValue()));
            }
            System.out.println("Top searches saved to top_searches_" + watchName1 + "_vs_" + watchName2 + ".csv");
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.apple.com/watch/compare/");

        WordFrequencyAnalyzer3 analyzer = new WordFrequencyAnalyzer3(driver);
        analyzer.analyzeWordFrequency();

        driver.quit();
    }
}