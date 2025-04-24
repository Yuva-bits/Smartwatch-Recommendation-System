package org.example.Parser;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexValidator {

    public static String readingContentFromFile(String filePath) throws IOException {
        StringBuilder fileContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContents.append(line).append("\n");
            }
        }
        return fileContents.toString();
    }

    public static void printingResults(String patternType, List<String> matches) {
        System.out.println("----- " + patternType + " -----");
        for (int i = 0; i < matches.size(); i++) {
            System.out.println((i + 1) + ". " + matches.get(i));
        }
        System.out.println();
    }

    public static List<String> extractURLs(String text) {
        String patternForUrls = "\\b(https?://|www\\.)\\S+\\b";
        return identifyPatterns(text, patternForUrls);
    }

    public static List<String> extractingPhoneNumbers(String text) {
        String patternForNumbers = "\\b(\\+\\d{1,3})?\\s?\\d{10,15}\\b";
        return identifyPatterns(text, patternForNumbers);
    }

    public static List<String> extractingEmail(String text) {
        String patternForEmail = "\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b";
        return identifyPatterns(text, patternForEmail);
    }

    public static List<String> extractingDates(String text) {
        String patternForDates = "\\b(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])\\b";
        return identifyPatterns(text, patternForDates);
    }

    public static List<String> extractPrices(String text) {
        String patternForPrices = "\\b(\\$\\d+(\\.\\d{1,2})?|\\d+(\\.\\d{1,2})?\\s?(USD|CAD|EUR|INR)|[A-Z]{3}\\s\\d+(\\.\\d{1,2})?)\\b";
        return identifyPatterns(text, patternForPrices);
    }

    private static List<String> identifyPatterns(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        List<String> foundMatches = new ArrayList<>();

        while (matcher.find()) {
            foundMatches.add(matcher.group());
        }
        return foundMatches;
    }

    public static void processRegexValidator() {
        //String directoryPath = "D:\\Fall 2024\\8547-ACC\\Assignments\\4\\4\\src\\TextFiles"; // Update this path to your folder containing .txt files

        System.out.println("Displaying Patterns found using regex in TXT files");
        File directory = new File("text_pages");
        if (!directory.isDirectory()) {
            System.err.println("The specified path is not a directory. Please provide a valid directory.");
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt")); // Filter only .txt files
        if (files == null || files.length == 0) {
            System.out.println("No .txt files found in the specified directory.");
            return;
        }

        for (File file : files) {
            try {
                String content = readingContentFromFile(file.getPath());

                List<String> urls = extractURLs(content);
                List<String> phoneNumbers = extractingPhoneNumbers(content);
                List<String> emails = extractingEmail(content);
                List<String> dates = extractingDates(content);
                //List<String> prices = extractPrices(content);

                if (!urls.isEmpty() || !phoneNumbers.isEmpty() || !emails.isEmpty() || !dates.isEmpty()
                      //  || !prices.isEmpty()
                ) {
                    //System.out.println("Processing file: " + file.getName());

                    if (!urls.isEmpty()) {
                        printingResults("Extracted URLs from the file:", urls);
                    }
                    if (!phoneNumbers.isEmpty()) {
                        printingResults("Extracted Phone Numbers from the file:", phoneNumbers);
                    }
                    if (!emails.isEmpty()) {
                        printingResults("Extracted Email Addresses from the file:", emails);
                    }
                    if (!dates.isEmpty()) {
                        printingResults("Extracted Dates from the file:", dates);
                    }
//                    if (!prices.isEmpty()) {
//                        printingResults("Extracted Prices from the file:", prices);
//                    }

                    System.out.println("-------------------------------------------------\n");
                }

            } catch (IOException e) {
                System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
