package network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Gson gson = new Gson(); // Công cụ chuyển đổi JSON

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // 1. Nhận chuỗi JSON từ Client (do Người 4 gửi lên)
                System.out.println("Client gửi: " + inputLine);

                // 2. Phân tích chuỗi JSON
                JsonObject request = gson.fromJson(inputLine, JsonObject.class);
                String action = request.get("action").getAsString();

                // 3. Xử lý theo từng yêu cầu (Core Logic)
                if (action.equals("LOGIN")) {
                    handleLogin(request, out);
                } else if (action.equals("BID")) {
                    handleBid(request, out);
                }
            }
        } catch (IOException e) {
            System.out.println("Client ngắt kết nối.");
        }
    }

    private void handleLogin(JsonObject request, PrintWriter out) {
        // Sau này bạn sẽ gọi Người 2 (Database) ở đây để check user
        JsonObject response = new JsonObject();
        response.addProperty("status", "SUCCESS");
        response.addProperty("message", "Đăng nhập thành công!");
        out.println(gson.toJson(response));
    }

    private void handleBid(JsonObject request, PrintWriter out) {
        // Logic đấu giá (Người 1 cần làm: Check giá cao nhất, Concurrency...)
        // Tạm thời trả về thông báo giả lập
        out.println("{\"status\":\"OK\", \"msg\":\"Đặt giá thành công\"}");
    }
}