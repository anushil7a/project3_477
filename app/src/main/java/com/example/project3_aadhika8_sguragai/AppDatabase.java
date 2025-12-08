package com.example.project3_aadhika8_sguragai;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;


@Database(
        entities = {Expense.class, Budget.class},
        version = 2,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExpenseDao expenseDao();

    public abstract BudgetDao budgetDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "budgetbuddy.db"
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .addCallback(seedData)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback seedData = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // November sample data
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

            // December sample data with mix of spend / no-spend days
            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 35.00, 'GROCERIES', 'Groceries Run', '2025-12-01', 'Produce and snacks');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 18.75, 'FOOD', 'Dinner Out', '2025-12-02', 'Italian restaurant');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 60.00, 'TRANSPORT', 'Uber Rides', '2025-12-03', 'Commute to office');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 120.00, 'BILLS', 'Internet + Phone', '2025-12-04', 'Monthly services');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 9.99, 'ENTERTAINMENT', 'Spotify Subscription', '2025-12-05', 'Premium plan');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 220.00, 'SHOPPING', 'Winter Clothes', '2025-12-06', 'Jacket and boots');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 55.25, 'GROCERIES', 'Warehouse Club', '2025-12-08', 'Bulk items');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 14.50, 'FOOD', 'Lunch with Friends', '2025-12-09', 'Sandwich shop');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 300.00, 'BILLS', 'Rent Partial', '2025-12-10', 'First half of rent');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 47.80, 'TRANSPORT', 'Gas Refill', '2025-12-11', 'Full tank');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 75.00, 'ENTERTAINMENT', 'Concert Tickets', '2025-12-13', 'Live show');");

            db.execSQL("INSERT INTO expenses (id, amount, category, title, date, note) VALUES " +
                    "(NULL, 19.99, 'OTHER', 'Gift Wrap and Cards', '2025-12-14', 'Holiday supplies');");

            db.execSQL("INSERT INTO budget (id, amount) VALUES (1, 1500.0);");
        }
    };
}
