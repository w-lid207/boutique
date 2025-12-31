package com.boutique.gestionboutique.service;

import com.boutique.gestionboutique.controller.Sale;
import com.boutique.gestionboutique.database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        // 1. Récupérer les ventes
        String sqlSales = "SELECT id, date, total FROM sales ORDER BY id DESC";

        // 2. Récupérer les détails avec JOINTURE pour avoir le nom du produit
        String sqlItems = "SELECT p.name, si.price, si.quantity " +
                "FROM sale_items si " +
                "JOIN products p ON si.product_id = p.id " +
                "WHERE si.sale_id = ?";

        try (Connection con = Database.getConnection()) {
            if (con == null) return sales;

            try (PreparedStatement psSales = con.prepareStatement(sqlSales);
                 ResultSet rsSales = psSales.executeQuery()) {

                while (rsSales.next()) {
                    Sale sale = new Sale(
                            rsSales.getInt("id"),
                            rsSales.getTimestamp("date").toLocalDateTime(),
                            rsSales.getDouble("total")
                    );

                    // Charger les produits rattachés
                    try (PreparedStatement psItems = con.prepareStatement(sqlItems)) {
                        psItems.setInt(1, sale.getId());
                        try (ResultSet rsItems = psItems.executeQuery()) {
                            while (rsItems.next()) {
                                sale.getItems().add(new Sale.SaleItem(
                                        rsItems.getString("name"), // Récupéré de la table products
                                        rsItems.getDouble("price"),
                                        rsItems.getInt("quantity")
                                ));
                            }
                        }
                    }
                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public void deleteSaleById(int id) {
        // Suppression sécurisée des dépendances d'abord
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement("DELETE FROM sale_items WHERE sale_id = ?");
                 PreparedStatement ps2 = con.prepareStatement("DELETE FROM sales WHERE id = ?")) {

                ps1.setInt(1, id);
                ps1.executeUpdate();
                ps2.setInt(1, id);
                ps2.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}