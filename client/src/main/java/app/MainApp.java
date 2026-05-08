package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.SocketClient;

import java.io.IOException;

public class MainApp extends Application {
    private static Stage primaryStage;
    private static final SocketClient socketClient = new SocketClient();

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Auction Client");
        showLogin();
        primaryStage.show();
    }

    public static SocketClient getSocketClient() {
        return socketClient;
    }

    public static void showLogin() throws IOException {
        setScene("/view/login.fxml", 420, 320);
    }

    public static void showHome() throws IOException {
        setScene("/view/home.fxml", 900, 560);
    }

    private static void setScene(String fxmlPath, double width, double height) throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));
        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.setMinWidth(width);
        primaryStage.setMinHeight(height);
    }

    @Override
    public void stop() {
        socketClient.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
