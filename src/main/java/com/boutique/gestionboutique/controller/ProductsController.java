package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.ProductService;
import com.boutique.gestionboutique.controller.Product; // Importation nécessaire
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private GridPane productsGrid;
    @FXML private Label statusLabel;

    private ProductService productService;
    private List<Product> allProducts;

    private static final String CSS_PATH = "/com/boutique/gestionboutique/stylesheets/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productService = new ProductService();
        loadStylesheet();
        loadProducts();
    }

    private void loadStylesheet() {
        try {
            URL cssResource = getClass().getResource(CSS_PATH + "products.css");
            if (cssResource != null) {
                String css = cssResource.toExternalForm();
                if (productsGrid.getScene() != null) {
                    productsGrid.getScene().getStylesheets().add(css);
                } else {
                    productsGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        if (newScene != null) {
                            newScene.getStylesheets().add(css);
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement products.css: " + e.getMessage());
        }
    }

    private void displayProducts(List<Product> products) {
        productsGrid.getChildren().clear();
        int row = 0;
        int col = 0;
        if (products != null) {
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
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(10));

        BorderPane imageContainer = new BorderPane();
        imageContainer.getStyleClass().add("product-image-container");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                Image img = new Image(product.getImagePath(), true);
                imageView.setImage(img);
            } catch (Exception e) {
                System.err.println("URL d'image invalide pour : " + product.getName());
            }
        }

        imageContainer.setCenter(imageView);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%.2f MAD", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        HBox buttons = new HBox(8);
        Button editBtn = new Button("Éditer", createIcon("✏"));
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setOnAction(e -> editProduct(product));

        Button deleteBtn = new Button("Supprimer", createIcon("🗑"));
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> deleteProduct(product));

        buttons.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, buttons);
        return card;
    }

    @FXML
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

    private Label createIcon(String emoji) {
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 14;");
        return icon;
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

        HBox parent = (HBox) clickedBtn.getParent();
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