package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.FileOutputStream;
import java.time.Duration;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class AppleWatchComparator {
    private WebDriver driver;
    private Map<String, Integer> searchFrequency;
    private Workbook workbook;

    public AppleWatchComparator() {
        this.driver = new ChromeDriver();
        this.searchFrequency = new TreeMap<>();
        this.workbook = new XSSFWorkbook();
    }

    public void searchAndCompare(String watchName) {
        try {
            driver.get("https://www.apple.com/ca/watch/compare/");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            Select select = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selector-0"))));
            selectOptionContaining(select, watchName);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".compare-table")));

            String priceInfo = extractPriceInfo();
            Map<String, List<String>> comparisonData = extractComparisonData(driver);

            updateSearchFrequency(watchName);

            // Print results to console
            System.out.println("Watch: " + watchName);
            System.out.println(priceInfo);
            System.out.println(comparisonData);



            // Assuming you have extracted data into a Map
            comparisonData = extractComparisonData(driver);

            // Now save to Excel
            saveToExcel(watchName, priceInfo, comparisonData);

        } catch (Exception e) {
            System.out.println("Error occurred while searching: " + e.getMessage());
        }
    }

    private void selectOptionContaining(Select select, String text) {
        for (WebElement option : select.getOptions()) {
            if (option.getText().toLowerCase().contains(text.toLowerCase())) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }
        System.out.println("No matching option found for: " + text);
    }


    private String extractPriceInfo() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".first-child.compare-column.template-price")));

        StringBuilder priceInfo = new StringBuilder();
        priceInfo.append("Price Information:\n");
        priceInfo.append("-".repeat(75)).append("\n");

        if (priceElement.findElements(By.cssSelector(".compare-column-price-group")).isEmpty()) {
            String notAvailable = priceElement.findElement(By.cssSelector(".visuallyhidden")).getText();
            priceInfo.append(String.format("%-20s | %-50s%n", "Price", notAvailable));
        } else {
            WebElement priceGroup = priceElement.findElement(By.cssSelector(".compare-column-price-group"));
            List<WebElement> priceElements = priceGroup.findElements(By.cssSelector(".template-item-default, .template-dynamic-price"));

            for (int i = 0; i < priceElements.size(); i += 2) {
                WebElement typeElement = priceElements.get(i);
                WebElement priceValueElement = priceElements.get(i + 1);

                String type = typeElement.findElement(By.cssSelector("span")).getText();
                String price = priceValueElement.findElement(By.cssSelector("span")).getText();

                priceInfo.append(String.format("%-20s | %-50s%n", type, price));
            }
        }

        return priceInfo.toString();
    }

