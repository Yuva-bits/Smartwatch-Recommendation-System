package org.example.Parsing;

import java.io.*;
import java.util.regex.*;

public class TextAnalyzer {

    private static final String TEXT_DIRECTORY = "converted_text";

    public static void main(String[] args) {
        analyzeAllTextFiles();
    }

    public static void analyzeAllTextFiles() {
        File textDir = new File(TEXT_DIRECTORY);
        File[] textFiles = textDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (textFiles != null) {
            for (File textFile : textFiles) {
                analyzeTextFile(textFile);
            }
        }
    }

    private static void analyzeTextFile(File textFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String text = content.toString();

            System.out.println("Analyzing " + textFile.getName() + ":");
            searchPhoneNumbers(text);
            searchEmailAddresses(text);
            searchURLs(text);
            System.out.println();

        } catch (IOException e) {
            System.err.println("Error reading " + textFile.getName() + ": " + e.getMessage());
        }
    }

    private static void searchPhoneNumbers(String text) {
        Pattern pattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        Matcher matcher = pattern.matcher(text);

        System.out.println("Phone numbers found:");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    private static void searchEmailAddresses(String text) {
        Pattern pattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher matcher = pattern.matcher(text);

        System.out.println("Email addresses found:");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    private static void searchURLs(String text) {
        Pattern pattern = Pattern.compile("https?://\\S+\\b");
        Matcher matcher = pattern.matcher(text);

        System.out.println("URLs found:");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}