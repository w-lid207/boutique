package com.boutique.gestionboutique.service;
import com.boutique.gestionboutique.model.Product;
import javafx.collections.FXCollections;
import com.boutique.gestionboutique.database.Database;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CartService {
    private Database db = new Database();
    private Connection connection  = db.getConnection();
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
        cartItems.add(product);
    }

    public void processSale(ObservableList<Product> cartItems, double total) throws SQLException{
        String query = "INSERT INTO sales(date, total) VALUES (CURRENT_TIMESTAMP(), ?)";
        try {
            int saleId;
            PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setDouble(1, total);
            int rowsInserted = pstmt.executeUpdate();
            var rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                saleId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to get Sale ID.");
            }
            if (rowsInserted > 0) {
                System.out.println("A new sale was inserted successfully!");
            }
            updateSaleItemsAndQuantity(cartItems, saleId);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void updateSaleItemsAndQuantity(ObservableList<Product> cartItems, int last_sale) throws SQLException{
        String query = "INSERT INTO sale_items(sale_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String query2 = "UPDATE products SET quantity =  quantity - ? WHERE id = ?";

        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            PreparedStatement pstmt2 = connection.prepareStatement(query2);
            for(Product p : cartItems){
                pstmt.setInt(1,last_sale);
                pstmt.setInt(2,p.getId());
                pstmt.setInt(3,p.getqCartItem());
                pstmt.setDouble(4,p.getPrice());
                int check = pstmt.executeUpdate();
                pstmt2.setInt(1,p.getqCartItem());
                pstmt2.setInt(2,p.getId());
                int check2 = pstmt2.executeUpdate();
                if(check > 0){
                    System.out.println("A new SALE ITEM was inserted successfully!");
                } if(check2 > 0){
                    System.out.println("Quantity updated succfecully");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

}
