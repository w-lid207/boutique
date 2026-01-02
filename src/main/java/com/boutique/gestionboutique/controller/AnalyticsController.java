package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.StatService;
import com.boutique.gestionboutique.service.AnalyticService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Map;

import static javafx.collections.FXCollections.observableArrayList;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import java.text.DecimalFormat;
public class AnalyticsController {
    private StatService statService = new StatService();
    private AnalyticService analyticService = new AnalyticService();

    @FXML
    private FlowPane chartContainer; // We will clear this and add a Grid inside it
    @FXML
    private Label todaySales;
    @FXML
    private Label monthRevenue;
    @FXML
    private Label totalProducts;

    // Color Palette based on the image
    private final String COLOR_PRIMARY = "#5F7161";   // Dark Green
    private final String COLOR_ACCENT  = "#86A789";   // Sage Green
    private final String COLOR_BG      = "#FFFFFF";   // White Card
    private final String COLOR_TEXT    = "#666666";   // Grey Text

    @FXML
    public void initialize() {
        // 1. Setup Data Labels
        todaySales.setText(statService.getTodayRevenue() + " DH");
        monthRevenue.setText(statService.getMonthRevenue() + " DH");
        totalProducts.setText(statService.getTotalProductCount());

        // 2. Setup Grid Layout to match the image
        GridPane dashboardGrid = new GridPane();
        dashboardGrid.setHgap(20);
        dashboardGrid.setVgap(20);
        dashboardGrid.setPadding(new Insets(10));
        dashboardGrid.setAlignment(Pos.TOP_CENTER);

        // Define Columns: Col 1 is wider (65%), Col 2 is narrower (35%)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(65);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(35);
        dashboardGrid.getColumnConstraints().addAll(col1, col2);

        // 3. Create and Place Charts
        // Slot A: Top Left (Wide) -> Sales Last 12 Months
        Node revenueChart = monthlyRevenueChart();
        dashboardGrid.add(revenueChart, 0, 0);

        // Slot B: Top Right (Narrow/Donut) -> Best Selling Products (Pie transformed to Donut)
        Node productShareChart = bestSellingProductsChart();
        dashboardGrid.add(productShareChart, 1, 0);

        // Slot C: Bottom Left (Wide) -> Sales by Category
        Node categoryChart = salesByCategChart();
        dashboardGrid.add(categoryChart, 0, 1);

        // Slot D: Bottom Right (Narrow) -> Daily Sales (Area)
        Node dailyChart = dailySalesChart();
        dashboardGrid.add(dailyChart, 1, 1);

        // 4. Update the Container
        chartContainer.getChildren().clear();
        chartContainer.getChildren().add(dashboardGrid);

        // Ensure the FlowPane stretches the grid
        chartContainer.setAlignment(Pos.CENTER);
        // Bind width if possible, or assume FlowPane is wide enough
    }

    // --- Chart Generators ---

