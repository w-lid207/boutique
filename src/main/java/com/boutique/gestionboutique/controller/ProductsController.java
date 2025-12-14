package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.controller.Product;
import com.boutique.gestionboutique.service.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductsController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private GridPane productsGrid;
    @FXML
    private Label statusLabel;

    private ProductService productService;
    private List<Product> allProducts;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productService = new ProductService();
        loadStylesheet();
        loadProducts();
    }

    /**
     * Charge le fichier CSS séparé
     */
    private void loadStylesheet() {
        try {
            String css = this.getClass().getResource("/styles.css").toExternalForm();
            if (productsGrid.getScene() != null) {
                productsGrid.getScene().getStylesheets().add(css);
            } else {
                productsGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.getStylesheets().add(css);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur CSS: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            allProducts = productService.getAllProducts();
            displayProducts(allProducts);
            statusLabel.setText("Produits chargés: " + allProducts.size());
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void displayProducts(List<Product> products) {
        productsGrid.getChildren().clear();

        int row = 0;
        int col = 0;

        for (Product product : products) {
            VBox card = createProductCard(product);
            productsGrid.add(card, col, row);

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");

        // Image Container
        BorderPane imageContainer = new BorderPane();
        imageContainer.getStyleClass().add("product-image-container");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        try {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                String imagePath = "file:src/main/resources/" + product.getImagePath();
                imageView.setImage(new Image(imagePath));
            }
        } catch (Exception e) {
            // Image non trouvée
        }

        imageContainer.setCenter(imageView);

        // Nom du produit
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);

        // Catégorie
        Label categoryLabel = new Label("Catégorie: " + product.getCategoryName());
        categoryLabel.getStyleClass().add("product-category");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getQuantity());
        stockLabel.getStyleClass().add("product-stock");

        // Prix
        Label priceLabel = new Label("MAD " + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        // Boutons d'action
        HBox buttons = new HBox(8);
        buttons.getStyleClass().add("product-buttons");

        Button editBtn = new Button("Éditer", createIcon("✏️"));
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setOnAction(e -> editProduct(product));

        Button deleteBtn = new Button("Supprimer", createIcon("🗑️"));
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> deleteProduct(product));

        buttons.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(imageContainer, nameLabel, categoryLabel, stockLabel, priceLabel, buttons);
        return card;
    }

    @FXML
    private void searchProducts() {
        String query = searchField.getText().toLowerCase().trim();

        if (query.isEmpty()) {
            displayProducts(allProducts);
            return;
        }

        try {
            List<Product> results = productService.searchProducts(query);
            displayProducts(results);
            statusLabel.setText("Trouvé: " + results.size());
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoryName = (String) btn.getUserData();

        if ("all".equals(categoryName)) {
            displayProducts(allProducts);
            statusLabel.setText("Tous les produits: " + allProducts.size());
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : allProducts) {
                if (p.getCategoryName() != null &&
                        p.getCategoryName().equalsIgnoreCase(categoryName)) {
                    filtered.add(p);
                }
            }
            displayProducts(filtered);
            statusLabel.setText("Catégorie " + categoryName + ": " + filtered.size());
        }
    }

    @FXML
    private void addProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Produit");
        dialog.setHeaderText("➕ Ajouter un nouveau produit");

        GridPane grid = createProductForm(null);
        dialog.getDialogPane().setContent(grid);

        ButtonType addBtn = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        // Appliquer les styles et icônes au dialogue
        applyAddDialogStyles(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                return getProductFromForm(grid, null);
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                productService.addProduct(result.get());
                loadProducts();
                statusLabel.setText("✓ Produit ajouté!");
            } catch (Exception e) {
                statusLabel.setText("✗ Erreur: " + e.getMessage());
            }
        }
    }

    private void editProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Modifier le Produit");
        dialog.setHeaderText("✏️ Modifier les informations du produit");

        GridPane grid = createProductForm(product);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Appliquer les styles et icônes au dialogue
        applyEditDialogStyles(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return getProductFromForm(grid, product.getId());
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                productService.updateProduct(result.get());
                loadProducts();
                statusLabel.setText("✓ Produit modifié!");
            } catch (Exception e) {
                statusLabel.setText("✗ Erreur: " + e.getMessage());
            }
        }
    }

    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("🗑️ Êtes-vous sûr?");
        alert.setContentText("Voulez-vous vraiment supprimer \"" + product.getName() + "\"?");

        // Appliquer les styles et icônes à l'alerte
        applyDeleteAlertStyles(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                loadProducts();
                statusLabel.setText("✓ Produit supprimé!");
            } catch (Exception e) {
                statusLabel.setText("✗ Erreur: " + e.getMessage());
            }
        }
    }

    private GridPane createProductForm(Product product) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #f0f0f0;");

        TextField nameField = new TextField();
        nameField.setPromptText("Entrez le nom du produit");
        nameField.getStyleClass().add("form-field");

        TextField priceField = new TextField();
        priceField.setPromptText("Entrez le prix (ex: 99.99)");
        priceField.getStyleClass().add("form-field");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Entrez la quantité en stock");
        quantityField.getStyleClass().add("form-field");

        TextField categoryIdField = new TextField();
        categoryIdField.setPromptText("Entrez l'ID de la catégorie");
        categoryIdField.getStyleClass().add("form-field");

        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Chemin de l'image (ex: images/produit.jpg)");
        imagePathField.getStyleClass().add("form-field");

        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            categoryIdField.setText(String.valueOf(product.getCategoryId()));
            imagePathField.setText(product.getImagePath());
        }

        // Labels avec icônes
        Label nameLabel = createLabelWithIcon("📝 Nom:", "form-label");
        Label priceLabel = createLabelWithIcon("💰 Prix:", "form-label");
        Label quantityLabel = createLabelWithIcon("📦 Quantité:", "form-label");
        Label categoryLabel = createLabelWithIcon("🏷️ Catégorie ID:", "form-label");
        Label imageLabel = createLabelWithIcon("🖼️ Image:", "form-label");

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(priceLabel, 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(quantityLabel, 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(categoryLabel, 0, 3);
        grid.add(categoryIdField, 1, 3);
        grid.add(imageLabel, 0, 4);
        grid.add(imagePathField, 1, 4);

        grid.setUserData(new Object[]{nameField, priceField, quantityField, categoryIdField, imagePathField});
        return grid;
    }

    /**
     * Crée une étiquette avec icône
     */
    private Label createLabelWithIcon(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    /**
     * Crée une icône pour un bouton
     */
    private Label createIcon(String emoji) {
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 14;");
        return icon;
    }

    private Product getProductFromForm(GridPane grid, Integer id) {
        Object[] fields = (Object[]) grid.getUserData();

        Product product = new Product();
        if (id != null) product.setId(id);
        product.setName(((TextField) fields[0]).getText());
        product.setPrice(Double.parseDouble(((TextField) fields[1]).getText()));
        product.setQuantity(Integer.parseInt(((TextField) fields[2]).getText()));
        product.setCategoryId(Integer.parseInt(((TextField) fields[3]).getText()));
        product.setImagePath(((TextField) fields[4]).getText());

        return product;
    }

    /**
     * Applique les styles au dialogue AJOUTER (Vert)
     */
    private void applyAddDialogStyles(Dialog<Product> dialog) {
        try {
            String css = this.getClass().getResource("/styles.css").toExternalForm();
            dialog.getDialogPane().getStylesheets().add(css);

            dialog.getDialogPane().setStyle("-fx-background-color: #f0f0f0;");

            for (ButtonType buttonType : dialog.getDialogPane().getButtonTypes()) {
                Button button = (Button) dialog.getDialogPane().lookupButton(buttonType);
                if (button != null) {
                    if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        button.setGraphic(createIcon("✅"));
                        button.getStyleClass().add("btn-add-dialog");
                    } else {
                        button.setGraphic(createIcon("❌"));
                        button.getStyleClass().add("btn-cancel-dialog");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur styles dialogue: " + e.getMessage());
        }
    }

    /**
     * Applique les styles au dialogue ÉDITER (Orange)
     */
    private void applyEditDialogStyles(Dialog<Product> dialog) {
        try {
            String css = this.getClass().getResource("/styles.css").toExternalForm();
            dialog.getDialogPane().getStylesheets().add(css);

            dialog.getDialogPane().setStyle("-fx-background-color: #f0f0f0;");

            for (ButtonType buttonType : dialog.getDialogPane().getButtonTypes()) {
                Button button = (Button) dialog.getDialogPane().lookupButton(buttonType);
                if (button != null) {
                    if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        button.setGraphic(createIcon("💾"));
                        button.getStyleClass().add("btn-edit-dialog");
                    } else {
                        button.setGraphic(createIcon("❌"));
                        button.getStyleClass().add("btn-cancel-dialog");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur styles dialogue: " + e.getMessage());
        }
    }

    /**
     * Applique les styles à l'alerte SUPPRIMER (Rouge)
     */
    private void applyDeleteAlertStyles(Alert alert) {
        try {
            String css = this.getClass().getResource("/styles.css").toExternalForm();
            alert.getDialogPane().getStylesheets().add(css);

            alert.getDialogPane().setStyle("-fx-background-color: #f0f0f0;");

            for (ButtonType buttonType : alert.getDialogPane().getButtonTypes()) {
                Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
                if (button != null) {
                    if (buttonType == ButtonType.OK) {
                        button.setGraphic(createIcon("🗑️"));
                        button.getStyleClass().add("btn-delete-dialog");
                    } else {
                        button.setGraphic(createIcon("❌"));
                        button.getStyleClass().add("btn-cancel-dialog");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur styles alerte: " + e.getMessage());
        }
    }
}