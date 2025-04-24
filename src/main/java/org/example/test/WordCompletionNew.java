package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class WordCompletionNew {

    // AVL Node Definition
    class AVLNode {
        String word;
        double frequency;
        AVLNode left, right;
        int height;

        AVLNode(String word, double frequency) {
            this.word = word.toLowerCase();
            this.frequency = frequency;
            this.height = 1;
        }
    }

    private AVLNode root;

    // Insert word into the AVL Tree
    public void insert(String word, double frequency) {
        root = insert(root, word.toLowerCase(), frequency);
    }

    private AVLNode insert(AVLNode node, String word, double frequency) {
        if (node == null) return new AVLNode(word, frequency);
        int cmp = word.compareTo(node.word);
        if (cmp < 0) {
            node.left = insert(node.left, word, frequency);
        } else if (cmp > 0) {
            node.right = insert(node.right, word, frequency);
        } else {
            node.frequency = frequency;
        }
        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }

    private int height(AVLNode node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(AVLNode node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    private AVLNode balance(AVLNode node) {
        int balance = getBalance(node);
        if (balance > 1 && getBalance(node.left) >= 0) {
            return rotateRight(node);
        }
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        if (balance < -1 && getBalance(node.right) <= 0) {
            return rotateLeft(node);
        }
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        return node;
    }

    // Autocomplete method
    public List<String> autocomplete(String prefix, int k) {
        List<AVLNode> candidates = new ArrayList<>();
        findWordsWithPrefix(root, prefix.toLowerCase(), candidates);

        PriorityQueue<AVLNode> minHeap = new PriorityQueue<>((a, b) -> Double.compare(a.frequency, b.frequency));
        for (AVLNode node : candidates) {
            minHeap.offer(node);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        List<String> suggestions = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            suggestions.add(0, minHeap.poll().word);
        }
        return suggestions;
    }

    private void findWordsWithPrefix(AVLNode node, String prefix, List<AVLNode> result) {
        if (node == null) return;
        if (node.word.startsWith(prefix)) {
            result.add(node);
        }
        if (prefix.compareTo(node.word) < 0) {
            findWordsWithPrefix(node.left, prefix, result);
        } else {
            findWordsWithPrefix(node.right, prefix, result);
        }
    }

    // Load vocabularies using the `readExcel` method
    public void loadVocabularies(List<String> filePaths) {
        for (String filePath : filePaths) {
            try {
                List<String> words = readExcel(filePath);
                for (String word : words) {
                    insert(word, 1.0); // Default frequency of 1.0
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + filePath);
                e.printStackTrace();
            }
        }
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
                        // Split cell value into words if it contains spaces
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
}
