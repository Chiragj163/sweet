package com.example.sweet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.example.sweet.Sale;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SweetShopDB";
    private static final int DB_VERSION = 3;

    public static final String TABLE_ITEMS = "items";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "price REAL,"
                + "unit TEXT)";

        db.execSQL(CREATE_ITEMS_TABLE);

        // ✅ CREATE ONLY ONCE (with items column)
        String CREATE_SALES_TABLE = "CREATE TABLE sales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "total REAL," +
                "items TEXT)";

        db.execSQL(CREATE_SALES_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS sales"); // 🔥 ADD THIS
        onCreate(db);
    }
    public void insertItem(String name, double price, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "INSERT INTO " + TABLE_ITEMS +
                "(name, price, unit) VALUES('" + name + "'," + price + ",'" + unit + "')";

        db.execSQL(query);
    }
    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Item(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3)
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
    public void updateItem(int id, String name, double price, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE " + TABLE_ITEMS +
                " SET name='" + name + "', price=" + price + ", unit='" + unit + "'" +
                " WHERE id=" + id;

        db.execSQL(query);
    }

    public double getTodaySales() {
        SQLiteDatabase db = this.getReadableDatabase();

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT SUM(total) FROM sales WHERE date='" + today + "'",
                null
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            if (!cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
        }

        cursor.close();
        return total;
    }
    public List<Float> getLast7DaysSales() {
        List<Float> sales = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        for (int i = 6; i >= 0; i--) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(
                    new Date(System.currentTimeMillis() - (long)i * 24 * 60 * 60 * 1000)
            );

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(total) FROM sales WHERE date='" + date + "'",
                    null
            );

            float value = 0;

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                value = cursor.getFloat(0);
            }

            sales.add(value);
            cursor.close();
        }

        return sales;
    }
    public List<Float> getMonthlySales() {
        List<Float> sales = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        for (int i = 1; i <= 30; i++) {
            String day = (i < 10 ? "0" + i : "" + i);
            String month = new SimpleDateFormat("yyyy-MM").format(new Date());
            String date = month + "-" + day;

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(total) FROM sales WHERE date='" + date + "'",
                    null
            );

            float value = 0;
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                value = cursor.getFloat(0);
            }

            sales.add(value);
            cursor.close();
        }

        return sales;
    }
    public int getTotalBills() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sales", null);

        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }

        return 0;
    }
    public double getAverageBill() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT AVG(total) FROM sales", null);

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            return cursor.getDouble(0);
        }

        return 0;
    }
    public void saveSale(double total, String itemsSummary) {
        SQLiteDatabase db = this.getWritableDatabase();

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        String query = "INSERT INTO sales (date, total, items) VALUES('"
                + date + "', " + total + ", '" + itemsSummary + "')";

        db.execSQL(query);
    }
    public String getTopItem() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT items FROM sales ORDER BY total DESC LIMIT 1",
                null
        );

        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }

        return "N/A";
    }
    public List<Sale> getAllSales() {
        List<Sale> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM sales", null);

        while (cursor.moveToNext()) {

            Sale s = new Sale();

            s.setDate(cursor.getString(1));
            s.setAmount(cursor.getDouble(2));
            String summary = cursor.getString(3);
            s.setSummary(summary);

            // 🔥 Parse items
            String[] items = summary.split(",");

            for (String item : items) {
                try {
                    String[] parts = item.trim().split("\\s+");

                    String name = parts[0];
                    double qty = Double.parseDouble(parts[1]);
                    double total = Double.parseDouble(parts[2]);
                    double price = total / qty;

                    s.items.add(new SaleItemRow(s.getDate(), name, qty, price, total));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            list.add(s);
        }

        cursor.close();
        return list;
    }

    public List<String> getSalesByItem() {
        List<String> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT items FROM sales", null);

        HashMap<String, Integer> qtyMap = new HashMap<>();
        HashMap<String, Double> amountMap = new HashMap<>();

        while (cursor.moveToNext()) {
            String summary = cursor.getString(0);

            String[] items = summary.split(",");

            for (String line : items) {
                try {
                    line = line.trim();

                    String[] parts = line.split("\\s+");

                    String name = parts[0];
                    int qty = Integer.parseInt(parts[1]);
                    double amt = Double.parseDouble(parts[2]);

                    qtyMap.put(name, qtyMap.getOrDefault(name, 0) + qty);
                    amountMap.put(name, amountMap.getOrDefault(name, 0.0) + amt);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        for (String key : qtyMap.keySet()) {
            result.add(
                    key +
                            "\nQty Sold: " + qtyMap.get(key) +
                            "\nRevenue: ₹" + amountMap.get(key)
            );
        }

        return result;
    }
    public List<SaleItemRow> getDetailedSales() {
        List<SaleItemRow> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT date, items FROM sales", null);

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            String summary = cursor.getString(1);

            String[] items = summary.split(",");

            for (String item : items) {
                try {
                    String[] parts = item.trim().split("\\s+");

                    String name = parts[0];
                    double qty = Double.parseDouble(parts[1]);
                    double total = Double.parseDouble(parts[2]);

                    double price = total / qty;

                    list.add(new SaleItemRow(date, name, qty, price, total));

                } catch (Exception e) {
                    e.printStackTrace(); // avoid crash
                }
            }
        }

        cursor.close();
        return list;
    }
}