//    private String extractComparisonData(WebDriver driver) {
//        StringBuilder data = new StringBuilder();
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//        // Extract Summary and Finishes sections
//        String[] initialSections = {"Summary", "Finishes"};
//        for (String section : initialSections) {
//            WebElement sectionElement = wait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//div[@role='rowgroup' and contains(@class, 'section-" + section.toLowerCase() + "')]")));
//
////            data.append(section).append("\n");
//
//            List<WebElement> rows = sectionElement.findElements(By.cssSelector("div[role='row']"));
//            for (WebElement row : rows) {
//                // Extract row header
//                List<WebElement> rowHeaders = row.findElements(By.cssSelector("div[role='rowheader'] span"));
//                if (!rowHeaders.isEmpty()) {
//                    String headerText = rowHeaders.get(0).getText().trim();
//                    if (!headerText.isEmpty()) {
//                        data.append(String.format("%-20s \n", headerText));
//                    }
//                }
//
//                // Target the first child with class 'first-child compare-column template-item-default'
//                List<WebElement> firstChildCells = row.findElements(By.cssSelector("div.first-child.compare-column.template-item-default"));
//                if (!firstChildCells.isEmpty()) {
//                    WebElement firstChildCell = firstChildCells.get(0);
//                    String cellText = firstChildCell.getText().trim();
//
//                    // Skip unwanted texts
//                    boolean hasImageIcon = !firstChildCell.findElements(By.cssSelector("div.image-icon-wrapper figure.image-icon")).isEmpty();
//                    if (cellText.isEmpty() || cellText.equals("—") || cellText.toLowerCase().contains("not available") || hasImageIcon) {
//                        continue; // Skip this cell if it contains an image or unwanted text
//                    }
//                    data.append(String.format("%-50s", cellText));
//                }
//                data.append("\n");
//            }
//            data.append("\n");
//        }
//
//        // Extract data from remaining sections
//        List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup'][class='compare-section']"));
//        for (WebElement section : sections) {
//            WebElement headerElement = section.findElement(By.cssSelector("div[role='rowheader'] span"));
//            String sectionHeading = headerElement.getText().trim();
//
//            // Only print heading if there is valid data
//            boolean hasValidData = false;
//            List<WebElement> rows = section.findElements(By.cssSelector("div[role='row']"));
//
//            for (WebElement row : rows) {
//                // Extract row header
//                List<WebElement> rowHeaders = row.findElements(By.cssSelector("div[role='rowheader'] span"));
//                if (!rowHeaders.isEmpty()) {
//                    String headerText = rowHeaders.get(0).getText().trim();
//                    if (!headerText.isEmpty()) {
//                        data.append(String.format("%-20s \n", headerText));
//                    }
//                }
//
//                // Target the first child with class 'first-child compare-column template-item-default'
//                List<WebElement> cells = row.findElements(By.cssSelector("div.first-child.compare-column.template-item-default"));
//                if (!cells.isEmpty()) {
//                    WebElement cell = cells.get(0);
//                    String cellText = cell.getText().trim();
//
//                    boolean hasImageIcon = !cell.findElements(By.cssSelector("div.image-icon-wrapper figure.image-icon")).isEmpty();
//                    if (cellText.isEmpty() || cellText.equals("—") || cellText.toLowerCase().contains("not available") || hasImageIcon) {
//                        continue;
//                    }
////                    if (!cellText.isEmpty() && !cellText.equals("—") && !cellText.equals("Not available")) {
//                        data.append(String.format("%-50s", cellText));
//                        hasValidData = true; // Mark that we have valid data
////                    }
//                }
//                data.append("\n");
//            }
//
//            // Only append heading if there was valid data in this section
////            if (hasValidData) {
////                data.append(sectionHeading).append("\n");
////            }
//
//            data.append("\n");
//        }
//
//        return data.toString();
//    }

    private Map<String, List<String>> extractComparisonData(WebDriver driver) {
        Map<String, List<String>> comparisonData = new LinkedHashMap<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Extract Summary and Finishes sections
        String[] initialSections = {"Summary", "Finishes"};
        for (String section : initialSections) {
            WebElement sectionElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@role='rowgroup' and contains(@class, 'section-" + section.toLowerCase() + "')]")));

            List<String> features = new ArrayList<>();
            List<WebElement> rows = sectionElement.findElements(By.cssSelector("div[role='row']"));

            for (WebElement row : rows) {
//                String headerText = getRowHeaderText(row);
//                if (!headerText.isEmpty()) {
//                    features.add(headerText);
//                }

                // Extract cell text
                String cellText = getCellText(row);
                if (!cellText.isEmpty()) {
                    features.add(cellText);
                }
            }

            if (!features.isEmpty()) {
                comparisonData.put(section, features);
            }
        }

        // Extract data from remaining sections
        List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup'][class='compare-section']"));
        for (WebElement section : sections) {
            String sectionHeading = section.findElement(By.cssSelector("div[role='rowheader'] span")).getText().trim();
            List<String> features = new ArrayList<>();

            List<WebElement> rows = section.findElements(By.cssSelector("div[role='row']"));
            for (WebElement row : rows) {
                WebElement cell = row.findElement(By.cssSelector("div.first-child.compare-column.template-item-default[role='cell gridcell']"));
                if (cell != null) {
                    String cellText = cell.getText();
                    if (!cellText.isEmpty()) {
                        features.add(cellText);
                    }
                }
            }

            if (!features.isEmpty()) {
                comparisonData.put(sectionHeading, features);
            }
        }

        return comparisonData;
    }

    private String getRowHeaderText(WebElement row) {
        List<WebElement> rowHeaders = row.findElements(By.cssSelector("div[role='rowheader'] span"));
        if (!rowHeaders.isEmpty()) {
            return rowHeaders.get(0).getText().trim();
        }
        return "";
    }

    private String getCellText(WebElement row) {
        List<WebElement> cells = row.findElements(By.cssSelector("div.first-child.compare-column.template-item-default"));
        if (!cells.isEmpty()) {
            WebElement cell = cells.get(0);
            String cellText = cell.getText().trim();

            boolean hasImageIcon = !cell.findElements(By.cssSelector("div.image-icon-wrapper figure.image-icon")).isEmpty();
            if (cellText.isEmpty() || cellText.equals("—") || cellText.toLowerCase().contains("not available") || hasImageIcon) {
                return ""; // Skip this cell
            }

            return cellText;
        }
        return "";
    }

    private void updateSearchFrequency(String watchName) {
        searchFrequency.put(watchName, searchFrequency.getOrDefault(watchName, 0) + 1);
    }

    private void saveToCSV(String watchName, String priceInfo, String comparisonData) throws IOException {
        try (FileWriter writer = new FileWriter("comparison.csv", true)) {
            writer.write(watchName + "\n");
            writer.write("Price Information\n");
            for (String line : priceInfo.split("\n")) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    writer.write(parts[0].trim() + "," + parts[1].trim() + "\n");
                }
            }
            writer.write("Features\n");
            for (String line : comparisonData.split("\n")) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    writer.write(parts[0].trim() + "," + parts[1].trim() + "\n");
                }
            }
            writer.write("\n");
        }
    }

