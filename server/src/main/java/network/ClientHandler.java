package network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket socket;

    // Đưa PrintWriter ra ngoài để hàm sendMessage() có thể sử dụng được
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private boolean authenticated;
    private String currentRole = "";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // 1. HÀM GỬI TIN: Server sẽ gọi hàm này khi dùng lệnh broadcast()
    public void sendMessage(String message) {
        if (out != null && authenticated) {
            out.println(message);
        }
    }

    @Override
    public void run() {
        try {
            // Khởi tạo luồng đọc (in) và ghi (out)
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String inputLine;
            // 2. VÒNG LẶP LẮNG NGHE: Liên tục đọc tin nhắn Client gửi lên
            while ((inputLine = in.readLine()) != null) {
                logger.info("📩 Nhận từ client: {}", inputLine);

                try {
                    // Dùng Gson để chuyển chuỗi thành đối tượng JSON
                    JsonObject request = gson.fromJson(inputLine, JsonObject.class);

                    if (request.has("action")) {
                        String action = request.get("action").getAsString();
                        handleAction(action, request); // Gọi hàm xử lý điều hướng
                    } else {
                        sendStatus("ERROR", "Request thiếu action.");
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi sai định dạng JSON từ Client: {}", e.getMessage());
                    logger.debug("JSON parsing exception", e);
                    sendStatus("ERROR", "Dữ liệu gửi lên không hợp lệ.");
                }
            }
        } catch (IOException e) {
            logger.warn("Client đột ngột ngắt kết nối: {}", socket.getInetAddress().getHostAddress());
            logger.debug("IOException in ClientHandler", e);
        } finally {
            // 3. DỌN DẸP: Khi Client tắt app, bắt buộc phải xóa khỏi danh sách Server
            cleanUp();
        }
    }

    // Bộ não điều hướng các yêu cầu (Dispatcher)
    private void handleAction(String action, JsonObject request) {
        switch (action) {
            case "LOGIN":
                String loginUser = request.has("username") ? request.get("username").getAsString() : "";
                String password = request.has("password") ? request.get("password").getAsString() : "";
                JsonObject loginResponse = AuctionManager.authenticate(loginUser, password);
                authenticated = "SUCCESS".equals(loginResponse.get("status").getAsString());
                currentRole = authenticated && loginResponse.has("role") ? loginResponse.get("role").getAsString() : "";
                out.println(gson.toJson(loginResponse));
                break;

            case "REGISTER":
                String registerUser = request.has("username") ? request.get("username").getAsString() : "";
                String registerPassword = request.has("password") ? request.get("password").getAsString() : "";
                String registerRole = request.has("role") ? request.get("role").getAsString() : "BIDDER";
                out.println(gson.toJson(AuctionManager.register(registerUser, registerPassword, registerRole)));
                break;

            case "CREATE_ITEM":
                if (!request.has("name") || !request.has("price") || !request.has("seller")) {
                    sendStatus("ERROR", "Request tạo sản phẩm thiếu dữ liệu.");
                    break;
                }
                String name = request.get("name").getAsString();
                String type = request.has("type") ? request.get("type").getAsString() : "Other";
                double price = request.get("price").getAsDouble();
                String seller = request.get("seller").getAsString();
                int duration = request.has("duration") ? request.get("duration").getAsInt() : 120;
                String imageBase64 = request.has("imageBase64") ? request.get("imageBase64").getAsString() : "";
                JsonObject createResponse = AuctionManager.createItem(name, type, price, seller, duration, imageBase64);
                out.println(gson.toJson(createResponse));
                if ("SUCCESS".equals(createResponse.get("status").getAsString())) {
                    JsonObject event = new JsonObject();
                    event.addProperty("action", "ITEM_CREATED");
                    event.add("item", createResponse.get("item"));
                    AuctionServer.broadcast(gson.toJson(event));
                }
                break;

            case "BID":
                if ("SELLER".equals(currentRole)) {
                    sendStatus("ERROR", "Seller khong duoc phep dat gia.");
                    break;
                }
                // Trong hàm handleAction của ClientHandler.java
                if (!request.has("itemId") || !request.has("amount") || !request.has("user")) {
                    sendStatus("ERROR", "Request đặt giá thiếu dữ liệu.");
                    break;
                }
                int itemId = request.get("itemId").getAsInt();
                double bidAmount = request.get("amount").getAsDouble();
                String username = request.get("user").getAsString();

                if (AuctionManager.updateBid(itemId, bidAmount, username)) {
                    // Lấy timeLeft hiện tại sau khi đã gia hạn (nếu có)
                    int currentTimeLeft = AuctionManager.getItemTimeLeft(itemId);
                    JsonObject update = new JsonObject();
                    update.addProperty("action", "UPDATE_PRICE");
                    update.addProperty("itemId", itemId);
                    update.addProperty("price", bidAmount);
                    update.addProperty("winner", username);
                    update.addProperty("bidder", username);
                    update.addProperty("timeLeft", currentTimeLeft);
                    AuctionServer.broadcast(gson.toJson(update));
                    sendStatus("SUCCESS", "Đặt giá thành công!");
                } else {
                    sendStatus("ERROR", "Giá đặt phải cao hơn giá hiện tại hoặc sản phẩm không tồn tại!");
                }
                break;

            case "VIEW_ITEMS":
            case "VIEW_MY_ITEMS":
                String sellerFilter = null;
                if ("VIEW_MY_ITEMS".equals(action) && request.has("seller")) {
                    sellerFilter = request.get("seller").getAsString();
                }
                String itemsJson = AuctionManager.getItemsJson(sellerFilter);
                JsonObject response = new JsonObject();
                response.addProperty("status", "OK");
                response.add("data", gson.fromJson(itemsJson, com.google.gson.JsonArray.class));
                out.println(gson.toJson(response));
                break;

            case "VIEW_USERS":
                if (!"ADMIN".equals(currentRole)) {
                    sendStatus("ERROR", "Chỉ admin mới được xem danh sách user.");
                    break;
                }
                JsonObject usersResponse = new JsonObject();
                usersResponse.addProperty("status", "OK");
                usersResponse.add("users", gson.fromJson(AuctionManager.getUsersJson(), com.google.gson.JsonArray.class));
                out.println(gson.toJson(usersResponse));
                break;

            default:
                sendStatus("ERROR", "Hành động không được hỗ trợ.");
        }
    }

    private void sendStatus(String status, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("message", message);
        out.println(gson.toJson(response));
    }

    // Hàm dọn dẹp an toàn bộ nhớ
    private void cleanUp() {
        try {
            // CỰC KỲ QUAN TRỌNG: Gọi ngược lại class AuctionServer để xóa tên khỏi danh sách
            AuctionServer.clients.remove(this);
            logger.info("🧹 Đã xóa client. Số người đang online: {}", AuctionServer.clients.size());

            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.error("Loi khi don dep client", e);
        }
    }
}