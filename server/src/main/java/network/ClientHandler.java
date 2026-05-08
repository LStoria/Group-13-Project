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

public class ClientHandler implements Runnable {
    private final Socket socket;
    
    // Đưa PrintWriter ra ngoài để hàm sendMessage() có thể sử dụng được
    private PrintWriter out; 
    private BufferedReader in;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // 1. HÀM GỬI TIN: Server sẽ gọi hàm này khi dùng lệnh broadcast()
    public void sendMessage(String message) {
        if (out != null) {
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
                System.out.println("📩 Nhận từ client: " + inputLine);

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
                    System.err.println("❌ Lỗi sai định dạng JSON từ Client.");
                    sendStatus("ERROR", "Dữ liệu gửi lên không hợp lệ.");
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Client đột ngột ngắt kết nối: " + socket.getInetAddress().getHostAddress());
        } finally {
            // 3. DỌN DẸP: Khi Client tắt app, bắt buộc phải xóa khỏi danh sách Server
            cleanUp();
        }
    }

    // Bộ não điều hướng các yêu cầu (Dispatcher)
    private void handleAction(String action, JsonObject request) {
        switch (action) {
            case "LOGIN":
                // Tạm thời trả về JSON thành công, sau này sẽ kết nối với UserRepository
                sendStatus("SUCCESS", "Đăng nhập thành công!");
                break;
                
            case "BID":
                // Trong hàm handleAction của ClientHandler.java
                if (!request.has("itemId") || !request.has("amount") || !request.has("user")) {
                    sendStatus("ERROR", "Request đặt giá thiếu dữ liệu.");
                    break;
                }
                int itemId = request.get("itemId").getAsInt();
                double bidAmount = request.get("amount").getAsDouble();
                String username = request.get("user").getAsString();

                if (AuctionManager.updateBid(itemId, bidAmount, username)) {
                    // Thông báo cho TẤT CẢ mọi người về mức giá mới
                    JsonObject update = new JsonObject();
                    update.addProperty("action", "UPDATE_PRICE");
                    update.addProperty("itemId", itemId);
                    update.addProperty("price", bidAmount);
                    update.addProperty("winner", username);
                    AuctionServer.broadcast(gson.toJson(update));
                    sendStatus("SUCCESS", "Đặt giá thành công!");
                } else {
                    sendStatus("ERROR", "Giá đặt phải cao hơn giá hiện tại hoặc sản phẩm không tồn tại!");
                }
                break;

            case "VIEW_ITEMS":
                String itemsJson = AuctionManager.getAllItemsJson();
                JsonObject response = new JsonObject();
                response.addProperty("status", "OK");
                response.add("data", gson.fromJson(itemsJson, com.google.gson.JsonArray.class));
                out.println(gson.toJson(response));
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
            System.out.println("🧹 Đã xóa client. Số người đang online: " + AuctionServer.clients.size());
            
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
