package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.FileOutputStream;
import java.time.Duration;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.openqa.selenium.WebElement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.ArrayList;
import java.util.List;



public class AppleWatchScraper {
    // Task 1: Extract data and save to CSV and Excel
    public static void modelDetails(WebDriver driver, String modelName0, String modelName1) {
        List<WebElement> allDivs = driver.findElements(By.cssSelector("div.compare-column.template-item-default[role='cell gridcell']"));

        ArrayList<String> model1 = new ArrayList<>();
        ArrayList<String> model2 = new ArrayList<>();
        model1.add(modelName0);
        model2.add(modelName1);

        for (int i = 0; i < allDivs.size(); i++) {
            WebElement div = allDivs.get(i);
            String text = div.findElement(By.cssSelector("span")).getText();

            if (i % 2 == 0) {
                // Even index, add to model1
                model1.add(text);
            } else {
                // Odd index, add to model2
                model2.add(text);
            }
        }

        // Print results
        System.out.println("Model 1:");
        model1.forEach(System.out::println);

        System.out.println("\nModel 2:");
        model2.forEach(System.out::println);

        // Create CSV file
        try (FileWriter writer = new FileWriter("comparison.csv")) {
            // Write header
            writer.write("Model1,Model2\n");

            // Write data
            int maxSize = Math.max(model1.size(), model2.size());
            for (int i = 0; i < maxSize; i++) {
                String model1Value = i < model1.size() ? model1.get(i) : "";
                String model2Value = i < model2.size() ? model2.get(i) : "";

                // Escape commas in the values
                model1Value = model1Value.replace(",", "\\,");
                model2Value = model2Value.replace(",", "\\,");

                writer.write(model1Value + "," + model2Value + "\n");
            }

            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Task 1: Interact with elements and extract data
    public static void fetchDetails(WebDriver driver) {
        String modelName0 = new Select(driver.findElement(By.id("selector-0"))).getFirstSelectedOption().getText();
        String modelName1 = new Select(driver.findElement(By.id("selector-1"))).getFirstSelectedOption().getText();

        System.out.println(modelName0);
        System.out.println(modelName1);

        modelDetails(driver, modelName0, modelName1);

    }

    // Task 3: Advanced Selenium commands (waiting for elements)
    public static void askSpecialist(WebDriver driver) {
        // Task 3: Wait for the "Ask an Apple Watch Specialist" link to be clickable
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement specialistLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.as-chat-button[data-autom='chat-with-a-specialist-link']")));

        // Extract the text of the link
                String linkText = specialistLink.getText().trim();
                System.out.println("Link text: " + linkText);

        // Extract the target attribute
                String targetAttribute = specialistLink.getAttribute("target");
                System.out.println("Target attribute: " + targetAttribute);

        // Extract the data-autom attribute
                String dataAutom = specialistLink.getAttribute("data-autom");
                System.out.println("Data-autom attribute: " + dataAutom);

        // Note: Clicking this link might open a chat window or a new tab
        // specialistLink.click();
    }

    public static void modalPopupDetailsScrapping(WebDriver driver) {
        // Task 1: Wait for the h2 element to be visible
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement headerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2.rf-digitalmat-overlay-header[data-autom='DigitalMat-overlay-header-0']")));

        // Task 1 and 3: Extract multiple elements and handle them
        List<WebElement> featureElements = driver.findElements(By.cssSelector("div.rf-digitalmat-featuretext"));
        Map<String, String> features = new LinkedHashMap<>();

        for (int i = 0; i < featureElements.size(); i++) {
            WebElement element = featureElements.get(i);
            String featureText = element.getText().replaceAll("\\s*[§\\d]+$", "").trim();
            if (!featureText.isEmpty()) {
                String featureTitle = "Feature " + (i + 1);
                features.put(featureTitle, featureText);
            }
        }

        // Task 1: Save the scraped data in a CSV file
        try (FileWriter csvWriter = new FileWriter("watch_features.csv")) {
            csvWriter.append("Feature Title,Feature Text\n");
            for (Map.Entry<String, String> entry : features.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    csvWriter.append(String.format("%s,%s\n",
                            entry.getKey().replace(",", "\\,"),
                            entry.getValue().replace(",", "\\,")));
                }
            }
            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            System.out.println("Error writing to CSV: " + e.getMessage());
        }

    }

    public static void navigatedPageData(WebDriver driver) {
        System.out.println("Navigated to https://apple.com/shop/buy-watch");
        try {
            // Task 3: Use advanced Selenium commands for waiting
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Task 1: Interact with elements on the page
            WebElement closerLookButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(text(), 'Take a closer look')]")));

            // Scroll the button into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", closerLookButton);

            // Click the button using JavaScript
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closerLookButton);

            System.out.println("Successfully clicked 'Take a closer look' button");
        } catch (Exception e) {
            System.out.println("Error interacting with 'Take a closer look' button: " + e.getMessage());
        } finally {
            modalPopupDetailsScrapping(driver);
        }
    }

    public static void combineCSVFiles(String[] inputFiles, String outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile);
            Set<String> writtenHeaders = new HashSet<>();

            for (String inputFile : inputFiles) {
                File file = new File(inputFile);
                if (!file.exists()) {
                    System.out.println("Warning: File " + inputFile + " does not exist. Skipping.");
                    continue;
                }
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        // Check if we've already written this header
                        if (!writtenHeaders.contains(line)) {
                            writer.write(line + "\n");
                            writtenHeaders.add(line);
                        }
                        isFirstLine = false;
                    } else {
                        // Write all non-header lines
                        writer.write(line + "\n");
                    }
                }
                reader.close();
            }

            writer.close();
            System.out.println("CSV files combined successfully into " + outputFile);
        } catch (IOException e) {
            System.out.println("An error occurred while combining CSV files: " + e.getMessage());
        }
    }



    public static void convertCSVsToExcel(String[] csvFiles) {
        for (String csvFile : csvFiles) {
            String excelFile = csvFile.replace(".csv", ".xlsx");

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
                 Workbook workbook = new XSSFWorkbook();
                 FileOutputStream outputStream = new FileOutputStream(excelFile)) {

                Sheet sheet = workbook.createSheet("Sheet1");
                String line;
                int rowNum = 0;

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    Row row = sheet.createRow(rowNum++);

                    for (int i = 0; i < data.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(data[i].replace("\"", ""));
                    }
                }

                // Auto-size columns
                for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(outputStream);
                System.out.println("Successfully converted " + csvFile + " to " + excelFile);
            } catch (IOException e) {
                System.out.println("Error converting " + csvFile + " to Excel: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        // Task 3: Increased wait time for slower connections
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Task 1: Open the website in a web browser
            driver.get("https://www.apple.com/watch/compare/");

            // Task 3: Wait for the page title to contain "Apple Watch"
            wait.until(ExpectedConditions.titleContains("Apple Watch"));
            System.out.println("Page title: " + driver.getTitle());

            // Task 3: Wait for the selector to be clickable before fetching details
            wait.until(ExpectedConditions.elementToBeClickable(By.id("selector-0")));
            // Task 1: Extract data
            fetchDetails(driver);

            System.out.println("Data scraped and saved to apple_watches.csv");

            // Task 2: Navigate to another page (part of scraping multiple pages)
            // Task 3: Wait for the "Shop Apple Watch" link to be clickable
            WebElement shopAppleWatchLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a.more[href='/us/shop/goto/buy_watch']")));
            shopAppleWatchLink.click();
            navigatedPageData(driver);


            String[] csvFiles = {"watch_features.csv", "comparison.csv"}; // Add all your CSV file names
            String combinedFile = "combined_watch_data.csv";
            combineCSVFiles(csvFiles, combinedFile);

            String[] csvFilesNew = {"combined_watch_data.csv", "watch_features.csv", "comparison.csv"};
            convertCSVsToExcel(csvFilesNew);

            // Automation if want to check if askSpecialist functionality is working
//            askSpecialist(driver);

            // Task 3: Wait for the new page to load
//            wait.until(ExpectedConditions.urlContains("/shop/goto/buy_watch"));

            // Task 3: Wait for an element on the new page to be visible
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.rf-header-title")));
//            System.out.println("Navigated to: " + driver.getTitle());

            // Task 3: Handle potential alert (uncomment if needed)
            /*
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                System.out.println("Alert text: " + alert.getText());
                alert.accept();
            } catch (TimeoutException e) {
                System.out.println("No alert present.");
            }
            */

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Task 3: Add a small delay before quitting to ensure all actions are completed
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }
}