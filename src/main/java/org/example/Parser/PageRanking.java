package org.example.Parser;
import javax.swing.*;
import java.io.*;
import java.util.*;
public class PageRanking
{
    // Parse a page to extract keyword frequencies
    public static Map<String, Integer> parsePage(File file) throws IOException {
        Map<String, Integer> frequencyMap = new TreeMap<>(); // TreeMap acts as a self-balancing tree
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] words = line.split("\\W+");
            for (String word : words) {
                word = word.toLowerCase();
                if (!word.isEmpty()) {
                    frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                }
            }
        }
        reader.close();
        return frequencyMap;
    }

    // Sort keywords by frequency using Heap Sort
    public static List<Map.Entry<String, Integer>> sortKeywordsByFrequency(Map<String, Integer> frequencyMap) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(frequencyMap.entrySet());

        // Custom comparator for max heap
        PriorityQueue<Map.Entry<String, Integer>> heap = new PriorityQueue<>(
                (a, b) -> b.getValue() - a.getValue()
        );

        heap.addAll(entries);
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>();
        while (!heap.isEmpty()) {
            sortedList.add(heap.poll());
        }
        return sortedList;
    }

    // Calculate page rank based on search keywords
    public static int calculatePageRank(Map<String, Integer> frequencyMap, List<String> searchKeywords) {
        int rank = 0;
        for (String keyword : searchKeywords) {
            rank += frequencyMap.getOrDefault(keyword.toLowerCase(), 0);
        }
        return rank;
    }

    // Use a max-heap to rank pages
    public static List<Map.Entry<String, Integer>> rankPages(File directory, List<String> searchKeywords) throws IOException {
        File[] files = directory.listFiles();
        PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>(
                (a, b) -> b.getValue() - a.getValue()
        );

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Map<String, Integer> frequencyMap = parsePage(file);
                    int rank = calculatePageRank(frequencyMap, searchKeywords);
                    maxHeap.offer(new AbstractMap.SimpleEntry<>(file.getName(), rank));
                }
            }
        }

        List<Map.Entry<String, Integer>> rankedPages = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            rankedPages.add(maxHeap.poll());
        }
        return rankedPages;
    }

    public static void ProcessPageRanking(String[] wordtosearch, JTextArea outputArea) throws IOException {
        // Directory containing web pages
        File directory = new File("text_pages");

        // User inputs search keywords
        //Scanner scanner = new Scanner(System.in);
        //System.out.print("Enter search keywords (comma-separated): ");
        //String[] keywords = scanner.nextLine().split(",");
        List<String> searchKeywords = new ArrayList<>();
        for (String keyword : wordtosearch) {
            searchKeywords.add(keyword.trim());
        }

        // Rank pages based on keywords
        List<Map.Entry<String, Integer>> rankedPages = rankPages(directory, searchKeywords);

        // Display results
        System.out.println("\nPage Rankings:");
        for (Map.Entry<String, Integer> entry : rankedPages) {
            System.out.println(entry.getKey() + ": Rank = " + entry.getValue());
        }
    }
}
