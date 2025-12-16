package com.boutique.gestionboutique.controller;

import java.time.LocalDateTime;

public class Sale {
    private int id;
    private LocalDateTime date;
    private double total;

    // Constructeurs
    public Sale() {
    }

    public Sale(int id, LocalDateTime date, double total) {
        this.id = id;
        this.date = date;
        this.total = total;
    }

    public Sale(LocalDateTime date, double total) {
        this.date = date;
        this.total = total;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", date=" + date +
                ", total=" + total +
                '}';
    }
}