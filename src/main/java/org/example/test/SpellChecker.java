package org.example.test;
import java.util.*;

import java.util.*;

public class SpellChecker {
    private Set<String> dictionary;

    public SpellChecker(Set<String> dictionary) {
        this.dictionary = dictionary;
    }

    // Method to calculate the Levenshtein distance
    public int getEditDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    int newValue = costs[j];
                    if (s1.charAt(i - 1) != s2.charAt(j - 1))
                        newValue = Math.min(Math.min(newValue + 1, lastValue + 1), costs[j - 1] + 1);
                    costs[j] = lastValue;
                    lastValue = newValue;
                }
            }
        }
        return costs[s2.length()];
    }

    // Method to suggest corrections for a given word
    public List<String> suggestCorrections(String word) {
        List<String> suggestions = new ArrayList<>();
        Map<String, Integer> distanceMap = new HashMap<>();

        for (String entry : dictionary) {
            int distance = getEditDistance(word.toLowerCase(), entry.toLowerCase());
            distanceMap.put(entry, distance);
        }

        // Sort by the smallest edit distance
        distanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(5) // Limit to 5 closest matches
                .forEach(entry -> suggestions.add(entry.getKey()));

        return suggestions;
    }
}
