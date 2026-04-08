package com.example.sweet;

import java.util.ArrayList;
import java.util.List;

public class Sale {

    private double amount;
    private String summary;
    private String date;

    public List<SaleItemRow> items = new ArrayList<>();

    // ✅ Empty constructor (required)
    public Sale() {}

    public Sale(double amount, String summary) {
        this.amount = amount;
        this.summary = summary;
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}