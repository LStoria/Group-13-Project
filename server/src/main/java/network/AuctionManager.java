package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    // Danh sách các sản phẩm đang đấu giá (giả lập)
    private static List<Item> items = new ArrayList<>();

    static {
        // Khởi tạo một vài món đồ mẫu
        items.add(new Item(1, "Laptop Dell XPS", 1500.0, "admin"));
        items.add(new Item(2, "iPhone 15 Pro", 1000.0, "system"));
    }

    // Hàm lấy danh sách sản phẩm dưới dạng JSON để gửi cho Client
    public static String getAllItemsJson() {
        JsonArray array = new JsonArray();
        for (Item item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", item.id);
            obj.addProperty("name", item.name);
            obj.addProperty("price", item.price);
            obj.addProperty("lastBidder", item.lastBidder);
            array.add(obj);
        }
        return array.toString();
    }

    // Lớp nội bộ để mô tả sản phẩm
    static class Item {
        int id;
        String name;
        double price;
        String lastBidder;

        Item(int id, String name, double price, String bidder) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.lastBidder = bidder;
        }
    }
}