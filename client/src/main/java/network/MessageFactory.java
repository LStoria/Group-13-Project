package network;

import com.google.gson.Gson;

/**
 * Lớp hỗ trợ chuyển đổi dữ liệu giữa Object và JSON.
 * Giúp Người 4 quản lý giao thức mạng sạch sẽ hơn.
 */
public class MessageFactory {
    // Khởi tạo Gson một lần duy nhất (Singleton-like) để tối ưu hiệu năng
    private static final Gson gson = new Gson();

    /**
     * Chuyển các đối tượng Request (LoginRequest, BidRequest) thành chuỗi JSON để gửi đi.
     */
    public static String toJson(Object request) {
        return gson.toJson(request);
    }

    /**
     * Chuyển chuỗi JSON nhận được từ Server thành một đối tượng cụ thể (nếu cần).
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