//    private void saveToExcel(String watchName, String priceInfo, String comparisonData) throws IOException {
//        Sheet sheet = workbook.getSheet("Comparison");
//        if (sheet == null) {
//            sheet = workbook.createSheet("Comparison");
//        }
//        int rowNum = sheet.getLastRowNum() + 1;
//
//        Row modelRow = sheet.createRow(rowNum++);
//        modelRow.createCell(0).setCellValue("Watch Model");
//        modelRow.createCell(1).setCellValue(watchName);
//
//        rowNum++; // Empty row for separation
//
//        Row priceRow = sheet.createRow(rowNum++);
//        priceRow.createCell(0).setCellValue("Price Information");
//        int priceColNum = 1;
//        for (String line : priceInfo.split("\n")) {
//            if (line.contains("|")) {
//                String[] parts = line.split("\\|");
//                priceRow.createCell(priceColNum++).setCellValue(parts[0].trim() + ": " + parts[1].trim());
//            }
//        }
//
//        rowNum++; // Empty row for separation
//
//        String[] lines = comparisonData.split("\n");
//        for (String line : lines) {
//            Row dataRow = sheet.createRow(rowNum++);
//            if (line.contains("|")) {
//                String[] parts = line.split("\\|");
//                dataRow.createCell(0).setCellValue(parts[0].trim());
//                dataRow.createCell(1).setCellValue(parts[1].trim());
//            } else {
//                // This is a main heading
//                Cell cell = dataRow.createCell(0);
//                cell.setCellValue(line.trim());
//                CellStyle style = workbook.createCellStyle();
//                Font font = workbook.createFont();
//                font.setBold(true);
//                style.setFont(font);
//                cell.setCellStyle(style);
//            }
//        }
//
//        // Auto-size columns
//        for (int i = 0; i < 2; i++) {
//            sheet.autoSizeColumn(i);
//        }
//
//        // Save workbook
//        try (FileOutputStream outputStream = new FileOutputStream("AppleWatchComparison.xlsx")) {
//            workbook.write(outputStream);
//        }
//    }

