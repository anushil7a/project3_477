package com.example.project3_aadhika8_sguragai;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private AppDatabase db;
    private ExpenseDao expenseDao;

    private Button buttonBack;
    private Button buttonFromDate;
    private Button buttonToDate;
    private Button buttonPresetWeek;
    private Button buttonPresetMonth;
    private Button buttonPresetYear;
    private Button buttonPresetAllTime;
    private Button buttonShowSummary;
    private Button buttonShowExpenses;
    private Button buttonExportCSV;
    private Button buttonViewCSV;
    private TextView textTotalRange;
    private TextView textRemainingBudget;
    private SharedPreferences prefs;
    private LinearLayout layoutSearch;
    private EditText editSearch;
    private Button buttonSearch;

    private ListView listSummary;
    private ListView listExpenses;

    private ArrayList<String> summaryLines;
    private ArrayList<String> expenseLines;
    private ArrayAdapter<String> summaryAdapter;
    private ArrayAdapter<String> expensesAdapter;

    private List<Expense> expensesInRange;

    private Calendar fromCal;
    private Calendar toCal;
    private Spinner spinnerSort;

    private SimpleDateFormat buttonFormat;
    private SimpleDateFormat queryFormat;

    private boolean isSummaryVisible = false;
    private boolean isExpensesVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        db = AppDatabase.getInstance(getApplicationContext());
        expenseDao = db.expenseDao();

        buttonBack = findViewById(R.id.buttonBack);
        buttonFromDate = findViewById(R.id.buttonFromDate);
        buttonToDate = findViewById(R.id.buttonToDate);
        buttonPresetWeek = findViewById(R.id.buttonPresetWeek);
        buttonPresetMonth = findViewById(R.id.buttonPresetMonth);
        buttonPresetYear = findViewById(R.id.buttonPresetYear);
        buttonPresetAllTime = findViewById(R.id.buttonPresetAllTime);
        buttonShowSummary = findViewById(R.id.buttonShowSummary);
        buttonShowExpenses = findViewById(R.id.buttonShowExpenses);
        buttonExportCSV = findViewById(R.id.buttonExportCSV);
        buttonViewCSV = findViewById(R.id.buttonViewCSV);
        textTotalRange = findViewById(R.id.textTotalRange);

        layoutSearch = findViewById(R.id.layoutSearch);
        editSearch = findViewById(R.id.editSearch);
        buttonSearch = findViewById(R.id.buttonSearch);

        listSummary = findViewById(R.id.listSummary);
        listExpenses = findViewById(R.id.listExpenses);

        textRemainingBudget = findViewById(R.id.textRemainingBudget);
        prefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);

        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpensesForCurrentRange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        listSummary.setOnItemClickListener((parent, view, position, id) -> {
            ExpenseCategory[] cats = ExpenseCategory.values();
            if (position < 0 || position >= cats.length) {
                return;
            }
            ExpenseCategory selectedCat = cats[position];

            String start = getFromDateString();
            String end = getToDateString();

            android.content.Intent intent =
                    new android.content.Intent(SummaryActivity.this, CategoryExpensesActivity.class);

            intent.putExtra(CategoryExpensesActivity.EXTRA_CATEGORY, selectedCat.name());
            intent.putExtra(CategoryExpensesActivity.EXTRA_START_DATE, start);
            intent.putExtra(CategoryExpensesActivity.EXTRA_END_DATE, end);

            startActivity(intent);
        });


        fromCal = Calendar.getInstance();
        toCal = Calendar.getInstance();

        buttonFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        queryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        setMonthPreset();


        buttonFromDate.setOnClickListener(v -> showDatePicker(true));
        buttonToDate.setOnClickListener(v -> showDatePicker(false));

        buttonPresetWeek.setOnClickListener(v -> {
            setWeekPreset();
        });

        buttonPresetMonth.setOnClickListener(v -> {
            setMonthPreset();
        });

        buttonPresetYear.setOnClickListener(v -> {
            setYearPreset();
        });

        buttonPresetAllTime.setOnClickListener(v -> {
            setAllTimePreset();
        });

        buttonShowSummary.setOnClickListener(v -> toggleSummary());

        buttonShowExpenses.setOnClickListener(v -> toggleExpenses());

        buttonSearch.setOnClickListener(v -> applySearchFilter());

        buttonExportCSV.setOnClickListener(v -> exportCSV());

        buttonBack.setOnClickListener(v -> finish());

        buttonViewCSV.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, CsvPreviewActivity.class);
            startActivity(intent);
        });

        listSummary.setVisibility(android.view.View.GONE);
        listExpenses.setVisibility(android.view.View.GONE);
        layoutSearch.setVisibility(android.view.View.GONE);
    }


    private void showDatePicker(boolean isFrom) {
        Calendar c = isFrom ? fromCal : toCal;

        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    c.set(year, month, dayOfMonth);
                    updateDateButtons();
                },
                y, m, d
        );
        dialog.show();
    }

    private void updateDateButtons() {
        buttonFromDate.setText("From: " + buttonFormat.format(fromCal.getTime()));
        buttonToDate.setText("To: " + buttonFormat.format(toCal.getTime()));
        updateTotalForRange();
        refreshVisibleSections();
    }

    private String getFromDateString() {
        return queryFormat.format(fromCal.getTime());
    }

    private String getToDateString() {
        return queryFormat.format(toCal.getTime());
    }

    private void setWeekPreset() {
        toCal = Calendar.getInstance();
        fromCal = Calendar.getInstance();
        fromCal.add(Calendar.DAY_OF_MONTH, -6);
        updateDateButtons();
    }

    private void setMonthPreset() {
        toCal = Calendar.getInstance();
        fromCal = Calendar.getInstance();
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        updateDateButtons();
    }

    private void setYearPreset() {
        toCal = Calendar.getInstance();
        fromCal = Calendar.getInstance();
        fromCal.set(Calendar.MONTH, Calendar.JANUARY);
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        updateDateButtons();
    }

    private void setAllTimePreset() {
        toCal = Calendar.getInstance();
        fromCal = Calendar.getInstance();
        fromCal.set(2000, Calendar.JANUARY, 1);
        updateDateButtons();
    }


    private void toggleSummary() {
        if (isSummaryVisible) {
            listSummary.setVisibility(android.view.View.GONE);
            isSummaryVisible = false;
        } else {
            loadSummaryForCurrentRange();
            listSummary.setVisibility(android.view.View.VISIBLE);
            isSummaryVisible = true;
        }
    }

    private void toggleExpenses() {
        if (isExpensesVisible) {
            listExpenses.setVisibility(android.view.View.GONE);
            layoutSearch.setVisibility(android.view.View.GONE);
            isExpensesVisible = false;
        } else {
            loadExpensesForCurrentRange();
            listExpenses.setVisibility(android.view.View.VISIBLE);
            layoutSearch.setVisibility(android.view.View.VISIBLE);
            isExpensesVisible = true;
        }
    }
    private void updateTotalForRange() {
        String start = getFromDateString();
        String end = getToDateString();

        if (start.compareTo(end) > 0) {
            textTotalRange.setText("Total spent for the selected dates: $0.00");
            return;
        }

        double total = expenseDao.getTotalForRange(start, end);
        String text = "Total spent for the selected dates: $" +
                String.format(Locale.getDefault(), "%.2f", total);
        textTotalRange.setText(text);
    }



    private void loadSummaryForCurrentRange() {
        String start = getFromDateString();
        String end = getToDateString();

        if (start.compareTo(end) > 0) {
            Toast.makeText(this, "From date must be before To date", Toast.LENGTH_SHORT).show();
            return;
        }

        summaryLines = new ArrayList<>();

        for (ExpenseCategory cat : ExpenseCategory.values()) {
            double total = expenseDao.getTotalByCategory(cat, start, end);
            summaryLines.add(cat.name() + ": $" +
                    String.format(Locale.getDefault(), "%.2f", total));
        }

        summaryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                summaryLines
        );

        listSummary.setAdapter(summaryAdapter);
    }


    private void loadExpensesForCurrentRange() {
        String start = getFromDateString();
        String end = getToDateString();

        if (start.compareTo(end) > 0) {
            Toast.makeText(this, "From date must be before To date", Toast.LENGTH_SHORT).show();
            return;
        }

        expensesInRange = expenseDao.getExpensesInRange(start, end);

        expenseLines = new ArrayList<>();
        for (Expense e : expensesInRange) {
            String line = e.date + " - " + e.title + " - $" +
                    String.format(Locale.getDefault(), "%.2f", e.amount) +
                    " (" + (e.category == null ? "" : e.category.name()) + ")";
            expenseLines.add(line);
        }

        expensesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                expenseLines
        );

        listExpenses.setAdapter(expensesAdapter);
    }

    private void applySearchFilter() {
        if (expensesInRange == null) {
            return;
        }

        String query = editSearch.getText().toString().trim().toLowerCase(Locale.getDefault());

        ArrayList<String> filtered = new ArrayList<>();

        for (Expense e : expensesInRange) {
            String title = e.title == null ? "" : e.title;
            String catName = e.category == null ? "" : e.category.name();
            String combined = title.toLowerCase(Locale.getDefault()) + " " +
                    catName.toLowerCase(Locale.getDefault());

            if (query.isEmpty() || combined.contains(query)) {
                String line = e.date + " - " + title + " - $" +
                        String.format(Locale.getDefault(), "%.2f", e.amount) +
                        " (" + catName + ")";
                filtered.add(line);
            }
        }

        expensesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                filtered
        );
        listExpenses.setAdapter(expensesAdapter);
    }


    private void exportCSV() {
        try {
            List<Expense> allExpenses = expenseDao.getAllExpenses();

            if (allExpenses == null || allExpenses.isEmpty()) {
                Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show();
                return;
            }

            File csv = new File(getExternalFilesDir(null), "expenses_full.csv");
            FileWriter writer = new FileWriter(csv);

            writer.write("Title,Amount,Category,Date,Note\n");

            for (Expense e : allExpenses) {
                String title = e.title == null ? "" : e.title;
                String amount = String.format(Locale.getDefault(), "%.2f", e.amount);
                String category = e.category == null ? "" : e.category.name();
                String date = e.date == null ? "" : e.date;
                String note = e.note == null ? "" : e.note;

                title = title.replace("\"", "\"\"");
                category = category.replace("\"", "\"\"");
                date = date.replace("\"", "\"\"");
                note = note.replace("\"", "\"\"");

                writer.write("\"" + title + "\"," +
                        "\"" + amount + "\"," +
                        "\"" + category + "\"," +
                        "\"" + date + "\"," +
                        "\"" + note + "\"\n");
            }

            writer.close();

            Toast.makeText(this,
                    "Exported to " + csv.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }
    private void refreshVisibleSections() {
        if (isSummaryVisible) {
            loadSummaryForCurrentRange();
        }
        if (isExpensesVisible) {
            loadExpensesForCurrentRange();
        }
    }
    private void updateBudgetWarning() {
        float budget = prefs.getFloat("monthly_budget", 0);
        Calendar now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, 1); // first day of current month
        String monthStart = queryFormat.format(now.getTime());
        now.set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH)); // last day
        String monthEnd = queryFormat.format(now.getTime());

        double totalThisMonth = expenseDao.getTotalForRange(monthStart, monthEnd);
        double remaining = budget - totalThisMonth;

        textRemainingBudget.setText("Remaining Budget: $" + String.format(Locale.getDefault(), "%.2f", remaining));

        if(totalThisMonth >= 0.9 * budget) {
            textRemainingBudget.setTextColor(Color.RED);
            Toast.makeText(this, "Warning: Approaching monthly budget!", Toast.LENGTH_SHORT).show();
        } else {
            textRemainingBudget.setTextColor(Color.BLACK);
        }
    }

}
