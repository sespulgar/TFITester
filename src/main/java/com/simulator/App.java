package com.simulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App  extends Application {

    private static Logger logger = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        logger.info("Starting Hello JavaFX and Maven demonstration application");

        String fxmlFile = "/fxml/simulation.fxml";
        logger.info("Loading FXML for main view from: {}"+fxmlFile);
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

        logger.debug("Showing JFX scene");
        Scene scene = new Scene(rootNode, 600, 500);
        //scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Simulator");
        stage.setScene(scene);
        stage.show();
    }


}