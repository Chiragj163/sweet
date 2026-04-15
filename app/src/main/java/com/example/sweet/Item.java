package com.example.sweet;

public class Item {
        int id;
        String name;
        double price;
        double gstRate; // 5, 18, 40
        String unit;
        String category;
        String image;


    public Item(int id, String name, double price, String unit , double gstRate , String category, String image) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.unit = unit;
            this.gstRate = gstRate;
            this.category = category;
            this.image = image;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getUnit() { return unit; }
        public  double getGstRate(){ return gstRate; }
        public String getCategory() { return category;}
        public  String getImage(){ return image;}
    }
