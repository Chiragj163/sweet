package com.example.sweet;

import android.content.ContentValues;
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
import com.github.mikephil.charting.data.PieEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SweetShopDB";
    private static final int DB_VERSION = 7;

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
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE sales ADD COLUMN payment_mode TEXT");
        }
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
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
        } finally {
            cursor.close();
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

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }
    public double getAverageBill() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(total) FROM sales", null);

        double avg = 0;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            avg = cursor.getDouble(0);
        }

        cursor.close();
        return avg;
    }
    public long saveSale(double subtotal, double gst, double discount, double total, String paymentMode, List<CartItem> cartList){

        SQLiteDatabase db = this.getWritableDatabase();

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .format(new Date());
        int billNo = getTodayBillCount() + 1;

        android.content.ContentValues values = new android.content.ContentValues();
        values.put("bill_no", billNo);
        values.put("date", date);
        values.put("subtotal", subtotal);
        values.put("gst", gst);
        values.put("discount", discount);
        values.put("payment_mode", paymentMode);
        values.put("total", total);

        db.beginTransaction();
        try {
            long saleId = db.insert("sales", null, values);

            for (CartItem item : cartList) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("sale_id", saleId);
                itemValues.put("name", item.getName());
                itemValues.put("qty", item.getQty());
                itemValues.put("price", item.getPrice());
                itemValues.put("total", item.getTotal());
                itemValues.put("gst_rate", item.getGstRate());

                db.insert("sale_items", null, itemValues);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
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

            int saleId = cursor.getInt(0);
            int billNo = cursor.getInt(1);
            String date = cursor.getString(2);

            // 🔥 SAFE PAYMENT MODE
            int index = cursor.getColumnIndex("payment_mode");
            String paymentMode = "Cash";

            if (index != -1 && !cursor.isNull(index)) {
                paymentMode = cursor.getString(index);
            }

            double total = cursor.getDouble(6);

            Sale s = new Sale();
            s.setBillNo(billNo);
            s.setPaymentMode(paymentMode);
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

        if (cursor.moveToFirst()) {

            do {
                String image = "";

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
    public List<SaleItemRow> getSaleItemsBySaleId(int saleId) {

        List<SaleItemRow> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT name, qty, price, total, gst_rate FROM sale_items WHERE sale_id = ?",
                new String[]{String.valueOf(saleId)}
        );

        while (c.moveToNext()) {
            list.add(new SaleItemRow(
                    "", // date if needed
                    c.getString(0),
                    c.getDouble(1),
                    c.getDouble(2),
                    c.getDouble(3),
                    c.getDouble(4)
            ));
        }

        c.close();
        return list;
    }
    public List<Sale> getSalesBetweenDates(String start, String end) {

        List<Sale> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT * FROM sales WHERE date(date) BETWEEN date(?) AND date(?) ORDER BY date DESC",
                new String[]{start, end}
        );

        while (c.moveToNext()) {
            int saleId = c.getInt(c.getColumnIndexOrThrow("id"));

            Sale sale = new Sale();

            sale.setBillNo(c.getInt(c.getColumnIndexOrThrow("bill_no")));
            sale.setDate(c.getString(c.getColumnIndexOrThrow("date")));
            sale.setAmount(c.getDouble(c.getColumnIndexOrThrow("total")));

            sale.items = getSaleItemsBySaleId(saleId);

            list.add(sale);
        }

        c.close();
        return list;
    }
    public List<PieEntry> getItemWiseSales(String start, String end) {
        List<PieEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT si.name, SUM(si.total) FROM sale_items si " +
                        "JOIN sales s ON si.sale_id = s.id " +
                        "WHERE date(s.date) BETWEEN date(?) AND date(?) " +
                        "GROUP BY si.name",
                new String[]{start, end}
        );

        while (c.moveToNext()) {
            list.add(new PieEntry((float) c.getDouble(1), c.getString(0)));
        }
        c.close();
        return list;
    }
    public List<PieEntry> getCategoryWiseSales(String start, String end) {

        List<PieEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT i.category, SUM(si.total) FROM sale_items si " +
                        "JOIN items i ON si.name = i.name " +
                        "JOIN sales s ON si.sale_id = s.id " +
                        "WHERE date(s.date) BETWEEN date(?) AND date(?) " +
                        "GROUP BY i.category",
                new String[]{start, end}
        );

        while (c.moveToNext()) {
            list.add(new PieEntry(
                    (float) c.getDouble(1),
                    c.getString(0)
            ));
        }

        c.close();
        return list;
    }
    public List<PieEntry> getItemWiseSalesFiltered(String start, String end, String category) {

        List<PieEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query;

        if (category.equals("All")) {
            query = "SELECT si.name, SUM(si.total) FROM sale_items si " +
                    "JOIN sales s ON si.sale_id = s.id " +
                    "WHERE date(s.date) BETWEEN date(?) AND date(?) " +
                    "GROUP BY si.name";
        } else {
            query = "SELECT si.name, SUM(si.total) FROM sale_items si " +
                    "JOIN items i ON si.name = i.name " +
                    "JOIN sales s ON si.sale_id = s.id " +
                    "WHERE i.category=? AND date(s.date) BETWEEN date(?) AND date(?) " +
                    "GROUP BY si.name";
        }

        Cursor c = category.equals("All")
                ? db.rawQuery(query, new String[]{start, end})
                : db.rawQuery(query, new String[]{category, start, end});

        while (c.moveToNext()) {
            list.add(new PieEntry(
                    (float) c.getDouble(1),
                    c.getString(0)
            ));
        }

        c.close();
        return list;
    }
    public double getMonthSales() {
        SQLiteDatabase db = this.getReadableDatabase();

        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                .format(new Date());

        Cursor c = db.rawQuery(
                "SELECT SUM(total) FROM sales WHERE date LIKE ?",
                new String[]{month + "%"}
        );

        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }
    public double getYearSales() {
        SQLiteDatabase db = this.getReadableDatabase();

        String year = new SimpleDateFormat("yyyy", Locale.getDefault())
                .format(new Date());

        Cursor c = db.rawQuery(
                "SELECT SUM(total) FROM sales WHERE date LIKE ?",
                new String[]{year + "%"}
        );

        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }
    public double getSalesBetweenDatesTotal(String start, String end) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT SUM(total) FROM sales WHERE date(date) BETWEEN date(?) AND date(?)",
                new String[]{start, end}
        );

        double total = 0;

        if (c.moveToFirst() && !c.isNull(0)) {
            total = c.getDouble(0);
        }

        c.close();
        return total;
    }
    public List<String> getSalesByItemBetweenDates(String start, String end) {

        List<String> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT si.name, SUM(si.qty), SUM(si.total) " +
                        "FROM sale_items si " +
                        "JOIN sales s ON si.sale_id = s.id " +
                        "WHERE date(s.date) BETWEEN date(?) AND date(?) " +
                        "GROUP BY si.name",
                new String[]{start, end}
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
    public List<String> getSalesByItemFiltered(String start, String end, String category) {

        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT si.name, SUM(si.qty), SUM(si.total) " +
                        "FROM sale_items si " +
                        "JOIN sales s ON si.sale_id = s.id " +
                        "JOIN items i ON si.name = i.name " +
                        "WHERE date(s.date) BETWEEN date(?) AND date(?) ";

        if (!category.equals("All")) {
            query += "AND i.category = ? ";
        }

        query += "GROUP BY si.name";

        Cursor c;

        if (category.equals("All")) {
            c = db.rawQuery(query, new String[]{start, end});
        } else {
            c = db.rawQuery(query, new String[]{start, end, category});
        }

        while (c.moveToNext()) {

            list.add(
                    c.getString(0) +
                            "\nQty Sold: " + c.getDouble(1) +
                            "\nRevenue: ₹" + c.getDouble(2)
            );
        }

        c.close();
        return list;
    }
    public double getSalesByItemTotal(String start, String end, String category) {

        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT SUM(si.total) " +
                        "FROM sale_items si " +
                        "JOIN sales s ON si.sale_id = s.id " +
                        "JOIN items i ON si.name = i.name " +
                        "WHERE date(s.date) BETWEEN date(?) AND date(?) ";

        if (!category.equals("All")) {
            query += "AND i.category = ?";
        }

        Cursor c;

        if (category.equals("All")) {
            c = db.rawQuery(query, new String[]{start, end});
        } else {
            c = db.rawQuery(query, new String[]{start, end, category});
        }

        double total = 0;

        if (c.moveToFirst() && !c.isNull(0)) {
            total = c.getDouble(0);
        }

        c.close();
        return total;
    }
    public List<Sale> getSalesByPaymentMode(String mode) {

        List<Sale> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM sales WHERE payment_mode=? ORDER BY id DESC",
                new String[]{mode}
        );

        while (cursor.moveToNext()) {

            int saleId = cursor.getInt(0);
            int billNo = cursor.getInt(1);
            String date = cursor.getString(2);
            String paymentMode = cursor.getString(cursor.getColumnIndexOrThrow("payment_mode"));
            double total = cursor.getDouble(6);

            Sale s = new Sale();
            s.setBillNo(billNo);
            s.setPaymentMode(paymentMode);
            s.setDate(date);
            s.setAmount(total);

            // 🔥 Load items
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
    public int getTodayBills() {

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
    public double getTodayAverageBill() {

        SQLiteDatabase db = this.getReadableDatabase();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT AVG(total) FROM sales WHERE date LIKE ?",
                new String[]{today + "%"}
        );

        double avg = 0;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            avg = cursor.getDouble(0);
        }

        cursor.close();
        return avg;
    }
    public String getTodayTopItem() {

        SQLiteDatabase db = this.getReadableDatabase();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT si.name, SUM(si.qty) as total_qty " +
                        "FROM sale_items si " +
                        "JOIN sales s ON si.sale_id = s.id " +
                        "WHERE s.date LIKE ? " +
                        "GROUP BY si.name " +
                        "ORDER BY total_qty DESC LIMIT 1",
                new String[]{today + "%"}
        );

        String result = "N/A";

        if (cursor.moveToFirst()) {
            result = cursor.getString(0) + " (" + cursor.getDouble(1) + ")";
        }

        cursor.close();
        return result;
    }
}