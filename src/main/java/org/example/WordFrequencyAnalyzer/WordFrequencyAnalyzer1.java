package org.example.WordFrequencyAnalyzer;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;

public class WordFrequencyAnalyzer1 {
    private WebDriver driver;
    private TreeMap<String, Integer> wordFrequency;

    public WordFrequencyAnalyzer1(WebDriver driver) {
        this.driver = driver;
        this.wordFrequency = new TreeMap<>();
    }

    public void analyzeWordFrequency() {
        String pageText = extractTextFromPage();
        List<String> words = preprocessText(pageText);
        countWordFrequencies(words);
        printTopWords(10);
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

    private void printTopWords(int n) {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(wordFrequency.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        System.out.println("Top " + n + " most frequent words:");
        for (int i = 0; i < Math.min(n, sortedEntries.size()); i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.apple.com/watch/compare/");

        WordFrequencyAnalyzer1 analyzer = new WordFrequencyAnalyzer1(driver);
        analyzer.analyzeWordFrequency();

        driver.quit();
    }
}

