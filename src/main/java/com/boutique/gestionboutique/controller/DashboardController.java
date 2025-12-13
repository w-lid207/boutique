package com.boutique.gestionboutique.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import java.net.URL;
import java.io.IOException;
import java.util.ResourceBundle;

import com.boutique.gestionboutique.service.StatService;

public class DashboardController implements Initializable{

    @FXML
    private BorderPane borderPane;
    @FXML
    private Label makeupCount;
    @FXML
    private Label serumCount;

    @FXML
    private Label vitamineCount;

    @FXML
    private Label bioCount;
    @FXML
    private Label todaySaleCount;
    @FXML
    private Label allTimeRevenue;
    @FXML
    private Label revenueForToday;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StatService ss = new StatService();
        int mC =  ss.getProductCount("Maquillage");
        int bC =  ss.getProductCount("Produits Bio (Soins & Divers)");
        int sC =  ss.getProductCount("Sérums");
        int vC =  ss.getProductCount("Vitamines & Suppléments");
        makeupCount.setText(String.valueOf(mC));
        serumCount.setText(String.valueOf(sC));
        vitamineCount.setText(String.valueOf(vC));
        bioCount.setText(String.valueOf(bC));
        todaySaleCount.setText(String.valueOf(ss.getTodaySaleCount()));
        allTimeRevenue.setText(String.valueOf(ss.getAllTimeRevnue()) + "DH");
        revenueForToday.setText(String.valueOf(ss.getTodayRevenue()) + "DH");

    }
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