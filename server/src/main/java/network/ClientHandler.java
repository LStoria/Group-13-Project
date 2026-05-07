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
                // Ví dụ: Nhận lệnh đặt giá
                // double amount = request.get("amount").getAsDouble();
                
                // Trả về cho người đặt giá biết là đã nhận lệnh
                out.println("{\"status\":\"SUCCESS\", \"message\":\"Hệ thống đã ghi nhận giá trị đặt!\"}");
                
                // GIẢ LẬP BROADCAST: Thông báo cho toàn bộ phòng biết có người vừa đặt giá!
                // AuctionServer.broadcast("{\"action\":\"NEW_BID\", \"message\":\"Có người vừa đặt mức giá mới!\"}");
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