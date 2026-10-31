package com.expensetracker;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * ExpenseTableModel – Custom Table Model for JTable
 *
 * JTable needs a "model" that tells it how many rows/columns there are
 * and what data to show in each cell. This class provides that.
 *
 * By extending AbstractTableModel, we only need to implement three methods.
 */
public class ExpenseTableModel extends AbstractTableModel {

    // Column headers shown at the top of the table
    private static final String[] COLUMN_NAMES = {"#", "Date", "Category", "Description", "Amount (₹)"};

    // The data currently displayed in the table
    private List<Expense> expenses;

    /**
     * Constructor – starts with an empty table.
     */
    public ExpenseTableModel() {
        this.expenses = new ArrayList<>();
    }

    /**
     * Updates the table with a new list of expenses.
     * Called whenever data changes (add, delete, filter, search).
     */
    public void setExpenses(List<Expense> expenses) {
        this.expenses = new ArrayList<>(expenses);
        fireTableDataChanged(); // tells JTable to refresh the display
    }

    /**
     * Returns the expense at a given row index.
     * Used when the user selects a row to delete.
     */
    public Expense getExpenseAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < expenses.size()) {
            return expenses.get(rowIndex);
        }
        return null;
    }

    // ===================== Required AbstractTableModel Methods =====================

    /**
     * How many rows are in the table? One row per expense.
     */
    @Override
    public int getRowCount() {
        return expenses.size();
    }

    /**
     * How many columns? We have 5 (index, date, category, description, amount).
     */
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    /**
     * What text should appear in a specific cell (row, column)?
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Expense expense = expenses.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1; // Row number (1-based)
            case 1 -> expense.getDate().format(Expense.DATE_FORMATTER);
            case 2 -> expense.getCategory();
            case 3 -> expense.getDescription();
            case 4 -> String.format("%.2f", expense.getAmount());
            default -> "";
        };
    }

    /**
     * What is the column header text?
     */
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    /**
     * What Java type does each column hold?
     * Used by JTable for sorting and rendering.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Integer.class;    // Row number
            case 4 -> String.class;     // Amount (formatted string with ₹)
            default -> String.class;
        };
    }

    /**
     * Are cells editable? No – the user should use the Add/Delete buttons.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
