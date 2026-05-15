package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionManager {
    private static final int DEFAULT_AUCTION_SECONDS = 120;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_FILE = Path.of("data", "auction-data.json");
    private static final boolean PERSISTENCE_ENABLED = !System.getProperty("java.class.path", "").contains("junit");
    private static final List<Item> items = new ArrayList<>();
    private static final Map<String, UserAccount> users = new HashMap<>();
    private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "auction-timer");
        thread.setDaemon(true);
        return thread;
    });
    private static int nextItemId = 1;

    static {
        loadData();
        startGlobalTimer();
    }

    public static synchronized JsonObject authenticate(String username, String password) {
        JsonObject response = new JsonObject();
        UserAccount account = users.get(username);

        if (account == null || !account.passwordHash.equals(hashPassword(password))) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Sai username hoặc password.");
            return response;
        }

        response.addProperty("status", "SUCCESS");
        response.addProperty("message", "Đăng nhập thành công!");
        response.addProperty("username", account.username);
        response.addProperty("role", account.role);
        return response;
    }

    public static synchronized JsonObject register(String username, String password, String role) {
        JsonObject response = new JsonObject();
        if (username == null || username.isBlank()) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Username không được trống.");
            return response;
        }
        if (password == null || password.length() < 3) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Password phải có ít nhất 3 ký tự.");
            return response;
        }

        String normalizedUsername = username.trim();
        if (users.containsKey(normalizedUsername)) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Username đã tồn tại.");
            return response;
        }

        UserAccount account = new UserAccount(normalizedUsername, hashPassword(password), normalizeRole(role));
        users.put(account.username, account);
        saveData();

        response.addProperty("status", "SUCCESS");
        response.addProperty("message", "Đăng ký tài khoản thành công.");
        response.addProperty("username", account.username);
        response.addProperty("role", account.role);
        return response;
    }

    public static synchronized String getAllItemsJson() {
        return getItemsJson(null);
    }

    public static synchronized String getItemsJson(String seller) {
        JsonArray array = new JsonArray();
        for (Item item : items) {
            if (seller == null || seller.equals(item.seller)) {
                array.add(item.toJson());
            }
        }
        return array.toString();
    }

    public static synchronized String getUsersJson() {
        JsonArray array = new JsonArray();
        for (UserAccount account : users.values()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", account.username);
            obj.addProperty("role", account.role);
            array.add(obj);
        }
        return array.toString();
    }

    public static synchronized JsonObject createItem(String name, String type, double startPrice, String seller) {
        JsonObject response = new JsonObject();
        if (name == null || name.isBlank()) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Tên sản phẩm không được trống.");
            return response;
        }
        if (startPrice <= 0) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Giá khởi điểm phải lớn hơn 0.");
            return response;
        }

        Item item = addItem(name.trim(), normalizeType(type), startPrice, seller);
        response.addProperty("status", "SUCCESS");
        response.addProperty("message", "Tạo sản phẩm đấu giá thành công.");
        response.add("item", item.toJson());
        saveData();
        return response;
    }

    public static synchronized boolean updateBid(int itemId, double bidAmount, String username) {
        for (Item item : items) {
            if (item.id == itemId) {
                if ("ACTIVE".equals(item.status) && bidAmount > item.currentPrice) {
                    item.currentPrice = bidAmount;
                    item.winner = username;
                    saveData();
                    return true;
                }
                return false;
            }
        }
        return false; // Không tìm thấy sản phẩm
    }

    private static Item addItem(String name, String type, double price, String seller) {
        Item item = new Item(nextItemId++, name, type, price, seller);
        items.add(item);
        return item;
    }

    private static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "Other";
        }
        return type.trim();
    }

    private static String normalizeRole(String role) {
        if ("SELLER".equals(role) || "ADMIN".equals(role)) {
            return role;
        }
        return "BIDDER";
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 không khả dụng.", ex);
        }
    }

    private static void loadData() {
        if (!PERSISTENCE_ENABLED) {
            seedData();
            return;
        }
        if (Files.exists(DATA_FILE)) {
            try {
                DataStore dataStore = GSON.fromJson(Files.readString(DATA_FILE, StandardCharsets.UTF_8), DataStore.class);
                if (dataStore != null) {
                    items.clear();
                    users.clear();
                    if (dataStore.items != null) {
                        items.addAll(dataStore.items);
                    }
                    if (dataStore.users != null) {
                        for (UserAccount account : dataStore.users) {
                            users.put(account.username, account);
                        }
                    }
                    nextItemId = Math.max(1, dataStore.nextItemId);
                    ensureSeedData();
                    return;
                }
            } catch (IOException ex) {
                System.err.println("Không đọc được dữ liệu lưu trữ: " + ex.getMessage());
            }
        }
        seedData();
        saveData();
    }

    private static void seedData() {
        users.clear();
        items.clear();
        nextItemId = 1;
        users.put("admin", new UserAccount("admin", hashPassword("admin"), "ADMIN"));
        users.put("seller", new UserAccount("seller", hashPassword("seller"), "SELLER"));
        users.put("bidder", new UserAccount("bidder", hashPassword("bidder"), "BIDDER"));
        addItem("Laptop Dell XPS", "Electronics", 1500.0, "seller");
        addItem("iPhone 15 Pro", "Electronics", 1000.0, "seller");
    }

    private static void ensureSeedData() {
        boolean changed = false;
        if (!users.containsKey("admin")) {
            users.put("admin", new UserAccount("admin", hashPassword("admin"), "ADMIN"));
            changed = true;
        }
        if (!users.containsKey("seller")) {
            users.put("seller", new UserAccount("seller", hashPassword("seller"), "SELLER"));
            changed = true;
        }
        if (!users.containsKey("bidder")) {
            users.put("bidder", new UserAccount("bidder", hashPassword("bidder"), "BIDDER"));
            changed = true;
        }
        if (items.isEmpty()) {
            addItem("Laptop Dell XPS", "Electronics", 1500.0, "seller");
            addItem("iPhone 15 Pro", "Electronics", 1000.0, "seller");
            changed = true;
        }
        if (changed) {
            saveData();
        }
    }

    private static void saveData() {
        if (!PERSISTENCE_ENABLED) {
            return;
        }
        try {
            Files.createDirectories(DATA_FILE.getParent());
            DataStore dataStore = new DataStore();
            dataStore.nextItemId = nextItemId;
            dataStore.items = new ArrayList<>(items);
            dataStore.users = new ArrayList<>(users.values());
            Files.writeString(DATA_FILE, GSON.toJson(dataStore), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Không lưu được dữ liệu: " + ex.getMessage());
        }
    }

    private static void startGlobalTimer() {
        timer.scheduleAtFixedRate(() -> {
            JsonArray ticks = new JsonArray();
            JsonArray ended = new JsonArray();
            synchronized (AuctionManager.class) {
                for (Item item : items) {
                    if ("ACTIVE".equals(item.status) && item.timeLeft > 0) {
                        item.timeLeft--;
                        JsonObject tick = new JsonObject();
                        tick.addProperty("id", item.id);
                        tick.addProperty("name", item.name);
                        tick.addProperty("timeLeft", item.timeLeft);
                        ticks.add(tick);
                        if (item.timeLeft == 0) {
                            item.status = "ENDED";
                            ended.add(item.toJson());
                            saveData();
                        }
                    }
                }
            }

            if (ticks.size() > 0) {
                JsonObject event = new JsonObject();
                event.addProperty("action", "TIME_TICK");
                event.add("items", ticks);
                AuctionServer.broadcast(event.toString());
            }
            for (int i = 0; i < ended.size(); i++) {
                JsonObject event = new JsonObject();
                JsonObject item = ended.get(i).getAsJsonObject();
                event.addProperty("action", "END_AUCTION");
                event.add("item", item);
                event.addProperty("message", "Phiên đấu giá đã kết thúc: " + item.get("name").getAsString());
                AuctionServer.broadcast(event.toString());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static class Item {
        public int id;
        public String name;
        public String type;
        public double startPrice;
        public double currentPrice;
        public String winner;
        public String seller;
        public String status = "ACTIVE";
        public int timeLeft = DEFAULT_AUCTION_SECONDS;

        public Item(int id, String name, String type, double price, String seller) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.startPrice = price;
            this.currentPrice = price;
            this.seller = seller;
            this.winner = "";
        }

        private JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", id);
            obj.addProperty("name", name);
            obj.addProperty("type", type);
            obj.addProperty("startPrice", startPrice);
            obj.addProperty("price", currentPrice);
            obj.addProperty("winner", winner == null || winner.isBlank() ? "-" : winner);
            obj.addProperty("seller", seller);
            obj.addProperty("status", status);
            obj.addProperty("timeLeft", timeLeft);
            return obj;
        }
    }

    private static class DataStore {
        private int nextItemId;
        private List<Item> items;
        private List<UserAccount> users;
    }

    private record UserAccount(String username, String passwordHash, String role) {
    }
}
