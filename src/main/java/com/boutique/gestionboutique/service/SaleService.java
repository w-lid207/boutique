package com.boutique.gestionboutique.service;

import com.boutique.gestionboutique.controller.Sale;
import com.boutique.gestionboutique.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    // ===================== GET ALL SALES =====================
    public List<Sale> getAllSales() {

        List<Sale> sales = new ArrayList<>();

        String sql = "SELECT id, date, total FROM sales ORDER BY id DESC";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Sale sale = new Sale(
                        rs.getInt("id"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getDouble("total")
                );
                sales.add(sale);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sales;
    }

    // ===================== DELETE SALE =====================
    public void deleteSaleById(int id) {

        String sql = "DELETE FROM sales WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
