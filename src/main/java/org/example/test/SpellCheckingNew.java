package org.example.test;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.stream.Collectors;

public class SpellCheckingNew {

    private static Set<String> allWords = new HashSet<>();

    // Initialize the word set from Excel files
    public static void initializeWords(String[] filePaths) throws IOException {
        for (String filePath : filePaths) {
            List<String> words = readExcel(filePath);
            allWords.addAll(words); // Add words from all files to the set
        }
    }

    // Check a word and return the result
    public static String checkWord(String userInput) {
        if (allWords.contains(userInput)) {
            return "The word is correct!";
        } else {
            String closestMatch = findClosestMatch(userInput, new ArrayList<>(allWords));
            return "The word is incorrect. Closest match: " + closestMatch;
        }
    }

    // Function to calculate the Edit Distance between two words
    public static int calculateEditDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }

        return dp[len1][len2];
    }

    // Function to read text data from an Excel file
    public static List<String> readExcel(String filePath) throws IOException {
        List<String> words = new ArrayList<>();
        try (InputStream fis = new FileInputStream(filePath)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet of the Excel file

            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String cellValue = cell.getStringCellValue().trim().toLowerCase();
                        String[] splitWords = cellValue.split("\\s+");
                        for (String word : splitWords) {
                            if (!word.isEmpty()) {
                                words.add(word);
                            }
                        }
                    }
                }
            }
        }
        return words;
    }

    // Function to find the closest match for a given word
    public static String findClosestMatch(String misspelledWord, List<String> words) {
        String closestWord = null;
        int minDistance = Integer.MAX_VALUE;
        int threshold = 3; // Define a reasonable threshold for similarity

        List<String> filteredWords = words.stream()
                .filter(word -> Math.abs(word.length() - misspelledWord.length()) <= 2)
                .collect(Collectors.toList());

        for (String word : filteredWords) {
            int distance = calculateEditDistance(misspelledWord, word);
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = word;
            }
        }

        return (minDistance <= threshold)
                ? closestWord + " (Edit Distance: " + minDistance + ")"
                : "No close matches found";
    }
}
