package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.ProductService;
import com.boutique.gestionboutique.service.CartService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.layout.Priority.ALWAYS;

public class POSController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane productsGrid;
    @FXML
    private VBox cart;
    @FXML
    private Label totalPrice;
    @FXML
    private Button orderDone;
    @FXML
    private  Button b1;  @FXML
    private  Button b2;  @FXML
    private  Button b3;  @FXML
    private  Button b4;  @FXML
    private  Button b5;
    private Button[] btns;
    private ProductService productService;
    private List<Product> allProducts;
    private final CartService cartManager = CartService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        productService = new ProductService();
        refreshCartDisplay();
        loadProducts();
        btns = new Button[]{b1,b2,b3,b4,b5};
    }

    private void loadProducts() {
        Task<List<Product>> loadTask = new Task<>() {
            @Override
            protected List<Product> call() throws Exception {
                return productService.getAllProducts();
            }
        };

        loadTask.setOnSucceeded(e -> {
            allProducts = loadTask.getValue();
            displayProducts(allProducts);
        });

        loadTask.setOnFailed(e -> loadTask.getException().printStackTrace());
        new Thread(loadTask).start();
    }

    private void displayProducts(List<Product> products) {
        productsGrid.getChildren().clear();
//        int row = 0;
//        int col = 0;

        for (Product product : products) {
            VBox card = createProductCard(product);
            productsGrid.getChildren().add(card);
//            col++;
//            if (col == 3) {
//                col = 0;
//                row++;
//            }
        }
    }

    // FIXED: Added defensive image loading to prevent "Invalid URL" crash
    private VBox createProductCard(Product product) {
        // 1. Main Card Container (Bigger Width)
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setSpacing(12);
        card.setPrefWidth(260); // INCREASED WIDTH

        // 2. Image Container
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setPrefHeight(160); // INCREASED HEIGHT SLIGHTLY

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220); // BIGGER IMAGE
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);

        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                Image image = new Image(product.getImagePath(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Skipping invalid image path: " + product.getImagePath());
            }
        }

        if (imageView.getImage() == null) {
            Label placeholderText = new Label("Visual Product");
            placeholderText.setStyle("-fx-text-fill: #8EA096; -fx-font-size: 14px;");
            imageContainer.getChildren().add(placeholderText);
        } else {
            imageContainer.getChildren().add(imageView);
        }

        // 3. Details Section
        VBox detailsBox = new VBox(5);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        Label categoryLabel = new Label(product.getCategoryName());
        categoryLabel.getStyleClass().add("product-category");

        detailsBox.getChildren().addAll(nameLabel, categoryLabel);

        // 4. Info Row: Stock (Left) + Price (Right)
        HBox infoRow = new HBox();
        infoRow.setAlignment(Pos.CENTER_LEFT);

        HBox stockBox = new HBox(6);
        stockBox.setAlignment(Pos.CENTER_LEFT);
        Circle stockDot = new Circle(4);
        stockDot.getStyleClass().add("stock-dot");
        Label stockLabel = new Label("En stock • " + product.getQuantity());
        stockLabel.getStyleClass().add("stock-label");
        stockBox.getChildren().addAll(stockDot, stockLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label(String.format("%.0f MAD", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        infoRow.getChildren().addAll(stockBox, spacer, priceLabel);

        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);
        // 5. Button Container (Aligned Right)
        HBox btnContainer = new HBox();
        btnContainer.setAlignment(Pos.CENTER_RIGHT); // ALIGN RIGHT
        btnContainer.setPadding(new Insets(5, 0, 0, 0));

        Button addToCart = new Button("Add to cart");
        addToCart.getStyleClass().add("product-add-btn");
        addToCart.setOnAction(e -> addToCart(product));

        btnContainer.getChildren().add(addToCart);

        // Combine
        card.getChildren().addAll(imageContainer, detailsBox, infoRow, verticalSpacer, btnContainer);
        return card;
    }    // FIXED: Re-added this method which was missing and causing FXML LoadException
    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoryName = (String) btn.getUserData();
        for(Button b: btns){
            b.getStyleClass().remove("active");
        }
        btn.getStyleClass().add("active");
        if (allProducts == null) return;

        if ("all".equals(categoryName)) {
            displayProducts(allProducts);
        } else {
            List<Product> filtered = allProducts.stream()
                    .filter(p -> p.getCategoryName() != null &&
                            p.getCategoryName().equalsIgnoreCase(categoryName))
                    .toList();
            displayProducts(filtered);
        }
    }

    @FXML
    private void searchProducts() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            displayProducts(allProducts);
            return;
        }
        List<Product> results = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(query))
                .toList();
        displayProducts(results);
    }

    @FXML
    private void addToCart(Product product){
        for(Product p : cartManager.getCartItems()){
            if(p.getId() == product.getId()){
                handleQuantity(p.getId(), '+');
                return;
            }
        }
        product.setqCartItem(1);
        product.setqPrice(product.getPrice());
        cartManager.addItem(product);
        refreshCartDisplay();
    }

    private HBox createCartItem(Product product) {
        // 1. Main Container
        HBox itemContainer = new HBox();
        itemContainer.setAlignment(Pos.CENTER_LEFT);
        itemContainer.getStyleClass().add("cart-item-container");
        itemContainer.setSpacing(10);

        // 2. Left Side: Product Name & Unit Price
        VBox infoBox = new VBox(2); // 2px spacing between name and unit price
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label productTitle = new Label(product.getName());
        productTitle.getStyleClass().add("cart-product-title");
        productTitle.setWrapText(true);

        // Assuming product.getPrice() is the unit price.
        // If you only have total price, calculate unit: total / quantity
        double unitPriceVal = (product.getqCartItem() > 0) ? (product.getqPrice() / product.getqCartItem()) : 0;
        Label unitPriceLabel = new Label("Unit " + String.format("%.2f DH", unitPriceVal));
        unitPriceLabel.getStyleClass().add("cart-unit-price");

        infoBox.getChildren().addAll(productTitle, unitPriceLabel);

        // 3. Spacer to push Right Side to the edge
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Right Side: Quantity & Total Price
        HBox rightSide = new HBox(15); // Gap between Quantity Pill and Total Price
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        // --- Quantity Pill Container ---
        HBox qtyContainer = new HBox();
        qtyContainer.getStyleClass().add("qty-container");
        qtyContainer.setAlignment(Pos.CENTER);

        Button minus = new Button("-");
        minus.getStyleClass().add("qty-btn");
        minus.setOnAction(e -> handleQuantity(product.getId(), '-'));

        Label quantity = new Label(String.valueOf(product.getqCartItem()));
        quantity.getStyleClass().add("qty-label");
        // Ensure label doesn't shrink
        quantity.setMinWidth(20);
        quantity.setAlignment(Pos.CENTER);

        Button plus = new Button("+");
        plus.getStyleClass().add("qty-btn");
        plus.setOnAction(e -> handleQuantity(product.getId(), '+'));

        qtyContainer.getChildren().addAll(minus, quantity, plus);

        // --- Total Price ---
        Label totalPriceLabel = new Label(String.format("%.2f", product.getqPrice()));
        totalPriceLabel.getStyleClass().add("cart-total-price");

        rightSide.getChildren().addAll(qtyContainer, totalPriceLabel);

        // 5. Add all to main container
        itemContainer.getChildren().addAll(infoBox, spacer, rightSide);

        return itemContainer;
    }
    private void refreshCartDisplay(){
        cart.getChildren().clear();
        for(Product product : cartManager.getCartItems()){
            cart.getChildren().add(createCartItem(product));
        }
        updateTotalPriceDisplay();
    }

    private void handleQuantity(int productId, Character action){
        for(Product p : cartManager.getCartItems()){
            if(p.getId() == productId){
                if(action == '+') {
                    p.setqCartItem(p.getqCartItem() + 1);
                } else if(action == '-' && p.getqCartItem() > 1){
                    p.setqCartItem(p.getqCartItem() - 1);
                }
                p.setqPrice(p.getPrice() * p.getqCartItem());
                break;
            }
        }
        refreshCartDisplay();
    }

    private void deleteItem(Product product){
        cartManager.getCartItems().remove(product);
        refreshCartDisplay();
    }

    private void updateTotalPriceDisplay() {
        totalPrice.setText(String.format("%.2f DH", calculateTotalPrice()));
    }

    private double calculateTotalPrice(){
        double total = 0;
        if(cartManager.getCartItems() != null) {
            for (Product product : cartManager.getCartItems()) {
                total += product.getqPrice();
            }
        }
        return total;
    }

    @FXML
    private void processSaleUpdates() {
        if(cartManager.getCartItems() == null || cartManager.getCartItems().isEmpty()) return;

        try {
            ObservableList<Product> observableCart = FXCollections.observableArrayList(cartManager.getCartItems());
            cartManager.processSale(observableCart, calculateTotalPrice());
            cartManager.getCartItems().clear();
            refreshCartDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}