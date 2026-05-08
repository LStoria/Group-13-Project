package network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class AuctionClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isRunning = false;

    // Hàm kết nối - Người 3 sẽ gọi khi bắt đầu ứng dụng
    public void connect(String host, int port, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.isRunning = true;

        // TẠO LUỒNG LẮNG NGHE (QUAN TRỌNG): Đảm bảo nhận dữ liệu realtime không treo GUI
        new Thread(() -> {
            try {
                String response;
                while (isRunning && (response = in.readLine()) != null) {
                    // Chuyển dữ liệu nhận được về cho GUI xử lý qua Consumer
                    onMessageReceived.accept(response);
                }
            } catch (IOException e) {
                System.err.println("Mất kết nối từ Server: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }).start();
    }

    // Hàm gửi yêu cầu (Đăng nhập, Đặt giá...) dưới dạng JSON
    public void sendRequest(String jsonRequest) {
        if (out != null) {
            out.println(jsonRequest);
        }
    }

    // Đóng kết nối an toàn
    public void closeConnection() {
        try {
            isRunning = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}