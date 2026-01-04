package com.boutique.gestionboutique.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private LocalDateTime date;
    private double total;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {}

    public Sale(int id, LocalDateTime date, double total) {
        this.id = id;
        this.date = date;
        this.total = total;
    }

    public int getId() { return id; }
    public LocalDateTime getDate() { return date; }
    public double getTotal() { return total; }
    public List<SaleItem> getItems() { return items; }

    public void setId(int id) { this.id = id; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setTotal(double total) { this.total = total; }

    // Classe interne pour les produits
    public static class SaleItem {
        private String name;
        private double price;
        private int quantity;

        public SaleItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return price * quantity; }
    }
}