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
import javafx.scene.control.TextField;
import model.AuctionItem;
import service.SocketClient;
import util.MessageFactory;

public class BidController {
    @FXML private Label statusLabel;
    @FXML private TableView<AuctionItem> itemTable;
    @FXML private TableColumn<AuctionItem, Number> idColumn;
    @FXML private TableColumn<AuctionItem, String> nameColumn;
    @FXML private TableColumn<AuctionItem, Number> priceColumn;
    @FXML private TableColumn<AuctionItem, String> winnerColumn;
    @FXML private TableColumn<AuctionItem, String> statusColumn;
    @FXML private TextField selectedItemField;
    @FXML private TextField bidAmountField;

    private final ObservableList<AuctionItem> items = FXCollections.observableArrayList();
    private SocketClient client;

    @FXML
    private void initialize() {
        client = MainApp.getSocketClient();

        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        priceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty());
        winnerColumn.setCellValueFactory(data -> data.getValue().winnerProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        itemTable.setItems(items);
        itemTable.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            selectedItemField.setText(newItem == null ? "" : newItem.getName());
        });

        client.setMessageListener(message -> Platform.runLater(() -> handleServerMessage(message)));
        refreshItems();
    }

    @FXML
    private void handleBid() {
        AuctionItem selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            statusLabel.setText("Hay chon san pham can dau gia.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(bidAmountField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Gia dat khong hop le.");
            return;
        }

        if (amount <= selectedItem.getCurrentPrice()) {
            statusLabel.setText("Gia dat phai cao hon gia hien tai.");
            return;
        }
        if (!"ACTIVE".equals(selectedItem.getStatus())) {
            statusLabel.setText("Phien dau gia nay da ket thuc.");
            return;
        }

        client.sendRequest(MessageFactory.bidRequest(selectedItem.getId(), amount, client.getUsername()));
        bidAmountField.clear();
    }

    private void refreshItems() {
        client.sendRequest(MessageFactory.viewItemsRequest());
        statusLabel.setText("Dang tai danh sach san pham...");
    }

    private void handleServerMessage(String message) {
        try {
            JsonObject json = MessageFactory.fromJson(message, JsonObject.class);
            if (json.has("data") && json.get("data").isJsonArray()) {
                loadItems(json.getAsJsonArray("data"));
                statusLabel.setText("Da tai danh sach san pham.");
            }
            if (json.has("action")) {
                handleRealtimeAction(json);
            }
            if (json.has("message")) {
                statusLabel.setText(json.get("message").getAsString());
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
        if ("UPDATE_PRICE".equals(action)) {
            updateItem(json.get("itemId").getAsInt(), json.get("price").getAsDouble(), json.get("winner").getAsString());
        } else if ("ITEM_CREATED".equals(action)) {
            refreshItems();
        } else if ("END_AUCTION".equals(action) && json.has("item")) {
            JsonObject item = json.getAsJsonObject("item");
            updateItemStatus(item.get("id").getAsInt(), "ENDED");
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
        refreshItems();
    }

    private void updateItemStatus(int itemId, String status) {
        for (AuctionItem item : items) {
            if (item.getId() == itemId) {
                item.setStatus(status);
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