    private Node dailySalesChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        // Minimalist Axis Styling
        xAxis.setLabel("Day");
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);

        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setLegendVisible(false);
        areaChart.setCreateSymbols(false); // Remove dots on the line for a cleaner look

        // Data
        Map<String, Double> data = analyticService.chartData();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((a, b) -> series.getData().add(new XYChart.Data<>(a, b)));
        areaChart.getData().add(series);

        // Inline CSS for the Area Chart Fill (Green Gradient feel)
        series.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba(134, 167, 137, 0.4);");
        series.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: " + COLOR_PRIMARY + "; -fx-stroke-width: 2px;");

        return createCard("Daily Sales", "This week's trend", areaChart);
    }

    private Node monthlyRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Month");
        xAxis.setCategories(observableArrayList(
                List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        ));

        // Style Axis
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setBarGap(10);
        barChart.setCategoryGap(20);

        Map<String, Double> data = analyticService.monthlyData();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((a, b) -> series.getData().add(new XYChart.Data<>(a, b)));
        barChart.getData().add(series);

        // Set bar color to Green
        for (XYChart.Data<String, Number> item : series.getData()) {
            item.getNode().setStyle("-fx-bar-fill: " + COLOR_ACCENT + "; -fx-background-radius: 5 5 0 0;");
        }

        return createCard("Revenue Over Time", "Monthly view", barChart);
    }

    private Node bestSellingProductsChart() {
        // 1. Prepare Data
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        double totalSales = 0;

        // Fetch data
        Map<String, Double> rawData = analyticService.bestSellProData();
        for (Map.Entry<String, Double> entry : rawData.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            totalSales += entry.getValue();
        }

        final double finalTotalSales = totalSales;

        // 2. Configure Pie Chart - Clean & Minimal
        PieChart pieChart = new PieChart(pieData);
        pieChart.setLabelsVisible(false);  // No clutter
        pieChart.setLegendVisible(false);  // We'll create custom legend
        pieChart.setStartAngle(90);
        pieChart.setPrefSize(200, 200);
        pieChart.setMaxSize(200, 200);

        // 3. Create Donut Hole - Small and Empty
        Circle hole = new Circle(50);
        hole.setFill(Color.WHITE);
        hole.setStroke(Color.TRANSPARENT);

        // 4. Create Custom Legend (Clean List)
        VBox legend = new VBox(10);
        legend.setPadding(new Insets(10, 0, 10, 20));
        legend.setAlignment(Pos.CENTER_LEFT);

        // Define colors matching CSS
        String[] colors = {"#5F7161", "#86A789", "#B2C8BA", "#DADDB1", "#EAE7B1", "#A79B89"};

        int colorIndex = 0;
        for (PieChart.Data data : pieChart.getData()) {
            // Apply color to slice
            String color = colors[colorIndex % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");

            // Create legend item
            HBox legendItem = new HBox(8);
            legendItem.setAlignment(Pos.CENTER_LEFT);

            // Color indicator (small circle)
            Circle indicator = new Circle(5);
            indicator.setFill(Color.web(color));

            // Product name and percentage
            double percentage = (data.getPieValue() / finalTotalSales) * 100;
            Label nameLabel = new Label(data.getName());
            nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2d3b36;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label percentLabel = new Label(String.format("%.0f%%", percentage));
            percentLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666666;");

            legendItem.getChildren().addAll(indicator, nameLabel, spacer, percentLabel);
            legendItem.setMinWidth(140);
            legend.getChildren().add(legendItem);

            // Add hover tooltip for product name and percentage only
            String tipText = data.getName() + " - " + String.format("%.1f%%", percentage);
            Tooltip tooltip = new Tooltip(tipText);
            tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;");
            tooltip.setShowDelay(Duration.millis(200));
            Tooltip.install(data.getNode(), tooltip);

            colorIndex++;
        }

        // 5. Assemble Layout
        StackPane donutChart = new StackPane(pieChart, hole);
        donutChart.setMaxWidth(200);

        HBox chartWithLegend = new HBox(15, donutChart, legend);
        chartWithLegend.setAlignment(Pos.CENTER);
        chartWithLegend.setPadding(new Insets(10));

        return createCard("Best Selling", "Top products by revenue", chartWithLegend);
    }
    private Node salesByCategChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Category");
        yAxis.setLabel("Revenue (DH)");
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);

        Map<String, Double> data = analyticService.salesByCategData();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((a, b) -> series.getData().add(new XYChart.Data<>(a, b)));
        barChart.getData().add(series);

        // Style bars darker green
        for (XYChart.Data<String, Number> item : series.getData()) {
            item.getNode().setStyle("-fx-bar-fill: " + COLOR_PRIMARY + "; -fx-background-radius: 0 5 5 0;");
        }

        return createCard("Category Sales", "Revenue by category", barChart);
    }

    // --- Helper for "Card" Look ---

    private Node createCard(String title, String subtitle, Node content) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 15;");

        // Shadow Effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        shadow.setOffsetY(5);
        card.setEffect(shadow);

        // Header
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        HBox header = new HBox(10, titleLbl, subLbl);
        header.setAlignment(Pos.BASELINE_LEFT);

        // Make content grow
        VBox.setVgrow(content, Priority.ALWAYS);

        card.getChildren().addAll(header, content);

        // Set fixed height for consistency
        card.setPrefHeight(300);

        return card;
    }
}