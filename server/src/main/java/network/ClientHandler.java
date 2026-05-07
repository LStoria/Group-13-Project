package network;

import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Luồng xử lý riêng cho client " + socket.getInetAddress() + " đang chạy.");
            
            // Tạm thời dừng ở đây. 
            // Bước tiếp theo của bạn (sau khi thống nhất với Người 4) là:
            // 1. Dùng BufferedReader để đọc dữ liệu JSON gửi lên từ Client.
            // 2. Phân tích chuỗi JSON đó xem Client muốn làm gì (Đăng nhập? Đặt giá? Xem SP?).
            // 3. Gọi các hàm xử lý tương ứng.
            // 4. Dùng PrintWriter để gửi chuỗi JSON kết quả trả về Client.

        } catch (Exception e) {
            System.err.println("Lỗi kết nối với client: " + e.getMessage());
        }
    }
}