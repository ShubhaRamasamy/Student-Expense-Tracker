package com.expensetracker;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileStorage – Data Persistence Layer
 *
 * This class reads and writes data to CSV files on disk.
 * Whenever the app starts, it loads saved data.
 * Whenever something changes, the data is saved back to disk.
 *
 * Files are stored in the "data/" folder next to the application.
 */
public class FileStorage {

    // Folder where the CSV files are saved
    private static final String DATA_DIR = "data";

    // File for expense records
    private static final String EXPENSES_FILE = DATA_DIR + "/expenses.csv";

    // File for budget setting (just one number)
    private static final String BUDGET_FILE = DATA_DIR + "/budget.txt";

    // CSV header line (first row of the expenses file)
    private static final String CSV_HEADER = "date,category,description,amount";

    /**
     * Ensures the data directory exists.
     * Called once at startup.
     */
    public static void ensureDataDirectoryExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created data directory: " + dir.getAbsolutePath());
            }
        }
    }

    // ===================== EXPENSE FILE OPERATIONS =====================

    /**
     * Saves all expenses to the CSV file.
     * Overwrites the entire file each time (simple approach for a small dataset).
     *
     * @param expenses The list of all expenses to save.
     */
    public static void saveExpenses(List<Expense> expenses) {
        ensureDataDirectoryExists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
            writer.println(CSV_HEADER); // write the header row first
            for (Expense expense : expenses) {
                writer.println(expense.toCsvLine());
            }
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    /**
     * Loads all expenses from the CSV file.
     * Returns an empty list if the file doesn't exist yet.
     *
     * @return List of Expense objects loaded from disk.
     */
    public static List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(EXPENSES_FILE);

        // If no file exists yet (first run), return empty list
        if (!file.exists()) {
            return expenses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip the header row
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Expense expense = Expense.fromCsvLine(line);
                    expenses.add(expense);
                } catch (Exception e) {
                    // If a line is corrupted, skip it and log a warning
                    System.err.println("Skipping invalid CSV line: " + line + " | Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
        }

        return expenses;
    }

    // ===================== BUDGET FILE OPERATIONS =====================

    /**
     * Saves the monthly budget to a text file.
     *
     * @param budget The budget amount to save.
     */
    public static void saveBudget(double budget) {
        ensureDataDirectoryExists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(BUDGET_FILE))) {
            writer.println(String.format("%.2f", budget));
        } catch (IOException e) {
            System.err.println("Error saving budget: " + e.getMessage());
        }
    }

    /**
     * Loads the monthly budget from file.
     * Returns the default budget if no file exists.
     *
     * @param defaultBudget The fallback value if no saved budget exists.
     * @return The saved budget amount.
     */
    public static double loadBudget(double defaultBudget) {
        File file = new File(BUDGET_FILE);

        if (!file.exists()) {
            return defaultBudget;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return Double.parseDouble(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading budget: " + e.getMessage());
        }

        return defaultBudget;
    }
}
