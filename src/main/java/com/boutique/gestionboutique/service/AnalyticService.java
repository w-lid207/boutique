package com.boutique.gestionboutique.service;
import com.boutique.gestionboutique.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AnalyticService {
    private Connection connection;
    public AnalyticService(){
        connection = Database.getConnection();
    }
    public Map<String, Double> chartData(){
            Map<String, Double> data = new HashMap<String, Double>();

            String query = "SELECT CONCAT(DATE_FORMAT(date, '%b'), ' ', DAY(date)), total FROM sales WHERE date >=  CURDATE() - INTERVAL 30 DAY";
            try {
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet reseultSet = pstmt.executeQuery();
                while(reseultSet.next()){
                    data.put(reseultSet.getString(1), reseultSet.getDouble(2));
                }
            }catch ( SQLException e){
                e.printStackTrace();
            }








            return data;
    }
    public Map<String, Double> monthlyData(){
            Map<String, Double> data = new HashMap<String, Double>();

            String query = "SELECT  DATE_FORMAT(date, '%b'), sum(total) FROM sales WHERE  year(date) = year(CURRENT_DATE()) GROUP BY month(date)";
            try {
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet reseultSet = pstmt.executeQuery();
                while(reseultSet.next()){
                    data.put(reseultSet.getString(1), reseultSet.getDouble(2));
                }
            }catch ( SQLException e){
                e.printStackTrace();
            }








            return data;
    }
    public Map<String, Double> monthlyData6(){
            Map<String, Double> data = new HashMap<String, Double>();

            String query = "SELECT DATE_FORMAT(date, '%b') AS month, SUM(total) FROM sales WHERE date >= DATE_SUB(LAST_DAY(CURRENT_DATE()), INTERVAL 6 MONTH) GROUP BY YEAR(date), MONTH(date) ORDER BY date ASC;";
            try {
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet reseultSet = pstmt.executeQuery();
                while(reseultSet.next()){
                    data.put(reseultSet.getString(1), reseultSet.getDouble(2));
                }
            }catch ( SQLException e){
                e.printStackTrace();
            }








            return data;
    }
    public Map<String, Double> bestSellProData(){
            Map<String, Double> data = new HashMap<String, Double>();

            String query = "SELECT p.name, SUM(o.quantity) AS total_sold, ROUND(SUM(o.quantity)/t.total*100,2) AS percentage FROM sale_items o JOIN products p ON o.product_id = p.id JOIN (SELECT SUM(quantity) AS total FROM sale_items) t GROUP BY p.id ORDER BY total_sold DESC LIMIT 5";
            try {
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet reseultSet = pstmt.executeQuery();
                while(reseultSet.next()){
                    data.put(reseultSet.getString(1), reseultSet.getDouble(2));
                }
            }catch ( SQLException e){
                e.printStackTrace();
            }








            return data;
    }
    public Map<String, Double> salesByCategData(){
            Map<String, Double> data = new HashMap<String, Double>();

            String query = "SELECT c.name, SUM(o.price * o.quantity) AS total_sold FROM sale_items o JOIN products p ON o.product_id = p.id JOIN categories c ON c.id = p.category_id  GROUP BY p.category_id";
            try {
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet reseultSet = pstmt.executeQuery();
                while(reseultSet.next()){
                    data.put(reseultSet.getString(1), reseultSet.getDouble(2));
                }
            }catch ( SQLException e){
                e.printStackTrace();
            }








            return data;
    }
}
