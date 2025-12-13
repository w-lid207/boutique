package com.boutique.gestionboutique.service;

import com.boutique.gestionboutique.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatService {
    private Connection connection;
    public StatService(){
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
    public String  getProductCount(String category){
        String query = "SELECT count(*) FROM products WHERE category_id = (SELECT id FROM categories WHERE name = ?)";
        int countResult = 20;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.println("It worked");
                countResult = rs.getInt(1);
            }

        }catch (SQLException e){
            System.out.println("It DID NOT WORK");

            e.printStackTrace();
        }
        return String.valueOf(countResult);
    }
    public String getTodaySaleCount(){
        String query = "SELECT count(*) FROM sales WHERE DATE(date) = CURRENT_DATE()";
        int r = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.println("It aaaaaaaaworked");
                r = rs.getInt(1);
            }

        }catch (SQLException e){
            System.out.println("It DID NOT WORK getTodaySaleCount");

            e.printStackTrace();
        }
        return String.valueOf(r);

    }
    public String getAllTimeRevnue(){
        String query = "SELECT sum(total) FROM sales";
        double r = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.println("It aaaaaaaaworked");
                r = rs.getDouble(1);
            }

        }catch (SQLException e){
            System.out.println("It DID NOT WORK getAllTimeRevnue");

            e.printStackTrace();
        }
        return String.valueOf(r);

    }  public String getTodayRevenue(){
        String query = "SELECT sum(total) FROM sales WHERE DATE(date) = ?";
        double r = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, "CURRENT_DATE()");
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.println("It aaaaaaaaworked");
                r = rs.getDouble(1);
            }

        }catch (SQLException e){
            System.out.println("It DID NOT WORK getTodayRevenue");

            e.printStackTrace();
        }
        return String.valueOf(r);

    }
}
