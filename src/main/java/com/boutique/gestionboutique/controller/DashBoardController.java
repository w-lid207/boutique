package com.boutique.gestionboutique.controller;
import com.boutique.gestionboutique.model.Sale;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableCell;
import javafx.geometry.Insets;
import com.boutique.gestionboutique.service.SaleService;
import com.boutique.gestionboutique.service.StatService;
import com.boutique.gestionboutique.service.AnalyticService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;

public class DashBoardController implements Initializable {
    private final AnalyticService analyticService = new AnalyticService();
    private final SaleService saleService = new SaleService();
    // --- Table Components ---
    @FXML private TableView<Sale> transactionsTable;
    @FXML private TableColumn<Sale, String> orderIdCol;
    @FXML private TableColumn<Sale, String> dateCol;
    @FXML private TableColumn<Sale, String> amountCol;
    @FXML private TableColumn<Sale, Void> actionCol; // Field kept for FXML, logic ignored as requested
    @FXML
    private Label todaySaleCount;
    @FXML
    private Button btnYearly;
    @FXML
    private Button btnLast6Months;
    @FXML
    private VBox chartContainer;
    @FXML
    private Label revenueForToday;
    @FXML
    private Label lowStockItems;
    @FXML
    private Label TotalProducts;

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        setupTableColumns();
        transactionsTable.setFixedCellSize(50);

