package org.example.WordFrequencyAnalyzer;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import java.util.stream.Collectors;

public class WordFrequencyAnalyzer2 {
    private WebDriver driver;
    private TreeMap<String, Integer> wordFrequency;
    private List<String> watchModels;
    private Map<String, String> watchData;
    private Map<String, Integer> searchFrequency;

    public WordFrequencyAnalyzer2(WebDriver driver) {
        this.driver = driver;
        this.wordFrequency = new TreeMap<>();
        watchModels = Arrays.asList(
                "Apple Watch SE (1st generation)",
                "Apple Watch SE (2nd generation)",
                "Apple Watch Series 1",
                "Apple Watch Series 2",
                "Apple Watch Series 3",
                "Apple Watch Series 4",
                "Apple Watch Series 5",
                "Apple Watch Series 6",
                "Apple Watch Series 7",
                "Apple Watch Series 8",
                "Apple Watch Series 9",
                "Apple Watch Series 10",
                "Apple Watch Ultra",
                "Apple Watch Ultra 2"
        );

        // Initialize watchData with some sample data
        watchData = new HashMap<>();
        for (String model : watchModels) {
            watchData.put(model, "Data for " + model + "...");
        }

        searchFrequency = new HashMap<>();
    }

    public void analyzeWordFrequency() {
        String pageText = extractTextFromPage();
        List<String> words = preprocessText(pageText);
        countWordFrequencies(words);
        printAndSaveTopWords(10);
    }

    private String extractTextFromPage() {
        List<WebElement> elements = driver.findElements(By.cssSelector("body *"));
        StringBuilder textBuilder = new StringBuilder();
        for (WebElement element : elements) {
            textBuilder.append(element.getText()).append(" ");
        }
        return textBuilder.toString();
    }

    private List<String> preprocessText(String text) {
        text = text.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String[] words = text.split("\\s+");
        List<String> processedWords = new ArrayList<>();
        Set<String> stopWords = new HashSet<>(Arrays.asList("the", "a", "an", "and", "or", "but", "in", "of", "with", "for", "to", "by"));
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

    private void printAndSaveTopWords(int n) {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(wordFrequency.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        System.out.println("Top " + n + " most frequent words:");
        try (FileWriter csvWriter = new FileWriter("word_frequencies.csv")) {
            csvWriter.append("Word,Frequency\n");
            for (int i = 0; i < Math.min(n, sortedEntries.size()); i++) {
                Map.Entry<String, Integer> entry = sortedEntries.get(i);
                System.out.println(entry.getKey() + ": " + entry.getValue());
                csvWriter.append(String.format("%s,%d\n", entry.getKey(), entry.getValue()));
            }
            System.out.println("Word frequencies saved to word_frequencies.csv");
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public List<String> getSuggestions(String input) {
        String lowercaseInput = input.toLowerCase();
        return watchModels.stream()
                .filter(model -> model.toLowerCase().contains(lowercaseInput))
                .limit(5) // Limit to top 5 suggestions
                .collect(Collectors.toList());
    }

    public String search(String query) {
        // Update search frequency
        searchFrequency.put(query, searchFrequency.getOrDefault(query, 0) + 1);

        // Find the exact match or the closest match
        String result = watchData.get(query);
        if (result == null) {
            // If no exact match, find the closest match
            Optional<String> closestMatch = watchModels.stream()
                    .filter(model -> model.toLowerCase().contains(query.toLowerCase()))
                    .findFirst();

            if (closestMatch.isPresent()) {
                result = watchData.get(closestMatch.get());
            } else {
                result = "No data found for this model.";
            }
        }

        return result;
    }

    public Map<String, Integer> getTopSearches(int n) {
        return searchFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.apple.com/watch/compare/");

        WordFrequencyAnalyzer2 analyzer = new WordFrequencyAnalyzer2(driver);
        analyzer.analyzeWordFrequency();

        // Test getSuggestions
        System.out.println("Suggestions for 'se': " + analyzer.getSuggestions("se"));
        System.out.println("Suggestions for 'ultra': " + analyzer.getSuggestions("ultra"));

        // Test search
        System.out.println("Search for 'Apple Watch Series 9': " + analyzer.search("Apple Watch Series 9"));
        System.out.println("Search for 'Series 8': " + analyzer.search("Series 8"));
        System.out.println("Search for 'Ultra': " + analyzer.search("Ultra"));

        // Test multiple searches to populate search frequency
        for (int i = 0; i < 5; i++) analyzer.search("Apple Watch Ultra");
        for (int i = 0; i < 3; i++) analyzer.search("Apple Watch Series 9");
        analyzer.search("Apple Watch SE (2nd generation)");

        // Display top searches
        System.out.println("Top 3 searches: " + analyzer.getTopSearches(3));


        driver.quit();
    }
}