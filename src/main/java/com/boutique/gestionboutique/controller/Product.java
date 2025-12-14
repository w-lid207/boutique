package com.boutique.gestionboutique.controller;

public class Product {
    // Attributs du produit
    private int id;
    private String name;
    private double price;
    private double qPrice;
    private int quantity;
    private int qCartItem = 0;
    private int categoryId;
    private String imagePath;
    private String categoryName;

    // Constructeur vide (obligatoire pour JavaFX)
    public Product() {
    }

    // Constructeur avec paramètres
    public Product(String name, double price, int quantity, int categoryId, String imagePath) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.imagePath = imagePath;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public void setqPrice(double qPrice) {
        if(qCartItem == 1){
            this.qPrice = this.price;
            return;
        }
        this.qPrice = qPrice;
    }

    public double getqPrice() {
        return qPrice;
    }
    public int getQuantity() {
        return quantity;
    }
    public int getqCartItem() {
        return qCartItem;
    }

    public void setqCartItem(int qCartItem) {
        if(qCartItem < 1){
            return;
        }
        this.qCartItem = qCartItem;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", categoryId=" + categoryId +
                ", imagePath='" + imagePath + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}