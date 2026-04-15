package com.example.sweet;
import java.io.Serializable;


public class SaleItemRow implements Serializable {
    String date;
    String name;
    double qty;
    double price;
    double total;
    double gstRate;

    public SaleItemRow(String date, String name, double qty, double price, double total, double gstRate) {
        this.date = date;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.total = total;
        this.gstRate= gstRate;
    }

}