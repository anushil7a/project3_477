package com.example.project3_aadhika8_sguragai;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity {

    private TextView textTotalAmount;
    private Button buttonSelectDate;
    private Button buttonViewSummary;
    private Button buttonAddExpense;
    private ListView listTransactions;

    private Calendar calendar;
    private AppDatabase db;
    private ExpenseDao expenseDao;

    private ArrayList<String> transactionStrings;
    private ArrayAdapter<String> adapter;
    private List<Expense> expenses;
    private Button buttonPrevDay;
    private Button buttonNextDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTotalAmount = findViewById(R.id.textTotalAmount);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonViewSummary = findViewById(R.id.buttonViewSummary);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        listTransactions = findViewById(R.id.listTransactions);
        buttonPrevDay = findViewById(R.id.buttonPrevDay);
        buttonNextDay = findViewById(R.id.buttonNextDay);

        listTransactions.setOnItemClickListener((parent, view, position, id) -> {
            if (expenses == null || position < 0 || position >= expenses.size()) {
                return;
            }
            Expense selected = expenses.get(position);
            showExpensePopup(selected);  
        });

        calendar = Calendar.getInstance();

        db = AppDatabase.getInstance(getApplicationContext());
        expenseDao = db.expenseDao();

        updateDateButtonText();
        loadDataForSelectedDate();

        buttonSelectDate.setOnClickListener(v -> showDatePicker());

        buttonViewSummary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
        buttonPrevDay.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateButtonText();
            loadDataForSelectedDate();
        });

        buttonNextDay.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateButtonText();
            loadDataForSelectedDate();
        });




        buttonAddExpense.setOnClickListener(v -> showExpensePopup(null));
    }

    private void showDatePicker() {
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateButtonText();
                    loadDataForSelectedDate();
                },
                y, m, d
        );
        dialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        buttonSelectDate.setText(sdf.format(calendar.getTime()));
    }

    private String getSelectedDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void loadDataForSelectedDate() {
        String day = getSelectedDateString();

        double total = expenseDao.getTotalForDay(day);
        textTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", total));

        expenses = expenseDao.getExpensesForDay(day);   

        transactionStrings = new ArrayList<>();

        for (Expense e : expenses) {
            String line = e.title + " - $" +
                    String.format(Locale.getDefault(), "%.2f", e.amount) +
                    " (" + e.category.name() + ")";
            transactionStrings.add(line);
        }

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                transactionStrings
        );
        listTransactions.setAdapter(adapter);
    }


    private void showExpensePopup(Expense expenseToEdit) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(expenseToEdit == null ? "Add Expense" : "Edit Expense");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_expense, null);
        builder.setView(view);

        EditText dialogTitle = view.findViewById(R.id.dialogTitle);
        EditText dialogAmount = view.findViewById(R.id.dialogAmount);
        EditText dialogNote = view.findViewById(R.id.dialogNote);
        Spinner dialogCategory = view.findViewById(R.id.dialogCategory);
        Button dialogDateButton = view.findViewById(R.id.dialogDateButton);

        ExpenseCategory[] cats = ExpenseCategory.values();
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.stream(cats).map(Enum::name).toArray(String[]::new)
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogCategory.setAdapter(catAdapter);

        Calendar tempCal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = getSelectedDateString(); 

        if (expenseToEdit != null) {
            try {
                tempCal.setTime(sdf.parse(expenseToEdit.date));
            } catch (Exception ignored) {}
        } else {
            try {
                tempCal.setTime(sdf.parse(currentDate));
            } catch (Exception ignored) {}
        }

        dialogDateButton.setText(sdf.format(tempCal.getTime()));

        dialogDateButton.setOnClickListener(v -> {
            int y = tempCal.get(Calendar.YEAR);
            int m = tempCal.get(Calendar.MONTH);
            int d = tempCal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(
                    MainActivity.this,
                    (picker, year, month, day) -> {
                        tempCal.set(year, month, day);
                        dialogDateButton.setText(sdf.format(tempCal.getTime()));
                    },
                    y, m, d
            );
            dp.show();
        });

        if (expenseToEdit != null) {
            dialogTitle.setText(expenseToEdit.title);
            dialogAmount.setText(String.valueOf(expenseToEdit.amount));
            dialogNote.setText(expenseToEdit.note);
            dialogCategory.setSelection(expenseToEdit.category.ordinal());
        }

        builder.setPositiveButton("Save", (dialog, which) -> {

            String title = dialogTitle.getText().toString().trim();
            String amtStr = dialogAmount.getText().toString().trim();
            String note = dialogNote.getText().toString().trim();
            String chosenDate = sdf.format(tempCal.getTime());
            ExpenseCategory cat = cats[dialogCategory.getSelectedItemPosition()];

            if (title.isEmpty() || amtStr.isEmpty()) {
                Toast.makeText(this, "Title and amount required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amtStr);

            if (expenseToEdit == null) {
                Expense e = new Expense();
                e.title = title;
                e.amount = amount;
                e.category = cat;
                e.note = note;
                e.date = chosenDate;
                expenseDao.insert(e);
                Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
            } else {
                expenseToEdit.title = title;
                expenseToEdit.amount = amount;
                expenseToEdit.category = cat;
                expenseToEdit.note = note;
                expenseToEdit.date = chosenDate;
                expenseDao.update(expenseToEdit);
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
            }

            loadDataForSelectedDate();
        });

        builder.setNegativeButton("Cancel", null);

        if (expenseToEdit != null) {
            builder.setNeutralButton("Delete", (d, w) -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete", (dialogConfirm, whichConfirm) -> {
                            expenseDao.delete(expenseToEdit);
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            loadDataForSelectedDate();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        }

        builder.create().show();
    }

}

