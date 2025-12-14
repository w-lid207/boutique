package com.boutique.gestionboutique.service;
import com.boutique.gestionboutique.controller.Product;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kotlin.contracts.ReturnsNotNull;

public class CartService {
    private static final CartService instance = new CartService();

    private final ObservableList<Product> cartItems = FXCollections.observableArrayList();

    private CartService(){}

    public static CartService getInstance(){
        return instance;
    }
    public ObservableList<Product> getCartItems(){
        return cartItems;
    }
    public void addItem(Product product) {
        // Logic to check quantity, etc.
        cartItems.add(product);
    }
}
