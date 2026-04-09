package com.example.sweet;

public class CartItem {

    String name;
    double qty;
    double price;

    public CartItem(String name, double qty, double price) {
        this.name = name;
        this.qty = qty;
        this.price = price;
    }

    public String getName() { return name; }
    public double getQty() { return qty; }
    public double getPrice() { return price; }
    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getTotal() {
        return qty * price;
    }
}