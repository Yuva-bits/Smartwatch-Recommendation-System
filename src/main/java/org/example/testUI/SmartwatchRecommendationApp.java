package org.example.testUI;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SmartwatchRecommendationApp extends JFrame {
    private SmartwatchRecommendationSystem recommendationSystem;
    private JTabbedPane tabbedPane;
    private JPanel recommendationPanel;
    private JPanel detailsPanel;
    private JPanel searchFrequencyPanel;

    public SmartwatchRecommendationApp() {
        try {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            System.out.println("Loading Excel files...");
            
            recommendationSystem = new SmartwatchRecommendationSystem(
                    "AppleWatchComparison.xlsx",
                    "GShockSmartwatchDetails.xlsx",
                    "garmin_models.xlsx",
                    "noise.xlsx");

            initComponents();
            System.out.println("GUI components initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error loading watch data: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error loading watch data: " + e.getMessage() + "\nCheck console for details.",
                    "Initialization Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("Smartwatch Recommendation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        tabbedPane = new JTabbedPane();

        // Recommendation Panel
        recommendationPanel = createRecommendationPanel();
        tabbedPane.addTab("Recommendations", recommendationPanel);

        // Details Panel
        detailsPanel = createDetailsPanel();
        tabbedPane.addTab("Watch Details", detailsPanel);

        // Search Frequency Panel
        searchFrequencyPanel = createSearchFrequencyPanel();
        tabbedPane.addTab("Search Frequencies", searchFrequencyPanel);

        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Feature Selection
        String[] availableFeatures = {
                "One Time", "OLED Always-on Retina display", "OLED Retina display", "ECG",
                "Up to 18 hours", "Up to 36 hours", "Up to 72 hours in Low Power Mode",
                "High and low heart rate notifications", "Blood Oxygen app", "Sleep Tracking", "GPS",
                "Swimproof", "Fast charging", "Cycle Tracking", "Bluetooth", "Offline maps",
                "aluminum", "titanium", "Mindfulness app with state of mind tracking",
                "Water-resistant", "Apple Pay", "Mineral Glass", "Shock Resistant",
                "REALTIME STAMINA", "Do not disturb mode"
        };

        JPanel featurePanel = new JPanel();
        featurePanel.setLayout(new BoxLayout(featurePanel, BoxLayout.Y_AXIS));

        List<JCheckBox> featureCheckBoxes = new ArrayList<>();
        for (String feature : availableFeatures) {
            JCheckBox checkBox = new JCheckBox(feature);
            featureCheckBoxes.add(checkBox);
            featurePanel.add(checkBox);
        }

        JScrollPane featureScrollPane = new JScrollPane(featurePanel);

        JButton recommendButton = new JButton("Recommend Smartwatches");
        JTextArea resultsArea = new JTextArea(10, 40);
        resultsArea.setEditable(false);

        recommendButton.addActionListener(e -> {
            Set<String> selectedFeatures = featureCheckBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(JCheckBox::getText)
                    .collect(Collectors.toSet());

            if (selectedFeatures.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please select at least one feature",
                        "No Features Selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> recommendations = new ArrayList<>();
            recommendationSystem.recommendSmartwatch(selectedFeatures, recommendations);

            if (recommendations.isEmpty()) {
                resultsArea.setText("No matching smartwatches found.");
            } else {
                resultsArea.setText("Recommended Models:\n" +
                        String.join("\n", recommendations));
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(recommendButton);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        panel.add(featureScrollPane, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(resultPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Watch Type Selection
        String[] watchTypes = {"Apple", "G-Shock", "Garmin", "Noise"};
        JComboBox<String> watchTypeCombo = new JComboBox<>(watchTypes);

        // Watch Name Input
        JTextField watchNameField = new JTextField(20);
        JButton showDetailsButton = new JButton("Show Details");

        // Details Display Area
        JTextArea detailsArea = new JTextArea(15, 50);
        detailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);

        showDetailsButton.addActionListener(e -> {
            String selectedType = (String) watchTypeCombo.getSelectedItem();
            String watchName = watchNameField.getText().trim();

            if (watchName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a watch name",
                        "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Capture details in a StringBuilder
            StringBuilder details = new StringBuilder();
            switch (selectedType) {
                case "Apple":
                    recommendationSystem.displayWatchDetails(watchName, details);
                    break;
                case "G-Shock":
                    recommendationSystem.displayGShockWatchDetails(watchName, details);
                    break;
                case "Garmin":
                    recommendationSystem.displayGarminWatchDetails(watchName, details);
                    break;
                case "Noise":
                    recommendationSystem.displayNoiseWatchDetails(watchName, details);
                    break;
            }

            // Update details area
            if (details.length() > 0) {
                detailsArea.setText(details.toString());
                recommendationSystem.updateSearchFrequency(watchName);
            } else {
                detailsArea.setText("No details found for the selected watch.");
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Watch Type:"));
        inputPanel.add(watchTypeCombo);
        inputPanel.add(new JLabel("Watch Name:"));
        inputPanel.add(watchNameField);
        inputPanel.add(showDetailsButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(detailsScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchFrequencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table to display search frequencies
        String[] columnNames = {"Watch", "Search Count"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable frequencyTable = new JTable(model);

        JButton refreshButton = new JButton("Refresh Search Frequencies");
        refreshButton.addActionListener(e -> {
            // Clear existing rows
            model.setRowCount(0);

            // Get and populate search frequencies
            Map<String, Integer> frequencies = recommendationSystem.getSearchFrequencies();
            frequencies.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry ->
                            model.addRow(new Object[]{entry.getKey(), entry.getValue()})
                    );
        });

        panel.add(new JScrollPane(frequencyTable), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for a native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel if system look and feel fails
                e.printStackTrace();
            }

            SmartwatchRecommendationApp app = new SmartwatchRecommendationApp();
            app.setVisible(true);
        });
    }
}