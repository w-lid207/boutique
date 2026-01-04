package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.model.Product;
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

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// ... SQL and Util imports ...
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

// --- iText Imports for PDF ---
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import javafx.stage.StageStyle;

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
                Image image = new Image(product.getImagePath(),
                        200,  // requestedWidth
                        200,  // requestedHeight
                        true, // preserveRatio
                        true, // smooth
                        true  // backgroundLoading
                );
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

        // 1. Calculate Total & Prepare Data
        double finalTotal = calculateTotalPrice();
        List<Product> purchasedItems = new ArrayList<>(cartManager.getCartItems());

        // 2. Show Custom Confirmation Dialog (Styled like your snippet)
        boolean wantsReceipt = showReceiptConfirmation(finalTotal);

        // 3. Generate PDF if confirmed
        if (wantsReceipt) {
            // call your PDF generation method here, e.g.:
            generateReceipt(purchasedItems, finalTotal);
            System.out.println("Receipt generation triggered."); // Placeholder
        }

        // 4. Process DB and Clear Cart
        try {
            ObservableList<Product> observableCart = FXCollections.observableArrayList(cartManager.getCartItems());
            cartManager.processSale(observableCart, finalTotal);
            cartManager.getCartItems().clear();
            refreshCartDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean showReceiptConfirmation(double totalAmount) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane pane = dialog.getDialogPane();
        pane.setGraphic(null);

        // --- Main Container ---
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-min-width: 450;");

        // --- Title ---
        Label title = new Label("Succès de la vente");
        title.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 22px; -fx-text-fill: #BEC400;");

        // --- Info Box (Green Background) ---
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 12;");

        Label msgLbl = new Label("La transaction a été enregistrée avec succès.");
        Label totalLbl = new Label("💰 Montant total: " + String.format("%.2f MAD", totalAmount));

        // Question Label
        Label askLbl = new Label("Voulez-vous télécharger le reçu PDF ?");
        askLbl.setStyle("-fx-font-family: 'Poppins Medium'; -fx-text-fill: #3d4a4d; -fx-padding: 10 0 0 0;");

        msgLbl.setStyle("-fx-font-family: 'Poppins Regular'; -fx-text-fill: #3d4a4d;");
        totalLbl.setStyle("-fx-font-family: 'Poppins Bold'; -fx-text-fill: #BEC400; -fx-font-size: 16px;");

        infoBox.getChildren().addAll(msgLbl, totalLbl, askLbl);

        root.getChildren().addAll(title, infoBox);
        pane.setContent(root);

        // --- Custom Buttons ---
        ButtonType yesType = new ButtonType("Télécharger", ButtonBar.ButtonData.OK_DONE);
        ButtonType noType = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

        pane.getButtonTypes().addAll(yesType, noType);

        // Styling the "Yes" button (Lime/Green)
        Button yesBtn = (Button) pane.lookupButton(yesType);
        yesBtn.setStyle("-fx-background-color: #BEC400; -fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-padding: 8 20; -fx-cursor: hand; -fx-font-family: 'Poppins Medium';");

        // Styling the "No" button (Grey/Subtle)
        Button noBtn = (Button) pane.lookupButton(noType);
        noBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; " +
                "-fx-padding: 8 20; -fx-cursor: hand; -fx-font-family: 'Poppins Regular';");

        // Logic: Return true if they clicked "Télécharger"
        return dialog.showAndWait().orElse(ButtonType.CANCEL) == yesType;
    }
    private void generateReceipt(List<Product> items, double totalAmount) {
        // 1. Open File Chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Receipt_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");

        // Get current stage
        Stage stage = (Stage) productsGrid.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // 2. Initialize PDF Writer (iText)
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // 3. Header
                document.add(new Paragraph("JTR SHOP")
                        .setBold()
                        .setFontSize(20)
                        .setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("EST AGADIR\nPhone: +212 600-000000")
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY));

                document.add(new Paragraph("\n------------------------------------------------------------------\n"));

                String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
                document.add(new Paragraph("Date: " + dateStr).setFontSize(10));

                // 4. Create Table
                float[] columnWidths = {4, 1, 2, 2};
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));
                table.setMarginTop(10);

                // Table Headers
                table.addHeaderCell(new Cell().add(new Paragraph("Item")).setBold().setBorder(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER));
                table.addHeaderCell(new Cell().add(new Paragraph("Qty")).setBold().setBorder(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER));
                table.addHeaderCell(new Cell().add(new Paragraph("Price")).setBold().setBorder(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER));
                table.addHeaderCell(new Cell().add(new Paragraph("Total")).setBold().setBorder(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER));

                // Table Rows
                for (Product p : items) {
                    table.addCell(new Cell().add(new Paragraph(p.getName())).setBorder(Border.NO_BORDER));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getqCartItem()))).setBorder(Border.NO_BORDER));

                    double unitPrice = p.getqCartItem() > 0 ? (p.getqPrice() / p.getqCartItem()) : 0;
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", unitPrice))).setBorder(Border.NO_BORDER));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", p.getqPrice()))).setBorder(Border.NO_BORDER));
                }

                document.add(table);

                // 5. Total & Footer
                document.add(new Paragraph("\n------------------------------------------------------------------\n"));
                Paragraph totalPara = new Paragraph(String.format("TOTAL: %.2f DH", totalAmount))
                        .setBold()
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.RIGHT);
                document.add(totalPara);

                document.add(new Paragraph("\n\nThank you for your purchase!")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setItalic());

                document.close();

                // --- CHANGED: Use Styled Alert for Success ---
                showStyledAlert("Succès", "Le reçu a été enregistré avec succès !");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                // --- CHANGED: Use Styled Alert for Error ---
                showStyledAlert("Erreur", "Impossible d'enregistrer le fichier (il est peut-être ouvert).");
            } catch (Exception e) {
                e.printStackTrace();
                showStyledAlert("Erreur", "Une erreur s'est produite lors de la création du reçu.");
            }
        }
    }
    // Helper method for alerts
    private void showStyledAlert(String titleText, String messageText) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane pane = dialog.getDialogPane();
        pane.setGraphic(null);

        // 1. Root Container
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-min-width: 400;");

        // 2. Title Label (Lime Color)
        Label title = new Label(titleText);
        title.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 22px; -fx-text-fill: #BEC400;");

        // 3. Message Container (Light Green Background)
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 12;");

        Label msgLbl = new Label(messageText);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-family: 'Poppins Regular'; -fx-text-fill: #3d4a4d; -fx-font-size: 14px;");

        infoBox.getChildren().add(msgLbl);

        root.getChildren().addAll(title, infoBox);
        pane.setContent(root);

        // 4. "OK" Button Styled
        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().add(okType);

        Button okBtn = (Button) pane.lookupButton(okType);
        okBtn.setStyle("-fx-background-color: #BEC400; -fx-text-fill: white; -fx-background-radius: 10; " +
                "-fx-padding: 8 25; -fx-cursor: hand; -fx-font-family: 'Poppins Medium';");

        dialog.showAndWait();
    }
}