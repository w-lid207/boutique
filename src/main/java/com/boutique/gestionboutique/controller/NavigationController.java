package com.boutique.gestionboutique.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class NavigationController {

    @FXML
    private BorderPane borderPane;

    private final Map<String, Parent> cache = new HashMap<>();

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
            Parent view;
            FXMLLoader loader;

            if (cache.containsKey(fileName)) {
                view = cache.get(fileName);
            } else {
                String fullPath = "/com/boutique/gestionboutique/views/" + fileName;
                loader = new FXMLLoader(getClass().getResource(fullPath));
                view = loader.load();

                // Store the controller so we can refresh it later
                view.setUserData(loader.getController());
                cache.put(fileName, view);
            }
// 2. Retrieve the Controller
            Object controller = view.getUserData();

            // 3. Refresh if it is the Dashboard
            // This ensures data updates every time you click "Dashboard", even if cached.
            if (controller instanceof DashBoardController) {
                ((DashBoardController) controller).refresh();
            }
            borderPane.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
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