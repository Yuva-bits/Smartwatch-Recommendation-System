package org.example.testUI;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartwatchRecommendationSystem {
    // Private fields for workbooks and utilities
    private Workbook appleWorkbook;
    private Workbook gshockWorkbook;
    private Workbook garminWorkbook;
    private Workbook noiseWorkbook;
    private FrequencyCounter frequencyCounter;
    private Set<String> watchNames;

    public SmartwatchRecommendationSystem(String appleFile, String gShockFile, String garminFile, String noiseFile) throws IOException {
        // Initialize workbooks from Excel files
        appleWorkbook = new XSSFWorkbook(new FileInputStream(appleFile));
        gshockWorkbook = new XSSFWorkbook(new FileInputStream(gShockFile));
        garminWorkbook = new XSSFWorkbook(new FileInputStream(garminFile));
        noiseWorkbook = new XSSFWorkbook(new FileInputStream(noiseFile));
        
        // Initialize frequency counter and watch names set
        frequencyCounter = new FrequencyCounter();
        watchNames = new HashSet<>();
        
        // Collect watch names from all workbooks
        loadWatchNames();
    }
    
    private void loadWatchNames() {
        // Add all sheet names from each workbook to the watchNames set
        for (int i = 0; i < appleWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(appleWorkbook.getSheetName(i));
        }
        
        for (int i = 0; i < gshockWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(gshockWorkbook.getSheetName(i));
        }
        
        for (int i = 0; i < garminWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(garminWorkbook.getSheetName(i));
        }
        
        for (int i = 0; i < noiseWorkbook.getNumberOfSheets(); i++) {
            watchNames.add(noiseWorkbook.getSheetName(i));
        }
    }

    // Recommend smartwatches based on selected features
    public void recommendSmartwatch(Set<String> selectedFeatures, List<String> recommendations) {
        recommendations.clear(); // Clear existing recommendations

        recommendations.addAll(checkWorkbook(appleWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(gshockWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(garminWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(noiseWorkbook, selectedFeatures));
    }
    
    private List<String> checkWorkbook(Workbook workbook, Set<String> selectedFeatures) {
        List<String> matches = new ArrayList<>();
        
        // Check each sheet (watch model) in the workbook
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String watchName = workbook.getSheetName(i);
            boolean matchesAllFeatures = true;
            
            // Check if all selected features are found in this watch's data
            for (String feature : selectedFeatures) {
                boolean featureFound = false;
                
                // Search all rows for the feature
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING) {
                            String cellValue = cell.getStringCellValue();
                            if (cellValue.contains(feature)) {
                                featureFound = true;
                                break;
                            }
                        }
                    }
                    if (featureFound) break;
                }
                
                if (!featureFound) {
                    matchesAllFeatures = false;
                    break;
                }
            }
            
            if (matchesAllFeatures) {
                matches.add(watchName);
            }
        }
        
        return matches;
    }

    // Display methods for different watch types
    public void displayWatchDetails(String modelName, StringBuilder details) {
        Sheet sheet = appleWorkbook.getSheet(modelName);
        if (sheet == null) {
            details.append("Model not found.");
            return;
        }

        details.append("Details for ").append(modelName).append(":\n");
        details.append("+--------------------------+--------------------------------------------------+\n");
        details.append(String.format("| %-24s | %-48s |\n", "Feature", "Details"));
        details.append("+--------------------------+--------------------------------------------------+\n");

        String currentHeading = "";
        for (Row row : sheet) {
            Cell headingCell = row.getCell(0);
            Cell detailCell = row.getCell(1);

            if (headingCell != null && headingCell.getCellType() == CellType.STRING) {
                String heading = headingCell.getStringCellValue().trim();
                if (!heading.isEmpty() && !heading.equals(currentHeading)) {
                    details.append("+--------------------------+--------------------------------------------------+\n");
                    details.append(String.format("| %-73s |\n", heading));
                    details.append("+--------------------------+--------------------------------------------------+\n");
                    currentHeading = heading;
                }
            }

            if (detailCell != null && detailCell.getCellType() == CellType.STRING) {
                String detail = detailCell.getStringCellValue().trim();
                if (!detail.isEmpty()) {
                    String[] words = detail.split("\\s+");
                    StringBuilder line = new StringBuilder();
                    for (String word : words) {
                        if (line.length() + word.length() > 48) {
                            details.append(String.format("| %-24s | %-48s |\n",
                                    line.length() == 0 ? headingCell.getStringCellValue().trim() : "",
                                    line.toString().trim()));
                            line = new StringBuilder();
                        }
                        line.append(word).append(" ");
                    }
                    if (line.length() > 0) {
                        details.append(String.format("| %-24s | %-48s |\n",
                                line.length() == 0 ? headingCell.getStringCellValue().trim() : "",
                                line.toString().trim()));
                    }
                }
            }
        }
        details.append("+--------------------------+--------------------------------------------------+\n");
    }

    public void displayGShockWatchDetails(String modelName, StringBuilder details) {
        Sheet sheet = gshockWorkbook.getSheet(modelName);
        if (sheet == null) {
            details.append("Model not found.");
            return;
        }

        // Similar implementation as displayWatchDetails but for G-Shock watches
        details.append("Details for ").append(modelName).append(":\n");
        details.append("+--------------------------+--------------------------------------------------+\n");
        // Format and append details similar to displayWatchDetails
        details.append("+--------------------------+--------------------------------------------------+\n");
    }

    public void displayGarminWatchDetails(String modelName, StringBuilder details) {
        Sheet sheet = garminWorkbook.getSheet(modelName);
        if (sheet == null) {
            details.append("Model not found.");
            return;
        }

        // Similar implementation for Garmin watches
        details.append("Details for ").append(modelName).append(":\n");
        details.append("+--------------------------+--------------------------------------------------+\n");
        // Format and append details similar to displayWatchDetails
        details.append("+--------------------------+--------------------------------------------------+\n");
    }

    public void displayNoiseWatchDetails(String modelName, StringBuilder details) {
        Sheet sheet = noiseWorkbook.getSheet(modelName);
        if (sheet == null) {
            details.append("Model not found.");
            return;
        }

        // Similar implementation for Noise watches
        details.append("Details for ").append(modelName).append(":\n");
        details.append("+--------------------------+--------------------------------------------------+\n");
        // Format and append details similar to displayWatchDetails
        details.append("+--------------------------+--------------------------------------------------+\n");
    }

    // Method to update search frequency when a watch is looked up
    public void updateSearchFrequency(String watchName) {
        frequencyCounter.addSearchQuery(watchName);
    }

    // Method to get search frequencies for the Swing app
    public Map<String, Integer> getSearchFrequencies() {
        return frequencyCounter.getSearchFrequencies();
    }
}
