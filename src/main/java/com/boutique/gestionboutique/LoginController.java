package com.boutique.gestionboutique;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    // Méthode appelée quand on clique sur le bouton Login
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation simple
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // Validation de l'email
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address");
            return;
        }

        // Vérification des identifiants (admin/admin123 par défaut)
        if (email.equals("admin@gmail.com") && password.equals("admin123")) {
            // Login réussi, rediriger vers le dashboard
            redirectToDashboard();
        } else {
            showError("Invalid email or password");
        }
    }

    // Rediriger vers le dashboard
    private void redirectToDashboard() {
        try {
            // Charger le fichier FXML du dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("views/dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Obtenir la scène actuelle et la fenêtre
            Stage currentStage = (Stage) loginButton.getScene().getWindow();

            // Créer une nouvelle scène avec le dashboard
            Scene dashboardScene = new Scene(dashboardRoot);

            // Appliquer la scène à la fenêtre
            currentStage.setScene(dashboardScene);
            currentStage.setTitle("Dashboard");
            currentStage.setMaximized(true); // Optionnel: plein écran

            // Centrer la fenêtre
            currentStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    // Afficher un message d'erreur
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(true);
    }
}
