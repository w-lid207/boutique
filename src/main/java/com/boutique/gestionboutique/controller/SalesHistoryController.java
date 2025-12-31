package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.SaleService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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

        // Charger les polices
        loadFonts();

        // Initialiser les composants
        initializeComponents();
        loadSales();
        updateStatistics();
    }

    private void loadFonts() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/com/boutique/gestionboutique/Fonts/Poppins-Regular.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/com/boutique/gestionboutique/Fonts/Poppins-Bold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/com/boutique/gestionboutique/Fonts/Poppins-Medium.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/com/boutique/gestionboutique/Fonts/Poppins-SemiBold.ttf"), 12);
        } catch (Exception e) {
            System.err.println("Erreur chargement polices: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        setupTableStyle();
        setupColumns();
        setupActionColumn();
        setupSearch();
        setupRefreshButton();
    }

    private void setupTableStyle() {
        salesTable.setPlaceholder(new Label("Aucune vente trouvée"));
    }

    private void setupColumns() {
        // ID Column
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label idLabel = new Label("#" + value);
                    idLabel.setStyle(
                            "-fx-background-color: #e0e7ff; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-padding: 6 12; " +
                                    "-fx-font-family: 'Poppins SemiBold'; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-text-fill: #4f46e5;"
                    );
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

        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(value);
                    setStyle(
                            "-fx-font-family: 'Poppins Regular'; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-text-fill: #475569;"
                    );
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Total Column
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f MAD", value));
                    setStyle(
                            "-fx-font-family: 'Poppins Bold'; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-text-fill: #059669;"
                    );
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox actionBox = new HBox(8);
            private final Button btnView = createViewButton();
            private final Button btnDelete = createDeleteButton();

            {
                actionBox.setAlignment(Pos.CENTER);
                actionBox.setStyle("-fx-padding: 5;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (actionBox.getChildren().isEmpty()) {
                        actionBox.getChildren().addAll(btnView, btnDelete);
                    }

                    Sale sale = getTableView().getItems().get(getIndex());

                    btnView.setOnAction(e -> showSaleDetails(sale));
                    btnDelete.setOnAction(e -> showDeleteDialog(sale));

                    setGraphic(actionBox);
                }
            }
        });
    }

    private Button createViewButton() {
        Button btn = new Button("👁️ Voir");
        btn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 5, 0, 0, 2);"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2563eb; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.5), 8, 0, 0, 3);"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 5, 0, 0, 2);"
        ));

        btn.setTooltip(new Tooltip("Voir les détails de la vente"));
        return btn;
    }

    private Button createDeleteButton() {
        Button btn = new Button("🗑️ Suppr");
        btn.setStyle(
                "-fx-background-color: #ef4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 5, 0, 0, 2);"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #dc2626; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.5), 8, 0, 0, 3);"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #ef4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 5, 0, 0, 2);"
        ));

        btn.setTooltip(new Tooltip("Supprimer cette vente"));
        return btn;
    }

    private void loadSales() {
        List<Sale> list = saleService.getAllSales();
        salesList = FXCollections.observableArrayList(list);
        salesTable.setItems(salesList);

        // Mettre à jour le compteur
        updateCountLabel();
    }

    private void updateStatistics() {
        if (salesList == null || salesList.isEmpty()) {
            totalSalesLabel.setText("0 MAD");
            totalSalesCount.setText("0 ventes");
            todaySalesLabel.setText("0");
            todaySalesAmount.setText("0 MAD");
            averageSaleLabel.setText("0 MAD");
            return;
        }

        // Calculer le total des ventes
        double totalAmount = salesList.stream()
                .mapToDouble(Sale::getTotal)
                .sum();
        totalSalesLabel.setText(String.format("%.2f MAD", totalAmount));
        totalSalesCount.setText(salesList.size() + " vente" + (salesList.size() > 1 ? "s" : ""));

        // Compter les ventes d'aujourd'hui
        LocalDateTime today = LocalDateTime.now();
        List<Sale> todaySales = salesList.stream()
                .filter(sale -> sale.getDate().toLocalDate().equals(today.toLocalDate()))
                .collect(Collectors.toList());

        long todayCount = todaySales.size();
        double todayAmount = todaySales.stream()
                .mapToDouble(Sale::getTotal)
                .sum();

        todaySalesLabel.setText(String.valueOf(todayCount));
        todaySalesAmount.setText(String.format("%.2f MAD", todayAmount));

        // Calculer la moyenne
        double average = totalAmount / salesList.size();
        averageSaleLabel.setText(String.format("%.2f MAD", average));
    }

    private void updateCountLabel() {
        if (salesList == null) {
            countLabel.setText("0 ventes");
            return;
        }

        int count = salesList.size();
        if (count == 0) {
            countLabel.setText("Aucune vente");
        } else if (count == 1) {
            countLabel.setText("1 vente");
        } else {
            countLabel.setText(count + " ventes");
        }
    }

    private void setupSearch() {
        // Écoute des changements dans le champ de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Si le champ est vide, on réaffiche toutes les ventes
            if (newVal == null || newVal.trim().isEmpty()) {
                salesTable.setItems(salesList);
            } else {
                String searchTerm = newVal.trim(); // Supprimer espaces inutiles
                ObservableList<Sale> filtered = FXCollections.observableArrayList();

                for (Sale sale : salesList) {
                    // Conversion de l'ID en chaîne et recherche par "contains"
                    if (String.valueOf(sale.getId()).contains(searchTerm)) {
                        filtered.add(sale);
                    }
                }

                // Affichage des résultats filtrés
                salesTable.setItems(filtered);
            }
        });
    }


    private void setupRefreshButton() {
        refreshButton.setOnMouseEntered(e -> {
            refreshButton.setStyle(
                    "-fx-background-color: #4f46e5; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Poppins SemiBold'; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 25; " +
                            "-fx-padding: 12 30; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(79,70,229,0.4), 10, 0, 0, 4);"
            );
        });

        refreshButton.setOnMouseExited(e -> {
            refreshButton.setStyle(
                    "-fx-background-color: gray; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-family: 'Poppins SemiBold'; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 25; " +
                            "-fx-padding: 12 30; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 8, 0, 0, 3);"
            );
        });
    }

    @FXML
    private void handleRefresh() {
        // Animation de rotation
        refreshButton.setRotate(0);
        javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(
                javafx.util.Duration.millis(500), refreshButton);
        rt.setByAngle(360);
        rt.play();

        // Rafraîchir les données
        searchField.clear();
        loadSales();
        updateStatistics();
    }

    // ================= DIALOGUES =================

    private void showSaleDetails(Sale sale) {
        // Créer un dialogue personnalisé
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Vente");

        // Style du dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 25, 0, 0, 5);"
        );

        // En-tête
        VBox header = new VBox(10);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #4f46e5, #6366f1); " +
                        "-fx-background-radius: 20 20 0 0; " +
                        "-fx-padding: 25;"
        );

        Label title = new Label("📄 Détails de la Vente");
        title.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 24px; -fx-text-fill: white;");

        Label idLabel = new Label("#" + sale.getId());
        idLabel.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 18px; -fx-text-fill: rgba(255,255,255,0.9);");

        header.getChildren().addAll(title, idLabel);
        dialogPane.setHeader(header);

        // Contenu
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white;");

        // Section montant
        HBox amountSection = new HBox(20);
        amountSection.setAlignment(Pos.CENTER);

        VBox amountBox = new VBox(5);
        amountBox.setAlignment(Pos.CENTER);
        Label amountLabel = new Label("MONTANT TOTAL");
        amountLabel.setStyle("-fx-font-family: 'Poppins Medium'; -fx-font-size: 14px; -fx-text-fill: #64748b;");
        Label amountValue = new Label(String.format("%.2f MAD", sale.getTotal()));
        amountValue.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 32px; -fx-text-fill: #059669;");
        amountBox.getChildren().addAll(amountLabel, amountValue);

        amountSection.getChildren().add(amountBox);

        // Section détails
        VBox detailsSection = new VBox(15);
        detailsSection.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20;"
        );

        Label detailsTitle = new Label("📋 Informations de la transaction");
        detailsTitle.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 16px; -fx-text-fill: #1e293b;");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);

        VBox details = new VBox(12);
        details.getChildren().addAll(
                createDetailRow("🔢 Numéro de vente:", "#" + sale.getId()),
                createDetailRow("📅 Date:", sale.getDate().format(dateFormatter)),
                createDetailRow("⏰ Heure:", sale.getDate().format(timeFormatter)),
                createDetailRow("🗓️ Jour:", sale.getDate().format(dayFormatter)),
                createDetailRow("💰 Montant:", String.format("%.2f MAD", sale.getTotal()))
        );

        detailsSection.getChildren().addAll(detailsTitle, new Separator(), details);

        content.getChildren().addAll(amountSection, detailsSection);
        dialogPane.setContent(content);

        // Boutons
        ButtonType printButton = new ButtonType("🖨️ Imprimer", ButtonBar.ButtonData.OTHER);
        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(printButton, closeButton);

        // Styliser les boutons
        Button printBtn = (Button) dialogPane.lookupButton(printButton);
        printBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 25; " +
                        "-fx-cursor: hand;"
        );

        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.setStyle(
                "-fx-background-color: #f1f5f9; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 25; " +
                        "-fx-cursor: hand;"
        );

        dialog.showAndWait();
    }

    private void showDeleteDialog(Sale sale) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");

        // Style du dialogue
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 25, 0, 0, 5);"
        );

        // En-tête
        VBox header = new VBox(10);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #ef4444, #f87171); " +
                        "-fx-background-radius: 20 20 0 0; " +
                        "-fx-padding: 25;"
        );

        Label title = new Label("⚠️ Confirmation de suppression");
        title.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 24px; -fx-text-fill: white;");

        Label warning = new Label("Cette action est irréversible");
        warning.setStyle("-fx-font-family: 'Poppins Medium'; -fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");

        header.getChildren().addAll(title, warning);
        dialogPane.setHeader(header);

        // Contenu
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));

        // Message principal
        Label message = new Label("Êtes-vous sûr de vouloir supprimer cette vente ?");
        message.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 18px; -fx-text-fill: #1e293b;");

        // Informations de la vente
        VBox saleInfo = new VBox(20);
        saleInfo.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20;"
        );

        // Section ID
        HBox idSection = new HBox(10);
        idSection.setAlignment(Pos.CENTER);
        Label idLabel = new Label("Vente #");
        idLabel.setStyle("-fx-font-family: 'Poppins Medium'; -fx-font-size: 16px; -fx-text-fill: #dc2626;");
        Label idValue = new Label(String.valueOf(sale.getId()));
        idValue.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 28px; -fx-text-fill: #dc2626;");
        idSection.getChildren().addAll(idLabel, idValue);

        // Détails
        VBox details = new VBox(10);
        details.getChildren().addAll(
                createDetailRow("📅 Date:", sale.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                createDetailRow("⏰ Heure:", sale.getDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                createDetailRow("💰 Montant:", String.format("%.2f MAD", sale.getTotal()))
        );

        saleInfo.getChildren().addAll(idSection, new Separator(), details);

        // Avertissement
        Label caution = new Label("⚠️ Cette action ne peut pas être annulée. Les données seront définitivement supprimées.");
        caution.setStyle("-fx-font-family: 'Poppins Light'; -fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-wrap-text: true;");

        content.getChildren().addAll(message, saleInfo, caution);
        dialogPane.setContent(content);

        alert.setHeaderText("Supprimer la vente #" + sale.getId());

        // Boutons
        ButtonType deleteButton = new ButtonType("🗑️ Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(deleteButton, cancelButton);

        // Styliser les boutons
        Button deleteBtn = (Button) dialogPane.lookupButton(deleteButton);
        deleteBtn.setStyle(
                "-fx-background-color: #dc2626; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins SemiBold'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 30; " +
                        "-fx-cursor: hand;"
        );

        Button cancelBtn = (Button) dialogPane.lookupButton(cancelButton);
        cancelBtn.setStyle(
                "-fx-background-color: #f1f5f9; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-font-family: 'Poppins Medium'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 10 30; " +
                        "-fx-cursor: hand;"
        );

        // Gérer la réponse
        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                try {
                    saleService.deleteSaleById(sale.getId());
                    loadSales();
                    updateStatistics();

                    // Notification de succès
                    showNotification("✅ Vente #" + sale.getId() + " supprimée avec succès", "#059669");
                } catch (Exception e) {
                    showNotification("❌ Erreur: " + e.getMessage(), "#dc2626");
                }
            }
        });
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-family: 'Poppins Medium'; -fx-font-size: 14px; -fx-text-fill: #475569; -fx-min-width: 120;");

        Label val = new Label(value);
        val.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

        row.getChildren().addAll(lbl, val);
        return row;
    }

    private void showNotification(String message, String color) {
        Label notification = new Label(message);
        notification.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Poppins Medium';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 12 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );

        // Afficher temporairement
        VBox parent = (VBox) salesTable.getParent();
        parent.getChildren().add(notification);

        new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(3),
                        e -> parent.getChildren().remove(notification)
                )
        ).play();
    }
}
