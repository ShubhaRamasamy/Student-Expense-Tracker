# Student Expense Tracker 💰

A clean, beginner-friendly **Java desktop application** that helps students track daily expenses, manage monthly budgets, and understand their spending patterns — all stored locally in CSV files.

---

## 📸 Screenshots

> _Run the application and take screenshots, then add them here._

| Dashboard | Add Expense |
|-----------|-------------|
| _(screenshot)_ | _(screenshot)_ |

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 📊 **Dashboard** | Overview of total expenses, budget, remaining balance, and transaction count |
| ➕ **Add Expense** | Form with category selection, description, date picker, and full validation |
| 📋 **Expense History** | Sortable and searchable table with all your expenses |
| 🔍 **Search & Filter** | Live search by description/category; filter by category |
| ↕️ **Sorting** | Sort by date (newest/oldest) or amount (highest/lowest) |
| ⚙️ **Budget Management** | Set a monthly budget; visual progress bar with color-coded warnings |
| ⚠️ **Budget Alerts** | Warning at 80% usage; error alert when budget is exceeded |
| 🗑️ **Delete Expense** | Select any row and delete it with a confirmation prompt |
| 💾 **Data Persistence** | Auto-saves to CSV files; data survives app restarts |

---

## 🛠️ Technology Stack

- **Language:** Java 17
- **GUI:** Java Swing (JFrame, JTable, JDialog, custom components)
- **Storage:** CSV files (plain text, human-readable)
- **IDE:** IntelliJ IDEA (Community or Ultimate)
- **Paradigm:** Object-Oriented Programming (OOP)

---

## 📁 Project Structure

```
StudentExpenseTracker/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── expensetracker/
│                   ├── Main.java               ← Entry point
│                   ├── Expense.java            ← Data model
│                   ├── ExpenseManager.java     ← Business logic
│                   ├── FileStorage.java        ← CSV read/write
│                   ├── Dashboard.java          ← Main window UI
│                   ├── AddExpenseDialog.java   ← Add expense form
│                   └── ExpenseTableModel.java  ← JTable model
├── data/                                       ← Auto-created at runtime
│   ├── expenses.csv                            ← Saved expenses
│   └── budget.txt                             ← Saved budget
├── README.md
└── .gitignore
```

---

## 🚀 How to Run in IntelliJ IDEA

### Prerequisites
- Java 17 (JDK 17) installed → [Download from Adoptium](https://adoptium.net/)
- IntelliJ IDEA installed → [Download](https://www.jetbrains.com/idea/download/)

### Steps

1. **Open IntelliJ IDEA** and click **"Open"** (or File → Open)
2. Navigate to the `StudentExpenseTracker` folder and click **OK**
3. IntelliJ will detect it as a Java project automatically
4. Set the **Project SDK** to Java 17:
   - Go to **File → Project Structure → Project**
   - Set **SDK** to Java 17
   - Set **Language Level** to 17
5. **Set the source root:**
   - Go to **File → Project Structure → Modules**
   - Right-click `src/main/java` → Mark as **Sources Root**
6. Find **`Main.java`** in the Project panel
7. Click the **▶ green play button** next to the `main` method
8. The application window will open! 🎉

---

## 💡 How to Use the App

1. **Set your budget** – Click "⚙ Set Budget" in the top navigation bar
2. **Add expenses** – Click "➕ Add Expense", fill in the form, and click Save
3. **View history** – All expenses appear in the Expense History table
4. **Search/Filter** – Type in the search box or select a category to filter
5. **Sort** – Use the Sort dropdown to order by date or amount
6. **Delete** – Click a row in the table and press "🗑 Delete Selected"
7. **Close and reopen** – Your data is automatically saved and will reload next time

---

## 📤 How to Upload to GitHub

### Step 1 – Create a GitHub repository
1. Go to [github.com](https://github.com) and sign in
2. Click the **"+"** icon → **"New repository"**
3. Name it `student-expense-tracker`
4. Make it **Public**, skip initialisation, click **Create repository**

### Step 2 – Initialize Git locally
Open a terminal (PowerShell or Command Prompt) in the `StudentExpenseTracker` folder:

```bash
git init
git add .
git commit -m "Initial commit: Student Expense Tracker"
```

### Step 3 – Push to GitHub
```bash
git remote add origin https://github.com/YOUR_USERNAME/student-expense-tracker.git
git branch -M main
git push -u origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username.

### Step 4 – Verify
Visit `https://github.com/YOUR_USERNAME/student-expense-tracker` — your code should appear! ✅

---

## 🧪 How to Test the Features

| Action | How to test |
|--------|-------------|
| Add expense | Click ➕, fill form, verify it appears in table and cards update |
| Validation | Try submitting empty form, future date, negative amount |
| Search | Type "lunch" in search box |
| Filter | Select "Food" from category dropdown |
| Sort | Switch between date and amount sort options |
| Delete | Click a row → Delete → Confirm → Verify it's gone |
| Budget warning | Set budget to ₹100, add ₹85 expense — see orange warning |
| Budget exceeded | Add ₹120 expense — see red alert |
| Data persistence | Add expense → close app → reopen → verify data is still there |

---

## 👤 Author

**Student Project** – Built independently to solve a real-life expense tracking problem.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
