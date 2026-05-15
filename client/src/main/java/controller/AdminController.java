package controller;

import app.MainApp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.AuctionItem;
import model.UserAccountView;
import service.SocketClient;
import util.MessageFactory;

public class AdminController {
    @FXML private Label statusLabel;
    @FXML private TableView<UserAccountView> userTable;
    @FXML private TableColumn<UserAccountView, String> usernameColumn;
    @FXML private TableColumn<UserAccountView, String> roleColumn;
    @FXML private TableView<AuctionItem> itemTable;
    @FXML private TableColumn<AuctionItem, Number> itemIdColumn;
    @FXML private TableColumn<AuctionItem, String> itemNameColumn;
    @FXML private TableColumn<AuctionItem, String> itemSellerColumn;
    @FXML private TableColumn<AuctionItem, Number> itemPriceColumn;
    @FXML private TableColumn<AuctionItem, String> itemStatusColumn;

    private final ObservableList<UserAccountView> users = FXCollections.observableArrayList();
    private final ObservableList<AuctionItem> items = FXCollections.observableArrayList();
    private SocketClient client;

    @FXML
    private void initialize() {
        client = MainApp.getSocketClient();

        usernameColumn.setCellValueFactory(data -> data.getValue().usernameProperty());
        roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());
        userTable.setItems(users);

        itemIdColumn.setCellValueFactory(data -> data.getValue().idProperty());
        itemNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        itemSellerColumn.setCellValueFactory(data -> data.getValue().sellerProperty());
        itemPriceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty());
        itemStatusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        itemTable.setItems(items);

        client.setMessageListener(message -> Platform.runLater(() -> handleServerMessage(message)));
        refreshAll();
    }

    @FXML
    private void refreshAll() {
        client.sendRequest(MessageFactory.viewUsersRequest());
        client.sendRequest(MessageFactory.viewItemsRequest());
        statusLabel.setText("Dang tai du lieu admin...");
    }

    @FXML
    private void openAuctionRoom() {
        try {
            MainApp.showHome();
        } catch (Exception ex) {
            statusLabel.setText("Khong mo duoc phong dau gia.");
        }
    }

    @FXML
    private void handleLogout() {
        client.closeConnection();
        try {
            MainApp.showLogin();
        } catch (Exception ex) {
            statusLabel.setText("Khong quay lai duoc man hinh dang nhap.");
        }
    }

    private void handleServerMessage(String message) {
        try {
            JsonObject json = MessageFactory.fromJson(message, JsonObject.class);
            if (json.has("users") && json.get("users").isJsonArray()) {
                loadUsers(json.getAsJsonArray("users"));
            }
            if (json.has("data") && json.get("data").isJsonArray()) {
                loadItems(json.getAsJsonArray("data"));
            }
            if (json.has("action")) {
                handleRealtimeAction(json);
            }
            if (json.has("message")) {
                statusLabel.setText(json.get("message").getAsString());
            } else {
                statusLabel.setText("Da tai du lieu admin.");
            }
        } catch (Exception ex) {
            statusLabel.setText("Du lieu server khong hop le.");
        }
    }

    private void loadUsers(JsonArray data) {
        users.clear();
        for (JsonElement element : data) {
            JsonObject user = element.getAsJsonObject();
            users.add(new UserAccountView(
                    user.get("username").getAsString(),
                    user.get("role").getAsString()
            ));
        }
    }

    private void loadItems(JsonArray data) {
        items.clear();
        for (JsonElement element : data) {
            JsonObject item = element.getAsJsonObject();
            items.add(new AuctionItem(
                    item.get("id").getAsInt(),
                    item.get("name").getAsString(),
                    getString(item, "type", ""),
                    getDouble(item, "startPrice", item.get("price").getAsDouble()),
                    item.get("price").getAsDouble(),
                    getString(item, "winner", "-"),
                    getString(item, "seller", ""),
                    getString(item, "status", "ACTIVE"),
                    getInt(item, "timeLeft", 0)
            ));
        }
    }

    private void handleRealtimeAction(JsonObject json) {
        String action = json.get("action").getAsString();
        if ("ITEM_CREATED".equals(action) || "END_AUCTION".equals(action)) {
            client.sendRequest(MessageFactory.viewItemsRequest());
        } else if ("UPDATE_PRICE".equals(action)) {
            updateItem(json.get("itemId").getAsInt(), json.get("price").getAsDouble(), json.get("winner").getAsString());
        }
    }

    private void updateItem(int itemId, double price, String winner) {
        for (AuctionItem item : items) {
            if (item.getId() == itemId) {
                item.setCurrentPrice(price);
                item.setWinner(winner);
                return;
            }
        }
    }

    private String getString(JsonObject item, String key, String defaultValue) {
        return item.has(key) && !item.get(key).isJsonNull() ? item.get(key).getAsString() : defaultValue;
    }

    private int getInt(JsonObject item, String key, int defaultValue) {
        return item.has(key) && !item.get(key).isJsonNull() ? item.get(key).getAsInt() : defaultValue;
    }

    private double getDouble(JsonObject item, String key, double defaultValue) {
        return item.has(key) && !item.get(key).isJsonNull() ? item.get(key).getAsDouble() : defaultValue;
    }
}
