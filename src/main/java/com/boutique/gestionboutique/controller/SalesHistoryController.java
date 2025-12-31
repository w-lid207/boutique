package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.controller.Sale;
import com.boutique.gestionboutique.service.SaleService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalesHistoryController {

    @FXML private BorderPane borderPane;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> idColumn;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, Double> totalColumn;
    @FXML private TextField searchField;

    private SaleService saleService;
    private ObservableList<Sale> salesList;
    private ObservableList<Sale> filteredSalesList;

    @FXML
    public void initialize() {
        saleService = new SaleService();

        setupTableColumns();
        setupSearch();
        loadSalesData();
    }

    private void setupTableColumns() {
        // Configuration de la colonne ID
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: CENTER;");

        // Configuration de la colonne Date
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new SimpleStringProperty(cellData.getValue().getDate().format(formatter));
        });
        dateColumn.setStyle("-fx-alignment: CENTER;");

        // Configuration de la colonne Total avec MAD
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(column -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f MAD", item));

                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSalesByID(newValue);
        });
    }

    private void loadSalesData() {
        try {
            List<Sale> sales = saleService.getAllSales();
            salesList = FXCollections.observableArrayList(sales);
            filteredSalesList = FXCollections.observableArrayList(sales);
            salesTable.setItems(filteredSalesList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sales data: " + e.getMessage());
        }
    }

    private void filterSalesByID(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredSalesList = FXCollections.observableArrayList(salesList);
        } else {
            filteredSalesList = FXCollections.observableArrayList();
            for (Sale sale : salesList) {
                if (String.valueOf(sale.getId()).contains(searchText.trim())) {
                    filteredSalesList.add(sale);
                }
            }
        }
        salesTable.setItems(filteredSalesList);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadSalesData();
    }

    // Navigation methods
    @FXML
    private void handleDashboardClick() {
        navigateTo("/fxml/Dashboard.fxml");
    }

    @FXML
    private void handleProductsClick() {
        navigateTo("/fxml/Products.fxml");
    }

    @FXML
    private void handlePOSClick() {
        navigateTo("/fxml/POS.fxml");
    }

    @FXML
    private void handleAnalyticsClick() {
        navigateTo("/fxml/Analytics.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}