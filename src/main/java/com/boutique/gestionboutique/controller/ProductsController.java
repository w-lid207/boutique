package com.boutique.gestionboutique.controller;

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
        loadProducts();
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

        Button editBtn = new Button("Éditer");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setOnAction(e -> editProduct(product));

        Button deleteBtn = new Button("Supprimer");
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

        GridPane grid = createProductForm(null);
        dialog.getDialogPane().setContent(grid);

        ButtonType addBtn = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

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
                statusLabel.setText("Produit ajouté!");
            } catch (Exception e) {
                statusLabel.setText("Erreur: " + e.getMessage());
            }
        }
    }

    private void editProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Modifier le Produit");

        GridPane grid = createProductForm(product);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

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
                statusLabel.setText("Produit modifié!");
            } catch (Exception e) {
                statusLabel.setText("Erreur: " + e.getMessage());
            }
        }
    }

    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Supprimer " + product.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                loadProducts();
                statusLabel.setText("Produit supprimé!");
            } catch (Exception e) {
                statusLabel.setText("Erreur: " + e.getMessage());
            }
        }
    }

    private GridPane createProductForm(Product product) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

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

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Prix:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Quantité:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Catégorie ID:"), 0, 3);
        grid.add(categoryIdField, 1, 3);
        grid.add(new Label("Image:"), 0, 4);
        grid.add(imagePathField, 1, 4);

        grid.setUserData(new Object[]{nameField, priceField, quantityField, categoryIdField, imagePathField});
        return grid;
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
}