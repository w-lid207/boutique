package com.boutique.gestionboutique.controller;

import com.almasb.fxgl.entity.action.Action;
import com.boutique.gestionboutique.service.ProductService;
import com.boutique.gestionboutique.service.CartService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.constant.ConstantDescs.NULL;
import static javafx.scene.layout.Priority.ALWAYS;

public class POSController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private GridPane productsGrid;
    private ProductService productService;
    private List<Product> allProducts;
    @FXML
    private VBox cart;
    @FXML
    private Label totalPrice;
    private final CartService cartManager = CartService.getInstance();
    @FXML
    private Button orderDone;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        productService = new ProductService();
        cart.getChildren().clear();
        List<Product> itemsSnapshot = new ArrayList<>(cartManager.getCartItems());
        for(Product product : itemsSnapshot){
            cart.getChildren().add(createCartItem(product));
        }
        loadProducts();
    }



    private void loadProducts() {
        try {
            allProducts = productService.getAllProducts();
            displayProducts(allProducts);
        } catch (Exception e) {
            e.printStackTrace();
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
        HBox button_container = new HBox(8);
        button_container.getStyleClass().add("product-buttons");

        Button addToCart = new Button("Ajouter au panier");
        addToCart.getStyleClass().add("btn-add-to-cart");
        addToCart.setOnAction(e-> addToCart(product));


        button_container.getChildren().addAll(addToCart);
        card.getChildren().addAll(imageContainer, nameLabel, categoryLabel, stockLabel, priceLabel, button_container);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoryName = (String) btn.getUserData();

        if ("all".equals(categoryName)) {
            displayProducts(allProducts);
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : allProducts) {
                if (p.getCategoryName() != null &&
                        p.getCategoryName().equalsIgnoreCase(categoryName)) {
                    filtered.add(p);
                }
            }
            displayProducts(filtered);
        }
    }
    @FXML
    private void addToCart(Product product){
        product.setqCartItem(1);
        product.setqPrice(product.getPrice());
        HBox cartItem = createCartItem(product);
        for(Product p : cartManager.getCartItems()){
            if(p.getId() == product.getId()){
                return;
            }
        }

        cartManager.addItem(product);
        cart.getChildren().add(cartItem);
        totalPrice.setText(String.valueOf(String.format("%.2f", calculateTotalPrice()))+" DH");

    }
    @FXML
    private HBox createCartItem(Product product){
        HBox itemContainer = new HBox();
        ImageView imageView = new ImageView();
        VBox info = new VBox();
        HBox.setHgrow(info, ALWAYS);
            HBox top = new HBox();
            Label productTitle = new Label(product.getName());
            Region spacer = new Region();
            top.setHgrow(spacer, ALWAYS);
            Button deleteBtn = new Button();
            SVGPath trashIcon = new SVGPath();
            trashIcon.setContent("M33.9602 5.71429H24.0833V1.42857C24.0833 1.04969 23.9341 0.686328 23.6684 0.418419C23.4027 0.15051 23.0424 0 22.6667 0H11.3333C10.9576 0 10.5973 0.15051 10.3316 0.418419C10.0659 0.686328 9.91667 1.04969 9.91667 1.42857V5.71429H0.0398435L0 9.28571H2.92187L4.70068 37.3214C4.7457 38.0464 5.06289 38.7269 5.58773 39.2245C6.11258 39.7221 6.80567 39.9994 7.52604 40H26.474C27.1939 39.9999 27.8868 39.7234 28.412 39.2267C28.9371 38.7301 29.255 38.0504 29.3011 37.3259L31.0781 9.28571H34L33.9602 5.71429ZM9.91667 34.2857L9.11979 11.4286H12.0417L12.8385 34.2857H9.91667ZM18.4167 34.2857H15.5833V11.4286H18.4167V34.2857ZM20.5417 5.71429H13.4583V3.21429C13.4583 3.11957 13.4956 3.02872 13.5621 2.96175C13.6285 2.89477 13.7186 2.85714 13.8125 2.85714H20.1875C20.2814 2.85714 20.3715 2.89477 20.4379 2.96175C20.5044 3.02872 20.5417 3.11957 20.5417 3.21429V5.71429ZM24.0833 34.2857H21.1615L21.9583 11.4286H24.8802L24.0833 34.2857Z");
            trashIcon.setScaleX(0.4);
            trashIcon.setScaleY(0.4);
            deleteBtn.setGraphic(trashIcon);
            trashIcon.getStyleClass().add("trash-icon");
            deleteBtn.getStyleClass().add("delete-btn");
            deleteBtn.setOnAction(e-> deleteItem(product));
            HBox bottom = new HBox();
            bottom.setAlignment(Pos.CENTER_LEFT);
            Button minus = new Button("-");
            Label quantity = new Label(String.valueOf(product.getqCartItem()));
            Region spacer2 = new Region();
            bottom.setHgrow(spacer2, ALWAYS);
            Button plus = new Button("+");
            Label price = new Label(String.valueOf(product.getqPrice()));
            imageView.getStyleClass().add("cart-image");
            info.getStyleClass().add("cart-info");

            top.getStyleClass().add("cart-top");
            bottom.getStyleClass().add("cart-bottom");

            productTitle.getStyleClass().add("product-title");
            price.getStyleClass().add("product-price");
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);

            minus.getStyleClass().add("qty-btn");
            plus.getStyleClass().add("qty-btn");
            quantity.getStyleClass().add("qty-label");

        top.getChildren().addAll(productTitle,spacer,deleteBtn);
            bottom.getChildren().addAll(price, spacer2, minus, quantity, plus);
            info.getChildren().addAll(top, bottom);
            plus.setOnAction(e->handleQuantity(product.getId(), '+'));
            minus.setOnAction(e->handleQuantity(product.getId(), '-'));

            itemContainer.getChildren().addAll(imageView, info);

            itemContainer.getStyleClass().add("cartItemContainer");

        return itemContainer;
    }
    private void refreshCartDisplay(){
        cart.getChildren().clear();
        List<Product> itemsSnapshot = new ArrayList<>(cartManager.getCartItems());
        for(Product product : itemsSnapshot){
            cart.getChildren().add(createCartItem(product));
        }
    }
    private void handleQuantity(int productId, Character action){


            for(Product p : cartManager.getCartItems()){
                if(p.getId() == productId){
                    if(action == '+') {
                        p.setqCartItem(p.getqCartItem() + 1);
                        p.setqPrice(p.getPrice()*p.getqCartItem());
                        totalPrice.setText(String.valueOf(String.format("%.2f", calculateTotalPrice()))+" DH");

                    }
                    if(action == '-'){

                        p.setqCartItem(p.getqCartItem() - 1);
                        p.setqPrice(p.getqPrice()-p.getPrice());
                        totalPrice.setText(String.valueOf(String.format("%.2f", calculateTotalPrice()))+" DH");


                    }
                }
            }
        refreshCartDisplay();
    }
    private void  deleteItem(Product product){
        cartManager.getCartItems().remove(product);
        totalPrice.setText(String.valueOf(String.format("%.2f", calculateTotalPrice()))+" DH");

        refreshCartDisplay();
    }
    private double calculateTotalPrice(){
        double totalprice = 0;
        if(cartManager.getCartItems() != NULL) {
            for (Product product : cartManager.getCartItems()) {
                totalprice += product.getqPrice();
            }
        }
        return totalprice;
    }
    @FXML
    private void processSaleUpdates()  {
        try {
            // Run the logic
            cartManager.processSale(cartManager.getCartItems(), calculateTotalPrice());



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
