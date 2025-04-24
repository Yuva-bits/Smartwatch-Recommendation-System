package org.example.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditDistance {

    // Method to calculate edit distance
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

    // Method to read dictionary from file
    public static List<String> readDictionary(String filename) throws IOException {
        List<String> dictionary = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.trim());
            }
        }
        return dictionary;
    }

    // Method to find closest match
    public static String findClosestMatch(String misspelledWord, List<String> dictionary) {
        String closestMatch = "";
        int minDistance = Integer.MAX_VALUE;

        for (String word : dictionary) {
            int distance = calculateEditDistance(misspelledWord, word);
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = word;
            }
        }

        return closestMatch + ", Edit Distance: " + minDistance;
    }
}
