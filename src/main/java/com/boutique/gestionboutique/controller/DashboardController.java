package com.boutique.gestionboutique.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private void handleDashboardClick() {
        // Charge la page home (contenu du dashboard)
        loadPage("home.fxml");
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