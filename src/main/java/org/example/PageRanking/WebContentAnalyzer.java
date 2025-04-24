package org.example.PageRanking;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.*;

// Name : Yuvashree Senthilmurugan

/**
 * WebContentAnalyzer class implements a web content analysis system using string matching algorithms.
 * It explores specified web pages, evaluates them based on keyword frequency, and offers
 * a user-friendly interface for content exploration and evaluation.
 */
public class WebContentAnalyzer {
    private WebDriver browserInterface;
    private Map<String, String> webpageData;

    /**
     * Constructor for WebContentAnalyzer.
     * Sets up the browser interface and initializes storage for webpage content.
     */
    public WebContentAnalyzer() {
        browserInterface = new ChromeDriver();
        webpageData = new HashMap<>();
    }

    /**
     * Explores specified web pages and stores their content.
     * Utilizes Selenium WebDriver for page navigation and content extraction.
     */
    public void exploreWebpages() {
        String[] targetUrls = {
                "https://www.apple.com/watch/",
                "https://www.apple.com/watch/compare/",
                "https://www.apple.com/watch/why-apple-watch/",
                "https://www.apple.com/watch/cellular/"
        };

        for (String url : targetUrls) {
            browserInterface.get(url);
            WebDriverWait pageLoadWait = new WebDriverWait(browserInterface, Duration.ofSeconds(10));
            pageLoadWait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            String pageText = browserInterface.findElement(By.tagName("body")).getText();
            webpageData.put(url, pageText);
            System.out.println("Explored: " + url);
        }
    }

    /**
     * Evaluates pages based on the frequency of a given keyword.
     * Employs an efficient string matching algorithm for keyword counting.
     *
     * @param searchTerm The keyword to search for in the webpage contents.
     * @return A list of evaluated pages with their keyword frequency counts.
     */
    public List<String> evaluatePages(String searchTerm) {
        Map<String, Integer> evaluationScores = new HashMap<>();
        StringMatcher matcher = new StringMatcher();

        for (Map.Entry<String, String> entry : webpageData.entrySet()) {
            String url = entry.getKey();
            String content = entry.getValue();
            int frequency = matcher.countOccurrences(content.toLowerCase(), searchTerm.toLowerCase());
            evaluationScores.put(url, frequency);
        }

        List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(evaluationScores.entrySet());
        sortedScores.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<String> evaluatedPages = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedScores) {
            evaluatedPages.add(entry.getKey() + " (Frequency: " + entry.getValue() + ")");
        }

        return evaluatedPages;
    }

    /**
     * Inner class implementing an efficient string matching algorithm.
     */
    private static class StringMatcher {
        /**
         * Prepares a character skip table for the string matching algorithm.
         *
         * @param pattern The pattern to prepare the table for.
         * @return A map representing the character skip table.
         */
        private Map<Character, Integer> prepareSkipTable(char[] pattern) {
            Map<Character, Integer> skipTable = new HashMap<>();
            for (int i = 0; i < pattern.length; i++) {
                skipTable.put(pattern[i], i);
            }
            return skipTable;
        }

        /**
         * Counts occurrences of a pattern in a text using an efficient string matching algorithm i.e Boyer-Moore Algorithm .
         *
         * @param text The text to search in.
         * @param pattern The pattern to search for.
         * @return The number of occurrences of the pattern in the text.
         */
        public int countOccurrences(String text, String pattern) {
            char[] textArray = text.toCharArray();
            char[] patternArray = pattern.toCharArray();
            int textLength = textArray.length;
            int patternLength = patternArray.length;
            Map<Character, Integer> skipTable = prepareSkipTable(patternArray);
            int occurrences = 0;

            int i = 0;
            while (i <= (textLength - patternLength)) {
                int j = patternLength - 1;
                while (j >= 0 && patternArray[j] == textArray[i + j]) {
                    j--;
                }
                if (j < 0) {
                    occurrences++;
                    i += (i + patternLength < textLength) ? patternLength - skipTable.getOrDefault(textArray[i + patternLength], -1) : 1;
                } else {
                    i += Math.max(1, j - skipTable.getOrDefault(textArray[i + j], -1));
                }
            }
            return occurrences;
        }
    }

    /**
     * Closes the browser interface.
     */
    public void cleanup() {
        if (browserInterface != null) {
            browserInterface.quit();
        }
    }

    /**
     * Main method to execute the WebContentAnalyzer.
     * Provides an interactive interface for users to input search terms and view evaluated pages.
     */
    public static void main(String[] args) {
        WebContentAnalyzer analyzer = new WebContentAnalyzer();
        Scanner inputReader = new Scanner(System.in);

        try {
            while (true) {
                System.out.println("Enter a search term (or type 'exit' to quit):");
                String searchTerm = inputReader.nextLine().trim();

                if (searchTerm.equalsIgnoreCase("exit")) {
                    break;
                }

                if (searchTerm.isEmpty()) {
                    System.out.println("Please enter a valid search term.");
                    continue;
                }

                analyzer.exploreWebpages();

                List<String> evaluatedPages = analyzer.evaluatePages(searchTerm);

                System.out.println("Evaluated pages based on search term '" + searchTerm + "':");
                for (int i = 0; i < evaluatedPages.size(); i++) {
                    System.out.println((i + 1) + ". " + evaluatedPages.get(i));
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            analyzer.cleanup();
            inputReader.close();
        }
    }
}