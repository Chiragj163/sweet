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
import java.util.Locale;

import com.example.sweet.Sale;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SweetShopDB";
    private static final int DB_VERSION = 5;

    public static final String TABLE_ITEMS = "items";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ITEMS_TABLE = "CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "price REAL," +
                "unit TEXT," +
                "gst_rate REAL, "+
                "category TEXT,"+
                "image TEXT)";
        db.execSQL(CREATE_ITEMS_TABLE);

        String CREATE_SALES_TABLE = "CREATE TABLE sales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bill_no INTEGER,"+
                "date TEXT," +
                "subtotal REAL," +
                "gst REAL," +
                "discount REAL," +
                "total REAL)";

        db.execSQL(CREATE_SALES_TABLE);

        String CREATE_SALE_ITEMS = "CREATE TABLE sale_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bill_no INTEGER," +
                "sale_id INTEGER," +
                "name TEXT," +
                "qty REAL," +
                "price REAL," +
                "total REAL," +
                "gst_rate REAL)";

        db.execSQL(CREATE_SALE_ITEMS);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS sale_items");
        db.execSQL("DROP TABLE IF EXISTS sales");
        db.execSQL("DROP TABLE IF EXISTS items");

        onCreate(db);
    }
    public void insertItem(String name, double price, String unit,
                           double gstRate, String category, String image) {

        SQLiteDatabase db = this.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put("name", name);
        values.put("price", price);
        values.put("unit", unit);
        values.put("gst_rate", gstRate);
        values.put("category", category);
        values.put("image", image);

        db.insert("items", null, values);
    }
    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (cursor.moveToFirst()) {
            do {
                String image = "";
                int imageIndex = cursor.getColumnIndex("image");

                if (imageIndex != -1 && !cursor.isNull(imageIndex)) {
                    image = cursor.getString(imageIndex);
                }
                list.add(new Item(
                        cursor.getInt(0),      // id
                        cursor.getString(1),   // name
                        cursor.getDouble(2),   // price
                        cursor.getString(3),   // unit
                        cursor.getDouble(4),
                        cursor.getString(5),
                        image
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
    public void updateItem(int id, String name, double price,
                           String unit, double gstRate,
                           String category, String image) {

        SQLiteDatabase db = this.getWritableDatabase();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put("name", name);
        values.put("price", price);
        values.put("unit", unit);
        values.put("gst_rate", gstRate);
        values.put("category", category);
        values.put("image", image);

        db.update("items", values, "id=?", new String[]{String.valueOf(id)});
    }

    public double getTodaySales() {

        SQLiteDatabase db = this.getReadableDatabase();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT SUM(total) FROM sales WHERE date LIKE ?",
                new String[]{today + "%"}
        );

        double total = 0;

        if (cursor != null && cursor.moveToFirst()) {
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

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis() - (long) i * 86400000));

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(total) FROM sales WHERE date LIKE ?",
                    new String[]{date + "%"}   // 🔥 FIX
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

        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                .format(new Date());

        for (int i = 1; i <= 30; i++) {

            String day = (i < 10 ? "0" + i : "" + i);
            String date = month + "-" + day;

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(total) FROM sales WHERE date LIKE ?",
                    new String[]{date + "%"}   // 🔥 FIX
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
    public long saveSale(double subtotal, double gst, double discount, double total, List<CartItem> cartList) {

        SQLiteDatabase db = this.getWritableDatabase();

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int billNo = getTodayBillCount() + 1;

        android.content.ContentValues values = new android.content.ContentValues();
        values.put("bill_no", billNo);
        values.put("date", date);
        values.put("subtotal", subtotal);
        values.put("gst", gst);
        values.put("discount", discount);
        values.put("total", total);

        long saleId = db.insert("sales", null, values);

        // 🔥 Insert each item
        for (CartItem item : cartList) {

            android.content.ContentValues itemValues = new android.content.ContentValues();
            itemValues.put("sale_id", saleId);
            itemValues.put("name", item.getName());
            itemValues.put("qty", item.getQty());
            itemValues.put("price", item.getPrice());
            itemValues.put("total", item.getTotal());
            itemValues.put("gst_rate", item.getGstRate());

            db.insert("sale_items", null, itemValues);
        }
        return billNo;
    }
    public String getTopItem() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT name, SUM(qty) as total_qty " +
                        "FROM sale_items " +
                        "GROUP BY name " +
                        "ORDER BY total_qty DESC " +
                        "LIMIT 1",
                null
        );

        String result = "N/A";

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            double qty = cursor.getDouble(1);

            result = name + " (" + qty + " sold)";
        }

        cursor.close();
        return result;
    }
    public List<Sale> getAllSales() {

        List<Sale> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM sales ORDER BY id DESC", null);

        while (cursor.moveToNext()) {

            int saleId = cursor.getInt(0);     // ✅ ID
            int billNo = cursor.getInt(1);     // ✅ bill_no
            String date = cursor.getString(2); // ✅ date
            double total = cursor.getDouble(6); // ✅ total

            Sale s = new Sale();
            s.setBillNo(billNo);   // 🔥 IMPORTANT
            s.setDate(date);
            s.setAmount(total);

            // 🔥 GET ITEMS
            Cursor itemCursor = db.rawQuery(
                    "SELECT name, qty, price, total, gst_rate FROM sale_items WHERE sale_id=?",
                    new String[]{String.valueOf(saleId)}
            );

            while (itemCursor.moveToNext()) {

                s.items.add(new SaleItemRow(
                        date,
                        itemCursor.getString(0),
                        itemCursor.getDouble(1),
                        itemCursor.getDouble(2),
                        itemCursor.getDouble(3),
                        itemCursor.getDouble(4)
                ));
            }

            itemCursor.close();
            list.add(s);
        }

        cursor.close();
        return list;
    }

    public List<String> getSalesByItem() {

        List<String> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT name, SUM(qty), SUM(total) FROM sale_items GROUP BY name",
                null
        );

        while (cursor.moveToNext()) {

            String name = cursor.getString(0);
            double qty = cursor.getDouble(1);
            double revenue = cursor.getDouble(2);

            result.add(
                    name +
                            "\nQty Sold: " + qty +
                            "\nRevenue: ₹" + revenue
            );
        }

        cursor.close();
        return result;
    }
    public List<SaleItemRow> getDetailedSales() {

        List<SaleItemRow> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT s.date, si.name, si.qty, si.price, si.total, si.gst_rate " +
                        "FROM sales s " +
                        "JOIN sale_items si ON s.id = si.sale_id " +
                        "ORDER BY s.id DESC",
                null
        );

        while (cursor.moveToNext()) {

            list.add(new SaleItemRow(
                    cursor.getString(0),  // date
                    cursor.getString(1),  // name
                    cursor.getDouble(2),  // qty
                    cursor.getDouble(3),  // price
                    cursor.getDouble(4),  // total
                    cursor.getDouble(5)   // gst_rate ✅ NEW
            ));
        }

        cursor.close();
        return list;
    }
    public List<Item> getItemsByCategory(String category) {

        List<Item> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        if (category.equals("All")) {
            cursor = db.rawQuery("SELECT * FROM items", null);
        } else {
            cursor = db.rawQuery(
                    "SELECT * FROM items WHERE category=?",
                    new String[]{category}
            );
        }

        if (cursor != null && cursor.moveToFirst()) {

            do {

                String image = "";

                // ✅ SAFE COLUMN CHECK
                int imageIndex = cursor.getColumnIndex("image");

                if (imageIndex != -1 && !cursor.isNull(imageIndex)) {
                    image = cursor.getString(imageIndex);
                }

                list.add(new Item(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        image
                ));

            } while (cursor.moveToNext());
        }


        if (cursor != null) cursor.close();

        return list;
    }
    public int getTodayBillCount() {

        SQLiteDatabase db = this.getReadableDatabase();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM sales WHERE date LIKE ?",
                new String[]{today + "%"}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }
    public Sale getSaleByBillId(int billNo) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM sales WHERE bill_no=?",
                new String[]{String.valueOf(billNo)}
        );

        if (cursor.moveToFirst()) {

            int saleId = cursor.getInt(0);
            String date = cursor.getString(2);
            double total = cursor.getDouble(6);

            Sale s = new Sale();
            s.setBillNo(billNo);
            s.setDate(date);
            s.setAmount(total);

            Cursor itemCursor = db.rawQuery(
                    "SELECT name, qty, price, total, gst_rate FROM sale_items WHERE sale_id=?",
                    new String[]{String.valueOf(saleId)}
            );

            while (itemCursor.moveToNext()) {
                s.items.add(new SaleItemRow(
                        date,
                        itemCursor.getString(0),
                        itemCursor.getDouble(1),
                        itemCursor.getDouble(2),
                        itemCursor.getDouble(3),
                        itemCursor.getDouble(4)
                ));
            }

            itemCursor.close();
            cursor.close();
            return s;
        }

        cursor.close();
        return null;
    }
    public List<Sale> getSalesByDate(String date) {

        List<Sale> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM sales WHERE date LIKE ? ORDER BY id DESC",
                new String[]{date + "%"}
        );

        while (cursor.moveToNext()) {

            int saleId = cursor.getInt(0);
            int billNo = cursor.getInt(1);
            String saleDate = cursor.getString(2);
            double total = cursor.getDouble(6);

            Sale s = new Sale();
            s.setBillNo(billNo);
            s.setDate(saleDate);
            s.setAmount(total);

            Cursor itemCursor = db.rawQuery(
                    "SELECT name, qty, price, total, gst_rate FROM sale_items WHERE sale_id=?",
                    new String[]{String.valueOf(saleId)}
            );

            while (itemCursor.moveToNext()) {
                s.items.add(new SaleItemRow(
                        saleDate,
                        itemCursor.getString(0),
                        itemCursor.getDouble(1),
                        itemCursor.getDouble(2),
                        itemCursor.getDouble(3),
                        itemCursor.getDouble(4)
                ));
            }

            itemCursor.close();
            list.add(s);
        }

        cursor.close();
        return list;
    }

}