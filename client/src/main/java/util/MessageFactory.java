package util;

import com.google.gson.Gson;

public class MessageFactory {
    private static final Gson GSON = new Gson();

    public static String loginRequest(String username, String password) {
        return toJson(new LoginRequest(username, password));
    }

    public static String registerRequest(String username, String password, String role) {
        return toJson(new RegisterRequest(username, password, role));
    }

    public static String viewItemsRequest() {
        return toJson(new ActionRequest("VIEW_ITEMS"));
    }

    public static String viewUsersRequest() {
        return toJson(new ActionRequest("VIEW_USERS"));
    }

    public static String bidRequest(int itemId, double amount, String username) {
        return toJson(new BidRequest(itemId, amount, username));
    }

    public static String viewMyItemsRequest(String seller) {
        return toJson(new SellerItemsRequest(seller));
    }

    public static String createItemRequest(String name, String type, double price, String seller) {
        return toJson(new CreateItemRequest(name, type, price, seller, 120));
    }

    public static String createItemRequest(String name, String type, double price, String seller, int durationSeconds) {
        return toJson(new CreateItemRequest(name, type, price, seller, durationSeconds));
    }
    public static String createItemRequest(String name, String type, double price,
                                           String seller, int durationSeconds, String imageBase64) {
        return toJson(new CreateItemRequest(name, type, price, seller, durationSeconds, imageBase64));
    }

    public static String toJson(Object request) {
        return GSON.toJson(request);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    private record ActionRequest(String action) {
    }

    private record LoginRequest(String action, String username, String password) {
        private LoginRequest(String username, String password) {
            this("LOGIN", username, password);
        }
    }

    private record RegisterRequest(String action, String username, String password, String role) {
        private RegisterRequest(String username, String password, String role) {
            this("REGISTER", username, password, role);
        }
    }

    private record BidRequest(String action, int itemId, double amount, String user) {
        private BidRequest(int itemId, double amount, String user) {
            this("BID", itemId, amount, user);
        }
    }

    private record SellerItemsRequest(String action, String seller) {
        private SellerItemsRequest(String seller) {
            this("VIEW_MY_ITEMS", seller);
        }
    }

    private record CreateItemRequest(String action, String name, String type, double price,
                                     String seller, int duration, String imageBase64) {
        private CreateItemRequest(String name, String type, double price, String seller, int duration) {
            this("CREATE_ITEM", name, type, price, seller, duration, "");
        }
        private CreateItemRequest(String name, String type, double price,
                                  String seller, int duration, String imageBase64) {
            this("CREATE_ITEM", name, type, price, seller, duration, imageBase64);
        }
    }
}
