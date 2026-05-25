package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    // Chọn một port trống, ví dụ 8080 hoặc 9999
    private static final int PORT = 8080;

    // Tạo một danh sách an toàn (thread-safe) để lưu các Client đang kết nối
    public static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    // Server dành sẵn 50 "chỗ ngồi" cho 50 Client cùng lúc.
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("=== Hệ thống Đấu giá Online ===");
            System.out.println("Server đang khởi chạy và lắng nghe tại port " + PORT + "...");

            // Vòng lặp vô hạn để liên tục chờ Client kết nối
            while (true) {
                // Khi có 1 Client (Người 4) kết nối tới, dòng này sẽ được thực thi
                Socket clientSocket = serverSocket.accept();
                System.out.println("✅ Có client mới kết nối từ: " + clientSocket.getInetAddress().getHostAddress());

                // NGAY LẬP TỨC: Tạo một luồng (Thread) mới để xử lý Client này
                // Điều này giúp Server không bị "đơ" và có thể đón tiếp nhiều người cùng lúc
                ClientHandler handler = new ClientHandler(clientSocket);
                // Ghi danh Client mới vào danh sách quản lý
                clients.add(handler);

                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi khởi động server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Để gửi tin nhắn cho TẤT CẢ mọi người trong phòng
    public static void broadcast(String message) {
        // Bắt buộc phải khóa danh sách lại khi duyệt vòng lặp
        // Duyệt qua danh sách và gửi tin nhắn đến từng Client
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }
}
