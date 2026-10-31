package com.expensetracker;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dashboard – Main Application Window
 *
 * This is the heart of the UI. It contains:
 *  - A top navigation bar
 *  - Summary cards (total expenses, budget, remaining, transactions)
 *  - Category-wise breakdown
 *  - Full expense history table with search, filter, and sort
 *  - Budget management panel
 *  - Add / Delete expense controls
 */
public class Dashboard extends JFrame {

    // ===================== COLOR PALETTE =====================
    private static final Color PRIMARY_BLUE    = new Color(52, 152, 219);
    private static final Color DARK_BLUE       = new Color(41, 128, 185);
    private static final Color LIGHT_BG        = new Color(245, 247, 250);
    private static final Color WHITE           = Color.WHITE;
    private static final Color TEXT_DARK       = new Color(44, 62, 80);
    private static final Color TEXT_GRAY       = new Color(127, 140, 141);
    private static final Color SUCCESS_GREEN   = new Color(39, 174, 96);
    private static final Color WARNING_ORANGE  = new Color(230, 126, 34);
    private static final Color DANGER_RED      = new Color(231, 76, 60);
    private static final Color BORDER_COLOR    = new Color(220, 230, 240);
    private static final Color CARD_SHADOW     = new Color(200, 215, 230);

    // Category accent colours (one per category)
    private static final Color[] CATEGORY_COLORS = {
        new Color(231, 76, 60),   // Food – red
        new Color(52, 152, 219),  // Transport – blue
        new Color(155, 89, 182),  // Education – purple
        new Color(230, 126, 34),  // Shopping – orange
        new Color(26, 188, 156),  // Entertainment – teal
        new Color(127, 140, 141)  // Other – grey
    };

    // ===================== DATA & LOGIC =====================
    private ExpenseManager manager;
    private ExpenseTableModel tableModel;

    // ===================== SUMMARY CARD LABELS =====================
    private JLabel totalExpensesValue;
    private JLabel budgetValue;
    private JLabel remainingValue;
    private JLabel transactionCountValue;

    // ===================== TABLE & CONTROLS =====================
    private JTable expenseTable;
    private JTextField searchField;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> sortCombo;
    private JProgressBar budgetProgressBar;
    private JLabel budgetStatusLabel;

    // ===================== CATEGORY BREAKDOWN LABELS =====================
    private JLabel[] categoryAmountLabels;

