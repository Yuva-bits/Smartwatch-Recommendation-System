package org.example.Parsing.WebCrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The class `WebCrawlerAndAnalyzer` implements a web crawler to visit and analyze web pages,
 * extracting phone numbers, email addresses, and URLs from the pages.
 */
public class WebCrawlerAndAnalyzer {

    // These lines of code are declaring constants in the `WebCrawlerAndAnalyzer` class:
    private static final String BASE_URL = "https://www.apple.com/";
    private static final String HTML_DIRECTORY = "saved_pages";
    private static final String TEXT_DIRECTORY = "converted_text";
    private static final int MAX_PAGES = 100;

    // These lines of code in the `WebCrawlerAndAnalyzer` class are initializing two data structures:
    private Set<String> visitedPages = new HashSet<>();
    private Queue<String> pagesToVisit = new LinkedList<>();

    // These lines of code in the `WebCrawlerAndAnalyzer` class are declaring and initializing integer
    // variables to keep track of various counts during the web crawling and analysis process:
    private int totalFilesProcessed = 0;
    private int totalContacts = 0;
    private int totalEmails = 0;
    private int totalLinks = 0;

    // These lines of code in the `WebCrawlerAndAnalyzer` class are declaring and initializing three
    // `List` objects to store phone numbers, email addresses, and URLs found during the web crawling
    // and analysis process. Each list is initialized as an empty `ArrayList<String>`, which will be
    // used to store the respective data extracted from the text files generated from the crawled web
    // pages.
    private List<String> phoneNumbers = new ArrayList<>();
    private List<String> emailAddresses = new ArrayList<>();
    private List<String> urls = new ArrayList<>();

    /**
     * The main function creates an instance of WebCrawlerAndAnalyzer and calls its crawlAndAnalyze
     * method.
     */
    public static void main(String[] args) {
        WebCrawlerAndAnalyzer crawler = new WebCrawlerAndAnalyzer();
        crawler.crawlAndAnalyze();
    }

    /**
     * The `crawlAndAnalyze` function crawls a website, converts HTML to text, analyzes the text files,
     * and prints a summary.
     */
    public void crawlAndAnalyze() {
        crawl(BASE_URL);
        convertHtmlToText();
        analyzeTextFiles();
        printSummary();
    }

    /**
     * The `crawl` function in Java crawls web pages starting from a specified URL until a maximum
     * number of pages have been visited.
     *
     * @param startUrl The `startUrl` parameter is a String that represents the starting URL from which
     * the crawling process will begin. In the provided `crawl` method, the `startUrl` is added to a
     * queue of pages to visit (`pagesToVisit`) and then the method iterates through this queue,
     * visiting
     */
    private void crawl(String startUrl) {
        pagesToVisit.add(startUrl);
        while (!pagesToVisit.isEmpty() && visitedPages.size() < MAX_PAGES) {
            String url = pagesToVisit.poll();
            if (url != null && !visitedPages.contains(url)) {
                visitPage(url);
            }
        }
    }

    /**
     * The `visitPage` function in Java uses Jsoup to connect to a URL, extract links, save the HTML
     * content to a file, and add valid links to a list of pages to visit.
     *
     * @param url The `url` parameter in the `visitPage` method is a String that represents the URL of
     * the webpage to be visited and scraped for information.
     */
    private void visitPage(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println("Visiting: " + url);
            visitedPages.add(url);

            saveHtmlToFile(doc, url);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String linkHref = link.absUrl("href");
                if (linkHref.startsWith(BASE_URL) && !visitedPages.contains(linkHref)) {
                    pagesToVisit.add(linkHref);
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing: " + url + " - " + e.getMessage());
        }
    }

