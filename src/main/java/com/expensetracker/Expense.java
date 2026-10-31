package com.expensetracker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Expense Model Class
 * Represents a single expense entry with all its details.
 * This is the "blueprint" for every expense the user adds.
 */
public class Expense {

    // Formatter used to convert dates to/from text (e.g., "2024-01-15")
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Categories a student can choose from
    public static final String[] CATEGORIES = {
        "Food", "Transport", "Education", "Shopping", "Entertainment", "Other"
    };

    // Each expense has these four pieces of information
    private LocalDate date;
    private String category;
    private String description;
    private double amount;

    /**
     * Constructor – creates a new Expense with all required fields.
     */
    public Expense(LocalDate date, String category, String description, double amount) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    // ---- Getters (read-only access to fields) ----

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    // ---- Setters (allow updating fields) ----

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Converts this expense into a CSV line.
     * Format: date,category,description,amount
     * Example: 2024-01-15,Food,Lunch at canteen,120.50
     */
    public String toCsvLine() {
        // Wrap description in quotes so commas inside it don't break CSV parsing
        return date.format(DATE_FORMATTER) + ","
                + category + ","
                + "\"" + description.replace("\"", "\"\"") + "\","
                + String.format("%.2f", amount);
    }

    /**
     * Creates an Expense object from a CSV line.
     * This is the reverse of toCsvLine().
     */
    public static Expense fromCsvLine(String csvLine) {
        // Simple CSV parser that handles quoted fields
        String[] parts = parseCsvLine(csvLine);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid CSV line: " + csvLine);
        }

        LocalDate date = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
        String category = parts[1].trim();
        String description = parts[2].trim();
        double amount = Double.parseDouble(parts[3].trim());

        return new Expense(date, category, description, amount);
    }

    /**
     * Parses a CSV line, correctly handling quoted fields with commas inside.
     */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString()); // add last field
        return result.toArray(new String[0]);
    }

    /**
     * Returns a human-readable summary of this expense.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: ₹%.2f",
                date.format(DATE_FORMATTER), category, description, amount);
    }
}
