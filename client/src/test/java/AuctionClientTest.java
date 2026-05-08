package network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.ServerSocket;

public class AuctionClientTest {
    private AuctionClient client;
    private final int TEST_PORT = 8888;

    @BeforeEach
    void setUp() {
        client = new AuctionClient();
    }

    @Test
    @DisplayName("TC1: Kiểm tra khởi tạo đối tượng Client")
    void testClientNotNull() {
        assertNotNull(client, "AuctionClient phải được khởi tạo thành công");
    }

    @Test
    @DisplayName("TC2: Kiểm tra kết nối thành công tới Server giả lập")
    void testConnectSuccess() {
        // Tạo một Server giả lập để test kết nối
        assertDoesNotThrow(() -> {
            try (ServerSocket mockServer = new ServerSocket(TEST_PORT)) {
                client.connect("localhost", TEST_PORT, response -> {});
                assertTrue(client.isConnected(), "Client phải ở trạng thái đã kết nối");
                client.closeConnection();
            }
        });
    }

    @Test
    @DisplayName("TC3: Kiểm tra xử lý lỗi khi sai Port hoặc Server chưa bật")
    void testConnectFailure() {
        // Cố tình kết nối đến port không tồn tại để kiểm tra xử lý ngoại lệ
        assertThrows(IOException.class, () -> {
            client.connect("localhost", 9999, response -> {});
        }, "Phải ném ra IOException khi không tìm thấy Server");
    }

    @Test
    @DisplayName("TC4: Kiểm tra gửi dữ liệu không gây lỗi khi chưa kết nối")
    void testSendWithoutConnection() {
        // Kiểm tra tính ổn định của hàm gửi yêu cầu
        assertDoesNotThrow(() -> {
            client.sendRequest("{\"type\":\"TEST\"}");
        }, "Hàm sendRequest phải xử lý an toàn nếu chưa kết nối thành công");
    }
}
