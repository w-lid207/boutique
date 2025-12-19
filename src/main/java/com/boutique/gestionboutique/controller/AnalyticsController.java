package com.boutique.gestionboutique.controller;
import com.boutique.gestionboutique.service.StatService;
import com.boutique.gestionboutique.service.AnalyticService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.Map;

import static javafx.collections.FXCollections.observableArrayList;


public class AnalyticsController {
    private StatService statService = new StatService();
    private AnalyticService analyticService = new AnalyticService();
    @FXML
    private FlowPane chartContainer;
    @FXML
    private Label todaySales;
    @FXML
    private Label monthRevenue;
    @FXML
    private Label totalProducts;

    @FXML
    public void initialize(){
        dailySalesChart();
        monthlyRevenueChart();
        bestSellingProductsChart();
        salesByCategChart();
        todaySales.setText(statService.getTodayRevenue() + " DH");
        monthRevenue.setText(statService.getMonthRevenue() + " DH");
        totalProducts.setText(statService.getTotalProductCount());
    }

    @FXML
    private void dailySalesChart(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 600, 100);
        xAxis.setLabel("Day");
        yAxis.setLabel("Revenue (DH)");


        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("Daily Sales");
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        areaChart.setLegendVisible(false); // Add this line!

        Map<String, Double> data = analyticService.chartData();

        data.forEach((a,b)->{
            salesSeries.getData().add(new XYChart.Data<>(a, b));

        });

        areaChart.getData().add(salesSeries);
        chartContainer.getChildren().add(areaChart);

    }
    @FXML
    private void monthlyRevenueChart(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 12000, 4000);
        xAxis.setLabel("Month");
        xAxis.setCategories(observableArrayList(
                List.of("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        ));
        yAxis.setLabel("Revenue (DH)");


        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Last 12 Months");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        barChart.setLegendVisible(false); // Add this line!

        Map<String, Double> data = analyticService.monthlyData();

        data.forEach((a,b)->{
            series.getData().add(new XYChart.Data<>(a, b));

        });

        barChart.getData().add(series);
        chartContainer.getChildren().add(barChart);

    }
    private void bestSellingProductsChart(){
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        analyticService.bestSellProData().forEach((a, b)->{
            pieData.add(new PieChart.Data(a,b));
        });

        PieChart pieChart = new PieChart(pieData);
        pieChart.setLabelsVisible(false);

        pieChart.setTitle("Best Selling Products");
        chartContainer.getChildren().add(pieChart);

    }
    private void salesByCategChart(){
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 1000, 300);
        xAxis.setLabel("Category");
        yAxis.setLabel("Revenue (DH)");


        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Sales by Category");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        barChart.setLegendVisible(false); // Add this line!

        Map<String, Double> data = analyticService.salesByCategData();

        data.forEach((a,b)->{
            series.getData().add(new XYChart.Data<>(a, b));

        });

        barChart.getData().add(series);
        chartContainer.getChildren().add(barChart);

    }


}
