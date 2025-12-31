package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.ProductService;
import com.boutique.gestionboutique.service.CartService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    private GridPane productsGrid;
    @FXML
    private VBox cart;
    @FXML
    private Label totalPrice;
    @FXML
    private Button orderDone;

    private ProductService productService;
    private List<Product> allProducts;
    private final CartService cartManager = CartService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        productService = new ProductService();
        refreshCartDisplay();
        loadProducts();
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

    // FIXED: Added defensive image loading to prevent "Invalid URL" crash
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");

        BorderPane imageContainer = new BorderPane();
        imageContainer.getStyleClass().add("product-image-container");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        // SAFE IMAGE LOADING
        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                // Use background loading (true) to keep UI fast
                Image image = new Image(product.getImagePath(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Skipping invalid image path: " + product.getImagePath());
            }
        }

        imageContainer.setCenter(imageView);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);

        Label categoryLabel = new Label("Catégorie: " + product.getCategoryName());
        categoryLabel.getStyleClass().add("product-category");

        Label stockLabel = new Label("Stock: " + product.getQuantity());
        stockLabel.getStyleClass().add("product-stock");

        Label priceLabel = new Label("MAD " + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        HBox button_container = new HBox(8);
        button_container.getStyleClass().add("product-buttons");

        Button addToCart = new Button("Ajouter au panier");
        addToCart.getStyleClass().add("btn-add-to-cart");
        addToCart.setOnAction(e -> addToCart(product));

        button_container.getChildren().addAll(addToCart);
        card.getChildren().addAll(imageContainer, nameLabel, categoryLabel, stockLabel, priceLabel, button_container);
        return card;
    }

    // FIXED: Re-added this method which was missing and causing FXML LoadException
    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoryName = (String) btn.getUserData();

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

    private HBox createCartItem(Product product){
        HBox itemContainer = new HBox();
        itemContainer.getStyleClass().add("cartItemContainer");

        ImageView imageView = new ImageView();
        if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                imageView.setImage(new Image(product.getImagePath(), true));
            } catch (Exception e) {}
        }
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("cart-image");

        VBox info = new VBox();
        info.getStyleClass().add("cart-info");
        HBox.setHgrow(info, ALWAYS);

        HBox top = new HBox();
        top.getStyleClass().add("cart-top");
        Label productTitle = new Label(product.getName());
        productTitle.getStyleClass().add("product-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, ALWAYS);

        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("delete-btn");
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M33.9602 5.71429H24.0833V1.42857C24.0833 1.04969 23.9341 0.686328 23.6684 0.418419C23.4027 0.15051 23.0424 0H22.6667 0H11.3333C10.9576 0 10.5973 0.15051 10.3316 0.418419C10.0659 0.686328 9.91667 1.04969 9.91667 1.42857V5.71429H0.0398435L0 9.28571H2.92187L4.70068 37.3214C4.7457 38.0464 5.06289 38.7269 5.58773 39.2245C6.11258 39.7221 6.80567 39.9994 7.52604 40H26.474C27.1939 39.9999 27.8868 39.7234 28.412 39.2267C28.9371 38.7301 29.255 38.0504 29.3011 37.3259L31.0781 9.28571H34L33.9602 5.71429ZM9.91667 34.2857L9.11979 11.4286H12.0417L12.8385 34.2857H9.91667ZM18.4167 34.2857H15.5833V11.4286H18.4167V34.2857ZM20.5417 5.71429H13.4583V3.21429C13.4583 3.11957 13.4956 3.02872 13.5621 2.96175C13.6285 2.89477 13.7186 2.85714 13.8125 2.85714H20.1875C20.2814 2.85714 20.3715 2.89477 20.4379 2.96175C20.5044 3.02872 20.5417 3.11957 20.5417 3.21429V5.71429ZM24.0833 34.2857H21.1615L21.9583 11.4286H24.8802L24.0833 34.2857Z");
        trashIcon.setScaleX(0.4);
        trashIcon.setScaleY(0.4);
        trashIcon.getStyleClass().add("trash-icon");
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setOnAction(e -> deleteItem(product));

        top.getChildren().addAll(productTitle, spacer, deleteBtn);

        HBox bottom = new HBox(5);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.getStyleClass().add("cart-bottom");

        Label price = new Label(String.format("%.2f DH", product.getqPrice()));
        price.getStyleClass().add("product-price");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, ALWAYS);

        Button minus = new Button("-");
        minus.getStyleClass().add("qty-btn");
        minus.setOnAction(e -> handleQuantity(product.getId(), '-'));

        Label quantity = new Label(String.valueOf(product.getqCartItem()));
        quantity.getStyleClass().add("qty-label");

        Button plus = new Button("+");
        plus.getStyleClass().add("qty-btn");
        plus.setOnAction(e -> handleQuantity(product.getId(), '+'));

        bottom.getChildren().addAll(price, spacer2, minus, quantity, plus);
        info.getChildren().addAll(top, bottom);
        itemContainer.getChildren().addAll(imageView, info);

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