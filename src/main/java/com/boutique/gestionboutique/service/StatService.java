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
    public String  getProductCount(){
        String query = "SELECT count(*) FROM products ";
        int countResult = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
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
    public String  getLowStockItems(){
        String query = "SELECT count(*) FROM products WHERE quantity <= 50 ";
        int countResult = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
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
        return String.valueOf(String.format("%.2f",r));

    }
    public String getTodayRevenue(){
        String query = "SELECT sum(total) FROM sales WHERE DATE(date) = CURRENT_DATE()";
        double r = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.println("It aaaaaaaaworked");
                r = rs.getDouble(1);
            }

        }catch (SQLException e){
            System.out.println("It DID NOT WORK getTodayRevenue");

            e.printStackTrace();
        }
        return String.valueOf(String.format("%.2f",r));

    }
    public String getMonthRevenue(){
        String query = "SELECT sum(total) FROM sales WHERE MONTH(date) = MONTH(CURRENT_DATE())";
        double r = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                System.out.println("It aaaaaaaaworked");
                r = rs.getDouble(1);
            }

        }catch (SQLException e){
            System.out.println("It DID NOT WORK getTodayRevenue");

            e.printStackTrace();
        }
        return String.valueOf(String.format("%.2f",r));

    }
    public String  getTotalProductCount(){
        String query = "SELECT count(*) FROM products";
        int countResult = 20;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
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


    public String getSalesComparisonPercentage() {
        String query = """
        WITH yesterday_sales AS (
            SELECT COALESCE(SUM(total), 0) AS total
            FROM sales
            WHERE DATE(date) = CURRENT_DATE() - INTERVAL 1 DAY
        ),
        today_sales AS (
            SELECT COALESCE(SUM(total), 0) AS total
            FROM sales
            WHERE DATE(date) = CURRENT_DATE()
        )
        SELECT 
            y.total AS yesterday_total,
            t.total AS today_total,
            (t.total - y.total) AS difference,
            CASE 
                WHEN y.total = 0 AND t.total > 0 THEN 100.0
                WHEN y.total = 0 AND t.total = 0 THEN 0.0
                ELSE ROUND(((t.total - y.total) / y.total) * 100, 2)
            END AS percentage_change
        FROM yesterday_sales y, today_sales t
    """;

        String result = "0.00";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double percentageChange = rs.getDouble("percentage_change");
                result = String.format("%+.2f", percentageChange); // +/- sign included
                System.out.println("Sales comparison worked - Percentage: " + result + "%");
            }
        } catch (SQLException e) {
            System.out.println("It DID NOT WORK getSalesComparisonPercentage");
            e.printStackTrace();
        }
        return result;
    }
    public String getMonthlyComparisonPercentage() {
        String query = """
        WITH last_month_sales AS (
            SELECT COALESCE(SUM(total), 0) AS total
            FROM sales
            WHERE YEAR(date) = YEAR(CURRENT_DATE() - INTERVAL 1 MONTH)
              AND MONTH(date) = MONTH(CURRENT_DATE() - INTERVAL 1 MONTH)
        ),
        current_month_sales AS (
            SELECT COALESCE(SUM(total), 0) AS total
            FROM sales
            WHERE YEAR(date) = YEAR(CURRENT_DATE())
              AND MONTH(date) = MONTH(CURRENT_DATE())
        )
        SELECT 
            l.total AS last_month_total,
            c.total AS current_month_total,
            (c.total - l.total) AS difference,
            CASE 
                WHEN l.total = 0 AND c.total > 0 THEN 100.0
                WHEN l.total = 0 AND c.total = 0 THEN 0.0
                ELSE ROUND(((c.total - l.total) / l.total) * 100, 2)
            END AS percentage_change
        FROM last_month_sales l, current_month_sales c
    """;

        String result = "0.00";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double percentageChange = rs.getDouble("percentage_change");
                result = String.format("%+.2f", percentageChange); // +/- sign included
                System.out.println("Monthly comparison worked - Percentage: " + result + "%");
            }
        } catch (SQLException e) {
            System.out.println("It DID NOT WORK getMonthlyComparisonPercentage");
            e.printStackTrace();
        }
        return result;
    }
}
