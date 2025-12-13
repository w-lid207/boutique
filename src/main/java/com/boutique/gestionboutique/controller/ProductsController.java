package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductsController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private GridPane productsGrid;
    @FXML
    private Label statusLabel;

    private ProductService productService;
    private List<Product> allProducts;
    private String currentCategory = "all";
    private Button lastActiveButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productService = new ProductService();
        loadProducts();
    }

    /**
     * Charge tous les produits depuis la base de données
     */
    private void loadProducts() {
        try {
            statusLabel.setText("Chargement des produits...");
            allProducts = productService.getAllProducts();
            displayProducts(allProducts);
            statusLabel.setText("✅ Produits chargés: " + allProducts.size());
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les produits dans la grille
     */
    private void displayProducts(List<Product> products) {
        productsGrid.getChildren().clear();

        if (products.isEmpty()) {
            statusLabel.setText("⚠️ Aucun produit trouvé");
            return;
        }

        int row = 0;
        int col = 0;

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, col, row);

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }

        statusLabel.setText("📦 Produits affichés: " + products.size());
    }

    /**
     * Crée une carte produit avec image et boutons d'action
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 10; " +
                "-fx-background-color: white; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
        card.setSpacing(10);

        // Conteneur image
        BorderPane imageContainer = new BorderPane();
        imageContainer.setStyle("-fx-border-color: #f0f0f0; -fx-border-radius: 8; " +
                "-fx-background-color: #f8f9fa; -fx-min-height: 200; " +
                "-fx-padding: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        // Charger l'image du produit
        try {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                String imagePath = "file:src/main/resources/" + product.getImagePath();
                Image img = new Image(imagePath, true);
                imageView.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }

        imageContainer.setCenter(imageView);

        // Informations produit
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-wrap-text: true;");

        Label categoryLabel = new Label("Catégorie: " + (product.getCategoryName() != null ? product.getCategoryName() : "N/A"));
        categoryLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        Label priceLabel = new Label("MAD " + String.format("%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label quantityLabel = new Label("Stock: " + product.getQuantity() + " unités");
        quantityLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        HBox priceBox = new HBox();
        priceBox.setSpacing(10);
        priceBox.getChildren().add(priceLabel);

        // Boutons d'action
        HBox actionBox = new HBox();
        actionBox.setSpacing(8);
        actionBox.setStyle("-fx-padding: 10 0 0 0;");

        Button editBtn = new Button("✏️ Éditer");
        editBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #3498db; " +
                "-fx-text-fill: white; -fx-border-radius: 5; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> editProduct(product));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; -fx-border-radius: 5; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> deleteProduct(product));

        Button viewBtn = new Button("👁️ Détails");
        viewBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #95a5a6; " +
                "-fx-text-fill: white; -fx-border-radius: 5; -fx-font-weight: bold;");
        viewBtn.setOnAction(e -> viewProductDetails(product));

        actionBox.getChildren().addAll(editBtn, deleteBtn, viewBtn);

        // Assemblage de la carte
        card.getChildren().addAll(
                imageContainer,
                nameLabel,
                categoryLabel,
                quantityLabel,
                priceBox,
                actionBox
        );

        return card;
    }

    /**
     * Recherche les produits
     */
    @FXML
    private void searchProducts() {
        String query = searchField.getText().toLowerCase().trim();

        if (query.isEmpty()) {
            displayProducts(allProducts);
            return;
        }

        try {
            List<Product> filtered = productService.searchProducts(query);
            displayProducts(filtered);
            statusLabel.setText("🔍 Résultats: " + filtered.size() + " produit(s)");
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur recherche: " + e.getMessage());
        }
    }

    /**
     * ✅ FILTRAGE PAR CATÉGORIE - VERSION CORRIGÉE AVEC DISTINCT
     */
    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoryName = (String) btn.getUserData();

        // Désactiver le style du bouton précédent
        if (lastActiveButton != null) {
            lastActiveButton.setStyle(lastActiveButton.getStyle().replace("-fx-background-color: #27ae60;", "-fx-background-color: #95a5a6;"));
        }

        // Activer le style du bouton cliqué
        btn.setStyle(btn.getStyle().replace("-fx-background-color: #95a5a6;", "-fx-background-color: #27ae60;"));
        lastActiveButton = btn;

        currentCategory = categoryName;

        try {
            List<Product> filtered;

            if ("all".equalsIgnoreCase(categoryName)) {
                // Afficher tous les produits (DISTINCTS par ID)
                filtered = allProducts.stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                p -> p,
                                (p1, p2) -> p1
                        ))
                        .values()
                        .stream()
                        .collect(Collectors.toList());
                statusLabel.setText("📦 Tous les produits: " + filtered.size());
            } else {
                // ✅ FILTRAGE PAR NOM DE CATÉGORIE (DISTINCTS par ID)
                filtered = allProducts.stream()
                        .filter(p -> p.getCategoryName() != null &&
                                p.getCategoryName().equalsIgnoreCase(categoryName))
                        .collect(Collectors.toMap(
                                Product::getId,
                                p -> p,
                                (p1, p2) -> p1
                        ))
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                statusLabel.setText("🏷️ Catégorie '" + categoryName + "': " + filtered.size() + " produit(s)");

                // Debug détaillé
                System.out.println("\n🔍 FILTRAGE POUR: " + categoryName);
                System.out.println("📊 Produits trouvés: " + filtered.size());
                System.out.println("📋 Liste des produits:");
                filtered.forEach(p -> System.out.println("   - ID: " + p.getId() + " | " + p.getName()));

                // Afficher les catégories disponibles si aucun résultat
                if (filtered.isEmpty()) {
                    System.out.println("⚠️ Aucun produit avec la catégorie: " + categoryName);
                    System.out.println("📋 Catégories disponibles:");
                    allProducts.stream()
                            .map(Product::getCategoryName)
                            .distinct()
                            .forEach(cat -> System.out.println("   - " + cat));
                }
            }

            displayProducts(filtered);

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur filtrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un nouveau produit
     */
    @FXML
    private void addProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Produit");
        dialog.setHeaderText("Créer un nouveau produit");

        GridPane grid = createProductForm(null);
        dialog.getDialogPane().setContent(grid);

        ButtonType addButton = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return extractProductFromForm(grid, null);
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            try {
                productService.addProduct(product);
                loadProducts();
                statusLabel.setText("✅ Produit ajouté avec succès!");
            } catch (Exception e) {
                statusLabel.setText("❌ Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Édite un produit existant
     */
    private void editProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Éditer le Produit");
        dialog.setHeaderText("Modifier: " + product.getName());

        GridPane grid = createProductForm(product);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return extractProductFromForm(grid, product.getId());
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(updatedProduct -> {
            try {
                productService.updateProduct(updatedProduct);
                loadProducts();
                statusLabel.setText("✅ Produit mis à jour!");
            } catch (Exception e) {
                statusLabel.setText("❌ Erreur: " + e.getMessage());
            }
        });
    }

    /**
     * Supprime un produit
     */
    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce produit?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + product.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                loadProducts();
                statusLabel.setText("✅ Produit supprimé!");
            } catch (Exception e) {
                statusLabel.setText("❌ Erreur: " + e.getMessage());
            }
        }
    }

    /**
     * Affiche les détails du produit
     */
    private void viewProductDetails(Product product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Produit");
        alert.setHeaderText(product.getName());

        String details = String.format(
                "ID: %d\nCatégorie: %s\nPrix: MAD %.2f\nQuantité: %d unités\nChemin image: %s",
                product.getId(),
                product.getCategoryName() != null ? product.getCategoryName() : "N/A",
                product.getPrice(),
                product.getQuantity(),
                product.getImagePath() != null ? product.getImagePath() : "N/A"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Crée un formulaire produit
     */
    private GridPane createProductForm(Product product) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Nom du produit");
        TextField priceField = new TextField();
        priceField.setPromptText("Prix");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantité");
        TextField categoryIdField = new TextField();
        categoryIdField.setPromptText("ID Catégorie");
        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Chemin image (ex: images/v1.jpg)");

        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            categoryIdField.setText(String.valueOf(product.getCategoryId()));
            imagePathField.setText(product.getImagePath() != null ? product.getImagePath() : "");
        }

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Prix:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Quantité:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("ID Catégorie:"), 0, 3);
        grid.add(categoryIdField, 1, 3);
        grid.add(new Label("Chemin Image:"), 0, 4);
        grid.add(imagePathField, 1, 4);

        grid.setUserData(new Object[]{nameField, priceField, quantityField, categoryIdField, imagePathField});

        return grid;
    }

    /**
     * Extrait les données du formulaire
     */
    private Product extractProductFromForm(GridPane grid, Integer id) {
        Object[] fields = (Object[]) grid.getUserData();
        TextField nameField = (TextField) fields[0];
        TextField priceField = (TextField) fields[1];
        TextField quantityField = (TextField) fields[2];
        TextField categoryIdField = (TextField) fields[3];
        TextField imagePathField = (TextField) fields[4];

        Product product = new Product();
        if (id != null) product.setId(id);
        product.setName(nameField.getText());
        product.setPrice(Double.parseDouble(priceField.getText()));
        product.setQuantity(Integer.parseInt(quantityField.getText()));
        product.setCategoryId(Integer.parseInt(categoryIdField.getText()));
        product.setImagePath(imagePathField.getText());

        return product;
    }
}