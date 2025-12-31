package com.boutique.gestionboutique.controller;

import java.time.LocalDateTime;

public class Sale {

    private int id;
    private LocalDateTime date;
    private double total;

    public Sale() {}

    public Sale(int id, LocalDateTime date, double total) {
        this.id = id;
        this.date = date;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public double getTotal() {
        return total;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
