package org.example.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HTML_To_Text {
    // Directory containing HTML files
    private static final String HtmlFileDirectoryname = "smartwatches_HTML";
    private static final String TextfileDirectoryname = "text_pages";

    public static void HTMLtoTextConversion() {
        // Create the text directory if it doesn't exist
        File _textDir = new File(TextfileDirectoryname);
        if (!_textDir.exists()) {
            _textDir.mkdir();
        }

        // Get all HTML files from the saved_pages directory
        File html_Directory = new File(HtmlFileDirectoryname);
        File[] _Html_Files = html_Directory.listFiles((dir, name) -> name.endsWith(".html"));

        if (_Html_Files == null || _Html_Files.length == 0) {
            System.out.println("There was no HTML files found in the " + HtmlFileDirectoryname);
            return;
        }

        // Process each HTML file
        for (File htmlFile : _Html_Files) {
            try {
                convertHtmlToText(htmlFile);
            } catch (IOException e) {
                System.err.println("There is a Error converting the HTML file: " + htmlFile.getName() + " - " + e.getMessage());
            }
        }
    }
    private static void convertHtmlToText(File htmlFile) throws IOException {
        // Parse the HTML file and extract the text
        Document doc = Jsoup.parse(htmlFile, "UTF-8");
        String textContent = doc.text();

        // Define the output text file with the same name as the HTML file
        String outputFileName = htmlFile.getName().replace(".html", ".txt");
        File textFile = new File(TextfileDirectoryname + File.separator + outputFileName);

        // Write the text content to the output file
        try (FileWriter writer = new FileWriter(textFile)) {
            writer.write(textContent);
            //System.out.println("The following Html file is Converted " + htmlFile.getName() + " to " + textFile.getAbsolutePath());
        }

    }

}


