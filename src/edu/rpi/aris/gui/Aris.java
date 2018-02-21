package edu.rpi.aris.gui;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Aris extends Application implements Thread.UncaughtExceptionHandler {

    public static final String VERSION = "";

    private static Logger logger = LogManager.getLogger(Aris.class);

    private static boolean GUI = false;

    public static void main(String[] args) {
        Aris.launch(args);
    }

    public static boolean isGUI() {
        return GUI;
    }

    @Override
    public void start(Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(this);
        GUI = true;
        MainWindow controller = new MainWindow(stage);
        controller.show();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.fatal("He's dead, Jim!");
        logger.catching(e);
        if (GUI) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
            alert.getDialogPane().setPrefWidth(Region.USE_COMPUTED_SIZE);

            alert.getDialogPane().setPrefWidth(600);

            alert.setTitle("Fatal Error");
            alert.setHeaderText("He's dead, Jim!");
            alert.setContentText("A fatal error has occurred and Aris was unable to recover");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("Error details:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        }
        System.exit(1);
    }
}
