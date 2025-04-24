package org.example.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class HTMLParser {
    public static void HTMLScrapper() {
        // List of websites to crawl
        String[] websites = {
                "https://www.gshock.com", // URL
                "https://www.garmin.com",
                "https://www.noisefit.com",
                "https://www.apple.com/watch"
        };

        String outputDirectory = "smartwatches_HTML"; // Directory to save HTML files
        createDirectory(outputDirectory);

        for (String website : websites) {
            try {
                System.out.println("Crawling Has started for the website: " + website);

                // Fetch the homepage of the website
                Document document = Jsoup.connect(website).get();

                // Identify smartwatch links (this might need fine-tuning for each site)
                Elements watchLinks = document.select("a[href]"); // Adjust based on website structure
                int counter = 1;

                for (Element link : watchLinks) {
                    String watchUrl = link.absUrl("href");

                    // Skip irrelevant links (you can add more conditions if needed)
                    if (!watchUrl.contains("watch") && !watchUrl.contains("smartwatch")) continue;

                    //System.out.println("Fetching watch details from: " + watchUrl);
                    try {
                        Document watchPage = Jsoup.connect(watchUrl).get();
                        String NameOftheFile = Paths.get(watchUrl.replace("https://", "").replaceAll("[^a-zA-Z0-9.-]", "_")).getFileName().toString();

                        saveHtmlFile(outputDirectory, NameOftheFile + counter + ".html", watchPage.outerHtml());
                        counter++;
                    } catch (IOException e) {
                        System.out.println("Failed to fetch the details from: " + watchUrl);
                    }
                }
                HTML_To_Text.HTMLtoTextConversion();
            } catch (IOException e) {
                System.out.println("There is a error in accessing a website: " + website);
            }
        }
    }

    // Create a directory to store the HTML files
    private static void createDirectory(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            boolean isCreated = directory.mkdir();
            if (isCreated) {
                System.out.println("Created directory in name: " + directoryName);
            } else {
                System.out.println("Failed to create directory: " + directoryName);
            }
        }
    }

    // Save HTML content to a file
    private static void saveHtmlFile(String directory, String fileName, String htmlContent) {
        File file = new File(directory + File.separator + fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(htmlContent);
            //System.out.println("Saved file: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("There is a error in saving a file: " + file.getName());
        }
    }
}
