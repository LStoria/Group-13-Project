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

public class HomeController {
    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<AuctionItem> itemTable;
    @FXML private TableColumn<AuctionItem, Number> idColumn;
    @FXML private TableColumn<AuctionItem, String> nameColumn;
    @FXML private TableColumn<AuctionItem, Number> priceColumn;
    @FXML private TableColumn<AuctionItem, String> winnerColumn;
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
        winnerColumn.setCellValueFactory(data -> data.getValue().winnerProperty());
        itemTable.setItems(items);

        client.setMessageListener(message -> Platform.runLater(() -> handleServerMessage(message)));
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

        client.sendRequest(MessageFactory.bidRequest(selectedItem.getId(), amount, client.getUsername()));
        bidAmountField.clear();
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
                    item.get("price").getAsDouble(),
                    item.get("winner").getAsString()
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
            statusLabel.setText("Gia san pham #" + itemId + " vua duoc cap nhat.");
        } else if ("END".equals(action) || "END_AUCTION".equals(action)) {
            statusLabel.setText("Phien dau gia da ket thuc.");
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
}
