package com.example.btl;

// Model đại diện cho một dòng trong bảng SQLite plants.
public class Plant {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private String description;
    private String imageUri;

    public Plant() {
    }

    public Plant(int id, String name, String category, double price,
                 int stock, String description, String imageUri) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imageUri = imageUri;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
