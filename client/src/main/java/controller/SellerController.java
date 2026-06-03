package controller;

import app.MainApp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.AuctionItem;
import service.SocketClient;
import util.MessageFactory;

public class SellerController {
    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<AuctionItem> itemTable;
    @FXML private TableColumn<AuctionItem, Number> idColumn;
    @FXML private TableColumn<AuctionItem, String> nameColumn;
    @FXML private TableColumn<AuctionItem, String> typeColumn;
    @FXML private TableColumn<AuctionItem, Number> startPriceColumn;
    @FXML private TableColumn<AuctionItem, Number> currentPriceColumn;
    @FXML private TableColumn<AuctionItem, String> statusColumn;
    @FXML private TableColumn<AuctionItem, Number> timeColumn;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private TextArea descriptionArea;

    private final ObservableList<AuctionItem> items = FXCollections.observableArrayList();
    private SocketClient client;

    @FXML
    private void initialize() {
        client = MainApp.getSocketClient();
        usernameLabel.setText("Seller: " + client.getUsername());

        typeCombo.setItems(FXCollections.observableArrayList("Electronics", "Vehicle", "Art", "Other"));
        typeCombo.getSelectionModel().select("Electronics");

        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        startPriceColumn.setCellValueFactory(data -> data.getValue().startPriceProperty());
        currentPriceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        timeColumn.setCellValueFactory(data -> data.getValue().timeLeftProperty());
        itemTable.setItems(items);

        client.setMessageListener(message -> Platform.runLater(() -> handleServerMessage(message)));
        refreshItems();
    }

    @FXML
    private void refreshItems() {
        client.sendRequest(MessageFactory.viewMyItemsRequest(client.getUsername()));
        statusLabel.setText("Dang tai san pham cua seller...");
    }

    @FXML
    private void createItem() {
        String name = nameField.getText().trim();
        String type = typeCombo.getValue();
        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Gia khoi diem khong hop le.");
            return;
        }

        int duration = 120; // mặc định 120 giây
        String durationText = durationField.getText().trim();
        if (!durationText.isEmpty()) {
            try {
                duration = Integer.parseInt(durationText);
                if (duration <= 0) {
                    statusLabel.setText("Thoi gian dau gia phai lon hon 0 giay.");
                    return;
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Thoi gian dau gia khong hop le.");
                return;
            }
        }

        client.sendRequest(MessageFactory.createItemRequest(name, type, price, client.getUsername(), duration));
        descriptionArea.clear();
        durationField.clear();
    }

    @FXML
    private void openBidderView() {
        try {
            MainApp.showHome();
        } catch (Exception ex) {
            statusLabel.setText("Khong mo duoc man hinh dau gia.");
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
            if (json.has("data") && json.get("data").isJsonArray()) {
                loadItems(json.getAsJsonArray("data"));
                statusLabel.setText("Da tai san pham cua seller.");
            }
            if (json.has("status") && "SUCCESS".equals(json.get("status").getAsString())) {
                statusLabel.setText(json.has("message") ? json.get("message").getAsString() : "Thanh cong.");
                nameField.clear();
                priceField.clear();
                refreshItems();
            } else if (json.has("status") && "ERROR".equals(json.get("status").getAsString())) {
                statusLabel.setText(json.has("message") ? json.get("message").getAsString() : "Co loi xay ra.");
            }
            if (json.has("action")) {
                handleRealtimeAction(json);
            }
        } catch (Exception ex) {
            statusLabel.setText("Du lieu server khong hop le.");
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
            refreshItems();
        } else if ("TIME_TICK".equals(action) && json.has("items")) {
            for (JsonElement element : json.getAsJsonArray("items")) {
                JsonObject item = element.getAsJsonObject();
                updateItemTime(item.get("id").getAsInt(), item.get("timeLeft").getAsInt());
            }
        }
    }

    private void updateItemTime(int itemId, int timeLeft) {
        for (AuctionItem item : items) {
            if (item.getId() == itemId) {
                item.setTimeLeft(timeLeft);
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