//    private void saveToExcel(String watchName, String priceInfo, Map<String, List<String>> comparisonData) throws IOException {
//        Sheet sheet = workbook.getSheet("Comparison");
//        if (sheet == null) {
//            sheet = workbook.createSheet("Comparison");
//        }
//
//        int rowNum = sheet.getLastRowNum() + 1;
//
//        // Write Watch Model
//        Row modelRow = sheet.createRow(rowNum++);
//        modelRow.createCell(0).setCellValue("Watch Model");
//        modelRow.createCell(1).setCellValue(watchName);
//
//        rowNum++; // Empty row for separation
//
//        // Write Price Information
//        Row priceRow = sheet.createRow(rowNum++);
//        priceRow.createCell(0).setCellValue("Price Information");
//
//        int priceColNum = 1;
//        for (String line : priceInfo.split("\n")) {
//            if (line.contains("|")) {
//                String[] parts = line.split("\\|");
//                priceRow.createCell(priceColNum++).setCellValue(parts[0].trim() + ": " + parts[1].trim());
//            }
//        }
//
//        rowNum++; // Empty row for separation
//
//        // Write Comparison Data
//        for (Map.Entry<String, List<String>> entry : comparisonData.entrySet()) {
//            String heading = entry.getKey();
//
//            Row headingRow = sheet.createRow(rowNum++);
//            headingRow.createCell(0).setCellValue(heading); // Main heading
//
//            // Write all associated data in the next column
//            for (String feature : entry.getValue()) {
//                Row dataRow = sheet.createRow(rowNum++);
//                dataRow.createCell(1).setCellValue(feature); // Feature text in column 2
//            }
//
//            rowNum++; // Empty row after each section's data
//        }
//
//        // Auto-size columns
//        for (int i = 0; i < 2; i++) {
//            sheet.autoSizeColumn(i);
//        }
//
//        // Save workbook
//        try (FileOutputStream outputStream = new FileOutputStream("AppleWatchComparison.xlsx")) {
//            workbook.write(outputStream);
//        }
//    }

    private void saveToExcel(String watchName, String priceInfo, Map<String, List<String>> comparisonData) throws IOException {
        Sheet sheet = workbook.getSheet("Comparison");
        if (sheet == null) {
            sheet = workbook.createSheet("Comparison");
        }

        int rowNum = sheet.getLastRowNum() + 1;

        // Write Watch Model
        Row modelRow = sheet.createRow(rowNum++);
        modelRow.createCell(0).setCellValue("Watch Model");
        modelRow.createCell(1).setCellValue(watchName);

        rowNum++; // Empty row for separation

        // Write Price Information
        Row priceRow = sheet.createRow(rowNum++);
        priceRow.createCell(0).setCellValue("Price Information");

        int priceColNum = 1;
        for (String line : priceInfo.split("\n")) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                priceRow.createCell(priceColNum++).setCellValue(parts[0].trim() + ": " + parts[1].trim());
            }
        }

        rowNum++; // Empty row for separation

        // Write Comparison Data
        for (Map.Entry<String, List<String>> entry : comparisonData.entrySet()) {
            String heading = entry.getKey();

            Row headingRow = sheet.createRow(rowNum++);
            headingRow.createCell(0).setCellValue(heading); // Main heading

            List<String> features = entry.getValue();
            if (features.size() > 1) { // Ensure there are more than one elements
                for (String feature : features.subList(1, features.size())) { // Start from index 1
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(1).setCellValue(feature); // Feature text in column 2
                }
            }

            rowNum++; // Empty row after each section's data
        }

        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }

        // Save workbook
        try (FileOutputStream outputStream = new FileOutputStream("AppleWatchComparison.xlsx")) {
            workbook.write(outputStream);
        }
    }

    public void displaySearchFrequencies() {
        System.out.println("\nSearch Frequencies:");
        searchFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    public void close() {
        driver.quit();
        try {
            workbook.close();
        } catch (IOException e) {
            System.out.println("Error closing workbook: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        AppleWatchComparator comparator = new AppleWatchComparator();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter a watch name to search (or 'exit' to quit):");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            comparator.searchAndCompare(input);
        }

        comparator.displaySearchFrequencies();
        comparator.close();
    }
}