package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class ExtractingAllModalsData {
    private WebDriver driver;
    private Workbook workbook;

    // List of Apple Watch models
    private static final String[] WATCH_MODELS = {
            "Apple Watch Series 1",
            "Apple Watch Series 2",
            "Apple Watch Series 3",
            "Apple Watch Series 4",
            "Apple Watch Series 5",
            "Apple Watch Series 6",
            "Apple Watch Series 7",
            "Apple Watch Series 8",
            "Apple Watch Series 9",
            "Apple Watch Series 10",
            "Apple Watch Ultra",
            "Apple Watch Ultra 2",
            "Apple Watch SE (1st generation)",
            "Apple Watch SE (2nd generation)"
    };

    public ExtractingAllModalsData() {
        this.driver = new ChromeDriver();
        this.workbook = new XSSFWorkbook(); // Create a new workbook
    }

    public void scrapeData() {
        try {
            driver.get("https://www.apple.com/watch/compare/");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.titleContains("Apple Watch"));

            // Iterate through each watch model
            for (String model : WATCH_MODELS) {
                selectWatchModel(model);
                String priceInfo = extractPriceInfo();
                Map<String, List<String>> comparisonData = extractComparisonData();
                System.out.println(comparisonData);
                saveToExcel(model, priceInfo, comparisonData);
            }
            System.out.println("Data extraction complete.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void selectWatchModel(String model) {
        Select selector0 = new Select(driver.findElement(By.id("selector-0")));
        selector0.selectByVisibleText(model);
    }

    private String extractPriceInfo() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        StringBuilder priceInfo = new StringBuilder();

        try {
            WebElement priceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".first-child.compare-column.template-price")));

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
        } catch (TimeoutException e) {
            System.out.println("Timeout while waiting for price information: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("Price element not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred while extracting price info: " + e.getMessage());
        }

        return priceInfo.toString();
    }

//    private Map<String, List<String>> extractComparisonData() {
//        Map<String, List<String>> comparisonData = new LinkedHashMap<>();
//
//        try {
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//            // Extract Summary and Finishes sections
//            String[] initialSections = {"Summary", "Finishes"};
//            for (String section : initialSections) {
//                WebElement sectionElement = wait.until(ExpectedConditions.presenceOfElementLocated(
//                        By.xpath("//div[@role='rowgroup' and contains(@class, 'section-" + section.toLowerCase() + "')]")));
//
//                List<String> features = new ArrayList<>();
//                List<WebElement> rows = sectionElement.findElements(By.cssSelector("div[role='row']"));
//
//                for (WebElement row : rows) {
//                    String cellText = getCellText(row);
//                    if (!cellText.isEmpty()) {
//                        features.add(cellText);
//                    }
//                }
//
//                if (!features.isEmpty()) {
//                    comparisonData.put(section, features);
//                }
//            }
//
//            // Extract data from remaining sections
//            List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup'][class='compare-section']"));
//            for (WebElement section : sections) {
//                String sectionHeading = section.findElement(By.cssSelector("div[role='rowheader'] span")).getText().trim();
//                List<String> features = new ArrayList<>();
//
//                List<WebElement> rows = section.findElements(By.cssSelector("div[role='row']"));
//                for (WebElement row : rows) {
//                    String cellText = getCellText(row);
//                    if (!cellText.isEmpty()) {
//                        features.add(cellText);
//                    }
//                }
//
//                if (!features.isEmpty()) {
//                    comparisonData.put(sectionHeading, features);
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("An error occurred while extracting comparison data: " + e.getMessage());
//        }
//
//        return comparisonData;
//    }

//    private Map<String, List<String>> extractComparisonData(WebDriver driver) {
//        Map<String, List<String>> comparisonData = new LinkedHashMap<>();
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//        // Extract Summary and Finishes sections
//        String[] initialSections = {"Summary", "Finishes"};
//        for (String section : initialSections) {
//            WebElement sectionElement = wait.until(ExpectedConditions.presenceOfElementLocated(
//                    By.xpath("//div[@role='rowgroup' and contains(@class, 'section-" + section.toLowerCase() + "')]")));
//
//            List<String> features = new ArrayList<>();
//            List<WebElement> rows = sectionElement.findElements(By.cssSelector("div[role='row']"));
//
//            for (WebElement row : rows) {
////                String headerText = getRowHeaderText(row);
////                if (!headerText.isEmpty()) {
////                    features.add(headerText);
////                }
//
//                // Extract cell text
//                String cellText = getCellText(row);
//                if (!cellText.isEmpty()) {
//                    features.add(cellText);
//                }
//            }
//
//            if (!features.isEmpty()) {
//                comparisonData.put(section, features);
//            }
//        }
//
//        // Extract data from remaining sections
//        List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup'][class='compare-section']"));
//        for (WebElement section : sections) {
//            String sectionHeading = section.findElement(By.cssSelector("div[role='rowheader'] span")).getText().trim();
//            List<String> features = new ArrayList<>();
//
//            List<WebElement> rows = section.findElements(By.cssSelector("div[role='row']"));
//            for (WebElement row : rows) {
//                WebElement cell = row.findElement(By.cssSelector("div.first-child.compare-column.template-item-default[role='cell gridcell']"));
//                if (cell != null) {
//                    String cellText = cell.getText();
//                    if (!cellText.isEmpty()) {
//                        features.add(cellText);
//                    }
//                }
//            }
//
//            if (!features.isEmpty()) {
//                comparisonData.put(sectionHeading, features);
//            }
//        }
//
//        return comparisonData;
//    }

