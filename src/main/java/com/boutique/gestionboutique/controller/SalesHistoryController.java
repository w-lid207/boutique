package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.SaleService;
import com.boutique.gestionboutique.controller.Sale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.StageStyle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;

public class SalesHistoryController {

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> idColumn;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, Double> totalColumn;
    @FXML private TableColumn<Sale, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel, totalSalesCount, todaySalesLabel, todaySalesAmount, averageSaleLabel, countLabel;
    @FXML private Button refreshButton;
    @FXML private Pagination pagination;

    private SaleService saleService;
    private ObservableList<Sale> salesList = FXCollections.observableArrayList();
    private final int ROWS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        saleService = new SaleService();
        setupColumns();
        setupActionColumn();
        pagination.setPageFactory(this::createPage);
        loadSales();
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilter(newVal));
    }


    @FXML
    private void handleExportCSV() {
        // 1. Ouvrir une boîte de dialogue pour choisir l'emplacement du fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'export CSV");
        fileChooser.setInitialFileName("export_ventes_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(pagination.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // 2. Écrire l'en-tête du CSV
                writer.println("ID;Date;Heure;Montant (MAD)");

                // 3. Parcourir la liste complète des ventes (pas seulement la page actuelle)
                for (Sale sale : salesList) {
                    String date = sale.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    String heure = sale.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    writer.println(String.format("%d;%s;%s;%.2f",
                            sale.getId(),
                            date,
                            heure,
                            sale.getTotal()
                    ));
                }

                // Message de succès
                System.out.println("Exportation réussie dans : " + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                // Optionnel : Afficher une alerte d'erreur à l'utilisateur
            }
        }
    }
    private Node createPage(int pageIndex) {
        if (salesList.isEmpty()) return new VBox(new Label("Aucune donnée"));
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, salesList.size());
        salesTable.setItems(FXCollections.observableArrayList(salesList.subList(fromIndex, toIndex)));
        salesTable.setFixedCellSize(50);
        salesTable.prefHeightProperty().bind(salesTable.fixedCellSizeProperty().multiply(ROWS_PER_PAGE + 1.1));
        return new VBox(salesTable);
    }

    private void loadSales() {
        List<Sale> list = saleService.getAllSales();
        salesList.setAll(list);
        updatePaginationUI();
        updateStatistics();
    }

    private void applyFilter(String query) {
        List<Sale> filtered = saleService.getAllSales().stream()
                .filter(s -> String.valueOf(s.getId()).contains(query))
                .collect(Collectors.toList());
        salesList.setAll(filtered);
        updatePaginationUI();
    }

    private void updatePaginationUI() {
        int pageCount = (int) Math.ceil((double) salesList.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        countLabel.setText(salesList.size() + " ventes");
    }

    private void updateStatistics() {
        // Si la liste est vide, on remet tout à zéro
        if (salesList == null || salesList.isEmpty()) {
            totalSalesLabel.setText("0.00 MAD");
            totalSalesCount.setText("0 ventes"); // Correction ici
            todaySalesLabel.setText("0");
            todaySalesAmount.setText("0.00 MAD");
            averageSaleLabel.setText("0.00 MAD");
            return;
        }

        // 1. Calcul du Total Global
        double totalAmount = salesList.stream().mapToDouble(Sale::getTotal).sum();
        totalSalesLabel.setText(String.format("%.2f MAD", totalAmount));

        // CORRECTION DU BUG "0 ventes" :
        // On utilise salesList.size() pour afficher le nombre réel de transactions
        int totalCount = salesList.size();
        totalSalesCount.setText(totalCount + (totalCount <= 1 ? " vente" : " ventes"));

        // 2. Calcul pour Aujourd'hui
        LocalDate today = LocalDate.now();
        List<Sale> todaySales = salesList.stream()
                .filter(sale -> sale.getDate().toLocalDate().equals(today))
                .collect(Collectors.toList());

        double todayAmount = todaySales.stream().mapToDouble(Sale::getTotal).sum();
        todaySalesLabel.setText(String.valueOf(todaySales.size()));
        todaySalesAmount.setText(String.format("%.2f MAD", todayAmount));

        // 3. Calcul de la Moyenne
        averageSaleLabel.setText(String.format("%.2f MAD", totalAmount / totalCount));
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) setGraphic(null);
                else {
                    Label lbl = new Label("#" + value);
                    lbl.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-padding: 4 12; -fx-text-fill: #3d4a4d; -fx-font-weight: bold;");
                    setGraphic(lbl); setAlignment(Pos.CENTER);
                }
            }
        });

        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm:ss"))));

        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) setText(null);
                else {
                    setText(String.format("%.2f MAD", value));
                    setStyle("-fx-text-fill: #BEC400; -fx-font-weight: bold;"); setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("👁 Voir Détails");
            { btn.setStyle("-fx-background-color: #BEC400; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;"); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setGraphic(null);
                else {
                    btn.setOnAction(e -> showSaleDetails((Sale) getTableRow().getItem()));
                    setGraphic(btn); setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void showSaleDetails(Sale sale) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane pane = dialog.getDialogPane();
        pane.setGraphic(null); // Supprime l'icône système bleue

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-min-width: 450;");

        // Titre de la fenêtre
        Label title = new Label("Détails de la Vente #" + sale.getId());
        title.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 22px; -fx-text-fill: #BEC400;");

        // Bloc d'information principale (Date et Montant)
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 12;");

        Label dateLbl = new Label("📅 Date: " + sale.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        Label totalLbl = new Label("💰 Montant: " + String.format("%.2f MAD", sale.getTotal()));

        dateLbl.setStyle("-fx-font-family: 'Poppins Medium'; -fx-text-fill: #3d4a4d;");
        totalLbl.setStyle("-fx-font-family: 'Poppins Bold'; -fx-text-fill: #BEC400;");
        infoBox.getChildren().addAll(dateLbl, totalLbl);

        // --- SECTION PRODUITS VENDUS ---
        VBox productsList = new VBox(12);
        productsList.setPadding(new Insets(10));
        productsList.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");

        Label prodHeader = new Label("Produits vendus :");
        prodHeader.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 15px; -fx-text-fill: #3d4a4d;");
        productsList.getChildren().add(prodHeader);

        // Boucle pour afficher chaque produit de la vente
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            for (Sale.SaleItem item : sale.getItems()) {
                // Format : Nom du produit — Prix x Quantité = Sous-total
                String text = String.format("• %s — %.2f MAD x %d = %.2f MAD",
                        item.getName(), item.getPrice(), item.getQuantity(), item.getSubtotal());

                Label productRow = new Label(text);
                productRow.setStyle("-fx-text-fill: #475569; -fx-font-family: 'Poppins Regular'; -fx-font-size: 13px;");
                productRow.setWrapText(true);
                productsList.getChildren().add(productRow);
            }
        } else {
            Label emptyLabel = new Label("Aucun produit enregistré.");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            productsList.getChildren().add(emptyLabel);
        }

        // Assemblage final
        root.getChildren().addAll(title, infoBox, productsList);
        pane.setContent(root);

        // Bouton Fermer stylisé en vert #6d8c6d
        ButtonType closeBtnType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().add(closeBtnType);
        Button closeBtn = (Button) pane.lookupButton(closeBtnType);
        closeBtn.setStyle("-fx-background-color: #BEC400; -fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-padding: 8 25; -fx-cursor: hand; -fx-font-family: 'Poppins Medium';");

        dialog.showAndWait();
    }

    @FXML private void handleRefresh() { loadSales(); }
}