package com.boutique.gestionboutique.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    // Quand on clique sur "Login"
    @FXML
    private void handleLogin() {
        // Récupérer ce que l'utilisateur a écrit
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Vérifier si les champs sont vides
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Vérifier si c'est un email valide
        if (!email.contains("@")) {
            showError("Email invalide");
            return;
        }

        // Vérifier si les identifiants sont corrects
        if (email.equals("admin@gmail.com") && password.equals("admin123")) {
            // ✅ Login réussi
            goToDashboard();
        } else {
            // ❌ Login échoué
            showError("Email ou mot de passe incorrect");
        }
    }

    // Aller au dashboard après un login réussi
    private void goToDashboard() {
        try {
            // Charger la page dashboard.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/boutique/gestionboutique/views/MainLayout.fxml"));
            Parent dashboard = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Changer la scène pour afficher le dashboard
            Scene scene = new Scene(dashboard);
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.setMaximized(true);

        } catch (Exception e) {
            showError("Erreur de chargement");
        }
    }

    // Afficher un message d'erreur en rouge
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);
    }
}