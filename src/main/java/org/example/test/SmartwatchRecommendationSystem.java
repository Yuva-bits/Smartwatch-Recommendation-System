package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartwatchRecommendationSystem {
    private Workbook appleWorkbook;
    private Workbook gshockWorkbook;
    private Workbook garminWorkbook;
    private Workbook noiseWorkbook;
    private Workbook workbook;
    private VocabularyBuilder vocabularyBuilder;
    private SpellChecker spellChecker;
    private WordCompletionNew wordCompletion;
    private FrequencyCounter frequencyCounter;
    private Set<String> watchNames;

    public SmartwatchRecommendationSystem(String appleFilePath, String gshockFilePath, String garminFilePath, String noiseFilePath) throws IOException {
        FileInputStream appleFileInputStream = new FileInputStream(appleFilePath);
        this.appleWorkbook = new XSSFWorkbook(appleFileInputStream);
        FileInputStream gshockFileInputStream = new FileInputStream(gshockFilePath);
        this.gshockWorkbook = new XSSFWorkbook(gshockFileInputStream);
        FileInputStream garminFileInputStream = new FileInputStream(garminFilePath);
        this.garminWorkbook = new XSSFWorkbook(garminFileInputStream);
        FileInputStream noiseFileInputStream = new FileInputStream(noiseFilePath);
        this.noiseWorkbook = new XSSFWorkbook(noiseFileInputStream);


        // Initialize the workbook with one of the loaded workbooks
        this.workbook = this.appleWorkbook;

        this.vocabularyBuilder = new VocabularyBuilder(this.appleWorkbook, this.gshockWorkbook);
        this.spellChecker = new SpellChecker(vocabularyBuilder.getVocabulary());
        this.frequencyCounter = new FrequencyCounter();
        this.watchNames = new HashSet<>();
        collectWatchNames();

        this.spellChecker = new SpellChecker(watchNames);
    }


    private void collectWatchNames() {
        watchNames = new HashSet<>();
        for (int i = 0; i < appleWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(appleWorkbook.getSheetAt(i).getSheetName().toLowerCase());
        }
        for (int i = 0; i < gshockWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(gshockWorkbook.getSheetAt(i).getSheetName().toLowerCase());
        }
        for (int i = 0; i < garminWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(garminWorkbook.getSheetAt(i).getSheetName().toLowerCase());
        }
        for (int i = 0; i < noiseWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(noiseWorkbook.getSheetAt(i).getSheetName().toLowerCase());
        }
        System.out.println("Collected watch names: " + watchNames); // Debugging output
        System.out.println(" Toatl watches = " + watchNames.size());
    }

    private void addWatchNames(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            watchNames.add(workbook.getSheetAt(i).getSheetName().toLowerCase());
        }
    }

    public List<String> suggestCorrections(String word) {
        return spellChecker.suggestCorrections(word);
    }

    public void updateWordFrequency(String word) {
        frequencyCounter.addWordOccurrence(word);
    }

    public void updateSearchFrequency(String query) {
        frequencyCounter.addSearchQuery(query);
    }

    public int getWordFrequency(String word) {
        return frequencyCounter.getWordFrequency(word);
    }

    public int getSearchFrequency(String query) {
        return frequencyCounter.getSearchFrequency(query);
    }

    private String getValidWatchName(Scanner scanner) {
        while (true) {
            System.out.println("Enter the name of the watch (or 'quit' to exit):");
            String watchName = scanner.nextLine().trim().toLowerCase();

            if (watchName.equals("quit")) {
                return null;
            }

            if (watchNames.contains(watchName)) {
                return watchName;
            }

            List<String> suggestions = spellChecker.suggestCorrections(watchName);
            if (suggestions.isEmpty()) {
                System.out.println("No matching watches found. Please try again.");
            } else {
                System.out.println("Did you mean one of these?");
                for (int i = 0; i < suggestions.size(); i++) {
                    System.out.println((i + 1) + ". " + suggestions.get(i));
                }
                System.out.println("Enter the number of your choice, or 'no' to try again:");
                String response = scanner.nextLine().toLowerCase();
                if (response.equals("no")) {
                    continue;
                }
                try {
                    int choice = Integer.parseInt(response);
                    if (choice > 0 && choice <= suggestions.size()) {
                        return suggestions.get(choice - 1);
                    }
                } catch (NumberFormatException e) {
                    // Invalid input, will prompt again
                }
            }
        }
    }



    public void displaySearchFrequencies() {
        System.out.println("\nWatch Search Frequencies:");
        Map<String, Integer> frequencies = frequencyCounter.getSearchFrequencies();
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            System.out.printf("%s: %d time(s)%n", entry.getKey(), entry.getValue());
        }
    }

    public void recommendSmartwatch(Set<String> selectedFeatures) {
        List<String> recommendations = new ArrayList<>();

        recommendations.addAll(checkWorkbook(appleWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(gshockWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(garminWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(noiseWorkbook, selectedFeatures));

        // Display recommendations
        if (!recommendations.isEmpty()) {
            System.out.println("Recommended Models:");
            for (String model : recommendations) {
                System.out.println("- " + model);
            }
        } else {
            System.out.println("No matching smartwatches found.");
        }
    }

    private static class WatchRecommendation implements Comparable<WatchRecommendation> {
        private String brand;
        private String model;
        private double price;

        public WatchRecommendation(String brand, String model, double price) {
            this.brand = brand;
            this.model = model;
            this.price = price;
        }

        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public double getPrice() { return price; }

        @Override
        public int compareTo(WatchRecommendation other) {
            return Double.compare(this.price, other.price);
        }
    }

    public void recommendWatchesByPriceRange(double minPrice, double maxPrice) {
        List<WatchRecommendation> recommendations = new ArrayList<>();

        // Check Apple watches
        for (int i = 0; i < appleWorkbook.getNumberOfSheets(); i++) {
            Sheet sheet = appleWorkbook.getSheetAt(i);
            double price = extractPrice(sheet);
            if (price >= minPrice && price <= maxPrice) {
                recommendations.add(new WatchRecommendation("Apple", sheet.getSheetName(), price));
            }
        }

        // Check G-Shock watches
        for (int i = 0; i < gshockWorkbook.getNumberOfSheets(); i++) {
            Sheet sheet = gshockWorkbook.getSheetAt(i);
            double price = extractPrice(sheet);
            if (price >= minPrice && price <= maxPrice) {
                recommendations.add(new WatchRecommendation("G-Shock", sheet.getSheetName(), price));
            }
        }

        // Check Garmin watches
        for (int i = 0; i < garminWorkbook.getNumberOfSheets(); i++) {
            Sheet sheet = garminWorkbook.getSheetAt(i);
            double price = extractPrice(sheet);
            if (price >= minPrice && price <= maxPrice) {
                recommendations.add(new WatchRecommendation("Garmin", sheet.getSheetName(), price));
            }
        }

        // Check Noise watches
        for (int i = 0; i < noiseWorkbook.getNumberOfSheets(); i++) {
            Sheet sheet = noiseWorkbook.getSheetAt(i);
            double price = extractPrice(sheet);
            if (price >= minPrice && price <= maxPrice) {
                recommendations.add(new WatchRecommendation("Noise", sheet.getSheetName(), price));
            }
        }

        // Sort and display recommendations
        Collections.sort(recommendations);

        if(minPrice == 500)
            System.out.println("Recommended Watches in Price Range $" + minPrice + "+");
        else
            System.out.println("Recommended Watches in Price Range $" + minPrice + " - $" + maxPrice + ":");
        for (WatchRecommendation recommendation : recommendations) {
            System.out.printf("- %s ==> %s: $%.2f%n", recommendation.getBrand(), recommendation.getModel(), recommendation.getPrice());
        }

        if (recommendations.isEmpty()) {
            System.out.println("No watches found in this price range.");
        }
    }

    private double extractPrice(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().trim();
                    // Check for "$679" or "From $679" format
                    Matcher matcher = Pattern.compile("\\$\\d+(\\.\\d{2})?").matcher(cellValue);
                    if (matcher.find()) {
                        return Double.parseDouble(matcher.group().substring(1));
                    }
                    // Check for "$60" format
                    if (cellValue.startsWith("$")) {
                        try {
                            return Double.parseDouble(cellValue.substring(1));
                        } catch (NumberFormatException e) {
                            // Continue to next cell
                        }
                    }
                    // Check for "60" format (without dollar sign)
                    try {
                        return Double.parseDouble(cellValue);
                    } catch (NumberFormatException e) {
                        // Continue to next cell
                    }
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    // Handle numeric cell types
                    return cell.getNumericCellValue();
                }
            }
        }
        return -1; // Price not found
    }


    private List<String> checkWorkbook(Workbook workbook, Set<String> selectedFeatures) {
        List<String> recommendations = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String modelName = sheet.getSheetName();
            if (checkFeatures(sheet, selectedFeatures)) {
                recommendations.add(modelName);
            }
        }
        return recommendations;
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    public void displayGarminWatchDetails(String modelName) {
        Sheet sheet = garminWorkbook.getSheet(modelName);
        if (sheet == null) {
            System.out.println("Model not found.");
            return;
        }

        System.out.println("Details for " + modelName + ":");
        System.out.println("+--------------------------+--------------------------------------------------+");
        System.out.printf("| %-24s | %-48s |%n", "Feature", "Details");
        System.out.println("+--------------------------+--------------------------------------------------+");

        String currentHeading = "";
        for (Row row : sheet) {
            Cell headingCell = row.getCell(0);
            Cell detailCell = row.getCell(1);

            if (headingCell != null) {
                String heading = getCellValueAsString(headingCell).trim();
                if (!heading.isEmpty() && !heading.equals(currentHeading)) {
                    System.out.println("+--------------------------+--------------------------------------------------+");
                    System.out.printf("| %-73s |%n", heading);
                    System.out.println("+--------------------------+--------------------------------------------------+");
                    currentHeading = heading;
                }
            }

            if (detailCell != null) {
                String detail = getCellValueAsString(detailCell).trim();
                if (!detail.isEmpty()) {
                    String[] words = detail.split("\\s+");
                    StringBuilder line = new StringBuilder();
                    for (String word : words) {
                        if (line.length() + word.length() > 48) {
                            System.out.printf("| %-24s | %-48s |%n",
                                    line.length() == 0 ? getCellValueAsString(headingCell).trim() : "",
                                    line.toString().trim());
                            line = new StringBuilder();
                        }
                        line.append(word).append(" ");
                    }
                    if (line.length() > 0) {
                        System.out.printf("| %-24s | %-48s |%n",
                                line.length() == 0 ? getCellValueAsString(headingCell).trim() : "",
                                line.toString().trim());
                    }
                }
            }
        }
        System.out.println("+--------------------------+--------------------------------------------------+");
    }

    // Create a similar method for Noise workbook
    public void displayNoiseWatchDetails(String modelName) {
        Sheet sheet = noiseWorkbook.getSheet(modelName);
        if (sheet == null) {
            System.out.println("Model not found.");
            return;
        }

        System.out.println("Details for " + modelName + ":");
        System.out.println("+--------------------------+--------------------------------------------------+");
        System.out.printf("| %-24s | %-48s |%n", "Feature", "Details");
        System.out.println("+--------------------------+--------------------------------------------------+");

        String currentHeading = "";
        for (Row row : sheet) {
            Cell headingCell = row.getCell(0);
            Cell detailCell = row.getCell(1);

            if (headingCell != null) {
                String heading = getCellValueAsString(headingCell).trim();
                if (!heading.isEmpty() && !heading.equals(currentHeading)) {
                    System.out.println("+--------------------------+--------------------------------------------------+");
                    System.out.printf("| %-73s |%n", heading);
                    System.out.println("+--------------------------+--------------------------------------------------+");
                    currentHeading = heading;
                }
            }

            if (detailCell != null) {
                String detail = getCellValueAsString(detailCell).trim();
                if (!detail.isEmpty()) {
                    String[] words = detail.split("\\s+");
                    StringBuilder line = new StringBuilder();
                    for (String word : words) {
                        if (line.length() + word.length() > 48) {
                            System.out.printf("| %-24s | %-48s |%n",
                                    line.length() == 0 ? getCellValueAsString(headingCell).trim() : "",
                                    line.toString().trim());
                            line = new StringBuilder();
                        }
                        line.append(word).append(" ");
                    }
                    if (line.length() > 0) {
                        System.out.printf("| %-24s | %-48s |%n",
                                line.length() == 0 ? getCellValueAsString(headingCell).trim() : "",
                                line.toString().trim());
                    }
                }
            }
        }
        System.out.println("+--------------------------+--------------------------------------------------+");
    }

    private boolean checkGShockFeatures(Sheet sheet, Set<String> selectedFeatures) {
        if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
            return false; // Sheet is null or doesn't have enough rows
        }

        Cell featuresCell = sheet.getRow(1).getCell(2); // C2 cell
        if (featuresCell == null) {
            return false; // Features cell doesn't exist
        }

        String features = featuresCell.getStringCellValue().toLowerCase();
        for (String feature : selectedFeatures) {
            if (!features.contains(feature.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public void displayGShockWatchDetails(String modelName) {
        Sheet sheet = gshockWorkbook.getSheet(modelName);
        if (sheet == null) {
            System.out.println("Model not found.");
            return;
        }

        System.out.println("Details for " + modelName + ":");
        System.out.println("+------------------------+--------------------------------------------------+");
        System.out.printf("| %-22s | %-48s |%n", "Feature", "Details");
        System.out.println("+------------------------+--------------------------------------------------+");

        // Display model name
        Row nameRow = sheet.getRow(0);
        if (nameRow != null && nameRow.getCell(1) != null) {
            String originalName = nameRow.getCell(1).getStringCellValue();
            System.out.printf("| %-22s | %-48s |%n", "Model Name", originalName);
        }

        // Display price
        Row priceRow = sheet.getRow(1);
        if (priceRow != null && priceRow.getCell(1) != null) {
            String price = priceRow.getCell(1).getStringCellValue();
            System.out.printf("| %-22s | %-48s |%n", "Price", price);
        }

        // Display features
        Row featuresRow = sheet.getRow(1); // Assuming features are in the second row
        if (featuresRow != null && featuresRow.getCell(2) != null) {
            String features = featuresRow.getCell(2).getStringCellValue();
            String[] featureList = features.split(";"); // Assuming features are separated by semicolons
            System.out.printf("| %-22s | %-48s |%n", "Features", featureList[0].trim());
            for (int i = 1; i < featureList.length; i++) {
                System.out.printf("| %-22s | %-48s |%n", "", featureList[i].trim());
            }
        }

        System.out.println("+------------------------+--------------------------------------------------+");
    }

    private boolean checkFeatures(Sheet sheet, Set<String> selectedFeatures) {
        for (String feature : selectedFeatures) {
            boolean found = false;
            for (Row row : sheet) {
                for (Cell cell : row) {
                    String cellValue = "";
                    switch (cell.getCellType()) {
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            cellValue = String.valueOf(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            cellValue = String.valueOf(cell.getBooleanCellValue());
                            break;
                        default:
                            continue;
                    }
                    if (cellValue.toLowerCase().contains(feature.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    public void displayWatchDetails(String modelName) {
        Sheet sheet = appleWorkbook.getSheet(modelName);
        if (sheet == null) {
            System.out.println("Model not found.");
            return;
        }

        System.out.println("Details for " + modelName + ":");
        System.out.println("+--------------------------+--------------------------------------------------+");
        System.out.printf("| %-24s | %-48s |%n", "Feature", "Details");
        System.out.println("+--------------------------+--------------------------------------------------+");

        String currentHeading = "";
        for (Row row : sheet) {
            Cell headingCell = row.getCell(0);
            Cell detailCell = row.getCell(1);

            if (headingCell != null) {
                String heading = headingCell.getStringCellValue().trim();
                if (!heading.isEmpty() && !heading.equals(currentHeading)) {
                    System.out.println("+--------------------------+--------------------------------------------------+");
                    System.out.printf("| %-73s |%n", heading);
                    System.out.println("+--------------------------+--------------------------------------------------+");
                    currentHeading = heading;
                }
            }

            if (detailCell != null) {
                String detail = detailCell.getStringCellValue().trim();
                if (!detail.isEmpty()) {
                    String[] words = detail.split("\\s+");
                    StringBuilder line = new StringBuilder();
                    for (String word : words) {
                        if (line.length() + word.length() > 48) {
                            System.out.printf("| %-24s | %-48s |%n",
                                    line.length() == 0 ? headingCell.getStringCellValue().trim() : "",
                                    line.toString().trim());
                            line = new StringBuilder();
                        }
                        line.append(word).append(" ");
                    }
                    if (line.length() > 0) {
                        System.out.printf("| %-24s | %-48s |%n",
                                line.length() == 0 ? headingCell.getStringCellValue().trim() : "",
                                line.toString().trim());
                    }
                }
            }
        }
        System.out.println("+--------------------------+--------------------------------------------------+");
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            SmartwatchRecommendationSystem system = new SmartwatchRecommendationSystem(
                    "AppleWatchComparison.xlsx", "GShockSmartwatchDetails.xlsx",
                    "garmin_models.xlsx", "noise.xlsx");

            System.out.println("How would you like to find a smartwatch?");
            System.out.println("1. Recommend by Features");
            System.out.println("2. Recommend by Price Range");
            System.out.println("3. Exit");

            int recommendationType = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (recommendationType) {
                case 1:
                    // Define available features with numbers
                    String[] availableFeatures = {"One Time", "OLED Always-on Retina display", "OLED Retina display", "ECG",
                            "Up to 18 hours", "Up to 36 hours", "Up to 72 hours in Low Power Mode",
                            "High and low heart rate notifications", "Blood Oxygen app", "Sleep Tracking", "GPS",
                            "Swimproof", "Fast charging", "Cycle Tracking", "Bluetooth", "Offline maps",
                            "aluminum", "titanium", "Mindfulness app with state of mind tracking",
                            "Water-resistant", "Apple Pay", "Mineral Glass", "Shock Resistant", "REALTIME STAMINA", "Do not disturb mode"};

                    System.out.println("Select features by entering the corresponding numbers, separated by commas:");
                    for (int i = 0; i < availableFeatures.length; i++) {
                        System.out.printf("%d: %s%n", i + 1, availableFeatures[i]);
                    }

                    String input = scanner.nextLine();
                    Set<String> selectedFeatures = new HashSet<>();

                    // Parse user input
                    String[] featureNumbers = input.split(",");
                    for (String number : featureNumbers) {
                        int index = Integer.parseInt(number.trim()) - 1;
                        if (index >= 0 && index < availableFeatures.length) {
                            selectedFeatures.add(availableFeatures[index]);
                        }
                    }



                    system.recommendSmartwatch(selectedFeatures);
                    break;
                case 2:
                    // Price-based recommendation logic
                    System.out.println("Select a price range:");
                    System.out.println("1. $0 - $100");
                    System.out.println("2. $100 - $200");
                    System.out.println("3. $200 - $500");
                    System.out.println("4. $500+");

                    int priceChoice = scanner.nextInt();

                    double minPrice, maxPrice;
                    switch (priceChoice) {
                        case 1: minPrice = 0; maxPrice = 100; break;
                        case 2: minPrice = 100; maxPrice = 200; break;
                        case 3: minPrice = 200; maxPrice = 500; break;
                        case 4: minPrice = 500; maxPrice = Double.MAX_VALUE; break;
                        default:
                            System.out.println("Invalid choice.");
                            return;
                    }

                    system.recommendWatchesByPriceRange(minPrice, maxPrice);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }

            while (true) {
                scanner.nextLine();
                System.out.println("Do you want a detailed review of any watch? Enter 'yes' or 'no':");
                String response = scanner.nextLine().toLowerCase();
                if (!response.equals("yes") && !response.equals("no")) {
                    System.out.println("Please enter a valid response");
                    System.out.println("Do you want a detailed review of any watch? Enter 'yes' or 'no':");
                    response = scanner.nextLine().toLowerCase();
                }
                else if (response.startsWith("n")) {
                    break;
                }

                System.out.println("Enter 'apple', 'gshock', 'garmin', or 'noise' for the watch type:");
                String watchType = scanner.nextLine().toLowerCase();
                String watchName = system.getValidWatchName(scanner);

                if (watchName == null) {
                    break; // User chose to quit
                }

                switch (watchType) {
                    case "apple":
                        system.displayWatchDetails(watchName);
                        break;
                    case "gshock":
                        system.displayGShockWatchDetails(watchName);
                        break;
                    case "garmin":
                        system.displayGarminWatchDetails(watchName);
                        break;
                    case "noise":
                        system.displayNoiseWatchDetails(watchName);
                        break;
                    default:
                        System.out.println("Invalid watch type.");
                }

                system.updateSearchFrequency(watchName);
            }

            system.displaySearchFrequencies();
        } catch (IOException e) {
            System.out.println("Error reading Excel file: " + e.getMessage());
        }
    }
}