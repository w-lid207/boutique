package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.controller.Sale;
import com.boutique.gestionboutique.service.SaleService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
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

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> idColumn;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, Double> totalColumn;
    @FXML private TextField searchField;

    private SaleService saleService;
    private ObservableList<Sale> salesList;
    private ObservableList<Sale> filteredSalesList;
    private FilteredList<Sale> filteredSales;


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
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredSales.setPredicate(sale ->
                    newVal == null || newVal.isEmpty() ||
                            String.valueOf(sale.getId()).contains(newVal)
            );
        });
    }

    private void loadSalesData() {
        Task<List<Sale>> task = new Task<>() {
            @Override
            protected List<Sale> call() throws Exception {
                return saleService.getAllSales();
            }
        };

        task.setOnSucceeded(e -> {
            salesList = FXCollections.observableArrayList(task.getValue());
            filteredSales = new FilteredList<>(salesList, p -> true);
            salesTable.setItems(filteredSales);
        });

        new Thread(task).start();
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




    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}