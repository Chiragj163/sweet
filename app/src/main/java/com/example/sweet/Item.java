package com.example.sweet;

public class Item {
        int id;
        String name;
        double price;
        String unit;

        public Item(int id, String name, double price, String unit) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.unit = unit;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getUnit() { return unit; }
    }