    /**
     * Constructor – builds the full UI and loads saved data.
     */
    public Dashboard(ExpenseManager manager) {
        this.manager = manager;
        this.tableModel = new ExpenseTableModel();

        setTitle("Student Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setPreferredSize(new Dimension(1200, 750));

        // Save data when the window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAllData();
            }
        });

        initUI();
        refreshDashboard(); // populate with saved data

        pack();
        setLocationRelativeTo(null); // centre on screen
    }

    /**
     * Builds the entire window layout.
     */
    private void initUI() {
        getContentPane().setBackground(LIGHT_BG);
        setLayout(new BorderLayout(0, 0));

        // Top navigation bar
        add(createNavBar(), BorderLayout.NORTH);

        // Main scrollable content
        JScrollPane scrollPane = new JScrollPane(createMainContent());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ===================== NAV BAR =====================

    /**
     * Creates the top navigation bar with logo and action buttons.
     */
    private JPanel createNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(PRIMARY_BLUE);
        nav.setBorder(new EmptyBorder(0, 0, 2, 0));
        nav.setPreferredSize(new Dimension(0, 60));

        // Left side – App title
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        leftSide.setOpaque(false);
        JLabel titleLabel = new JLabel("💰  Student Expense Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(WHITE);
        leftSide.add(titleLabel);

        // Right side – Month label and buttons
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        rightSide.setOpaque(false);

        String monthYear = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + LocalDate.now().getYear();
        JLabel monthLabel = new JLabel("📅 " + monthYear);
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        monthLabel.setForeground(new Color(255, 255, 255, 200));

        JButton addButton   = createNavButton("➕ Add Expense", SUCCESS_GREEN);
        JButton budgetBtn   = createNavButton("⚙ Set Budget", WARNING_ORANGE);

        addButton.addActionListener(e -> openAddExpenseDialog());
        budgetBtn.addActionListener(e -> openSetBudgetDialog());

        rightSide.add(monthLabel);
        rightSide.add(addButton);
        rightSide.add(budgetBtn);

        nav.add(leftSide, BorderLayout.WEST);
        nav.add(rightSide, BorderLayout.EAST);
        return nav;
    }

    /**
     * Creates a styled navigation button.
     */
    private JButton createNavButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(WHITE);
        btn.setBackground(bgColor);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bgColor); }
        });
        return btn;
    }

    // ===================== MAIN CONTENT =====================

    /**
     * Builds the scrollable main content area.
     */
    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(LIGHT_BG);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Row 1: Summary Cards
        content.add(createSummaryCardsRow());
        content.add(Box.createVerticalStrut(20));

        // Row 2: Budget Progress + Category Breakdown (side by side)
        content.add(createMiddleRow());
        content.add(Box.createVerticalStrut(20));

        // Row 3: Expense History Table
        content.add(createExpenseHistoryPanel());

        return content;
    }

    // ===================== SUMMARY CARDS =====================

    /**
     * Creates the horizontal row of 4 summary cards.
     */
    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        row.add(createSummaryCard("Total Expenses", "₹0.00",    "📊", DANGER_RED));
        row.add(createSummaryCard("Monthly Budget", "₹0.00",    "🎯", PRIMARY_BLUE));
        row.add(createSummaryCard("Remaining",      "₹0.00",    "💵", SUCCESS_GREEN));
        row.add(createSummaryCard("Transactions",   "0",         "🧾", WARNING_ORANGE));

        // Remember the value labels so we can update them later
        // We extract them by component order (card → content panel → value label)
        totalExpensesValue    = extractValueLabel(row, 0);
        budgetValue           = extractValueLabel(row, 1);
        remainingValue        = extractValueLabel(row, 2);
        transactionCountValue = extractValueLabel(row, 3);

        return row;
    }

    /**
     * Creates a single summary card panel.
     */
    private JPanel createSummaryCard(String title, String value, String icon, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Top accent bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(0, 4));
        card.add(accentBar, BorderLayout.NORTH);

        // Centre: icon + value + title
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 4, 12);
        centre.add(iconLabel, gc);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1.0;
        gc.insets = new Insets(0, 0, 0, 0);
        centre.add(textPanel, gc);

        card.add(centre, BorderLayout.CENTER);
        return card;
    }

    /**
     * Helper to extract the JLabel that shows the value from a card.
     */
    private JLabel extractValueLabel(JPanel row, int cardIndex) {
        JPanel card   = (JPanel) row.getComponent(cardIndex);
        JPanel centre = (JPanel) card.getComponent(1);
        JPanel text   = (JPanel) ((JPanel) centre.getComponent(1));
        return (JLabel) text.getComponent(1);
    }

    // ===================== MIDDLE ROW =====================

    /**
     * Creates the row with Budget Progress on the left and Category Breakdown on the right.
     */
    private JPanel createMiddleRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        row.add(createBudgetProgressPanel());
        row.add(createCategoryBreakdownPanel());
        return row;
    }

    /**
     * Creates the budget progress card.
     */
    private JPanel createBudgetProgressPanel() {
        JPanel card = createCard("Budget Status");

        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setValue(0);
        budgetProgressBar.setStringPainted(false);
        budgetProgressBar.setBackground(new Color(240, 243, 246));
        budgetProgressBar.setForeground(SUCCESS_GREEN);
        budgetProgressBar.setBorder(null);
        budgetProgressBar.setPreferredSize(new Dimension(0, 20));
        // Make the progress bar rounded
        budgetProgressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() { return TEXT_DARK; }
            @Override
            protected Color getSelectionForeground() { return WHITE; }
        });

        budgetStatusLabel = new JLabel("Budget usage: 0%");
        budgetStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        budgetStatusLabel.setForeground(TEXT_GRAY);

        JLabel spentLabel  = new JLabel("Spent this month");
        spentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spentLabel.setForeground(TEXT_GRAY);

        card.add(Box.createVerticalStrut(8));
        card.add(spentLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(budgetProgressBar);
        card.add(Box.createVerticalStrut(8));
        card.add(budgetStatusLabel);

        return card;
    }

    /**
     * Creates the category breakdown card (shows spending per category).
     */
    private JPanel createCategoryBreakdownPanel() {
        JPanel card = createCard("Category Breakdown (This Month)");
        categoryAmountLabels = new JLabel[Expense.CATEGORIES.length];

        for (int i = 0; i < Expense.CATEGORIES.length; i++) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            // Coloured dot + category name
            JLabel dotLabel = new JLabel("\u25CF  " + Expense.CATEGORIES[i]);
            dotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dotLabel.setForeground(CATEGORY_COLORS[i]);

            // Amount for that category
            categoryAmountLabels[i] = new JLabel("₹0.00");
            categoryAmountLabels[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
            categoryAmountLabels[i].setForeground(TEXT_DARK);

            row.add(dotLabel, BorderLayout.WEST);
            row.add(categoryAmountLabels[i], BorderLayout.EAST);
            card.add(row);
        }

        return card;
    }

    /**
     * Creates a generic white card panel with a title.
     */
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER_COLOR);
        card.add(Box.createVerticalStrut(10));
        card.add(sep);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    // ===================== EXPENSE HISTORY PANEL =====================

    /**
     * Creates the full expense history section with search, filter, sort, table, and delete button.
     */
    private JPanel createExpenseHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header with title
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("📋  Expense History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Controls bar (search + filter + sort + delete button)
        panel.add(createControlsBar(), BorderLayout.CENTER);

        // Table wrapped in a card
        panel.add(createTablePanel(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the search, filter, and sort controls bar.
     */
    private JPanel createControlsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Search field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setPreferredSize(new Dimension(220, 35));

        // Placeholder behaviour for search field
        final String SEARCH_PLACEHOLDER = "Search expenses...";
        searchField.setForeground(TEXT_GRAY);
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText(""); searchField.setForeground(TEXT_DARK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(SEARCH_PLACEHOLDER); searchField.setForeground(TEXT_GRAY);
                }
            }
        });
        // Live search as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Category filter
        String[] filterOptions = new String[Expense.CATEGORIES.length + 1];
        filterOptions[0] = "All Categories";
        System.arraycopy(Expense.CATEGORIES, 0, filterOptions, 1, Expense.CATEGORIES.length);
        categoryFilterCombo = new JComboBox<>(filterOptions);
        categoryFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilterCombo.setPreferredSize(new Dimension(160, 35));
        categoryFilterCombo.addActionListener(e -> applyFilters());

        // Sort dropdown
        sortCombo = new JComboBox<>(new String[]{
            "Date ↓ (Newest)", "Date ↑ (Oldest)", "Amount ↓ (Highest)", "Amount ↑ (Lowest)"
        });
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sortCombo.setPreferredSize(new Dimension(185, 35));
        sortCombo.addActionListener(e -> applyFilters());

        // Delete button
        JButton deleteButton = new JButton("🗑  Delete Selected");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setForeground(WHITE);
        deleteButton.setBackground(DANGER_RED);
        deleteButton.setBorder(new CompoundBorder(
                new LineBorder(DANGER_RED.darker(), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { deleteButton.setBackground(DANGER_RED.darker()); }
            @Override public void mouseExited(MouseEvent e)  { deleteButton.setBackground(DANGER_RED); }
        });

        bar.add(new JLabel("Filter: "));
        bar.add(searchField);
        bar.add(categoryFilterCombo);
        bar.add(new JLabel("  Sort: "));
        bar.add(sortCombo);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(deleteButton);

        return bar;
    }

    /**
     * Creates the JTable wrapped in a white card with custom styling.
     */
    private JPanel createTablePanel() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(WHITE);
        tableCard.setBorder(new LineBorder(BORDER_COLOR, 1, true));

        // Build the table
        expenseTable = new JTable(tableModel);
        expenseTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        expenseTable.setRowHeight(40);
        expenseTable.setGridColor(BORDER_COLOR);
        expenseTable.setShowVerticalLines(false);
        expenseTable.setSelectionBackground(new Color(235, 245, 255));
        expenseTable.setSelectionForeground(TEXT_DARK);
        expenseTable.setFocusable(false);
        expenseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        expenseTable.getTableHeader().setBackground(new Color(248, 250, 252));
        expenseTable.getTableHeader().setForeground(TEXT_DARK);
        expenseTable.getTableHeader().setBorder(new MatteBorder(0, 0, 2, 0, BORDER_COLOR));
        expenseTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        expenseTable.getColumnModel().getColumn(0).setPreferredWidth(40);   // #
        expenseTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Date
        expenseTable.getColumnModel().getColumn(2).setPreferredWidth(130);  // Category
        expenseTable.getColumnModel().getColumn(3).setPreferredWidth(350);  // Description
        expenseTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // Amount

        // Right-align the amount column
        DefaultTableCellRenderer rightAlignRenderer = new DefaultTableCellRenderer();
        rightAlignRenderer.setHorizontalAlignment(JLabel.RIGHT);
        expenseTable.getColumnModel().getColumn(4).setCellRenderer(rightAlignRenderer);

        // Alternate row colours
        expenseTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        JScrollPane tableScroll = new JScrollPane(expenseTable);
        tableScroll.setPreferredSize(new Dimension(0, 320));
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(WHITE);

        // Empty-state label (shown when there are no expenses)
        JPanel emptyPanel = new JPanel(new FlowLayout());
        emptyPanel.setBackground(WHITE);
        JLabel emptyLabel = new JLabel("No expenses yet. Click '➕ Add Expense' to get started!");
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        emptyLabel.setForeground(TEXT_GRAY);
        emptyPanel.add(emptyLabel);

        tableCard.add(tableScroll, BorderLayout.CENTER);
        return tableCard;
    }

    // ===================== DATA REFRESH =====================

    /**
     * Refreshes all UI components with the latest data from the manager.
     * Call this after any add/delete/budget change.
     */
    public void refreshDashboard() {
        double totalExpenses = manager.getTotalMonthlyExpenses();
        double budget        = manager.getMonthlyBudget();
        double remaining     = manager.getRemainingBudget();
        int    txCount       = manager.getMonthlyTransactionCount();

        // Update summary cards
        totalExpensesValue.setText(String.format("₹%.2f", totalExpenses));
        budgetValue.setText(String.format("₹%.2f", budget));
        transactionCountValue.setText(String.valueOf(txCount));

        // Remaining: green if positive, red if overdrawn
        remainingValue.setText(String.format("₹%.2f", Math.abs(remaining)));
        if (remaining < 0) {
            remainingValue.setForeground(DANGER_RED);
            remainingValue.setText("-₹" + String.format("%.2f", Math.abs(remaining)));
        } else {
            remainingValue.setForeground(SUCCESS_GREEN);
        }

        // Update budget progress bar
        int percentage = (int) Math.min(manager.getSpendingPercentage(), 100);
        budgetProgressBar.setValue(percentage);

        if (manager.isBudgetExceeded()) {
            budgetProgressBar.setForeground(DANGER_RED);
            budgetStatusLabel.setText(String.format("⚠ Budget exceeded! (%.0f%% spent)", manager.getSpendingPercentage()));
            budgetStatusLabel.setForeground(DANGER_RED);
        } else if (manager.isNearBudgetLimit()) {
            budgetProgressBar.setForeground(WARNING_ORANGE);
            budgetStatusLabel.setText(String.format("⚠ Nearing limit: %.0f%% spent", manager.getSpendingPercentage()));
            budgetStatusLabel.setForeground(WARNING_ORANGE);
        } else {
            budgetProgressBar.setForeground(SUCCESS_GREEN);
            budgetStatusLabel.setText(String.format("Budget usage: %.0f%%", manager.getSpendingPercentage()));
            budgetStatusLabel.setForeground(TEXT_GRAY);
        }

        // Update category breakdown
        Map<String, Double> summary = manager.getCategoryWiseSummary();
        for (int i = 0; i < Expense.CATEGORIES.length; i++) {
            double amt = summary.getOrDefault(Expense.CATEGORIES[i], 0.0);
            categoryAmountLabels[i].setText(String.format("₹%.2f", amt));
            categoryAmountLabels[i].setForeground(amt > 0 ? CATEGORY_COLORS[i] : TEXT_GRAY);
        }

        // Update the table
        applyFilters();

        // Trigger budget warnings as pop-ups if thresholds are crossed
        checkBudgetAlerts();
    }

    /**
     * Applies current search/filter/sort settings and updates the table.
     */
    private void applyFilters() {
        // Get search keyword (ignore placeholder text)
        String keyword = "";
        if (!searchField.getForeground().equals(TEXT_GRAY)) {
            keyword = searchField.getText().trim();
        }

        // Get category filter
        String selectedCategory = (String) categoryFilterCombo.getSelectedItem();
        if ("All Categories".equals(selectedCategory)) {
            selectedCategory = "All";
        }

        // Apply search first, then category filter
        List<Expense> filtered = manager.searchExpenses(keyword);
        if (!"All".equals(selectedCategory)) {
            final String cat = selectedCategory;
            filtered.removeIf(e -> !e.getCategory().equals(cat));
        }

        // Apply sort
        int sortIndex = sortCombo.getSelectedIndex();
        filtered = switch (sortIndex) {
            case 0 -> manager.sortByDateDescending(filtered);
            case 1 -> manager.sortByDateAscending(filtered);
            case 2 -> manager.sortByAmountDescending(filtered);
            case 3 -> manager.sortByAmountAscending(filtered);
            default -> filtered;
        };

        tableModel.setExpenses(filtered);
    }

    // ===================== DIALOGS & ACTIONS =====================

    /**
     * Opens the Add Expense dialog and handles the result.
     */
    private void openAddExpenseDialog() {
        AddExpenseDialog dialog = new AddExpenseDialog(this);
        dialog.setVisible(true);

        Expense newExpense = dialog.getResult();
        if (newExpense != null) {
            manager.addExpense(newExpense);
            saveAllData();
            refreshDashboard();

            JOptionPane.showMessageDialog(this,
                    "Expense added successfully! ✅",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Opens the Set Budget dialog.
     */
    private void openSetBudgetDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Enter your monthly budget (₹):",
                String.format("%.2f", manager.getMonthlyBudget()));

        if (input != null && !input.trim().isEmpty()) {
            try {
                double newBudget = Double.parseDouble(input.trim());
                if (newBudget <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Budget must be a positive number.", "Invalid Budget", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                manager.setMonthlyBudget(newBudget);
                saveAllData();
                refreshDashboard();
                JOptionPane.showMessageDialog(this,
                        String.format("Budget set to ₹%.2f ✅", newBudget),
                        "Budget Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid number. Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes the expense selected in the table, after confirmation.
     */
    private void deleteSelectedExpense() {
        int viewRow = expenseTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an expense row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find the actual Expense object in the table model
        Expense selected = tableModel.getExpenseAt(viewRow);
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this expense?\n\n" + selected.toString(),
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Find and remove from the manager's master list
            List<Expense> all = manager.getAllExpenses();
            for (int i = 0; i < all.size(); i++) {
                Expense e = all.get(i);
                if (e.getDate().equals(selected.getDate())
                        && e.getCategory().equals(selected.getCategory())
                        && e.getDescription().equals(selected.getDescription())
                        && Math.abs(e.getAmount() - selected.getAmount()) < 0.001) {
                    manager.deleteExpense(i);
                    break;
                }
            }
            saveAllData();
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Expense deleted. ✅", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ===================== BUDGET ALERTS =====================

    // Flag to avoid showing the same alert repeatedly in the same session
    private boolean warningShown = false;
    private boolean exceededShown = false;

    /**
     * Shows pop-up alerts when the user crosses budget thresholds.
     */
    private void checkBudgetAlerts() {
        if (manager.isBudgetExceeded() && !exceededShown) {
            exceededShown = true;
            warningShown  = true;
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "🚨 You have exceeded your monthly budget!\n"
                    + String.format("Spent: ₹%.2f | Budget: ₹%.2f",
                            manager.getTotalMonthlyExpenses(), manager.getMonthlyBudget()),
                    "Budget Exceeded!", JOptionPane.ERROR_MESSAGE)
            );
        } else if (manager.isNearBudgetLimit() && !warningShown && !manager.isBudgetExceeded()) {
            warningShown = true;
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "⚠ You have used 80% of your monthly budget!\n"
                    + String.format("Spent: ₹%.2f | Budget: ₹%.2f",
                            manager.getTotalMonthlyExpenses(), manager.getMonthlyBudget()),
                    "Budget Warning", JOptionPane.WARNING_MESSAGE)
            );
        }
    }

    // ===================== DATA SAVE =====================

    /**
     * Saves all expenses and the budget to disk.
     */
    private void saveAllData() {
        FileStorage.saveExpenses(manager.getAllExpenses());
        FileStorage.saveBudget(manager.getMonthlyBudget());
    }

    // ===================== INNER CLASS: ALTERNATING ROW RENDERER =====================

    /**
     * Custom renderer that gives alternating grey/white row backgrounds.
     */
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private static final Color ROW_ALT = new Color(250, 252, 255);

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                c.setForeground(TEXT_DARK);
            }
            // Add left padding
            if (c instanceof JLabel label) {
                label.setBorder(new EmptyBorder(0, 8, 0, 8));
            }
            return c;
        }
    }
}
