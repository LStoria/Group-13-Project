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
            obj.addProperty("price", item.currentPrice);
            obj.addProperty("winner", item.winner);
            array.add(obj);
        }
        return array.toString();
    }

    // Hàm cập nhật giá đấu giá cho một sản phẩm
    public static boolean updateBid(int itemId, double bidAmount, String username) {
        for (Item item : items) {
            if (item.id == itemId) {
                if (bidAmount > item.currentPrice) {
                    item.currentPrice = bidAmount;
                    item.winner = username;
                    return true;
                }
                return false;
            }
        }
        return false; // Không tìm thấy sản phẩm
    }

    // Lớp nội bộ để mô tả sản phẩm
    public static class Item {
        public int id;
        public String name;
        public double currentPrice;
        public String winner;
        public int timeLeft = 60; // 60 giây đếm ngược

        public Item(int id, String name, double price, String winner) {
            this.id = id;
            this.name = name;
            this.currentPrice = price;
            this.winner = winner;
        }
    }

    // Hàm đếm ngược:
    public static void startTimer(int itemId) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Đợi 1 giây
                    for (Item item : items) {
                        if (item.id == itemId && item.timeLeft > 0) {
                            item.timeLeft--;
                            if (item.timeLeft == 0) {
                                AuctionServer.broadcast("{\"action\":\"END\", \"item\":\"" + item.name + "\", \"winner\":\"" + item.winner + "\"}");
                                return;
                            }
                        }
                    }
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }
}