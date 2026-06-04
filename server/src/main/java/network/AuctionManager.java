package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.auction.Auction;
import model.auction.BidTransaction;
import model.item.Art;
import model.item.Electronics;
import model.item.Vehicle;
import model.user.User;

import repository.repointerface.BidTransactionRepository;
import repository.repointerface.ItemRepository;
import repository.repointerface.AuctionRepository;

import repository.repointerface.UserRepository;
import repository.sqlite.SQLiteBidTransactionRepository;
import repository.sqlite.SQLiteItemRepository;
import repository.sqlite.SQLiteAuctionRepository;
import repository.sqlite.SQLiteUserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionManager {
    private static final int DEFAULT_AUCTION_SECONDS = 120;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_FILE = Path.of("data", "auction-data.json");
    private static final boolean PERSISTENCE_ENABLED = !System.getProperty("java.class.path", "").contains("junit");
    public static final List<Item> items = new ArrayList<>();
    private static final Map<String, UserAccount> users = new HashMap<>();
    private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "auction-timer");
        thread.setDaemon(true);
        return thread;
    });
    private static int nextItemId = 1;

    static {
        loadData();
    }

    private static final UserRepository userRepo =
            new SQLiteUserRepository();

    private static final ItemRepository itemRepo =
            new SQLiteItemRepository();

    private static final AuctionRepository auctionRepo =
            new SQLiteAuctionRepository();

    public static synchronized JsonObject authenticate(
            String username,
            String password)
    {
        JsonObject response = new JsonObject();

        Optional<User> userOpt =
                userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Sai username hoặc password.");
            return response;
        }

        User user = userOpt.get();

        if (!user.getPasswordHash()
                .equals(hashPassword(password))) {

            response.addProperty("status", "ERROR");
            response.addProperty("message", "Sai username hoặc password.");
            return response;
        }

        response.addProperty("status", "SUCCESS");
        response.addProperty("username", user.getUsername());
        response.addProperty(
                "role",
                user.getRole()
        );

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

        String normalizedRole =
                normalizeRole(role);

        String hashedPassword =
                hashPassword(password);

        final UserRepository userRepo =
                new SQLiteUserRepository();

        if (userRepo.existsByUsername(username)) {
            response.addProperty("status", "ERROR");
            response.addProperty("message", "Username đã tồn tại.");
            return response;
        }

        User user =
                new User(
                        username,
                        username + "@local",
                        hashedPassword,
                        role
                );

        userRepo.save(user);

        response.addProperty("status", "SUCCESS");
        response.addProperty("message", "Đăng ký tài khoản thành công.");
        return response;
    }

    public static synchronized String getAllItemsJson() {
        return getItemsJson(null);
    }

    public static synchronized String getItemsJson(String seller) {


        //log
        System.out.println("GET ITEMS JSON CALLED");
        System.out.println("SELLER FILTER = " + seller);



        JsonArray array = new JsonArray();

        List<Auction> auctions =
                auctionRepo.findAll();

        for (Auction auction : auctions) {

            model.item.Item item =
                    auction.getItem();

            if (item == null) {
                continue;
            }

            System.out.println(
                    "COMPARE: filter=" + seller
                            + " itemSeller="
                            + (item.getSeller() == null
                            ? "NULL"
                            : item.getSeller().getUsername())
            );

            if (seller != null
                    && item.getSeller() != null
                    && !seller.equals(
                    item.getSeller().getUsername())) {

                continue;
            }

            JsonObject obj =
                    new JsonObject();

            obj.addProperty(
                    "id",
                    auction.getId()
            );

            obj.addProperty(
                    "name",
                    item.getName()
            );

            obj.addProperty(
                    "type",
                    item.getType()
            );

            obj.addProperty(
                    "seller",
                    item.getSeller() != null
                            ? item.getSeller().getUsername()
                            : ""
            );

            obj.addProperty(
                    "startPrice",
                    item.getStartPrice()
            );

            obj.addProperty(
                    "currentPrice",
                    auction.getCurrentPrice()
            );

            obj.addProperty(
                    "status",
                    auction.getStatus().name()
            );

            obj.addProperty(
                    "endTime",
                    auction.getEndTime().toString()
            );

            array.add(obj);

        }

        return array.toString();
    }

    public static synchronized String getUsersJson() {
        JsonArray array = new JsonArray();

        List<User> users = userRepo.findAll();

        for (User user : users) {

            JsonObject obj = new JsonObject();

            obj.addProperty(
                    "username",
                    user.getUsername()
            );

            obj.addProperty(
                    "email",
                    user.getEmail()
            );

            array.add(obj);
        }

        return array.toString();
    }

    public static synchronized JsonObject createItem(
            String name,
            String type,
            double startPrice,
            String seller) {

        return createItem(
                name,
                type,
                startPrice,
                seller,
                DEFAULT_AUCTION_SECONDS,
                ""
        );
    }

    public static synchronized JsonObject createItem(
            String name,
            String type,
            double startPrice,
            String seller,
            int durationSeconds,
            String imageBase64) {

        JsonObject response = new JsonObject();

        if (name == null || name.isBlank()) {

            response.addProperty("status", "ERROR");
            response.addProperty(
                    "message",
                    "Tên sản phẩm không được trống."
            );

            return response;
        }

        if (startPrice <= 0) {

            response.addProperty("status", "ERROR");
            response.addProperty(
                    "message",
                    "Giá khởi điểm phải lớn hơn 0."
            );

            return response;
        }

        if (durationSeconds <= 0) {

            response.addProperty("status", "ERROR");
            response.addProperty(
                    "message",
                    "Thời gian đấu giá phải lớn hơn 0 giây."
            );

            return response;
        }

        User sellerUser =
                userRepo.findByUsername(seller)
                        .orElse(null);

        if (sellerUser == null) {

            response.addProperty("status", "ERROR");
            response.addProperty(
                    "message",
                    "Không tìm thấy người bán."
            );

            return response;
        }

        model.item.Item item;

        String normalizedType =
                normalizeType(type);

        switch (normalizedType) {

            case "ART":

                item = new Art(
                        name.trim(),
                        sellerUser,
                        startPrice,
                        startPrice
                );

                break;

            case "VEHICLE":

                item = new Vehicle(
                        name.trim(),
                        sellerUser,
                        startPrice,
                        startPrice
                );

                break;

            default:

                item = new Electronics(
                        name.trim(),
                        sellerUser,
                        startPrice,
                        startPrice
                );
        }

        itemRepo.save(item);

        Auction auction =
                new Auction(
                        item,
                        startPrice,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                                .plusSeconds(durationSeconds)
                );

        auctionRepo.save(auction);

        JsonObject itemJson =
                new JsonObject();

        itemJson.addProperty(
                "id",
                item.getId()
        );

        itemJson.addProperty(
                "name",
                item.getName()
        );

        itemJson.addProperty(
                "type",
                normalizedType
        );

        itemJson.addProperty(
                "seller",
                sellerUser.getUsername()
        );

        itemJson.addProperty(
                "startPrice",
                startPrice
        );

        itemJson.addProperty(
                "currentPrice",
                auction.getCurrentPrice()
        );

        response.addProperty(
                "status",
                "SUCCESS"
        );

        response.addProperty(
                "message",
                "Tạo sản phẩm đấu giá thành công."
        );

        response.add(
                "item",
                itemJson
        );

        return response;
    }

    private static final int EXTEND_THRESHOLD_SECONDS = 20; // ngưỡng kích hoạt gia hạn
    private static final int EXTEND_DURATION_SECONDS = 20;  // số giây gia hạn thêm

    private static final BidTransactionRepository bidRepo =
            new SQLiteBidTransactionRepository();

    public static synchronized boolean updateBid(
            Long auctionId,
            double bidAmount,
            String username) {

        Auction auction =
                auctionRepo.findById(auctionId)
                        .orElse(null);

        if (auction == null) {
            return false;
        }

        User bidder =
                userRepo.findByUsername(username)
                        .orElse(null);

        if (bidder == null) {
            return false;
        }

        try {

            BidTransaction bid =
                    new BidTransaction(
                            auction,
                            bidder,
                            bidAmount,
                            LocalDateTime.now()
                    );

            auction.placeBid(bid);

            bidRepo.save(bid);

            long secondsLeft =
                    Duration.between(
                            LocalDateTime.now(),
                            auction.getEndTime()
                    ).getSeconds();

            if (secondsLeft <= 15) {

                auction.setEndTime(
                        auction.getEndTime()
                                .plusSeconds(15)
                );

                auctionRepo.update(auction);

                JsonObject event = new JsonObject();

                event.addProperty(
                        "action",
                        "AUCTION_EXTENDED"
                );

                event.addProperty(
                        "itemId",
                        auction.getId()
                );

                event.addProperty(
                        "timeLeft",
                        Duration.between(
                                LocalDateTime.now(),
                                auction.getEndTime()
                        ).getSeconds()
                );

                event.addProperty(
                        "endTime",
                        auction.getEndTime().toString()
                );

                event.addProperty(
                        "message",
                        "Co nguoi dat gia trong 15 giay cuoi. Phien dau gia duoc gia han them 15 giay."
                );

                AuctionServer.broadcast(
                        event.toString()
                );
            }

            auctionRepo.update(auction);

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    private static Item addItem(String name, String type, double price, String seller, int durationSeconds) {
        return addItem(name, type, price, seller, durationSeconds, "");
    }

    private static Item addItem(String name, String type, double price, String seller, int durationSeconds, String imageBase64) {
        Item item = new Item(nextItemId++, name, type, price, seller, durationSeconds, imageBase64);
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
        addItem("Laptop Dell XPS", "Electronics", 1500.0, "seller", 120);
        addItem("iPhone 15 Pro", "Electronics", 1000.0, "seller", 120);
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
            addItem("Laptop Dell XPS", "Electronics", 1500.0, "seller", 120);
            addItem("iPhone 15 Pro", "Electronics", 1000.0, "seller", 120);
            changed = true;
        }
        if (changed) {
            saveData();
        }
    }

    private static synchronized void saveData() {
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

    public static class Item {
        public int id;
        public String name;
        public String type;
        public double startPrice;
        public double currentPrice;
        public String winner;
        public String seller;
        public String status = "ACTIVE";
        public int timeLeft;
        public String imageBase64 = "";

        public Item(int id, String name, String type, double price, String seller) {
            this(id, name, type, price, seller, DEFAULT_AUCTION_SECONDS, "");
        }

        public Item(int id, String name, String type, double price, String seller,
                    int durationSeconds, String imageBase64) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.startPrice = price;
            this.currentPrice = price;
            this.seller = seller;
            this.winner = "";
            this.timeLeft = durationSeconds;
            this.imageBase64 = imageBase64 != null ? imageBase64 : "";
        }

        private JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", id);
            obj.addProperty("imageBase64", imageBase64 != null ? imageBase64 : "");
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
