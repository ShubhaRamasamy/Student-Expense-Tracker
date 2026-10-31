package com.expensetracker;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * AddExpenseDialog – Modal Dialog for Adding a New Expense
 *
 * This dialog pops up when the user clicks "Add Expense".
 * It collects the expense details, validates them, and returns
 * the new Expense object to the caller (Dashboard).
 */
public class AddExpenseDialog extends JDialog {

    // ---- Color palette (matches the main Dashboard theme) ----
    private static final Color PRIMARY_BLUE   = new Color(52, 152, 219);
    private static final Color LIGHT_BG       = new Color(245, 247, 250);
    private static final Color WHITE          = Color.WHITE;
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_GRAY      = new Color(127, 140, 141);
    private static final Color SUCCESS_GREEN  = new Color(39, 174, 96);
    private static final Color BORDER_COLOR   = new Color(220, 230, 240);

    // ---- Form input components ----
    private JTextField amountField;
    private JComboBox<String> categoryCombo;
    private JTextField descriptionField;
    private JTextField dateField;
    private JButton saveButton;
    private JButton cancelButton;

    // The result of the dialog – null if cancelled, otherwise the new Expense
    private Expense result = null;

    /**
     * Constructor – creates and shows the dialog.
     *
     * @param parent The parent window (Dashboard) so the dialog appears centred on it.
     */
    public AddExpenseDialog(JFrame parent) {
        super(parent, "Add New Expense", true); // 'true' = modal (blocks the parent window)
        initComponents();
        pack();
        setLocationRelativeTo(parent); // centre on parent window
        setResizable(false);
    }

    /**
     * Builds all the UI components for the dialog.
     */
    private void initComponents() {
        setBackground(WHITE);

        // ---- Main panel with padding ----
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setBackground(WHITE);

        // ---- Header ----
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ---- Form ----
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ---- Buttons ----
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Creates the blue header strip with title and icon.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        header.setBackground(PRIMARY_BLUE);

        JLabel titleLabel = new JLabel("➕  Add New Expense");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(WHITE);

        header.add(titleLabel);
        return header;
    }

    /**
     * Creates the form with labelled input fields.
     */
    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(25, 30, 10, 30));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 8, 0);

        // ---- Amount Field ----
        addFormRow(form, gc, 0, "Amount (₹) *", amountField = createTextField("Enter amount (e.g., 150.00)"));

        // ---- Category Dropdown ----
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 1;
        JLabel catLabel = createLabel("Category *");
        form.add(catLabel, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        categoryCombo = new JComboBox<>(Expense.CATEGORIES);
        styleComboBox(categoryCombo);
        form.add(categoryCombo, gc);

        // ---- Description Field ----
        addFormRow(form, gc, 3, "Description *", descriptionField = createTextField("Enter description (e.g., Lunch at canteen)"));

        // ---- Date Field ----
        String today = LocalDate.now().format(Expense.DATE_FORMATTER);
        dateField = createTextField(today);
        dateField.setText(today); // pre-fill with today's date
        addFormRow(form, gc, 5, "Date * (yyyy-MM-dd)", dateField);

        // ---- Hint label ----
        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2;
        JLabel hintLabel = new JLabel("* All fields are required");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(TEXT_GRAY);
        form.add(hintLabel, gc);

        return form;
    }

    /**
     * Adds a label + field pair to the form grid.
     */
    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String labelText, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        form.add(createLabel(labelText), gc);

        gc.gridx = 0; gc.gridy = row + 1; gc.gridwidth = 2;
        form.add(field, gc);
    }

    /**
     * Creates a styled label.
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }

    /**
     * Creates a styled text field with placeholder hint.
     */
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField(25);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(340, 38));

        // Show placeholder text in grey when field is empty
        field.setForeground(TEXT_GRAY);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(PRIMARY_BLUE, 2, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_GRAY);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }
        });

        return field;
    }

    /**
     * Applies styling to the category combo box.
     */
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(WHITE);
        combo.setForeground(TEXT_DARK);
        combo.setPreferredSize(new Dimension(340, 38));
        combo.setBorder(new LineBorder(BORDER_COLOR, 1, true));
    }

    /**
     * Creates the Save/Cancel button panel at the bottom.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setForeground(TEXT_DARK);
        cancelButton.setBackground(WHITE);
        cancelButton.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose()); // close dialog, result stays null

        saveButton = new JButton("  Save Expense  ");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setForeground(WHITE);
        saveButton.setBackground(SUCCESS_GREEN);
        saveButton.setBorder(new CompoundBorder(
                new LineBorder(SUCCESS_GREEN.darker(), 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> handleSave()); // validate and save

        // Hover effects
        saveButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { saveButton.setBackground(SUCCESS_GREEN.darker()); }
            @Override public void mouseExited(MouseEvent e)  { saveButton.setBackground(SUCCESS_GREEN); }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { cancelButton.setBackground(LIGHT_BG); }
            @Override public void mouseExited(MouseEvent e)  { cancelButton.setBackground(WHITE); }
        });

        panel.add(cancelButton);
        panel.add(saveButton);
        return panel;
    }

    /**
     * Validates all inputs and creates the Expense if everything is valid.
     * Shows an error message for any invalid field.
     */
    private void handleSave() {
        // Get trimmed values (ignore placeholder text)
        String amountText = getFieldValue(amountField);
        String category   = (String) categoryCombo.getSelectedItem();
        String description = getFieldValue(descriptionField);
        String dateText   = getFieldValue(dateField);

        // ---- Validate: Amount ----
        if (amountText.isEmpty()) {
            showError("Please enter an amount.", amountField);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showError("Amount must be greater than zero.", amountField);
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount. Please enter a number (e.g., 150.00).", amountField);
            return;
        }

        // ---- Validate: Description ----
        if (description.isEmpty()) {
            showError("Please enter a description.", descriptionField);
            return;
        }
        if (description.length() > 100) {
            showError("Description is too long (max 100 characters).", descriptionField);
            return;
        }

        // ---- Validate: Date ----
        if (dateText.isEmpty()) {
            showError("Please enter a date.", dateField);
            return;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateText, Expense.DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Use yyyy-MM-dd (e.g., 2024-01-15).", dateField);
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            showError("Date cannot be in the future.", dateField);
            return;
        }

        // ---- All valid – create the Expense object ----
        result = new Expense(date, category, description, amount);
        dispose(); // close the dialog
    }

    /**
     * Returns the field's current text, unless it looks like a placeholder.
     */
    private String getFieldValue(JTextField field) {
        String text = field.getText().trim();
        // If text is grey (placeholder), treat as empty
        if (field.getForeground().equals(TEXT_GRAY)) {
            return "";
        }
        return text;
    }

    /**
     * Highlights a field with a red border and shows an error popup.
     */
    private void showError(String message, JComponent field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 2, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        field.requestFocus();
    }

    /**
     * Returns the new Expense after the dialog is closed.
     * Returns null if the user cancelled.
     */
    public Expense getResult() {
        return result;
    }
}
