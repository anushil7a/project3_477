package com.example.project3_aadhika8_sguragai;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(
        entities = {Expense.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExpenseDao expenseDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "budgetbuddy.db"
                    )
                    .allowMainThreadQueries()
                    .addCallback(seedData)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback seedData = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 12.50, 'FOOD', 'Chipotle Lunch', '2025-11-30', 'Bowl with chicken');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 4.25, 'FOOD', 'Starbucks Latte', '2025-11-30', 'Caramel latte');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 42.00, 'TRANSPORT', 'Gas Refill', '2025-11-29', 'Filled half tank');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 15.99, 'ENTERTAINMENT', 'Netflix Subscription', '2025-11-28', 'Monthly plan');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 68.40, 'GROCERIES', 'Weekly Groceries', '2025-11-28', 'Vegetables and snacks');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 90.00, 'BILLS', 'Electric Bill', '2025-11-27', 'November electric bill');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 25.00, 'OTHER', 'Gift for Friend', '2025-11-27', 'Birthday present');");
        }
    };
}
