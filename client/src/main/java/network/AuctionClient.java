package network;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;

public class AuctionClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✅ Đã kết nối tới Server!");

            // Gửi thử một gói tin JSON Đăng nhập
            String loginJson = "{\"action\": \"LOGIN\", \"username\": \"tung_nhom_truong\"}";
            out.println(loginJson);

            // Đợi nghe phản hồi từ Server
            String response = in.readLine();
            System.out.println("📩 Server phản hồi: " + response);

        } catch (IOException e) {
            System.err.println("❌ Không thể kết nối tới Server. Hãy chắc chắn Server đang chạy!");
        }
    }
}