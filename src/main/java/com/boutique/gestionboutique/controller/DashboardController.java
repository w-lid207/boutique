package com.boutique.gestionboutique.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private BorderPane borderPane;

    /**
     * Charge le fichier product.fxml au clic sur le bouton Products
     */
    @FXML
    private void handleProductsClick(ActionEvent event) {
        loadPage("com/boutique/gestionboutique/views/product.fxml");
    }

    /**
     * Charge le Dashboard
     */
    @FXML
    private void handleDashboardClick(ActionEvent event) {
        loadPage("com/boutique/gestionboutique/views/dashboard.fxml");
    }

    /**
     * Charge une page FXML dans le centre du BorderPane
     */
    private void loadPage(String fxmlFile) {
        try {
            // Chemin absolu depuis resources
            URL resource = getClass().getResource("/" + fxmlFile);

            if (resource == null) {
                System.err.println("❌ Fichier non trouvé: /" + fxmlFile);
                System.err.println("Essai avec chemin alternatif...");
                resource = getClass().getClassLoader().getResource(fxmlFile);
            }

            if (resource == null) {
                System.err.println("❌ Fichier introuvable: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node page = loader.load();
            borderPane.setCenter(page);
            System.out.println("✅ Page chargée: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}