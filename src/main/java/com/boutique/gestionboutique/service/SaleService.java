package com.boutique.gestionboutique.service;

import com.boutique.gestionboutique.database.Database;
import com.boutique.gestionboutique.controller.Sale;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    /**
     * Récupère toutes les ventes de la base de données
     */
    public List<Sale> getAllSales() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String query = "SELECT * FROM sales ORDER BY date DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Sale sale = new Sale();
                sale.setId(rs.getInt("id"));
                sale.setDate(rs.getTimestamp("date").toLocalDateTime());
                sale.setTotal(rs.getDouble("total"));
                sales.add(sale);
            }
        }

        return sales;
    }

    /**
     * Récupère une vente par son ID
     */
    public Sale getSaleById(int id) throws SQLException {
        String query = "SELECT * FROM sales WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("id"));
                    sale.setDate(rs.getTimestamp("date").toLocalDateTime());
                    sale.setTotal(rs.getDouble("total"));
                    return sale;
                }
            }
        }

        return null;
    }

    /**
     * Ajoute une nouvelle vente
     */
    public int addSale(Sale sale) throws SQLException {
        String query = "INSERT INTO sales (date, total) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(sale.getDate()));
            pstmt.setDouble(2, sale.getTotal());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }

        throw new SQLException("Creating sale failed, no ID obtained.");
    }

    /**
     * Met à jour une vente existante
     */
    public boolean updateSale(Sale sale) throws SQLException {
        String query = "UPDATE sales SET date = ?, total = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(sale.getDate()));
            pstmt.setDouble(2, sale.getTotal());
            pstmt.setInt(3, sale.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Supprime une vente
     */
    public boolean deleteSale(int id) throws SQLException {
        String query = "DELETE FROM sales WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Récupère les ventes d'une période donnée
     */
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String query = "SELECT * FROM sales WHERE date BETWEEN ? AND ? ORDER BY date DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("id"));
                    sale.setDate(rs.getTimestamp("date").toLocalDateTime());
                    sale.setTotal(rs.getDouble("total"));
                    sales.add(sale);
                }
            }
        }

        return sales;
    }

    /**
     * Calcule le revenu total de toutes les ventes
     */
    public double getTotalRevenue() throws SQLException {
        String query = "SELECT SUM(total) as total_revenue FROM sales";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("total_revenue");
            }
        }

        return 0.0;
    }

    /**
     * Calcule le revenu total pour aujourd'hui
     */
    public double getTodayRevenue() throws SQLException {
        String query = "SELECT SUM(total) as today_revenue FROM sales WHERE DATE(date) = CURDATE()";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("today_revenue");
            }
        }

        return 0.0;
    }

    /**
     * Compte le nombre de ventes pour aujourd'hui
     */
    public int getTodaySalesCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM sales WHERE DATE(date) = CURDATE()";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }

        return 0;
    }

    /**
     * Récupère le nombre total de ventes
     */
    public int getTotalSalesCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM sales";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }

        return 0;
    }
}