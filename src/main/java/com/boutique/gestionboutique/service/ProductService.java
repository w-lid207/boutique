package com.boutique.gestionboutique.service;

import com.boutique.gestionboutique.controller.Product;
import com.boutique.gestionboutique.database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductService {
    private Connection connection;

    public ProductService() {
        try {
            this.connection = Database.getConnection();
            if (this.connection != null) {
                System.out.println("✅ ProductService: Connexion BD établie");
            } else {
                System.err.println("❌ ProductService: Connexion null");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur connexion BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifier la connexion
     */
    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.err.println("❌ Connexion perdue, reconnexion...");
            this.connection = Database.getConnection();
            if (this.connection == null) {
                throw new SQLException("Impossible d'établir la connexion à la BD");
            }
        }
    }

    /**
     * ✅ Récupérer tous les produits UNIQUES avec le nom de la catégorie
     */
    public List<Product> getAllProducts() throws SQLException {
        checkConnection();

        // Utiliser LinkedHashMap pour garantir l'unicité par ID et conserver l'ordre
        Map<Integer, Product> productsMap = new LinkedHashMap<>();

        String query = "SELECT DISTINCT p.id, p.name, p.price, p.quantity, p.category_id, p.image_path, c.name as category_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "ORDER BY p.name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int productId = rs.getInt("id");

                // ✅ Ajouter seulement si l'ID n'existe pas déjà
                if (!productsMap.containsKey(productId)) {
                    Product product = new Product();
                    product.setId(productId);
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setQuantity(rs.getInt("quantity"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setImagePath(rs.getString("image_path"));
                    product.setCategoryName(rs.getString("category_name"));

                    productsMap.put(productId, product);
                }
            }

            List<Product> products = new ArrayList<>(productsMap.values());
            System.out.println("✅ getAllProducts: " + products.size() + " produit(s) UNIQUE(S) chargé(s)");

            // Debug: Afficher les IDs pour vérifier l'unicité
            System.out.println("📋 IDs chargés: " + productsMap.keySet());

        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllProducts: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return new ArrayList<>(productsMap.values());
    }

    /**
     * Récupérer un produit par ID
     */
    public Product getProductById(int id) throws SQLException {
        checkConnection();
        String query = "SELECT p.id, p.name, p.price, p.quantity, p.category_id, p.image_path, c.name as category_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.id = ?";
        Product product = null;

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setQuantity(rs.getInt("quantity"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setImagePath(rs.getString("image_path"));
                    product.setCategoryName(rs.getString("category_name"));
                    System.out.println("✅ Produit trouvé: " + product.getName());
                } else {
                    System.err.println("⚠️  Aucun produit avec l'ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getProductById: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return product;
    }

    /**
     * ✅ Récupérer les produits UNIQUES par catégorie
     */
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        checkConnection();
        Map<Integer, Product> productsMap = new LinkedHashMap<>();

        String query = "SELECT DISTINCT p.id, p.name, p.price, p.quantity, p.category_id, p.image_path, c.name as category_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.category_id = ? " +
                "ORDER BY p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("id");

                    if (!productsMap.containsKey(productId)) {
                        Product product = new Product();
                        product.setId(productId);
                        product.setName(rs.getString("name"));
                        product.setPrice(rs.getDouble("price"));
                        product.setQuantity(rs.getInt("quantity"));
                        product.setCategoryId(rs.getInt("category_id"));
                        product.setImagePath(rs.getString("image_path"));
                        product.setCategoryName(rs.getString("category_name"));

                        productsMap.put(productId, product);
                    }
                }
                System.out.println("✅ getProductsByCategory: " + productsMap.size() + " produit(s) UNIQUE(S) trouvé(s)");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getProductsByCategory: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return new ArrayList<>(productsMap.values());
    }

    /**
     * AJOUTER un produit
     */
    public void addProduct(Product product) throws SQLException {
        checkConnection();
        String query = "INSERT INTO products (name, price, quantity, category_id, image_path) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setString(5, product.getImagePath());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ AJOUT: '" + product.getName() + "' inséré avec succès!");
            } else {
                throw new SQLException("Aucune ligne insérée");
            }
        } catch (SQLException e) {
            System.err.println("❌ AJOUT - Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * METTRE À JOUR un produit
     */
    public void updateProduct(Product product) throws SQLException {
        checkConnection();
        String query = "UPDATE products SET name=?, price=?, quantity=?, category_id=?, image_path=? WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setString(5, product.getImagePath());
            pstmt.setInt(6, product.getId());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ ÉDITION: '" + product.getName() + "' (ID " + product.getId() + ") modifié");
            } else {
                throw new SQLException("Produit ID " + product.getId() + " non trouvé");
            }
        } catch (SQLException e) {
            System.err.println("❌ ÉDITION - Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * SUPPRIMER un produit
     */
    public void deleteProduct(int id) throws SQLException {
        checkConnection();
        String query = "DELETE FROM products WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ SUPPRESSION: Produit ID " + id + " supprimé");
            } else {
                throw new SQLException("Produit ID " + id + " non trouvé");
            }
        } catch (SQLException e) {
            System.err.println("❌ SUPPRESSION - Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ✅ Rechercher des produits UNIQUES par nom
     */
    public List<Product> searchProducts(String keyword) throws SQLException {
        checkConnection();
        Map<Integer, Product> productsMap = new LinkedHashMap<>();

        String query = "SELECT DISTINCT p.id, p.name, p.price, p.quantity, p.category_id, p.image_path, c.name as category_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.name LIKE ? " +
                "ORDER BY p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String searchTerm = "%" + keyword + "%";
            pstmt.setString(1, searchTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("id");

                    if (!productsMap.containsKey(productId)) {
                        Product product = new Product();
                        product.setId(productId);
                        product.setName(rs.getString("name"));
                        product.setPrice(rs.getDouble("price"));
                        product.setQuantity(rs.getInt("quantity"));
                        product.setCategoryId(rs.getInt("category_id"));
                        product.setImagePath(rs.getString("image_path"));
                        product.setCategoryName(rs.getString("category_name"));

                        productsMap.put(productId, product);
                    }
                }
                System.out.println("✅ Recherche: " + productsMap.size() + " produit(s) UNIQUE(S) trouvé(s) pour '" + keyword + "'");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur searchProducts: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return new ArrayList<>(productsMap.values());
    }

    /**
     * Obtenir toutes les catégories
     */
    public List<String> getAllCategories() throws SQLException {
        checkConnection();
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT name FROM categories ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
            System.out.println("✅ getAllCategories: " + categories.size() + " catégorie(s) chargée(s)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllCategories: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return categories;
    }
}