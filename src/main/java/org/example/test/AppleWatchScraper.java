package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class AppleWatchScraper {
    private WebDriver driver;
    private WebDriverWait wait;
    private Workbook workbook;
    private Sheet sheet;
    private int rowNum = 0;

    public AppleWatchScraper() {
//        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Apple Watch Specs");
    }

    public void run() {
        try {
            driver.get("https://www.apple.com/ca/watch/compare/");
            String watchModel = getUserInput();
            selectWatchModel(watchModel);
            navigateToTechSpecs();
            scrapeAndSaveData(watchModel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            saveExcelFile();
        }
    }

    private String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the Apple Watch model name: ");
        return scanner.nextLine().trim().toLowerCase();
    }

    private void selectWatchModel(String watchModel) {
        String[] unavailableModels = {
                "apple watch series 1", "apple watch series 2", "apple watch series 3",
                "apple watch series 4", "apple watch series 5", "apple watch series 6",
                "apple watch series 7", "apple watch series 8", "apple watch series 9",
                "apple watch ultra"
        };

        for (String unavailableModel : unavailableModels) {
            if (watchModel.equalsIgnoreCase(unavailableModel)) {
                System.out.println("Tech specs not available for " + watchModel);
                driver.quit();
                System.exit(0);
            }
        }

        try {
            WebElement dropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#selector-0")));
            Select select = new Select(dropdown);
            List<WebElement> options = select.getOptions();

            for (WebElement option : options) {
                if (option.getText().trim().toLowerCase().equals(watchModel)) {
                    option.click();
                    wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.tagName("body"))));
                    return;
                }
            }
            throw new NoSuchElementException("Watch model not found: " + watchModel);
        } catch (Exception e) {
            System.out.println("Error selecting watch model: " + e.getMessage());
            driver.quit();
            System.exit(1);
        }
    }

    private void navigateToTechSpecs() {
        try {
            WebElement techSpecsLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("div.template-item-link a[aria-label^='Tech specs']")));
            techSpecsLink.click();
            wait.until(ExpectedConditions.urlContains("/specs/"));
        } catch (Exception e) {
            System.out.println("Error navigating to tech specs: " + e.getMessage());
        }
    }

    private void scrapeAndSaveData(String watchModel) {
        createHeaderRow(watchModel);

        List<WebElement> sections = driver.findElements(By.cssSelector("div[role='rowgroup']"));
        for (WebElement section : sections) {
            try {
                WebElement rowHeader = section.findElement(By.cssSelector("div[role='rowheader'] span"));
                String header = rowHeader.getText().trim();

                List<WebElement> dataCells = section.findElements(By.cssSelector("div[role='cell']"));
                if (dataCells.size() == 1) {
                    String data = extractTextFromElement(dataCells.get(0));
                    addRowToExcel(header, data);
                } else if (dataCells.size() == 2) {
                    String data1 = extractTextFromElement(dataCells.get(0));
                    String data2 = extractTextFromElement(dataCells.get(1));
                    addRowToExcel(header, data1, data2);
                }

                System.out.println(header);
                dataCells.forEach(cell -> System.out.println(extractTextFromElement(cell)));
                System.out.println();
            } catch (Exception e) {
                System.out.println("Error processing section: " + e.getMessage());
            }
        }
    }

    private String extractTextFromElement(WebElement element) {
        return element.findElements(By.cssSelector("p, span")).stream()
                .map(WebElement::getText)
                .filter(text -> !text.isEmpty())
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private void createHeaderRow(String watchModel) {
        Row headerRow = sheet.createRow(rowNum++);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue(watchModel);
    }

    private void addRowToExcel(String header, String... data) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(header);
        for (int i = 0; i < data.length; i++) {
            row.createCell(i + 1).setCellValue(data[i]);
        }
    }

    private void saveExcelFile() {
        try (FileOutputStream outputStream = new FileOutputStream("AppleWatchSpecs.xlsx")) {
            workbook.write(outputStream);
        } catch (IOException e) {
            System.out.println("Error saving Excel file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AppleWatchScraper().run();
    }
}