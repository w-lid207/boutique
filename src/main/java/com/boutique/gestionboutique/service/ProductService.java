package com.boutique.gestionboutique.service;

import com.boutique.gestionboutique.model.Product;
import com.boutique.gestionboutique.database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private Connection connection;

    public ProductService() {
        try {
            this.connection = Database.getConnection();
        } catch (Exception e) {
            System.err.println("Erreur connexion BD: " + e.getMessage());
        }
    }

    /**
     * Récupérer tous les produits avec le nom de la catégorie
     */
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.id, p.name, p.price, p.quantity, p.category_id, p.image_path, c.name as category_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id "
                ;

        try ( Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query) ) {

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantity(rs.getInt("quantity"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setImagePath(rs.getString("image_path"));
                product.setCategoryName(rs.getString("category_name"));
                products.add(product);
            }
        }
        return products;
    }


    /**
     * Ajouter un produit
     */
    public void addProduct(Product product) throws SQLException {
        String query = "INSERT INTO products (name, price, quantity, category_id, image_path) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setString(5, product.getImagePath());
            pstmt.executeUpdate();
        }
    }

    /**
     * Mettre à jour un produit
     */
    public void updateProduct(Product product) throws SQLException {
        String query = "UPDATE products SET name=?, price=?, quantity=?, category_id=?, image_path=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setString(5, product.getImagePath());
            pstmt.setInt(6, product.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Supprimer un produit
     */
    public void deleteProduct(int id) throws SQLException {
        String query = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }




}