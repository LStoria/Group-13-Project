package controller;

import app.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.SocketClient;
import util.MessageFactory;

public class LoginController {
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        hostField.setText("localhost");
        portField.setText("8080");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String host = hostField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Nhap ten nguoi dung.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Port khong hop le.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Dang ket noi server...");

        Thread loginThread = new Thread(() -> {
            try {
                SocketClient client = MainApp.getSocketClient();
                client.setUsername(username);
                client.connect(host, port, message -> {
                    // HomeController se dang ky listener rieng sau khi man hinh home duoc mo.
                });
                client.sendRequest(MessageFactory.loginRequest(username, password));
                Platform.runLater(() -> {
                    try {
                        MainApp.showHome();
                    } catch (Exception ex) {
                        statusLabel.setText("Khong mo duoc man hinh chinh.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    statusLabel.setText("Ket noi that bai: " + ex.getMessage());
                });
            }
        }, "auction-login");
        loginThread.setDaemon(true);
        loginThread.start();
    }
}
