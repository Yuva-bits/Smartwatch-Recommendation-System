package org.example.Parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;

public class HtmltoTextConverter {

    private static final String HTML_DIRECTORY = "saved_pages";
    private static final String TEXT_DIRECTORY = "converted_text";

    public static void main(String[] args) {
        convertAllHtmlFiles();
    }

    public static void convertAllHtmlFiles() {
        File htmlDir = new File(HTML_DIRECTORY);
        File textDir = new File(TEXT_DIRECTORY);

        if (!textDir.exists()) {
            textDir.mkdir();
        }

        File[] htmlFiles = htmlDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));

        if (htmlFiles != null) {
            for (File htmlFile : htmlFiles) {
                convertHtmlToText(htmlFile);
            }
        }
    }

    private static void convertHtmlToText(File htmlFile) {
        try {
            Document doc = Jsoup.parse(htmlFile, "UTF-8");
            String text = doc.text();

            String textFileName = htmlFile.getName().replaceFirst("[.][^.]+$", "") + ".txt";
            File textFile = new File(TEXT_DIRECTORY, textFileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
                writer.write(text);
            }

            System.out.println("Converted " + htmlFile.getName() + " to " + textFile.getName());
        } catch (IOException e) {
            System.err.println("Error converting " + htmlFile.getName() + ": " + e.getMessage());
        }
    }
}