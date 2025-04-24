package org.example.test;

import java.util.HashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;

public class FrequencyCounter {
    private Map<String, Integer> wordFrequency;
    private Map<String, Integer> searchFrequency;

    public FrequencyCounter() {
        this.wordFrequency = new HashMap<>();
        this.searchFrequency = new HashMap<>();
    }

    public void addWordOccurrence(String word) {
        try {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        } catch (Exception e) {
            System.err.println("Error adding word occurrence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addSearchQuery(String query) {
        try {
            searchFrequency.put(query, searchFrequency.getOrDefault(query, 0) + 1);
        } catch (Exception e) {
            System.err.println("Error adding search query: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getWordFrequency(String word) {
        try {
            return wordFrequency.getOrDefault(word, 0);
        } catch (Exception e) {
            System.err.println("Error retrieving word frequency: " + e.getMessage());
            e.printStackTrace();
            return 0; // Return 0 if an error occurs
        }
    }

    public int getSearchFrequency(String query) {
        try {
            return searchFrequency.getOrDefault(query, 0);
        } catch (Exception e) {
            System.err.println("Error retrieving search frequency: " + e.getMessage());
            e.printStackTrace();
            return 0; // Return 0 if an error occurs
        }
    }

    public Map<String, Integer> getSearchFrequencies() {
        try {
            return new HashMap<>(searchFrequency);
        } catch (Exception e) {
            System.err.println("Error retrieving search frequencies: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>(); // Return an empty map if an error occurs
        }
    }
}