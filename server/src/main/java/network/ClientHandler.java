package network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    
    // Đưa PrintWriter ra ngoài để hàm sendMessage() có thể sử dụng được
    private PrintWriter out; 
    private BufferedReader in;
    private Gson gson = new Gson();

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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

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
                    }
                } catch (Exception e) {
                    System.err.println("❌ Lỗi sai định dạng JSON từ Client.");
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
                out.println("{\"status\":\"SUCCESS\", \"message\":\"Đăng nhập thành công!\"}");
                break;
                
            case "BID":
                int itemId = request.get("itemId").getAsInt();
                double bidAmount = request.get("amount").getAsDouble();
                String username = request.get("user").getAsString();

                if (AuctionManager.updateBid(itemId, bidAmount, username)) {
                    // Thông báo cho TẤT CẢ mọi người về mức giá mới
                    AuctionServer.broadcast("{\"action\":\"UPDATE_PRICE\", \"itemId\":" + itemId + ", \"price\":" + bidAmount + ", \"winner\":\"" + username + "\"}");
                    out.println("{\"status\":\"SUCCESS\", \"message\":\"Đặt giá thành công!\"}");
                } else {
                    out.println("{\"status\":\"ERROR\", \"message\":\"Giá đặt phải cao hơn giá hiện tại hoặc sản phẩm không tồn tại!\"}");
                }

                // Đảm bảo tại một thời điểm chỉ 1 luồng được xử lý món hàng này
                synchronized (AuctionManager.items) {
                    for (AuctionManager.Item item : AuctionManager.items) {
                        if (item.id == itemId) {
                            if (bidAmount > item.currentPrice) {
                                item.currentPrice = bidAmount;
                                item.winner = username;

                                // Thông báo giá mới cho tất cả mọi người
                                AuctionServer.broadcast("{\"action\":\"UPDATE\", \"price\":" + bidAmount + "}");
                            } else {
                                sendMessage("{\"status\":\"FAILED\", \"reason\":\"Giá quá thấp!\"}");
                            }
                            break;
                        }
                    }
                }
                break;

            case "VIEW_ITEMS":
                String itemsJson = AuctionManager.getAllItemsJson();
                out.println("{\"status\":\"OK\", \"data\":" + itemsJson + "}");
                break;
                
            default:
                out.println("{\"status\":\"ERROR\", \"message\":\"Hành động không được hỗ trợ.\"}");
        }
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