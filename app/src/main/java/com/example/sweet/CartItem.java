package com.example.sweet;

public class CartItem {

    int id;              // ✅ for unique identification
    String name;
    double qty;
    double price;        // GST INCLUDED price
    double gstRate;      // 5 / 18 / 40

    public CartItem(int id, String name, double qty, double price, double gstRate) {
        this.id = id;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.gstRate = gstRate;
    }

    // ================= GETTERS =================

    public int getId() { return id; }

    public String getName() { return name; }

    public double getQty() { return qty; }

    public double getPrice() { return price; }

    public double getGstRate() { return gstRate; }

    // ================= SETTERS =================

    public void setQty(double qty) {
        this.qty = qty;
    }

    // ================= CALCULATIONS =================

    public double getTotal() {
        return qty * price; // already includes GST
    }

    // 🔥 Extract GST amount from total
    public double getGstAmount() {
        double total = getTotal();
        return (total * gstRate) / (100 + gstRate);
    }

    // 🔥 Base price (excluding GST)
    public double getBaseAmount() {
        return getTotal() - getGstAmount();
    }
}