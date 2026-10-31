package com.expensetracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ExpenseManager – Business Logic Layer
 *
 * This class manages all expenses in memory.
 * It handles adding, deleting, filtering, sorting, and
 * calculating summaries. Think of it as the "brain" of the app.
 */
public class ExpenseManager {

    // The list that holds all expenses loaded from file (or added during session)
    private List<Expense> expenses;

    // The user's monthly budget (default: 5000)
    private double monthlyBudget;

    // Warning threshold – warn the user when spending exceeds this percentage
    public static final double WARNING_THRESHOLD = 0.80; // 80%

    /**
     * Constructor – creates an empty manager with a default budget.
     */
    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.monthlyBudget = 5000.0;
    }

    // ===================== EXPENSE OPERATIONS =====================

    /**
     * Adds a new expense to the list.
     */
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    /**
     * Removes an expense at the given index.
     */
    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
        }
    }

    /**
     * Returns all expenses (unfiltered).
     */
    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses); // return a copy so the original can't be changed externally
    }

    /**
     * Sets the full list of expenses (used when loading from file).
     */
    public void setExpenses(List<Expense> expenses) {
        this.expenses = new ArrayList<>(expenses);
    }

    // ===================== SEARCH & FILTER =====================

    /**
     * Filters expenses by keyword (searches in description and category).
     * Case-insensitive search.
     */
    public List<Expense> searchExpenses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllExpenses();
        }
        String lower = keyword.toLowerCase().trim();
        return expenses.stream()
                .filter(e -> e.getDescription().toLowerCase().contains(lower)
                          || e.getCategory().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses by category.
     * Returns all expenses if category is null or "All".
     */
    public List<Expense> filterByCategory(String category) {
        if (category == null || category.equals("All")) {
            return getAllExpenses();
        }
        return expenses.stream()
                .filter(e -> e.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Returns expenses for the current month only.
     */
    public List<Expense> getCurrentMonthExpenses() {
        LocalDate now = LocalDate.now();
        return expenses.stream()
                .filter(e -> e.getDate().getMonth() == now.getMonth()
                          && e.getDate().getYear() == now.getYear())
                .collect(Collectors.toList());
    }

    // ===================== SORTING =====================

    /**
     * Sorts a list of expenses by date (newest first).
     */
    public List<Expense> sortByDateDescending(List<Expense> list) {
        return list.stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of expenses by date (oldest first).
     */
    public List<Expense> sortByDateAscending(List<Expense> list) {
        return list.stream()
                .sorted(Comparator.comparing(Expense::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of expenses by amount (highest first).
     */
    public List<Expense> sortByAmountDescending(List<Expense> list) {
        return list.stream()
                .sorted(Comparator.comparingDouble(Expense::getAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of expenses by amount (lowest first).
     */
    public List<Expense> sortByAmountAscending(List<Expense> list) {
        return list.stream()
                .sorted(Comparator.comparingDouble(Expense::getAmount))
                .collect(Collectors.toList());
    }

    // ===================== CALCULATIONS =====================

    /**
     * Calculates total expenses for the current month.
     */
    public double getTotalMonthlyExpenses() {
        return getCurrentMonthExpenses().stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Calculates remaining budget for this month.
     */
    public double getRemainingBudget() {
        return monthlyBudget - getTotalMonthlyExpenses();
    }

    /**
     * Returns how many transactions exist for this month.
     */
    public int getMonthlyTransactionCount() {
        return getCurrentMonthExpenses().size();
    }

    /**
     * Groups expenses by category and sums up amounts.
     * Returns a map like: { "Food" -> 1200.0, "Transport" -> 450.0, ... }
     */
    public Map<String, Double> getCategoryWiseSummary() {
        Map<String, Double> summary = new HashMap<>();
        for (String cat : Expense.CATEGORIES) {
            summary.put(cat, 0.0); // initialise all categories at 0
        }
        for (Expense e : getCurrentMonthExpenses()) {
            summary.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return summary;
    }

    // ===================== BUDGET =====================

    /**
     * Returns the monthly budget.
     */
    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    /**
     * Sets a new monthly budget. Must be positive.
     */
    public void setMonthlyBudget(double budget) {
        if (budget > 0) {
            this.monthlyBudget = budget;
        }
    }

    /**
     * Returns true if spending has exceeded 80% of the budget.
     */
    public boolean isNearBudgetLimit() {
        if (monthlyBudget <= 0) return false;
        return getTotalMonthlyExpenses() >= monthlyBudget * WARNING_THRESHOLD;
    }

    /**
     * Returns true if spending has exceeded the total budget.
     */
    public boolean isBudgetExceeded() {
        return getTotalMonthlyExpenses() > monthlyBudget;
    }

    /**
     * Returns the spending percentage (0–100+).
     */
    public double getSpendingPercentage() {
        if (monthlyBudget <= 0) return 0;
        return (getTotalMonthlyExpenses() / monthlyBudget) * 100.0;
    }
}
