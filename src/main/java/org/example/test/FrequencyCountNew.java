package org.example.test;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FrequencyCountNew {

    private static Map<String, Integer> wordFrequencyMap = new HashMap<>();

    // Initialize the word frequency map from Excel files
    public static void initializeFrequencies(String[] filePaths) throws IOException {
        for (String filePath : filePaths) {
            List<String> words = readExcel(filePath);
            for (String word : words) {
                wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1);
            }
        }
    }

    // Query the frequency of a given word
    public static String queryFrequency(String word) {
        int count = wordFrequencyMap.getOrDefault(word.toLowerCase(), 0);
        return count > 0
                ? "The word '" + word + "' appears " + count + " times."
                : "The word '" + word + "' does not appear in the dataset.";
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
