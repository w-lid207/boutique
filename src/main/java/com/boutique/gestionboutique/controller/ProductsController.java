package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.model.Product;
import com.boutique.gestionboutique.service.ProductService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class ProductsController {

    @FXML private TextField searchField;
    @FXML private FlowPane productsFlowPane;
    @FXML private Label statusLabel;

    private ProductService productService;
    private List<Product> allProducts;

    private static final String CSS_PATH = "/com/boutique/gestionboutique/stylesheets/";

    @FXML
    public void initialize() {
        productService = new ProductService();
        loadProducts();
    }


    private void displayProducts(List<Product> products) {
        productsFlowPane.getChildren().clear();
        if (products != null) {
            for (Product product : products) {
                VBox card = createProductCard(product);
                productsFlowPane.getChildren().add(card);
            }
        }
    }

    public void loadProducts() {
        statusLabel.setText("Chargement des produits...");
        Task<List<Product>> loadTask = new Task<>() {
            @Override
            protected List<Product> call() throws Exception {
                // On récupère les données fraîches de la base de données
                return productService.getAllProducts();
            }
        };
        loadTask.setOnSucceeded(e -> {
            allProducts = loadTask.getValue();
            displayProducts(allProducts);
            statusLabel.setText("Produits chargés: " + allProducts.size());
        });
        new Thread(loadTask).start();
    }

    private VBox createProductCard(Product product) {
        // 1. Main Card Container
        VBox card = new VBox(12);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setMinHeight(420);

        // 2. Image Container with Background
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setPrefHeight(180);
        imageContainer.setMinHeight(180);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                Image img = new Image(product.getImagePath(),
                        200,  // requestedWidth
                        200,  // requestedHeight
                        true, // preserveRatio
                        true, // smooth
                        true  // backgroundLoading
                );                imageView.setImage(img);
            } catch (Exception e) {
                System.err.println("Invalid image URL for: " + product.getName());
            }
        }
        imageContainer.getChildren().add(imageView);

        // 3. Category & Stock Badge (Top Section)
        HBox topInfo = new HBox(8);
        topInfo.setAlignment(Pos.CENTER_LEFT);
        topInfo.getStyleClass().add("margin");

        // Category Badge
        Label categoryLabel = new Label(product.getCategoryName() != null ? product.getCategoryName() : "Général");
        categoryLabel.getStyleClass().add("product-category-badge");

        // Stock Status
        HBox stockInfo = new HBox(5);
        stockInfo.setAlignment(Pos.CENTER_LEFT);
        Label stockDot = new Label("●");
        stockDot.getStyleClass().add(product.getQuantity() > 10 ? "stock-dot-available" : "stock-dot-low");
        Label stockLabel = new Label("Stock: " + product.getQuantity());
        stockLabel.getStyleClass().add("stock-label");
        stockInfo.getChildren().addAll(stockDot, stockLabel);

        topInfo.getChildren().addAll(categoryLabel, new Region(), stockInfo);
        HBox.setHgrow(topInfo.getChildren().get(1), Priority.ALWAYS);

        // 4. Product Details
        VBox detailsBox = new VBox(8);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(230);

        // Price Row with larger styling
        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        Label currencyLabel = new Label("MAD");
        currencyLabel.getStyleClass().add("product-currency");
        priceRow.getChildren().addAll(priceLabel, currencyLabel);

        detailsBox.getChildren().addAll(nameLabel, priceRow);

        // 5. Vertical Spacer
        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);



        // 7. Action Buttons with Better Icons
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER);

        // Edit Button
        SVGPath editIcon = new SVGPath();
        editIcon.setContent("M13.498.795l.149-.149a1.207 1.207 0 1 1 1.707 1.708l-.149.148a1.5 1.5 0 0 1-.059 2.059L4.854 14.854a.5.5 0 0 1-.233.131l-4 1a.5.5 0 0 1-.606-.606l1-4a.5.5 0 0 1 .131-.232l9.642-9.642a.5.5 0 0 0-.642.056L6.854 4.854a.5.5 0 1 1-.708-.708L9.44.854A1.5 1.5 0 0 1 11.5.796a1.5 1.5 0 0 1 1.998-.001z");
        editIcon.getStyleClass().add("btn-icon-svg");

        Button editBtn = new Button("Modifier", editIcon);
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(editBtn, Priority.ALWAYS);
        editBtn.setOnAction(e -> editProduct(product));

        // Delete Button
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z");
        deleteIcon.getStyleClass().add("btn-icon-svg");

        Button deleteBtn = new Button("Supprimer", deleteIcon);
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        deleteBtn.setOnAction(e -> deleteProduct(product));

        buttons.getChildren().addAll(editBtn, deleteBtn);

        // 8. Assemble Card
        card.getChildren().addAll(
                imageContainer,
                topInfo,
                detailsBox,
                verticalSpacer,
                buttons
        );

        return card;
    }    @FXML
    private void addProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Ajouter un Produit");
        dialog.setHeaderText("➕ Ajouter un nouveau produit");

        GridPane grid = createProductForm(null);
        dialog.getDialogPane().setContent(grid);

        ButtonType addBtn = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        applyDialogStyle(dialog, "add");

        dialog.setResultConverter(btn -> (btn == addBtn) ? getProductFromForm(grid, null) : null);

        Optional<Product> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                productService.addProduct(result.get());
                // CORRECTION : On recharge tout pour mettre à jour 'allProducts' et la grille
                loadProducts();
                statusLabel.setText("✓ Produit ajouté !");
            } catch (Exception e) {
                statusLabel.setText("✗ Erreur lors de l'ajout");
                e.printStackTrace();
            }
        }
    }

    private void editProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Modifier le Produit");
        dialog.setHeaderText("✏ Modifier les informations");

        GridPane grid = createProductForm(product);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        applyDialogStyle(dialog, "edit");

        dialog.setResultConverter(btn -> (btn == saveBtn) ? getProductFromForm(grid, product.getId()) : null);

        Optional<Product> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                productService.updateProduct(result.get());
                loadProducts(); // On recharge tout après modification
                statusLabel.setText("✓ Produit modifié !");
            } catch (Exception e) {
                statusLabel.setText("✗ Erreur lors de la modification");
            }
        }
    }

    private void deleteProduct(@NotNull Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Confirmation");
        alert.setHeaderText("🗑 Supprimer \"" + product.getName() + "\"?");

        applyDialogStyle(alert, "delete");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                loadProducts(); // On recharge tout après suppression
                statusLabel.setText("✓ Produit supprimé !");
            } catch (Exception e) {
                statusLabel.setText("✗ Erreur lors de la suppression");
            }
        }
    }

    private void applyDialogStyle(Dialog<?> dialog, String buttonMode) {
        try {
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setGraphic(null); // Retire le point d'interrogation bleu

            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.getIcons().clear(); // Retire l'icône de titre

            URL cssResource = getClass().getResource(CSS_PATH + "dialogue.css");
            if (cssResource != null) {
                String css = cssResource.toExternalForm();
                dialogPane.getStylesheets().add(css);
                dialogPane.getStyleClass().add("custom-dialog");

                Button okBtn = (Button) dialogPane.lookupButton(dialogPane.getButtonTypes().get(0));
                if (okBtn != null) {
                    okBtn.getStyleClass().add("btn-round");
                    if (buttonMode.equals("delete")) {
                        okBtn.getStyleClass().add("btn-delete-style");
                    } else {
                        okBtn.getStyleClass().add("btn-ok-style");
                    }
                }

                Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
                if (cancelBtn != null) {
                    cancelBtn.getStyleClass().addAll("btn-round", "btn-cancel-style");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur style : " + e.getMessage());
        }
    }

    @NotNull
    private GridPane createProductForm(Product product) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        TextField categoryIdField = new TextField();
        TextField imagePathField = new TextField();

        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            categoryIdField.setText(String.valueOf(product.getCategoryId()));
            imagePathField.setText(product.getImagePath());
        }

        grid.add(new Label("Nom :"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Prix :"), 0, 1); grid.add(priceField, 1, 1);
        grid.add(new Label("Quantité :"), 0, 2); grid.add(quantityField, 1, 2);
        grid.add(new Label("Catégorie ID :"), 0, 3); grid.add(categoryIdField, 1, 3);
        grid.add(new Label("Image URL :"), 0, 4); grid.add(imagePathField, 1, 4);

        grid.setUserData(new TextField[]{nameField, priceField, quantityField, categoryIdField, imagePathField});
        return grid;
    }

    private Product getProductFromForm(GridPane grid, Integer id) {
        TextField[] fields = (TextField[]) grid.getUserData();
        Product product = new Product();
        if (id != null) product.setId(id);
        try {
            product.setName(fields[0].getText());
            product.setPrice(Double.parseDouble(fields[1].getText()));
            product.setQuantity(Integer.parseInt(fields[2].getText()));
            product.setCategoryId(Integer.parseInt(fields[3].getText()));
            product.setImagePath(fields[4].getText());
        } catch (Exception e) {
            System.err.println("Erreur de conversion numérique dans le formulaire.");
        }
        return product;
    }


    @FXML
    private void searchProducts() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            displayProducts(allProducts);
            return;
        }
        if (allProducts != null) {
            List<Product> results = allProducts.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(query))
                    .toList();
            displayProducts(results);
        }
    }

    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button clickedBtn = (Button) event.getSource();
        String categoryName = (String) clickedBtn.getUserData();

        FlowPane parent = (FlowPane) clickedBtn.getParent();
        parent.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("filter-btn-active");
            }
        });

        clickedBtn.getStyleClass().add("filter-btn-active");

        if (allProducts != null) {
            if ("all".equals(categoryName)) {
                displayProducts(allProducts);
            } else {
                List<Product> filtered = allProducts.stream()
                        .filter(p -> p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(categoryName))
                        .toList();
                displayProducts(filtered);
            }
        }
    }
}