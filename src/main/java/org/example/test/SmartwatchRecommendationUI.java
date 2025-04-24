package org.example.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Parser.InvertedIndex;
import org.example.Parser.PageRanking;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class SmartwatchRecommendationUI extends JFrame {
    private Workbook appleWorkbook;
    private Workbook gshockWorkbook;
    private Workbook garminWorkbook;
    private Workbook noiseWorkbook;
    private JTable table;
    private VocabularyBuilder vocabularyBuilder;
    private SpellChecker spellChecker;
    private FrequencyCounter frequencyCounter;
    private WordCompletionNew wordCompletion;
    private JButton pageRankingButton;
    private JButton invertedIndexButton;
    private JButton spellCheckerButton;
    private JButton frequencyCountButton;
    private JButton wordCompletionButton;




    public SmartwatchRecommendationUI(String appleFilePath, String gshockFilePath, String garminFilePath, String noiseFilePath) throws IOException {
        FileInputStream appleFileInputStream = new FileInputStream(appleFilePath);
        this.appleWorkbook = new XSSFWorkbook(appleFileInputStream);

        FileInputStream gshockFileInputStream = new FileInputStream(gshockFilePath);
        this.gshockWorkbook = new XSSFWorkbook(gshockFileInputStream);

        FileInputStream garminFileInputStream = new FileInputStream(garminFilePath);
        this.garminWorkbook = new XSSFWorkbook(garminFileInputStream);

        FileInputStream noiseFileInputStream = new FileInputStream(noiseFilePath);
        this.noiseWorkbook = new XSSFWorkbook(noiseFileInputStream);

        this.vocabularyBuilder = new VocabularyBuilder(appleWorkbook, gshockWorkbook);
        this.spellChecker = new SpellChecker(vocabularyBuilder.getVocabulary());
        this.frequencyCounter = new FrequencyCounter();

        initUI();
    }

    private void initUI() {
        setTitle("Smartwatch Recommendation System");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Initialize WordCompletionNew and load vocabularies
        try {
            initializeWordCompletion();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error initializing Word Completion: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Add title
        JLabel titleLabel = new JLabel("Smartwatch Recommendation System", SwingConstants.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Feature selection
        JPanel featurePanel = new JPanel(new GridLayout(0, 1));
        String[] features = {
                "OLED Always-on Retina display", "OLED Retina display", "ECG",
                "Up to 18 hours", "Up to 36 hours", "Up to 72 hours in Low Power Mode",
                "High and low heart rate notifications", "Blood Oxygen app", "Sleep Tracking", "GPS",
                "Swimproof", "Fast charging", "Cycle Tracking", "Bluetooth", "Offline maps",
                "aluminum", "titanium", "Mindfulness app with state of mind tracking",
                "Water-resistant", "Apple Pay", "Mineral Glass", "Shock Resistant", "REALTIME STAMINA", "Do not disturb mode"
        };
        JCheckBox[] featureCheckboxes = new JCheckBox[features.length];

        for (int i = 0; i < features.length; i++) {
            featureCheckboxes[i] = new JCheckBox(features[i]);
            featurePanel.add(featureCheckboxes[i]);
        }
        panel.add(featurePanel, BorderLayout.WEST);

        // Table
        table = new JTable();
        table.setModel(new DefaultTableModel(new Object[]{"Model Name", "Price"}, 0));
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton recommendButton = new JButton("Recommend");
        JButton detailsButton = new JButton("Details");
        JButton priceButton = new JButton("Filter by Price");
        JButton frequencyButton = new JButton("Search Frequency");


        recommendButton.addActionListener(e -> {
            Set<String> selectedFeatures = new HashSet<>();
            for (JCheckBox box : featureCheckboxes) {
                if (box.isSelected()) selectedFeatures.add(box.getText());
            }
            displayRecommendations(selectedFeatures);
        });

        detailsButton.addActionListener(e -> {
            // List of valid watch types
            Set<String> validWatchTypes = Set.of("apple", "garmin", "gshock", "noise");

            while (true) {
                // Prompt user to input the watch type
                String watchType = JOptionPane.showInputDialog(this, "Enter watch type (Apple, Garmin, GShock, Noise):");
                if (watchType == null || watchType.trim().isEmpty()) {
                    return; // Cancel or empty input, exit the loop
                }

                // Normalize the input for case-insensitive comparison
                String normalizedWatchType = watchType.trim().toLowerCase();

                // Validate the watch type
                if (!validWatchTypes.contains(normalizedWatchType)) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid watch type. Please enter a valid type: Apple, Garmin, GShock, or Noise.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    // If valid, prompt for the watch name
                    String watchName = JOptionPane.showInputDialog(this, "Enter the watch name:");
                    if (watchName == null || watchName.trim().isEmpty()) {
                        return; // Exit if no watch name is provided
                    }

                    // Call displayDetails with validated inputs
                    displayDetails(watchName.trim(), normalizedWatchType);
                    break; // Exit the loop after successful validation and processing
                }
            }
        });



        frequencyButton.addActionListener(e -> {
            // Display search frequencies in a dialog
            Map<String, Integer> frequencies = frequencyCounter.getSearchFrequencies();

            if (frequencies.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No searches have been recorded yet.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder frequencyMessage = new StringBuilder("Search Frequencies:\n");
            frequencies.forEach((key, value) -> frequencyMessage.append(key).append(": ").append(value).append(" times\n"));

            JOptionPane.showMessageDialog(this, frequencyMessage.toString(), "Search Frequencies", JOptionPane.INFORMATION_MESSAGE);
        });


        priceButton.addActionListener(e -> {
            // Create a panel with radio buttons for price range selection
            JRadioButton range1 = new JRadioButton("$0 - $100");
            JRadioButton range2 = new JRadioButton("$100 - $200");
            JRadioButton range3 = new JRadioButton("$200 - $500");
            JRadioButton range4 = new JRadioButton("$500+");

            // Group the radio buttons to allow only one selection
            ButtonGroup group = new ButtonGroup();
            group.add(range1);
            group.add(range2);
            group.add(range3);
            group.add(range4);

            JPanel panelPrice = new JPanel(new GridLayout(0, 1));
            panelPrice.add(new JLabel("Select a price range:"));
            panelPrice.add(range1);
            panelPrice.add(range2);
            panelPrice.add(range3);
            panelPrice.add(range4);

            int result = JOptionPane.showConfirmDialog(
                    this, panelPrice, "Price Range Selection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                double minPrice = 0, maxPrice = Double.MAX_VALUE;

                if (range1.isSelected()) {
                    minPrice = 0;
                    maxPrice = 100;
                } else if (range2.isSelected()) {
                    minPrice = 100;
                    maxPrice = 200;
                } else if (range3.isSelected()) {
                    minPrice = 200;
                    maxPrice = 500;
                } else if (range4.isSelected()) {
                    minPrice = 500;
                    maxPrice = Double.MAX_VALUE;
                } else {
                    JOptionPane.showMessageDialog(this, "No price range selected.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Display watches in the selected price range
                displayPriceFilteredWatches(minPrice, maxPrice);
            }
        });

        pageRankingButton = new JButton("Page Ranking");
        pageRankingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Page Ranking window when the button is clicked
                openPageRankingWindow();
            }
        });
        buttonPanel.add(pageRankingButton);

        // Add the bottom panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);
        invertedIndexButton = new JButton("Inverted Indexing");
        invertedIndexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openInvertedIndexWindow();
            }
        });
        buttonPanel.add(invertedIndexButton);

        // Add the bottom panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);


        spellCheckerButton = new JButton("Spell Checker");
        spellCheckerButton.addActionListener(e -> openSpellCheckerWindow());
        buttonPanel.add(spellCheckerButton);

        frequencyCountButton = new JButton("Frequency Count");
        frequencyCountButton.addActionListener(e -> openFrequencyCountWindow());
        buttonPanel.add(frequencyCountButton);


        buttonPanel.add(recommendButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(priceButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(frequencyButton);

        wordCompletionButton = new JButton("Word Completion");
        wordCompletionButton.addActionListener(e -> openWordCompletionWindow());
        buttonPanel.add(wordCompletionButton);



        add(panel);
    }

    private void initializeWordCompletion() throws IOException {
        wordCompletion = new WordCompletionNew();
        List<String> filePaths = Arrays.asList(
                "noise.xlsx",
                "garmin_models.xlsx",
                "AppleWatchComparison.xlsx",
                "GShockSmartwatchDetails.xlsx"
        );
        wordCompletion.loadVocabularies(filePaths);
    }

    // This function will open the Page Ranking window when the button is clicked
    // This function will open the Page Ranking window when the button is clicked
    private void openPageRankingWindow() {
        // Create a new window (JFrame) for Page Ranking
        JFrame pageRankingWindow = new JFrame("Page Ranking");
        pageRankingWindow.setSize(600, 400);
        pageRankingWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a JPanel to hold the content inside the page ranking window
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add a label explaining what the user needs to do
        JLabel label = new JLabel("Enter the keywords for Page Ranking (comma-separated):");
        panel.add(label);

        // Add a text field for user input
        JTextField textField = new JTextField(20);
        panel.add(textField);

        // Add a button to start the Page Ranking process
        JButton startButton = new JButton("Start Ranking");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the input keywords from the text field
                String input = textField.getText().trim();
                if (!input.isEmpty()) {
                    String[] keywords = input.split(",");
                    try {
                        // Create a JTextArea to display the output
                        JTextArea outputArea = new JTextArea(15, 50);
                        outputArea.setEditable(false); // Make the JTextArea non-editable

                        // Call the PageRanking class to process the ranking
                        PageRankingGUI.ProcessPageRanking(keywords, outputArea);

                        // Add the JTextArea to the window
                        JScrollPane scrollPane = new JScrollPane(outputArea);
                        panel.add(scrollPane);

                        // Revalidate the window to refresh the content and show the output
                        pageRankingWindow.revalidate();
                        pageRankingWindow.repaint(); // Ensure the window is repainted to show the updated content

                    } catch (IOException ioException) {
                        JOptionPane.showMessageDialog(pageRankingWindow, "Error processing page ranking.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(pageRankingWindow, "Please enter keywords to search.", "Input Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        panel.add(startButton);

        // Add the panel to the page ranking window
        pageRankingWindow.add(panel);

        // Make the page ranking window visible
        pageRankingWindow.setVisible(true);
    }

    // Inverted Indexing window
    private void openInvertedIndexWindow() {
        JFrame invertedIndexWindow = new JFrame("Inverted Indexing");
        invertedIndexWindow.setSize(600, 400);
        invertedIndexWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Enter a word to search:");
        panel.add(label);

        JTextField textField = new JTextField(20);
        panel.add(textField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchWord = textField.getText().trim();
                if (!searchWord.isEmpty()) {
                    try {
                        JTextArea outputArea = new JTextArea(15, 50);
                        outputArea.setEditable(false);
                        InvertedIndex.ProcessInvertedIndex(searchWord, outputArea);
                        panel.add(new JScrollPane(outputArea));
                        invertedIndexWindow.revalidate();
                        invertedIndexWindow.repaint();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(invertedIndexWindow, "Error processing inverted index.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(invertedIndexWindow, "Please enter a word to search.", "Input Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        panel.add(searchButton);
        invertedIndexWindow.add(panel);
        invertedIndexWindow.setVisible(true);
    }

    private void openSpellCheckerWindow() {
        // Modal Dialog for Spell Checker
        JDialog spellCheckerDialog = new JDialog(this, "Spell Checker", true);
        spellCheckerDialog.setSize(400, 300);
        spellCheckerDialog.setLayout(new FlowLayout());

        // Input and Output Components
        JLabel instructionLabel = new JLabel("Enter a word to check spelling:");
        JTextField inputField = new JTextField(20);
        JButton checkButton = new JButton("Check Spelling");
        JLabel resultLabel = new JLabel("<html>Result will appear here.</html>");
        resultLabel.setPreferredSize(new Dimension(350, 50));

        // Add ActionListener to the Check Button
        checkButton.addActionListener(e -> {
            String input = inputField.getText().trim().toLowerCase();
            if (!input.isEmpty()) {
                try {
                    // Perform Spell Check
                    String result = SpellCheckingNew.checkWord(input);
                    resultLabel.setText("<html>" + result.replace("\n", "<br>") + "</html>");
                } catch (Exception ex) {
                    resultLabel.setText("Error: Unable to process the word.");
                    ex.printStackTrace();
                }
            } else {
                resultLabel.setText("Please enter a word.");
            }
        });

        // Add Components to the Dialog
        spellCheckerDialog.add(instructionLabel);
        spellCheckerDialog.add(inputField);
        spellCheckerDialog.add(checkButton);
        spellCheckerDialog.add(resultLabel);

        spellCheckerDialog.setVisible(true);
    }

    private void openWordCompletionWindow() {
        JDialog wordCompletionDialog = new JDialog(this, "Word Completion", true);
        wordCompletionDialog.setSize(400, 300);
        wordCompletionDialog.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Enter prefix for autocomplete:");
        JTextField inputField = new JTextField(20);
        JButton autocompleteButton = new JButton("Autocomplete");
        JTextArea resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);

        autocompleteButton.addActionListener(e -> {
            String prefix = inputField.getText().trim();
            if (!prefix.isEmpty()) {
                if (wordCompletion != null) { // Ensure wordCompletion is initialized
                    List<String> suggestions = wordCompletion.autocomplete(prefix, 5);
                    resultArea.setText(String.join("\n", suggestions));
                } else {
                    resultArea.setText("Word Completion not initialized.");
                }
            } else {
                resultArea.setText("Please enter a prefix.");
            }
        });

        wordCompletionDialog.add(instructionLabel);
        wordCompletionDialog.add(inputField);
        wordCompletionDialog.add(autocompleteButton);
        wordCompletionDialog.add(new JScrollPane(resultArea));

        wordCompletionDialog.setVisible(true);
    }

    private void openFrequencyCountWindow() {
        // Modal Dialog for Frequency Count
        JDialog frequencyCountDialog = new JDialog(this, "Frequency Count", true);
        frequencyCountDialog.setSize(400, 300);
        frequencyCountDialog.setLayout(new FlowLayout());

        // Input and Output Components
        JLabel instructionLabel = new JLabel("Enter a word to check its frequency:");
        JTextField inputField = new JTextField(20);
        JButton checkButton = new JButton("Check Frequency");
        JLabel resultLabel = new JLabel("<html>Result will appear here.</html>");
        resultLabel.setPreferredSize(new Dimension(350, 50));

        // Add ActionListener to the Check Button
        checkButton.addActionListener(e -> {
            String input = inputField.getText().trim().toLowerCase();
            if (!input.isEmpty()) {
                String result = FrequencyCountNew.queryFrequency(input);
                resultLabel.setText("<html>" + result.replace("\n", "<br>") + "</html>");
            } else {
                resultLabel.setText("Please enter a word.");
            }
        });

        // Add Components to the Dialog
        frequencyCountDialog.add(instructionLabel);
        frequencyCountDialog.add(inputField);
        frequencyCountDialog.add(checkButton);
        frequencyCountDialog.add(resultLabel);

        frequencyCountDialog.setVisible(true);
    }


    private void displayRecommendations(Set<String> selectedFeatures) {
        List<String> recommendations = new ArrayList<>();
        recommendations.addAll(checkWorkbook(appleWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(gshockWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(garminWorkbook, selectedFeatures));
        recommendations.addAll(checkWorkbook(noiseWorkbook, selectedFeatures));

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Recommended Watches"}, 0);
        for (String watch : recommendations) {
            model.addRow(new Object[]{watch});
        }

        if (recommendations.isEmpty()) {
            model.addRow(new Object[]{"No matching watches found."});
        }
        table.setModel(model);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        // Check the type of the cell
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();  // Handle string type
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()).trim();  // Handle numeric type
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();  // Handle boolean type
            case FORMULA:
                return cell.getCellFormula();  // Handle formulas (if they exist)
            default:
                return "";
        }
    }




    private void displayDetails(String watchName, String watchType) {
        Sheet sheet = null;

        // Determine the appropriate workbook based on the watch type
        switch (watchType.toLowerCase()) {
            case "apple":
                sheet = appleWorkbook.getSheet(watchName);
                break;
            case "garmin":
                sheet = garminWorkbook.getSheet(watchName);
                break;
            case "gshock":
                sheet = gshockWorkbook.getSheet(watchName);
                break;
            case "noise":
                sheet = noiseWorkbook.getSheet(watchName);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Invalid watch type. Please enter a valid type: Apple, Garmin, GShock, or Noise.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }

        // If no sheet is found, suggest corrections using SpellChecker
        if (sheet == null) {
            List<String> suggestions = spellChecker.suggestCorrections(watchName);
            if (suggestions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Watch not found and no suggestions available.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                StringBuilder suggestionMessage = new StringBuilder("Watch not found. Did you mean:\n");
                for (String suggestion : suggestions) {
                    suggestionMessage.append("- ").append(suggestion).append("\n");
                }
                JOptionPane.showMessageDialog(this, suggestionMessage.toString(), "Suggestions", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        // Update search frequency
        frequencyCounter.addSearchQuery(watchName);

        // Create a table model to display the details
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Feature", "Detail"}, 0);

        // Call the brand-specific display method based on watch type
        if (watchType.equalsIgnoreCase("apple")) {
            displayAppleWatchDetails(sheet, model);
        } else if (watchType.equalsIgnoreCase("garmin")) {
            displayGarminWatchDetails(sheet, model);
        } else if (watchType.equalsIgnoreCase("gshock")) {
            displayGShockWatchDetails(sheet, model);
        } else if (watchType.equalsIgnoreCase("noise")) {
            displayNoiseWatchDetails(sheet, model);
        }

        // Update the table to display the details
        table.setModel(model);
    }


    private void displayGarminWatchDetails(Sheet sheet, DefaultTableModel model) {
        String currentHeading = "";

        // Display model name (assumed to be in the first row, second column)
        Row nameRow = sheet.getRow(0);
        if (nameRow != null && nameRow.getCell(1) != null) {
            String originalName = getCellValueAsString(nameRow.getCell(1));
            model.addRow(new Object[]{"Model Name", originalName});
        }

        // Display price (assumed to be in the second row, second column)
        Row priceRow = sheet.getRow(1);
        if (priceRow != null && priceRow.getCell(1) != null) {
            String price = getCellValueAsString(priceRow.getCell(1));
            model.addRow(new Object[]{"Price", price});
        }

        // Display features (assumed to be in the second row, third column, separated by semicolons)
        Row featuresRow = sheet.getRow(1);
        if (featuresRow != null && featuresRow.getCell(2) != null) {
            String features = getCellValueAsString(featuresRow.getCell(2));
            String[] featureList = features.split(";");  // Assuming features are separated by semicolons
            model.addRow(new Object[]{"Features", featureList[0].trim()});
            for (int i = 1; i < featureList.length; i++) {
                model.addRow(new Object[]{"", featureList[i].trim()});
            }
        }

        // Additional details (if any) can be added here following similar patterns
        for (Row row : sheet) {
            Cell headingCell = row.getCell(0);
            Cell detailCell = row.getCell(1);

            if (headingCell != null) {
                String heading = getCellValueAsString(headingCell).trim();
                if (!heading.isEmpty() && !heading.equals(currentHeading)) {
                    model.addRow(new Object[]{heading, ""});
                    currentHeading = heading;
                }
            }

            if (detailCell != null) {
                String detail = getCellValueAsString(detailCell).trim();
                if (!detail.isEmpty()) {
                    model.addRow(new Object[]{"", detail});
                }
            }
        }
    }



    private void displayNoiseWatchDetails(Sheet sheet, DefaultTableModel model) {
        // Loop through the rows of the sheet
        for (Row row : sheet) {
            // Ensure the row has data in both columns A and B (index 0 for A, 1 for B)
            Cell headingCell = row.getCell(0);  // Column A (Heading)
            Cell valueCell = row.getCell(1);    // Column B (Value)

            if (headingCell != null && valueCell != null) {
                // Get the heading and value as strings
                String heading = getCellValueAsString(headingCell).trim();
                String value = getCellValueAsString(valueCell).trim();

                if (!heading.isEmpty() && !value.isEmpty()) {
                    // Add the heading and value to the table
                    model.addRow(new Object[]{heading, value});
                }
            }
        }

        // After filling the model, configure the table to use a JTextArea for large text rendering
        table.setModel(model);
        configureTableForTextArea(table);  // Configure the table for better text display
    }





    private void displayGShockWatchDetails(Sheet sheet, DefaultTableModel model) {
        // Get model name from cell A2
        Row nameRow = sheet.getRow(1); // Assuming row 1 has the model name (A2)
        if (nameRow != null && nameRow.getCell(0) != null) { // A2 contains the model name
            String modelName = getCellValueAsString(nameRow.getCell(0)); // Get value from A2
            model.addRow(new Object[]{"Model Name", modelName});
        }

        // Get price from cell B2
        Row priceRow = sheet.getRow(1); // Assuming row 1 has the price (B2)
        if (priceRow != null && priceRow.getCell(1) != null) { // B2 contains the price
            String price = getCellValueAsString(priceRow.getCell(1)); // Get value from B2
            model.addRow(new Object[]{"Price", price});
        }

        // Get features from cell C2 (C1 contains "Features", C2 contains the actual features)
        Row featuresRow = sheet.getRow(1); // Assuming row 1 has the features (C2)
        if (featuresRow != null && featuresRow.getCell(2) != null) { // C2 contains the features
            String features = getCellValueAsString(featuresRow.getCell(2)); // Get value from C2
            String[] featureList = features.split(";");  // Split features by semicolons if multiple

            // Add each feature as a new row
            for (String feature : featureList) {
                model.addRow(new Object[]{"Feature", feature.trim()});
            }
        }

        // After populating the model, configure the table for text rendering
        table.setModel(model);  // Set the model to the JTable
        configureTableForTextArea(table);  // Configure the table for large text rendering
    }




    private void configureTableForTextArea(JTable table) {
        // Set the row height to a larger size for better readability
        table.setRowHeight(100);  // Adjust the row height based on your needs

        // Set the renderer for the table cells to JTextArea for large text handling
        table.getColumnModel().getColumn(1).setCellRenderer(new JTextAreaRenderer());

        // Adjust the column widths to allow enough space for text
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(500);  // Make the second column wider for large text
    }


    class JTextAreaRenderer extends JTextArea implements TableCellRenderer {
        public JTextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(value != null ? value.toString() : "");
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }






    private void displayAppleWatchDetails(Sheet sheet, DefaultTableModel model) {
        String currentHeading = "";

        // Display model name (assumed to be in the first row, first column)
        Row nameRow = sheet.getRow(0);
        if (nameRow != null && nameRow.getCell(1) != null) {
            String originalName = nameRow.getCell(1).getStringCellValue();
            model.addRow(new Object[]{"Model Name", originalName});
        }

        // Display price (assumed to be in the second row, first column)
        Row priceRow = sheet.getRow(1);
        if (priceRow != null && priceRow.getCell(1) != null) {
            String price = priceRow.getCell(1).getStringCellValue();
            model.addRow(new Object[]{"Price", price});
        }

        // Display features (assumed to be in the second row, third column, separated by semicolons)
        Row featuresRow = sheet.getRow(1);
        if (featuresRow != null && featuresRow.getCell(2) != null) {
            String features = featuresRow.getCell(2).getStringCellValue();
            String[] featureList = features.split(";");  // Assuming features are separated by semicolons
            model.addRow(new Object[]{"Features", featureList[0].trim()});
            for (int i = 1; i < featureList.length; i++) {
                model.addRow(new Object[]{"", featureList[i].trim()});
            }
        }

        // Additional details (if any) can be added here following similar patterns
        for (Row row : sheet) {
            Cell headingCell = row.getCell(0);
            Cell detailCell = row.getCell(1);

            if (headingCell != null) {
                String heading = headingCell.getStringCellValue().trim();
                if (!heading.isEmpty() && !heading.equals(currentHeading)) {
                    model.addRow(new Object[]{heading, ""});
                    currentHeading = heading;
                }
            }

            if (detailCell != null) {
                String detail = detailCell.getStringCellValue().trim();
                if (!detail.isEmpty()) {
                    model.addRow(new Object[]{"", detail});
                }
            }
        }
    }

    class WatchEntry {
        private String name;
        private double price;

        public WatchEntry(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }
    }



    private void displayPriceFilteredWatches(double minPrice, double maxPrice) {
        List<WatchEntry> filteredWatches = new ArrayList<>();

        // Collect watches in all workbooks within the selected price range
        filteredWatches.addAll(getWatchesByPrice(appleWorkbook, minPrice, maxPrice));
        filteredWatches.addAll(getWatchesByPrice(gshockWorkbook, minPrice, maxPrice));
        filteredWatches.addAll(getWatchesByPrice(garminWorkbook, minPrice, maxPrice));
        filteredWatches.addAll(getWatchesByPrice(noiseWorkbook, minPrice, maxPrice));

        // Sort the watches by price in ascending order
        filteredWatches.sort(Comparator.comparingDouble(WatchEntry::getPrice));

        // Create a table model to display the filtered watches with prices
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Watch Name", "Price"}, 0);
        for (WatchEntry watch : filteredWatches) {
            model.addRow(new Object[]{watch.getName(), String.format("$%.2f", watch.getPrice())});
        }

        if (filteredWatches.isEmpty()) {
            model.addRow(new Object[]{"No watches found in the selected price range.", ""});
        }
        table.setModel(model);
    }



    private List<String> checkWorkbook(Workbook workbook, Set<String> selectedFeatures) {
        List<String> matches = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (matchesFeatures(sheet, selectedFeatures)) {
                matches.add(sheet.getSheetName());
            }
        }
        return matches;
    }

    private boolean matchesFeatures(Sheet sheet, Set<String> selectedFeatures) {
        for (String feature : selectedFeatures) {
            boolean found = false;
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(feature)) {
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

    private List<WatchEntry> getWatchesByPrice(Workbook workbook, double minPrice, double maxPrice) {
        List<WatchEntry> watches = new ArrayList<>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            double price = extractPrice(sheet);
            if (price >= minPrice && price <= maxPrice) {
                watches.add(new WatchEntry(sheet.getSheetName(), price));
            }
        }
        return watches;
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

    public static void main(String[] args) {
        // Initialize words from Excel files
        String[] filePaths = {
                "AppleWatchComparison.xlsx",
                "garmin_models.xlsx",
                "GShockSmartwatchDetails.xlsx",
                "noise.xlsx"
        };

        try {
            SpellCheckingNew.initializeWords(filePaths);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to initialize spell checker data.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        try {
            FrequencyCountNew.initializeFrequencies(filePaths);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to initialize frequency data.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new SmartwatchRecommendationUI(
                        "AppleWatchComparison.xlsx",
                        "GShockSmartwatchDetails.xlsx",
                        "garmin_models.xlsx",
                        "noise.xlsx"
                ).setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}