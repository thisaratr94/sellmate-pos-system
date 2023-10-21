package lk.ijse.dep11.pos.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;

public class SplashScreenController {
    public AnchorPane root;

    public void initialize() {
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(1500));
        pauseTransition.setOnFinished(e -> {
            try{
                openMainScene();
                closeSplashScreen();

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        pauseTransition.play();
    }

    private void openMainScene() throws IOException {
        Stage stage = new Stage();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/MainForm.fxml"))));
        stage.setTitle("SellMate POS System");
        stage.centerOnScreen();
        stage.show();
    }

    private void closeSplashScreen() {
        Stage splashStage = (Stage) root.getScene().getWindow();
        splashStage.close();
    }

}
