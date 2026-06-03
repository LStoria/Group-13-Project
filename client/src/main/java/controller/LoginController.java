package controller;

import app.MainApp;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    private void initialize() {
        hostField.setText("localhost");
        portField.setText("8080");
        roleCombo.setItems(FXCollections.observableArrayList("BIDDER", "SELLER"));
        roleCombo.getSelectionModel().select("BIDDER");
    }

    @FXML
    private void handleLogin() {
        connectAndSend(true);
    }

    @FXML
    private void handleRegister() {
        connectAndSend(false);
    }

    private void connectAndSend(boolean loginMode) {
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

        if (!loginMode && password.length() < 3) {
            statusLabel.setText("Password dang ky phai co it nhat 3 ky tu.");
            return;
        }

        loginButton.setDisable(true);
        registerButton.setDisable(true);
        statusLabel.setText(loginMode ? "Dang ket noi server..." : "Dang dang ky tai khoan...");

        Thread loginThread = new Thread(() -> {
            try {
                SocketClient client = MainApp.getSocketClient();
                client.closeConnection();
                client.setUsername(username);
                client.connect(host, port, message -> {
                    Platform.runLater(() -> handleAuthResponse(message, loginMode));
                });
                if (loginMode) {
                    client.sendRequest(MessageFactory.loginRequest(username, password));
                } else {
                    client.sendRequest(MessageFactory.registerRequest(username, password, roleCombo.getValue()));
                }
            } catch (Exception ex) {
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoginController.class);
                logger.error("Error during connectAndSend", ex);
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    registerButton.setDisable(false);
                    statusLabel.setText("Ket noi that bai: " + ex.getMessage());
                });
            }
        }, "auction-login");
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void handleAuthResponse(String message, boolean loginMode) {
        try {
            JsonObject json = MessageFactory.fromJson(message, JsonObject.class);
            String status = json.has("status") ? json.get("status").getAsString() : "ERROR";
            if (!"SUCCESS".equals(status)) {
                loginButton.setDisable(false);
                registerButton.setDisable(false);
                statusLabel.setText(json.has("message") ? json.get("message").getAsString() : "Dang nhap that bai.");
                return;
            }

            if (!loginMode) {
                MainApp.getSocketClient().closeConnection();
                loginButton.setDisable(false);
                registerButton.setDisable(false);
                statusLabel.setText("Dang ky thanh cong. Hay dang nhap bang tai khoan vua tao.");
                return;
            }

            String role = json.has("role") ? json.get("role").getAsString() : "BIDDER";
            MainApp.getSocketClient().setRole(role);
            if ("ADMIN".equals(role)) {
                MainApp.showAdmin();
            } else if ("SELLER".equals(role)) {
                MainApp.showSeller();
            } else {
                MainApp.showHome();
            }
        } catch (Exception ex) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoginController.class);
            logger.error("Error handling auth response", ex);
            loginButton.setDisable(false);
            registerButton.setDisable(false);
            statusLabel.setText("Phan hoi dang nhap khong hop le.");
        }
    }
}