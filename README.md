# Smartwatch Recommendation System

A comprehensive Java application that helps users find the perfect smartwatch based on their desired features and preferences.

## Overview

The Smartwatch Recommendation System is a desktop application built with Java and Swing that provides recommendations and detailed information about various smartwatch models across different brands including Apple, G-Shock, Garmin, and Noise. The system analyzes smartwatch specifications stored in Excel spreadsheets and matches them against user-selected features to provide personalized recommendations.

## Features

- **Feature-Based Recommendation**: Select specific features you want in a smartwatch (like ECG, GPS, water resistance, etc.) and get recommendations for models that match all selected criteria.

- **Detailed Watch Information**: View comprehensive specifications for smartwatch models from Apple, G-Shock, Garmin, and Noise brands.

- **Search History Tracking**: The system keeps track of watch models you've searched for, making it easy to revisit previously viewed models.

- **User-Friendly Interface**: Simple and intuitive tabbed interface with sections for recommendations, watch details, and search history.

## Components

### 1. User Interface

The system offers a GUI built with Java Swing, featuring:
- Tab-based navigation
- Feature selection checkboxes
- Watch detail lookup
- Search frequency tracking

### 2. Data Processing

- Reads watch specifications from Excel files using Apache POI
- Performs feature matching across watch models
- Formats and displays detailed watch information

### 3. Web Scraping Tools

The project includes several tools for data collection:
- **AppleWatchScraper**: Extracts smartwatch information from the Apple website
- **WebCrawler**: Collects watch data from various smartwatch websites
- **HTMLParser**: Processes HTML content from websites to extract watch information

### 4. Text Analysis

- **WebContentAnalyzer**: Analyzes web content using string matching algorithms to evaluate pages based on keyword frequency
- **WordFrequencyAnalyzer**: Determines common terms and features mentioned in smartwatch descriptions

## Technical Details

### Technologies Used

- **Java**: Core programming language
- **Swing**: For the graphical user interface
- **Apache POI**: For reading and writing Excel files
- **Selenium WebDriver**: For web scraping and data collection
- **JSoup**: For HTML parsing
- **Maven**: For dependency management

### Data Sources

The system works with Excel data files:
- AppleWatchComparison.xlsx
- GShockSmartwatchDetails.xlsx
- garmin_models.xlsx
- noise.xlsx

## Getting Started

### Prerequisites

- Java JDK 19 or higher
- Maven
- Chrome WebDriver (for web scraping features)

### Installation

1. Clone the repository
2. Navigate to the project directory
3. Build the project using Maven:
   ```
   mvn clean install
   ```

### Running the Application

Execute the application using Maven:
```
mvn exec:java -Dexec.mainClass="org.example.RecommendationSystemLauncher"
```

Or run the compiled JAR file:
```
java -jar target/Apple-Watch-Scrapping-1.0-SNAPSHOT.jar
```

## Usage Instructions

1. **Finding Recommendations**:
   - Go to the "Recommendations" tab
   - Select the features you want in your smartwatch
   - Click "Recommend Smartwatches" to view matching models

2. **Viewing Watch Details**:
   - Navigate to the "Watch Details" tab
   - Select the watch brand from the dropdown
   - Enter the specific watch model name
   - Click "Show Details" to view specifications

3. **Checking Search History**:
   - Go to the "Search Frequencies" tab
   - Click "Refresh Search Frequencies" to see your most viewed watches

## Project Structure

- `src/main/java/org/example/RecommendationSystemLauncher.java`: Main entry point for the application
- `src/main/java/org/example/testUI/`: Contains the UI components and recommendation engine
- `src/main/java/org/example/WebCrawler/`: Web scraping utilities
- `src/main/java/org/example/PageRanking/`: Text analysis and page evaluation tools
- `src/main/java/org/example/Parser/`: HTML parsing utilities
- `src/main/java/org/example/WordFrequencyAnalyzer/`: Text analysis for determining common watch features

## System Requirements

- **Operating System**: Windows, macOS, or Linux
- **Memory**: Minimum 4GB RAM recommended
- **Disk Space**: 100MB for the application, plus additional space for data files

## Future Enhancements

- Add support for additional smartwatch brands
- Implement price-based filtering
- Create comparison view for multiple watches
- Add user profiles to save preferences
- Provide image gallery for each watch model

---

_Author: Yuvashree Senthilmurugan_