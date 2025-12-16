package com.boutique.gestionboutique.controller;

import com.boutique.gestionboutique.service.StatService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable {
    @FXML
    private Label serumCount;
    @FXML
    private Label makeupCount;
    @FXML
    private Label vitamineCount;
    @FXML
    private Label bioCount;
    @FXML
    private Label serumCount1;
    @FXML
    private Label makeupCount1;
    @FXML
    private Label vitamineCount1;
    @FXML
    private Label bioCount1;
    @FXML
    private Label todaySaleCount;
    @FXML
    private Label revenueForToday;
    @FXML
    private Label allTimeRevenue;

    @Override
    public void initialize(URL url, ResourceBundle resource){
        StatService statService = new StatService();
        serumCount.setText(statService.getProductCount("Sérums"));
        vitamineCount.setText(statService.getProductCount("Vitamines & Suppléments"));
        bioCount.setText(statService.getProductCount("Produits Bio (Soins & Divers)"));
        makeupCount.setText(statService.getProductCount("Maquillage"));
        serumCount.setText(statService.getProductCount("Sérums"));

        serumCount1.setText(statService.getTotalStock("Sérums"));
        vitamineCount1.setText(statService.getTotalStock("Vitamines & Suppléments"));
        bioCount1.setText(statService.getTotalStock("Produits Bio (Soins & Divers)"));
        makeupCount1.setText(statService.getTotalStock("Maquillage"));
        serumCount1.setText(statService.getTotalStock("Sérums"));

        todaySaleCount.setText(statService.getProductCount("Sérums"));
        revenueForToday.setText(statService.getTodayRevenue()+" DH");
        allTimeRevenue.setText(statService.getAllTimeRevnue()+" DH");
        todaySaleCount.setText(statService.getTodaySaleCount());
    }
}
