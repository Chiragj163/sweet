package com.example.sweet;

public class SaleItemRow {
    String date;
    String name;
    double qty;
    double price;
    double total;

    public SaleItemRow(String date, String name, double qty, double price, double total) {
        this.date = date;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.total = total;
    }

}