//    private String getCellText(WebElement row) {
//        List<WebElement> cells = row.findElements(By.cssSelector("div.first-child.compare-column.template-item-default[role='cell gridcell']"));
//
//        if (!cells.isEmpty()) {
////            WebElement cell = cells.get(0);
//            WebElement cell = row.findElement(By.cssSelector("div.first-child.compare-column.template-item-default[role='cell gridcell']"));
//            String cellText = cell.getText().trim();
//
//            boolean hasImageIcon = !cell.findElements(By.cssSelector("div.image-icon-wrapper figure.image-icon")).isEmpty();
//            if (cellText.isEmpty() || cellText.equals("—") || cellText.toLowerCase().contains("not available") || hasImageIcon) {
//                return ""; // Skip this cell
//            }
//
//            return cellText;
//        }
//
//        return "";
//    }

    private Map<String, List<String>> extractComparisonData() {
        Map<String, List<String>> comparisonData = new LinkedHashMap<>();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Extract Summary and Finishes sections
            String[] initialSections = {"Summary", "Finishes"};
            for (String section : initialSections) {
                WebElement sectionElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@role='rowgroup' and contains(@class, 'section-" + section.toLowerCase() + "')]")));

                List<String> features = extractFeaturesFromSection(sectionElement);
                if (!features.isEmpty()) {
                    comparisonData.put(section, features);
                }
            }

            // Extract data from remaining sections
            List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup'][class='compare-section']"));
            for (WebElement section : sections) {
                String sectionHeading = section.findElement(By.cssSelector("div[role='rowheader'] span")).getText().trim();
                List<String> features = extractFeaturesFromSection(section);
                if (!features.isEmpty()) {
                    comparisonData.put(sectionHeading, features);
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while extracting comparison data: " + e.getMessage());
        }

        return comparisonData;
    }

    private List<String> extractFeaturesFromSection(WebElement section) {
        List<String> features = new ArrayList<>();
        List<WebElement> rows = section.findElements(By.cssSelector("div[role='row']"));
        for (WebElement row : rows) {
            String cellText = getCellText(row);
            if (!cellText.isEmpty()) {
                features.add(cellText);
            }
        }
        return features;
    }

    private String getCellText(WebElement row) {
        try {
            WebElement cell = row.findElement(By.cssSelector("div.first-child.compare-column.template-item-default[role='cell gridcell']"));
            String cellText = cell.getText().trim();

            boolean hasImageIcon = !cell.findElements(By.cssSelector("div.image-icon-wrapper")).isEmpty();
            if (cellText.isEmpty() || cellText.equals("—") || cellText.toLowerCase().contains("not available")) {
                return ""; // Skip this cell
            }

            if (hasImageIcon) {
                String iconText = cell.findElement(By.cssSelector("div.image-icon-wrapper")).getAttribute("aria-label");
                return iconText.isEmpty() ? "Image Icon Present" : iconText;
            }

            return cellText;
        } catch (NoSuchElementException e) {
            return ""; // Return empty string if no suitable cell is found
        }
    }

    private void saveToExcel(String watchName, String priceInfo, Map<String, List<String>> comparisonData) throws IOException {
        Sheet sheet = workbook.createSheet(watchName); // Create a sheet with the watch name

        int rowNum = 0;

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
            for (String feature : features) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(1).setCellValue(feature); // Feature text in column 2
            }

            rowNum++; // Empty row after each section's data
        }

        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void close() {
        driver.quit();
        try {
            FileOutputStream outputStream = new FileOutputStream("ExtractedAllModalsData.xlsx"); // Save to specified file
            workbook.write(outputStream);
            outputStream.close(); // Close output stream
            workbook.close();
        } catch (IOException e) {
            System.out.println("Error closing workbook: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ExtractingAllModalsData comparator = new ExtractingAllModalsData();
        comparator.scrapeData(); // Start scraping data for all watch models
    }
}