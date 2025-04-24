package org.example.testUI;

import java.util.HashMap;
import java.util.Map;

public class FrequencyCounter {
    private Map<String, Integer> wordFrequencies;
    private Map<String, Integer> searchFrequencies;

    public FrequencyCounter() {
        wordFrequencies = new HashMap<>();
        searchFrequencies = new HashMap<>();
    }

    public void addWordOccurrence(String word) {
        wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
    }

    public void addSearchQuery(String query) {
        searchFrequencies.put(query, searchFrequencies.getOrDefault(query, 0) + 1);
    }

    public int getWordFrequency(String word) {
        return wordFrequencies.getOrDefault(word, 0);
    }

    public int getSearchFrequency(String query) {
        return searchFrequencies.getOrDefault(query, 0);
    }

    public Map<String, Integer> getSearchFrequencies() {
        return new HashMap<>(searchFrequencies);
    }


}