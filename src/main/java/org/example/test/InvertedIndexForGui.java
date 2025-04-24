//package org.example.test;
//
//import javax.swing.*;
//import java.io.*;
//import java.util.*;
//
//class TrieNode {
//    Map<Character, TrieNode> children; // Child nodes for characters
//    Map<String, Integer> documentPositions; // Maps document names to the first occurrence index
//
//    public TrieNode() {
//        children = new HashMap<>();
//        documentPositions = new HashMap<>();
//    }
//}
//
//class InvertedIndexTrieForGui {
//    private final TrieNode root;
//
//    public InvertedIndexTrieForGui() {
//        root = new TrieNode();
//    }
//
//    // Insert a word into the Trie with its associated document and position
//    public void insert(String word, String documentName, int position) {
//        TrieNode current = root;
//        for (char c : word.toCharArray()) {
//            current.children.putIfAbsent(c, new TrieNode());
//            current = current.children.get(c);
//        }
//        // Only store the first occurrence index if not already present
//        current.documentPositions.putIfAbsent(documentName, position);
//    }
//
//    // Search for a word in the Trie and return the documents containing the word with positions
//    public Map<String, Integer> search(String word) {
//        TrieNode current = root;
//        for (char c : word.toCharArray()) {
//            current = current.children.get(c);
//            if (current == null) {
//                return Collections.emptyMap(); // Word not found
//            }
//        }
//        return current.documentPositions; // Return the document positions
//    }
//}
//
//public class InvertedIndex {
//
//    public static void ProcessInvertedIndex(String searchWord, JTextArea outputArea) throws IOException {
//        // Directory containing documents
//        File directory = new File("text_pages");
//
//        // Create an inverted index Trie
//        InvertedIndexTrieForGui invertedIndex = new InvertedIndexTrieForGui();
//
//        // Build the inverted index by parsing all documents
//        if (directory.isDirectory()) {
//            for (File file : Objects.requireNonNull(directory.listFiles())) {
//                if (file.isFile()) {
//                    buildIndexFromFile(file, invertedIndex);
//                }
//            }
//        }
//
//        // Search for the word in the inverted index
//        Map<String, Integer> result = invertedIndex.search(searchWord.toLowerCase());
//
//        // Display the results in the JTextArea
//        outputArea.setText(""); // Clear previous results
//        if (result.isEmpty()) {
//            outputArea.append("The word '" + searchWord + "' was not found in any document.\n");
//        } else {
//            outputArea.append("The word '" + searchWord + "' is found in the following documents:\n");
//            for (Map.Entry<String, Integer> entry : result.entrySet()) {
//                outputArea.append("- " + entry.getKey() + ": First occurrence at index " + entry.getValue() + "\n");
//            }
//        }
//    }
//
//    private static void buildIndexFromFile(File file, InvertedIndexTrie invertedIndex) throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        String line;
//        int index = 0; // Track word index in the file
//
//        while ((line = reader.readLine()) != null) {
//            String[] words = line.split("\\W+");
//            for (String word : words) {
//                if (!word.isEmpty()) {
//                    invertedIndex.insert(word.toLowerCase(), file.getName(), index);
//                }
//                index++;
//            }
//        }
//        reader.close();
//    }
//}