   /**
    * The function `saveHtmlToFile` saves the HTML content of a webpage to a file in a specified
    * directory.
    *
    * @param doc The `doc` parameter in the `saveHtmlToFile` method is of type `Document`. It is likely
    * referring to an HTML document that you want to save to a file. This document could be parsed
    * using a library like Jsoup, which provides a way to manipulate HTML documents.
    * @param url The `url` parameter in the `saveHtmlToFile` method is the URL of the web page from
    * which the HTML content is retrieved and saved to a file.
    */
    private void saveHtmlToFile(Document doc, String url) {
        try {
            File directory = new File(HTML_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdir();
            }

            String safeFileName = Paths.get(url.replace("https://", "").replaceAll("[^a-zA-Z0-9.-]", "_")).getFileName().toString();
            File file = new File(HTML_DIRECTORY + File.separator + safeFileName + ".html");

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(doc.html());
            }

            System.out.println("Saved page to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving file for URL: " + url + " - " + e.getMessage());
        }
    }

    /**
     * The function converts HTML files to text files in a specified directory.
     */
    private void convertHtmlToText() {
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

    /**
     * The function `convertHtmlToText` converts an HTML file to plain text and saves it as a .txt
     * file.
     *
     * @param htmlFile The `htmlFile` parameter in the `convertHtmlToText` method is a File object that
     * represents an HTML file that you want to convert to plain text. The method uses Jsoup library to
     * parse the HTML content and extract the text from it. The extracted text is then written to a new
     */
    private void convertHtmlToText(File htmlFile) {
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

    /**
     * The `analyzeTextFiles` method processes all text files in a specified directory by analyzing
     * each file and updating a counter for the total files processed.
     */
    private void analyzeTextFiles() {
        File textDir = new File(TEXT_DIRECTORY);
        File[] textFiles = textDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (textFiles != null) {
            for (File textFile : textFiles) {
                analyzeTextFile(textFile);
                totalFilesProcessed++;
            }
        }
    }

    /**
     * The `analyzeTextFile` function reads a text file, extracts its content, and searches for phone
     * numbers, email addresses, and URLs within the text.
     *
     * @param textFile The `textFile` parameter in the `analyzeTextFile` method represents the file
     * that contains the text to be analyzed. The method reads the content of this file line by line
     * and then searches for phone numbers, email addresses, and URLs within the text. If an error
     * occurs while reading the file
     */
    private void analyzeTextFile(File textFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String text = content.toString();

            searchPhoneNumbers(text);
            searchEmailAddresses(text);
            searchURLs(text);

        } catch (IOException e) {
            System.err.println("Error reading " + textFile.getName() + ": " + e.getMessage());
        }
    }


    /**
     * The `searchPhoneNumbers` function scans the provided text for phone numbers using a regular expression
     * and adds them to the phoneNumbers list, updating the totalContacts count for each match.
     *
     * @param text The text parameter contains the content to be analyzed for phone numbers.
     */
    private void searchPhoneNumbers(String text) {
        Pattern pattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String phoneNumber = matcher.group();
            phoneNumbers.add(phoneNumber);
            totalContacts++;
        }
    }

    /**
     * The `searchEmailAddresses` function searches the text for email addresses using a regex pattern,
     * then adds each found email to the emailAddresses list and updates the totalEmails count.
     *
     * @param text The text parameter contains the content to be analyzed for email addresses.
     */
    private void searchEmailAddresses(String text) {
        Pattern pattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String email = matcher.group();
            emailAddresses.add(email);
            totalEmails++;
        }
    }

    /**
     * The `searchURLs` function identifies URLs within the text using a regex pattern, adds each URL to
     * the urls list, and increments the totalLinks count for each match found.
     *
     * @param text The text parameter contains the content to be analyzed for URLs.
     */
    private void searchURLs(String text) {
        Pattern pattern = Pattern.compile("https?://\\S+\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String url = matcher.group();
            urls.add(url);
            totalLinks++;
        }
    }

    /**
     * The `printSummary` function outputs a summary of the analysis, including the total number of
     * files processed, contacts found, and lists of phone numbers, emails, and URLs collected.
     */
    private void printSummary() {
        System.out.println("\n--- Summary ---");
        System.out.println("Total number of files processed: " + totalFilesProcessed);
        System.out.println("Total number of Contacts: " + totalContacts);
        System.out.println("Total Emails: " + totalEmails);
        System.out.println("Total Links: " + totalLinks);

        System.out.println("\n--- Contact Numbers Found ---");
        for (int i = 0; i < phoneNumbers.size(); i++) {
            System.out.println("Number" + (i + 1) + ": " + phoneNumbers.get(i));
        }

        System.out.println("\n--- Email Addresses Found ---");
        for (int i = 0; i < emailAddresses.size(); i++) {
            System.out.println("Email" + (i + 1) + ": " + emailAddresses.get(i));
        }

        System.out.println("\n--- URLs Found ---");
        for (int i = 0; i < urls.size(); i++) {
            System.out.println("URL" + (i + 1) + ": " + urls.get(i));
        }
    }

}