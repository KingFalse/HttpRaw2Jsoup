package me.kagura;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("HttpRaw2Jsoup.fxml"));
        primaryStage.setTitle("HttpRaw2Jsoup v1.6");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("logo_x72.png")));
        primaryStage.show();
    }
}
