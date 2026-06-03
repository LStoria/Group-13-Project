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
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public class HomeController {
    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Button goToSellerButton;
    @FXML private TableView<AuctionItem> itemTable;
    @FXML private TableColumn<AuctionItem, Number> idColumn;
    @FXML private TableColumn<AuctionItem, String> nameColumn;
    @FXML private TableColumn<AuctionItem, Number> priceColumn;
    @FXML private TableColumn<AuctionItem, String> winnerColumn;
    @FXML private TableColumn<AuctionItem, String> statusColumn;
    @FXML private TableColumn<AuctionItem, Number> timeColumn;
    @FXML private TextField bidAmountField;

    private final ObservableList<AuctionItem> items = FXCollections.observableArrayList();
    private SocketClient client;

    @FXML
    private void initialize() {
        client = MainApp.getSocketClient();
        usernameLabel.setText("User: " + client.getUsername());

        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        priceColumn.setCellValueFactory(data -> data.getValue().currentPriceProperty());
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", value.doubleValue()));
                }
            }
        });
        winnerColumn.setCellValueFactory(data -> data.getValue().winnerProperty());
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        }
        if (timeColumn != null) {
            timeColumn.setCellValueFactory(data -> data.getValue().timeLeftProperty());
        }
        itemTable.setItems(items);

        client.setMessageListener(message -> Platform.runLater(() -> handleServerMessage(message)));

        // Hiện nút "Quay lại Seller" nếu người dùng là Seller
        if (goToSellerButton != null) {
            goToSellerButton.setVisible("SELLER".equals(client.getRole()));
            goToSellerButton.setManaged("SELLER".equals(client.getRole()));
        }

        refreshItems();
    }

    @FXML
    private void refreshItems() {
        client.sendRequest(MessageFactory.viewItemsRequest());
        statusLabel.setText("Dang tai danh sach san pham...");
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

    @FXML
    private void handleGoToSeller() {
        try {
            MainApp.showSeller();
        } catch (Exception ex) {
            statusLabel.setText("Khong mo duoc man hinh seller.");
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
            int itemId = json.get("itemId").getAsInt();
            double price = json.get("price").getAsDouble();
            String winner = json.get("winner").getAsString();
            updateItem(itemId, price, winner);
            // Cập nhật timeLeft nếu có (sau khi gia hạn)
            if (json.has("timeLeft")) {
                updateItemTime(itemId, json.get("timeLeft").getAsInt());
            }
            statusLabel.setText("Gia san pham #" + itemId + " vua duoc cap nhat.");
        } else if ("END".equals(action) || "END_AUCTION".equals(action)) {
            if (json.has("item") && json.get("item").isJsonObject()) {
                JsonObject item = json.getAsJsonObject("item");
                updateItemStatus(item.get("id").getAsInt(), "ENDED", 0);
            }
            statusLabel.setText(json.has("message") ? json.get("message").getAsString() : "Phien dau gia da ket thuc.");
        } else if ("TIME_TICK".equals(action) && json.has("items") && json.get("items").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("items")) {
                JsonObject item = element.getAsJsonObject();
                updateItemTime(item.get("id").getAsInt(), item.get("timeLeft").getAsInt());
            }
        } else if ("ITEM_CREATED".equals(action)) {
            refreshItems();
        } else if ("AUCTION_EXTENDED".equals(action)) {
            int itemId = json.get("itemId").getAsInt();
            int timeLeft = json.get("timeLeft").getAsInt();
            updateItemTime(itemId, timeLeft);
            statusLabel.setText(json.has("message") ? json.get("message").getAsString() : "Phien dau gia duoc gia han!");
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

    private void updateItemTime(int itemId, int timeLeft) {
        for (AuctionItem item : items) {
            if (item.getId() == itemId) {
                item.setTimeLeft(timeLeft);
                return;
            }
        }
    }

    private void updateItemStatus(int itemId, String status, int timeLeft) {
        for (AuctionItem item : items) {
            if (item.getId() == itemId) {
                item.setStatus(status);
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