        // We can leave this empty or load initial data.
        // The NavigationController will call refresh() immediately after loading anyway.
        refresh();
    }

    // This method is public so NavigationController can call it
    public void refresh() {
        StatService statService = new StatService();

        // Fetch data (Background Thread)
        String count = statService.getTodaySaleCount();
        String revenue = statService.getTodayRevenue();
        String lowStock = statService.getLowStockItems();
        String totalProd = statService.getProductCount();
        todaySaleCount.setText(count);
        revenueForToday.setText(revenue + " DH");
        lowStockItems.setText(lowStock);
        TotalProducts.setText(totalProd);
        monthlyRevenueChart();
        loadRecentSales();
    }
    @FXML
    private void handleSalesHistoryClick(){
        NavigationController.navigationController.handleSalesHistoryClick();
    }
    @FXML
    private void monthlyRevenueChart(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 40000, 2000);
        yAxis.setMinorTickVisible(false);
        xAxis.setCategories(observableArrayList(
                List.of("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        ));


        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Double> data = analyticService.monthlyData();

        data.forEach((a,b)->{
            series.getData().add(new XYChart.Data<>(a, b));

        });

        barChart.getData().add(series);
        barChart.setPrefHeight(250);
        barChart.setMinHeight(250);
        // Disable the default animation
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
        barChart.setBarGap(10);
        barChart.setCategoryGap(20);
        btnLast6Months.getStyleClass().remove("active");
        btnYearly.getStyleClass().remove("active");
        btnYearly.getStyleClass().add("active");
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        chartContainer.getChildren().clear();
        chartContainer.getChildren().add(barChart);

    }
    @FXML
    private void monthlyRevenueChart6(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 40000, 2000);
        yAxis.setMinorTickVisible(false);
        xAxis.setCategories(observableArrayList(
                List.of("Jan","Feb","Mar","Apr","May","Jun")
        ));


        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        barChart.setLegendVisible(false); // Add this line!

        Map<String, Double> data = analyticService.monthlyData6();

        data.forEach((a,b)->{
            series.getData().add(new XYChart.Data<>(a, b));

        });

        barChart.getData().add(series);
        barChart.setPrefHeight(250);
        barChart.setMinHeight(250);
        barChart.setLegendVisible(false);
        // Disable the default animation
        barChart.setAnimated(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setBarGap(10);
        barChart.setCategoryGap(20);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        chartContainer.getChildren().clear();
        chartContainer.getChildren().add(barChart);


    }

    @FXML
    private void handleChartToggle(ActionEvent event) {
        Button clickedBtn = (Button) event.getSource();

        // 1. Remove 'active' class from all buttons
        btnLast6Months.getStyleClass().remove("active");
        btnYearly.getStyleClass().remove("active");
        System.out.println("YOOOOOOOOOOO");

        // 2. Add 'active' class to the clicked button
        clickedBtn.getStyleClass().add("active");

        // 3. Logic to update chart data goes here...
        if (clickedBtn == btnYearly) {
            monthlyRevenueChart();
        } else {
            monthlyRevenueChart6();
        }
    }

    private void setupTableColumns() {
        orderIdCol.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty("#" + cellData.getValue().getId());
            } catch (Exception e) {
                System.err.println("Error in orderIdCol: " + e.getMessage());
                return new SimpleStringProperty("ERROR");
            }
        });

        // 2. Date: Format to readable string
        dateCol.setCellValueFactory(cellData -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getDate().format(formatter));
            } catch (Exception e) {
                System.err.println("Error in dateCol: " + e.getMessage());
                return new SimpleStringProperty("ERROR");
            }
        });

        // 3. Amount: Format with "DH"
        amountCol.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(String.format("%.2f DH", cellData.getValue().getTotal()));
            } catch (Exception e) {
                System.err.println("Error in amountCol: " + e.getMessage());
                return new SimpleStringProperty("ERROR");
            }
        });
        // 4. Actions Column: Add "View Details" button
        actionCol.setCellFactory(col -> new TableCell<Sale, Void>() {
            private final Button viewBtn = new Button("View Details");

            {
                // Style the button
                viewBtn.setStyle(
                        "-fx-background-color: #BEC400; " +
                                "-fx-text-fill: #2C2C2C; " +
                                "-fx-background-radius: 5; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 10 10; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold;"
                );




                // Handle button click
                viewBtn.setOnAction(event -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    showSaleDetails(sale);
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
    }
    private void showSaleDetails(Sale sale) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Vente #" + sale.getId());
        DialogPane dialogPane = dialog.getDialogPane();

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-min-width: 450;");

        Label title = new Label("Détails de la Vente #" + sale.getId());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #BEC400;");

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;");

        Label dateLbl = new Label("📅 Date: " + sale.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        Label totalLbl = new Label("💰 Montant: " + String.format("%.2f DH", sale.getTotal()));
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        totalLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        infoBox.getChildren().addAll(dateLbl, totalLbl);

        VBox productsBox = new VBox(12);
        productsBox.setPadding(new Insets(15));
        productsBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label prodHeader = new Label("Produits vendus :");
        prodHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        productsBox.getChildren().add(prodHeader);

        if (sale.getItems().isEmpty()) {
            Label empty = new Label("Aucun détail disponible.");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8;");
            productsBox.getChildren().add(empty);
        } else {
            for (Sale.SaleItem item : sale.getItems()) {
                Label row = new Label(String.format("• %s — %.2f DH x %d = %.2f DH",
                        item.getName(), item.getPrice(), item.getQuantity(), item.getSubtotal()));
                row.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                productsBox.getChildren().add(row);
            }
        }

        root.getChildren().addAll(title, infoBox, productsBox);
        dialogPane.setContent(root);

        ButtonType closeBtnType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeBtnType);
        Button closeBtn = (Button) dialogPane.lookupButton(closeBtnType);
        closeBtn.setStyle(
                "-fx-background-color: #BEC400; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 10 30; " +
                        "-fx-font-weight: bold;"
        );

        dialog.showAndWait();
    }
    private void loadRecentSales() {
        // Fetch all sales from database
        List<Sale> allSales = saleService.getAllSales();

        System.out.println("DEBUG: Total sales from DB: " + allSales.size());

        // Get only the 5 most recent ones
        List<Sale> recentFive = allSales.stream()
                .limit(5)
                .collect(Collectors.toList());

        // CRITICAL: Run on JavaFX thread
        Platform.runLater(() -> {
            transactionsTable.setItems(FXCollections.observableArrayList(recentFive));


            transactionsTable.setPrefHeight(350);
            transactionsTable.setMinHeight(350);


            if (!recentFive.isEmpty()) {
                System.out.println("DEBUG: First item test: " + recentFive.get(0).getId());
            }

            transactionsTable.refresh();
        });

        System.out.println("Dashboard: Loaded " + recentFive.size() + " recent orders.");
    }

}