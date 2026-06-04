package controller;

import app.MainApp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import model.AuctionItem;
import service.SocketClient;
import util.MessageFactory;

import java.time.LocalDateTime;

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


        //log
        System.out.println("BID CONTROLLER INITIALIZED");



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


        //client.setMessageListener(
        //        message -> Platform.runLater(
        //                () -> handleServerMessage(message)
        //        )
        //);


        client.setMessageListener(message -> {

            System.out.println(
                    "BID CONTROLLER RECEIVED => "
                            + message
            );

            Platform.runLater(
                    () -> handleServerMessage(message)
            );
        });


        refreshItems();

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        e -> {
                            for (AuctionItem item : items) {
                                item.updateTimeLeft();
                            }
                            itemTable.refresh();
                        }
                )
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
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
        if ("ENDED".equals(selectedItem.getStatus())) {
            statusLabel.setText("Phien dau gia nay da ket thuc.");
            return;
        }

        client.sendRequest(MessageFactory.bidRequest(selectedItem.getId(), amount, client.getUsername()));
        bidAmountField.clear();
    }

    private void refreshItems() {


        //log

        System.out.println("REQUESTING ITEMS");



        client.sendRequest(MessageFactory.viewItemsRequest());
        statusLabel.setText("Dang tai danh sach san pham...");
    }

    private void handleServerMessage(String message) {

        //log

        System.out.println("AAAAAAAAAAAA BID CONTROLLER");

        System.out.println("HOME RECEIVED = " + message);
        System.out.println("BID CONTROLLER RECEIVED:");
        System.out.println(message);


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

        //log
        System.out.println("LOAD ITEMS CALLED");
        System.out.println("LOAD ITEMS: " + data.size());

        items.clear();

        for (JsonElement element : data) {

            JsonObject item = element.getAsJsonObject();

            LocalDateTime endTime =
                    LocalDateTime.parse(
                            getString(
                                    item,
                                    "endTime",
                                    LocalDateTime.now().toString()
                            )
                    );

            items.add(new AuctionItem(
                    getInt(item, "id", 0),
                    getString(item, "name", ""),
                    getString(item, "type", ""),
                    getDouble(item, "startPrice", 0),
                    getDouble(item, "currentPrice", 0),
                    getString(item, "winner", "-"),
                    getString(item, "seller", ""),
                    getString(item, "status", "OPEN"),
                    endTime,
                    getString(item, "imageBase64", "")
            ));
        }


        //log
        System.out.println("ITEMS SIZE = " + items.size());
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
