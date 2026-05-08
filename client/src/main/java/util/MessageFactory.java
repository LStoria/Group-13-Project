package util;

import com.google.gson.Gson;

public class MessageFactory {
    private static final Gson GSON = new Gson();

    public static String loginRequest(String username, String password) {
        return toJson(new LoginRequest(username, password));
    }

    public static String viewItemsRequest() {
        return toJson(new ActionRequest("VIEW_ITEMS"));
    }

    public static String bidRequest(int itemId, double amount, String username) {
        return toJson(new BidRequest(itemId, amount, username));
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

    private record BidRequest(String action, int itemId, double amount, String user) {
        private BidRequest(int itemId, double amount, String user) {
            this("BID", itemId, amount, user);
        }
    }
}
