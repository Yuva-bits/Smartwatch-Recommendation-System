package org.example;

import org.example.testUI.SmartwatchRecommendationApp;

import javax.swing.*;

/**
 * Main launcher for the Smartwatch Recommendation System GUI.
 * This class provides a convenient way to start the recommendation system.
 */
public class RecommendationSystemLauncher {
    
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

            // Create and display the recommendation app
            SmartwatchRecommendationApp app = new SmartwatchRecommendationApp();
            app.setVisible(true);
        });
    }
} 