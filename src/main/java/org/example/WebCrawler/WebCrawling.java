package org.example.WebCrawler;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class WebCrawling {

    public static void extractdata()
    {
        WebDriver driver = new ChromeDriver();
        WebDriverWait _sleepWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        FluentWait<WebDriver> _fluent_hold = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(20))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        driver.get("https://gshock.ca/collections/smart-watch");
        System.out.println("Navigated  to smart watch section...");
        List<WebElement> allWatches = driver.findElements(By.className("grid-product__image-link"));
        Map<String, Map<String, Object>> productDetails = new HashMap<>();

        List<String> urls = new ArrayList<>();
        for (WebElement watch : allWatches) {
            String productUrl = watch.getAttribute("href");
            urls.add(productUrl);
        }
        for (int m = 0; m < urls.size(); m++) {
            driver.navigate().to(urls.get(m));
            WebElement pageloaded = _sleepWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product-single__title")));

            // Extract model name, price, and features
            String modelName = driver.findElement(By.cssSelector("h1.product-single__title")).getText();
            String price = driver.findElement(By.cssSelector(".product-single__price")).getText();
            WebElement featurediv =  driver.findElement(By.cssSelector("div[class='grid__item description-2 medium--one-half large--one-half  small--one-whole']"));
            List<WebElement> feature = featurediv.findElements(By.tagName("li"));

            List<String> features = new ArrayList<>();
            for (WebElement featureElement : feature) {
                features.add(featureElement.getText());
            }
            Map<String, Object> details = new HashMap<>();
            details.put("price", price);
            details.put("features", features);
            productDetails.put(modelName, details);



        }

        for (Map.Entry<String, Map<String, Object>> entry : productDetails.entrySet()) {
            String modelName = entry.getKey();
            Map<String, Object> details = entry.getValue();
            String price = (String) details.get("price");
            List<String> features = (List<String>) details.get("features");

            System.out.println("Model Name: " + modelName);
            System.out.println("Price: " + price);
            System.out.println("Features: ");
            for (String feature : features) {
                System.out.println("- " + feature);
            }
            System.out.println("-------------------------------");
        }
    }
}