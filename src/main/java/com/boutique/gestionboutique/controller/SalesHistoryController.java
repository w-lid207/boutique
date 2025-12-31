package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.SaleService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SalesHistoryController {

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> idColumn;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, Double> totalColumn;
    @FXML private TableColumn<Sale, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalSalesCount;
    @FXML private Label todaySalesLabel;
    @FXML private Label todaySalesAmount;
    @FXML private Label averageSaleLabel;
    @FXML private Label countLabel;
    @FXML private Button refreshButton;

    private SaleService saleService;
    private ObservableList<Sale> salesList;

    @FXML
    public void initialize() {
        saleService = new SaleService();
        loadFonts();
        initializeComponents();
        loadSales();
        updateStatistics();
    }

    private void loadFonts() {
        try {
            String[] fonts = {"Regular", "Bold", "Medium", "SemiBold"};
            for (String font : fonts) {
                Font.loadFont(getClass().getResourceAsStream("/com/boutique/gestionboutique/Fonts/Poppins-" + font + ".ttf"), 12);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement polices: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        salesTable.setPlaceholder(new Label("Aucune vente trouvée"));
        setupColumns();
        setupActionColumn();
        setupSearch();
        setupRefreshButton();
    }

    private void setupColumns() {
        // ID Column
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    Label idLabel = new Label("#" + value);
                    idLabel.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 12; -fx-padding: 6 12; " +
                            "-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 13px; -fx-text-fill: #4f46e5;");
                    setGraphic(idLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Date Column
        dateColumn.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm:ss");
            return new SimpleStringProperty(data.getValue().getDate().format(formatter));
        });

        // Total Column
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f MAD", value));
                    setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 14px; -fx-text-fill: #059669;");
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox actionBox = new HBox();
            private final Button btnView = createViewButton();

            {
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Sale sale = getTableView().getItems().get(getIndex());
                    btnView.setOnAction(e -> showSaleDetails(sale));
                    if (actionBox.getChildren().isEmpty()) actionBox.getChildren().add(btnView);
                    setGraphic(actionBox);
                }
            }
        });
    }

    private Button createViewButton() {
        Button btn = new Button("👁 Voir Détails");
        String baseStyle = "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-family: 'Poppins Medium'; " +
                "-fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 8 18; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-family: 'Poppins Medium'; " +
                "-fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 8 18; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(79, 70, 229, 0.4), 8, 0, 0, 3);";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    private void loadSales() {
        List<Sale> list = saleService.getAllSales();
        salesList = FXCollections.observableArrayList(list);
        salesTable.setItems(salesList);
        updateCountLabel();
    }

    private void updateStatistics() {
        if (salesList == null || salesList.isEmpty()) {
            totalSalesLabel.setText("0 MAD");
            todaySalesLabel.setText("0");
            return;
        }

        double totalAmount = salesList.stream().mapToDouble(Sale::getTotal).sum();
        totalSalesLabel.setText(String.format("%.2f MAD", totalAmount));
        totalSalesCount.setText(salesList.size() + " vente" + (salesList.size() > 1 ? "s" : ""));

        LocalDateTime today = LocalDateTime.now();
        List<Sale> todaySales = salesList.stream()
                .filter(sale -> sale.getDate().toLocalDate().equals(today.toLocalDate()))
                .collect(Collectors.toList());

        todaySalesLabel.setText(String.valueOf(todaySales.size()));
        todaySalesAmount.setText(String.format("%.2f MAD", todaySales.stream().mapToDouble(Sale::getTotal).sum()));
        averageSaleLabel.setText(String.format("%.2f MAD", totalAmount / salesList.size()));
    }

    private void updateCountLabel() {
        if (salesList == null || salesList.isEmpty()) {
            countLabel.setText("Aucune vente");
        } else {
            countLabel.setText(salesList.size() + (salesList.size() == 1 ? " vente" : " ventes"));
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                salesTable.setItems(salesList);
            } else {
                String term = newVal.trim();
                salesTable.setItems(salesList.stream()
                        .filter(s -> String.valueOf(s.getId()).contains(term))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
            }
        });
    }

    private void setupRefreshButton() {
        String style = "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-family: 'Poppins SemiBold'; " +
                "-fx-font-size: 14px; -fx-background-radius: 25; -fx-padding: 12 30; -fx-cursor: hand;";
        refreshButton.setStyle(style);
    }

    @FXML
    private void handleRefresh() {
        javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(javafx.util.Duration.millis(500), refreshButton);
        rt.setByAngle(360);
        rt.play();
        searchField.clear();
        loadSales();
        updateStatistics();
    }

    private void showSaleDetails(Sale sale) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Vente #" + sale.getId());
        DialogPane dialogPane = dialog.getDialogPane();

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-min-width: 450;");

        Label title = new Label("Détails de la Vente #" + sale.getId());
        title.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 22px; -fx-text-fill: #6366f1;");

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;");

        Label dateLbl = new Label("📅 Date: " + sale.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        Label totalLbl = new Label("💰 Montant: " + String.format("%.2f MAD", sale.getTotal()));
        dateLbl.setStyle("-fx-font-family: 'Poppins Medium'; -fx-text-fill: #475569;");
        totalLbl.setStyle("-fx-font-family: 'Poppins Medium'; -fx-text-fill: #475569;");
        infoBox.getChildren().addAll(dateLbl, totalLbl);

        VBox productsBox = new VBox(12);
        productsBox.setPadding(new Insets(15));
        productsBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label prodHeader = new Label("Produits vendus :");
        prodHeader.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 15px; -fx-text-fill: #1e293b;");
        productsBox.getChildren().add(prodHeader);

        if (sale.getItems().isEmpty()) {
            Label empty = new Label("Aucun détail disponible.");
            empty.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-style: italic; -fx-text-fill: #94a3b8;");
            productsBox.getChildren().add(empty);
        } else {
            for (Sale.SaleItem item : sale.getItems()) {
                Label row = new Label(String.format("• %s — %.2f MAD x %d = %.2f MAD",
                        item.getName(), item.getPrice(), item.getQuantity(), item.getSubtotal()));
                row.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Poppins Regular'; -fx-font-size: 13px;");
                productsBox.getChildren().add(row);
            }
        }

        root.getChildren().addAll(title, infoBox, productsBox);
        dialogPane.setContent(root);

        ButtonType closeBtnType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeBtnType);
        Button closeBtn = (Button) dialogPane.lookupButton(closeBtnType);
        closeBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-padding: 10 30; -fx-font-family: 'Poppins SemiBold';");

        dialog.showAndWait();
    }
}