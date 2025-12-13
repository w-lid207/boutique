package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.StatService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;


public class DashboardController implements Initializable {

    @FXML
    private BorderPane borderPane;
    @FXML
    private Label serumCount;
    @FXML
    private Label makeupCount;
    @FXML
    private Label vitamineCount;
    @FXML
    private Label bioCount;
    @FXML
    private Label todaySaleCount;
    @FXML
    private Label revenueForToday;
    @FXML
    private Label allTimeRevenue;

    @Override
    public void initialize(URL url, ResourceBundle resource){
        StatService statService = new StatService();
        serumCount.setText(statService.getProductCount("Sérums"));
        vitamineCount.setText(statService.getProductCount("Vitamines & Suppléments"));
        bioCount.setText(statService.getProductCount("Produits Bio (Soins & Divers)"));
        makeupCount.setText(statService.getProductCount("Maquillage"));
        todaySaleCount.setText(statService.getProductCount("Sérums"));
        serumCount.setText(statService.getProductCount("Sérums"));
        revenueForToday.setText(statService.getTodayRevenue()+" DH");
        allTimeRevenue.setText(statService.getAllTimeRevnue()+" DH");
        todaySaleCount.setText(statService.getTodaySaleCount());
    }

    @FXML
    private void handleDashboardClick() {
        // Charge la page home (contenu du dashboard)
        loadPage("dashboard.fxml");
    }

    @FXML
    private void handleProductsClick() {
        loadPage("product.fxml");
    }

    @FXML
    private void handlePOSClick() {
        loadPage("pos.fxml");
    }

    @FXML
    private void handleSalesHistoryClick() {
        loadPage("sales-history.fxml");
    }

    @FXML
    private void handleAnalyticsClick() {
        loadPage("analytics.fxml");
    }

    private void loadPage(String fileName) {
        try {
            // Construire le chemin complet
            String fullPath = "/com/boutique/gestionboutique/views/" + fileName;

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fullPath));

            // Vérifier si le fichier existe
            if (loader.getLocation() == null) {
                System.err.println("❌ Fichier introuvable: " + fullPath);
                // Afficher un message dans le borderPane
                showPlaceholder(fileName);
                return;
            }

            // Charger et afficher la page
            borderPane.setCenter(loader.load());
            System.out.println("✅ Page chargée: " + fileName);

        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement: " + fileName);
            e.printStackTrace();
            showPlaceholder(fileName);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            showPlaceholder(fileName);
        }
    }

    private void showPlaceholder(String fileName) {
        // Afficher un message temporaire si le fichier n'existe pas
        javafx.scene.control.Label label = new javafx.scene.control.Label(
                "Page en développement\n" + fileName
        );
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #666; -fx-padding: 50px;");
        borderPane.setCenter(label);
    }
}