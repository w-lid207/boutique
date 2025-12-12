module com.boutique.gestionboutique {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.boutique.gestionboutique to javafx.fxml;
    exports com.boutique.gestionboutique;
    exports com.boutique.gestionboutique.controller;
    opens com.boutique.gestionboutique.controller to javafx.fxml;
}