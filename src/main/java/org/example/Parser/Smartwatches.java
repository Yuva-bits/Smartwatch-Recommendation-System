package org.example.Parser;

import java.io.IOException;
import java.util.Scanner;

public class Smartwatches {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println(" _____                                                                                             _____ \n" +
                "( ___ )-------------------------------------------------------------------------------------------( ___ )\n" +
                " |   |                                                                                             |   | \n" +
                " |   | ███████╗███╗   ███╗ █████╗ ██████╗ ████████╗    ██╗    ██╗ █████╗ ████████╗ ██████╗██╗  ██╗ |   | \n" +
                " |   | ██╔════╝████╗ ████║██╔══██╗██╔══██╗╚══██╔══╝    ██║    ██║██╔══██╗╚══██╔══╝██╔════╝██║  ██║ |   | \n" +
                " |   | ███████╗██╔████╔██║███████║██████╔╝   ██║       ██║ █╗ ██║███████║   ██║   ██║     ███████║ |   | \n" +
                " |   | ╚════██║██║╚██╔╝██║██╔══██║██╔══██╗   ██║       ██║███╗██║██╔══██║   ██║   ██║     ██╔══██║ |   | \n" +
                " |   | ███████║██║ ╚═╝ ██║██║  ██║██║  ██║   ██║       ╚███╔███╔╝██║  ██║   ██║   ╚██████╗██║  ██║ |   | \n" +
                " |   | ╚══════╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝        ╚══╝╚══╝ ╚═╝  ╚═╝   ╚═╝    ╚═════╝╚═╝  ╚═╝ |   | \n" +
                " |___|                                                                                             |___| \n" +
                "(_____)-------------------------------------------------------------------------------------------(_____)");

        HTMLParser.HTMLScrapper();
        System.out.println("--------------------------------");
        System.out.println("            Page Ranking        ");
        System.out.println("--------------------------------");

        System.out.print("Enter The words to find in the file: ");
        String[] keywords = scanner.nextLine().split(",");
//        PageRanking.ProcessPageRanking(keywords, outputArea);
        System.out.println("--------------------------------");
        System.out.println("        Inverted Indexing       ");
        System.out.println("--------------------------------");
        System.out.print("Enter a word to find its occurrence in file: ");
        String searchWord = scanner.nextLine().toLowerCase();


//        InvertedIndex.ProcessInveretedIndex(searchWord);

        RegexValidator.processRegexValidator();
    }
}