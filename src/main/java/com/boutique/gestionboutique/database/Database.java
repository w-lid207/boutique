package com.boutique.gestionboutique.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public static Connection getConnection() {
        try {
            // Juste ces 2 lignes !
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/boutique",
                    "root",
                    ""
            );
        } catch (Exception e) {
            e.printStackTrace(); // 👈 IMPORTANT

            return null;
        }
    }
}
