package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpellChecker_Vocab {
    private Set<String> vocabulary = new HashSet<>();

    // Constructor to initialize vocabulary from Excel files or a dictionary file
    public SpellChecker_Vocab(String... files) throws IOException {
        for (String file : files) {
            if (file.endsWith(".xlsx")) {
                readExcelFile(file);
            } else {
                readDictionary(file);
            }
        }
        System.out.println("Vocabulary size: " + vocabulary.size());
    }

    // Read words from an Excel file
    private void readExcelFile(String fileName) {
        try (FileInputStream fis = new FileInputStream(new File(fileName));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String word = cell.getStringCellValue().toLowerCase().trim();
                        vocabulary.add(word);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + fileName);
            e.printStackTrace();
        }
    }

    // Read words from a dictionary text file
    private void readDictionary(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                vocabulary.add(line.trim().toLowerCase());
            }
        }
    }

    // Suggest word using edit distance
    public String suggestWord(String input) {
        String[] words = input.toLowerCase().trim().split("\\s+");
        StringBuilder suggestions = new StringBuilder();

        for (String word : words) {
            suggestions.append(suggestSingleWord(word)).append("\n");
        }

        return suggestions.toString();
    }

    // Suggest a single word
    private String suggestSingleWord(String input) {
        if (vocabulary.contains(input)) {
            return "Correct spelling: " + input;
        }

        // Use findClosestMatch method from EditDistance class
        List<String> dictionaryList = List.copyOf(vocabulary); // Convert Set to List for compatibility
        String closestMatch = EditDistance.findClosestMatch(input, dictionaryList);

        return closestMatch != null ? closestMatch : "No close matches found for '" + input + "'.";
    }

    // Levenshtein edit distance calculation
    public static int calculateEditDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) dp[i][j] = j; // If first string is empty
                else if (j == 0) dp[i][j] = i; // If second string is empty
                else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // Characters match
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1])); // Insert, delete, replace
                }
            }
        }

        return dp[len1][len2];
    }

    // Interactive user input method
    public void startInteractiveSpellCheck() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Spell Checker ---");
            System.out.println("Enter a word or phrase (or 'exit' to quit):");

            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Spell Checker closed. Goodbye!");
                break;
            }

            System.out.println("Input: " + userInput);
            String suggestions = suggestWord(userInput);
            System.out.println(suggestions);
        }

        scanner.close();
    }

    // Main method to demonstrate usage
    public static void main(String[] args) throws IOException {
        SpellChecker_Vocab spellChecker = new SpellChecker_Vocab(
                "AppleWatchComparison.xlsx",   // Replace with your actual Excel file path or dictionary text file path
                "GShockSmartwatchDetails.xlsx",
                "noise.xlsx",
                "garmin_models.xlsx"// Example dictionary file path
        );

        spellChecker.startInteractiveSpellCheck();
    }
}