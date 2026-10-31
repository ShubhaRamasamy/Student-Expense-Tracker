package com.expensetracker;

import javax.swing.*;
import java.util.List;

/**
 * Main – Application Entry Point
 *
 * This is the first class that Java runs when you start the application.
 * It:
 *  1. Sets the look and feel to match the OS (or a clean system theme)
 *  2. Loads saved expenses and budget from disk
 *  3. Creates the Dashboard window and shows it
 */
public class Main {

    public static void main(String[] args) {

        // ---- Step 1: Set a clean look-and-feel ----
        // 'invokeLater' ensures the GUI is created on the Event Dispatch Thread (Swing rule)
        SwingUtilities.invokeLater(() -> {

            // Try to use the Nimbus look-and-feel for a modern appearance.
            // If it's not available, fall back to the system default.
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());

                        // Customise Nimbus colours to match our blue theme
                        UIManager.put("nimbusBase",           new java.awt.Color(52, 152, 219));
                        UIManager.put("nimbusBlueGrey",       new java.awt.Color(220, 230, 240));
                        UIManager.put("control",              new java.awt.Color(245, 247, 250));
                        UIManager.put("text",                 new java.awt.Color(44, 62, 80));
                        UIManager.put("nimbusSelectionBackground", new java.awt.Color(52, 152, 219));
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus fails, use the default cross-platform look-and-feel
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ex) {
                    System.err.println("Could not set look-and-feel: " + ex.getMessage());
                }
            }

            // ---- Step 2: Load saved data from disk ----
            FileStorage.ensureDataDirectoryExists();

            ExpenseManager manager = new ExpenseManager();

            // Load previously saved expenses
            List<Expense> savedExpenses = FileStorage.loadExpenses();
            manager.setExpenses(savedExpenses);

            // Load previously saved budget (default: 5000 if no file found)
            double savedBudget = FileStorage.loadBudget(5000.0);
            manager.setMonthlyBudget(savedBudget);

            System.out.println("Loaded " + savedExpenses.size() + " expenses from disk.");
            System.out.println("Monthly budget: ₹" + savedBudget);

            // ---- Step 3: Show the Dashboard ----
            Dashboard dashboard = new Dashboard(manager);
            dashboard.setVisible(true);
        });
    }
}
