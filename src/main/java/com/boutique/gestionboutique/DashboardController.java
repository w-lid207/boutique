package com.boutique.gestionboutique;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private VBox overviewPane;
    @FXML private VBox usersPane;
    @FXML private VBox productsPane;
    @FXML private VBox ordersPane;
    @FXML private VBox analyticsPane;
    @FXML private VBox settingsPane;

    @FXML private Button overviewBtn;
    @FXML private Button usersBtn;
    @FXML private Button productsBtn;
    @FXML private Button ordersBtn;
    @FXML private Button analyticsBtn;
    @FXML private Button settingsBtn;

    @FXML
    private void initialize() {
        // Initialiser avec la page Overview par défaut
        showOverview();
    }

    // Navigation methods
    @FXML
    private void showOverview() {
        setActivePage(overviewPane, overviewBtn);
    }

    @FXML
    private void showUsers() {
        setActivePage(usersPane, usersBtn);
    }

    @FXML
    private void showProducts() {
        setActivePage(productsPane, productsBtn);
    }

    @FXML
    private void showOrders() {
        setActivePage(ordersPane, ordersBtn);
    }

    @FXML
    private void showAnalytics() {
        setActivePage(analyticsPane, analyticsBtn);
    }

    @FXML
    private void showSettings() {
        setActivePage(settingsPane, settingsBtn);
    }

    // Version SIMPLE de logout
    @FXML
    private void handleLogout() throws IOException {
        Parent loginRoot = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage) overviewPane.getScene().getWindow();
        stage.setScene(new Scene(loginRoot));
        stage.setTitle("Login");
    }

    // Méthodes utilitaires
    private void setActivePage(VBox page, Button button) {
        // Masquer toutes les pages
        overviewPane.setVisible(false);
        usersPane.setVisible(false);
        productsPane.setVisible(false);
        ordersPane.setVisible(false);
        analyticsPane.setVisible(false);
        settingsPane.setVisible(false);

        // Désactiver tous les boutons
        removeActiveClass(overviewBtn);
        removeActiveClass(usersBtn);
        removeActiveClass(productsBtn);
        removeActiveClass(ordersBtn);
        removeActiveClass(analyticsBtn);
        removeActiveClass(settingsBtn);

        // Afficher la page sélectionnée et activer le bouton
        page.setVisible(true);
        if (button != null) {
            button.getStyleClass().add("active");
        }
    }

    private void removeActiveClass(Button button) {
        button.getStyleClass().remove("active");
    }

    private void showAlert(String type, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}