package network;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionClientTest {
    private AuctionClient client;
    private final int TEST_PORT = 8888;

    @BeforeEach
    void setUp() {
        client = new AuctionClient();
    }

    @AfterEach
    void tearDown() {
        client.closeConnection();
    }

    // =====================================================================
    //  NHÓM 1: KIỂM TRA KẾT NỐI (TC1 – TC4)  — đã có sẵn, giữ nguyên
    // =====================================================================

    @Test
    @DisplayName("TC1: Kiểm tra khởi tạo đối tượng Client")
    void testClientNotNull() {
        assertNotNull(client, "AuctionClient phải được khởi tạo thành công");
    }

    @Test
    @DisplayName("TC2: Kiểm tra kết nối thành công tới Server giả lập")
    void testConnectSuccess() {
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
        assertThrows(IOException.class, () -> {
            client.connect("localhost", 9999, response -> {});
        }, "Phải ném ra IOException khi không tìm thấy Server");
    }

    @Test
    @DisplayName("TC4: Kiểm tra gửi dữ liệu không gây lỗi khi chưa kết nối")
    void testSendWithoutConnection() {
        assertDoesNotThrow(() -> {
            client.sendRequest("{\"type\":\"TEST\"}");
        }, "Hàm sendRequest phải xử lý an toàn nếu chưa kết nối thành công");
    }

    // =====================================================================
    //  NHÓM 2: KIỂM TRA GỬI & NHẬN DỮ LIỆU (TC5 – TC8)  — THÊM MỚI
    // =====================================================================

    @Test
    @DisplayName("TC5: Client gửi JSON login — Server nhận đúng nội dung")
    void testSendLoginRequest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        Thread serverThread = new Thread(() -> {
            try (ServerSocket mockServer = new ServerSocket(TEST_PORT + 1)) {
                Socket conn = mockServer.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                received.set(reader.readLine());
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(100); // Đợi server giả lập sẵn sàng
        client.connect("localhost", TEST_PORT + 1, r -> {});
        String loginJson = "{\"action\":\"LOGIN\",\"username\":\"alice\",\"password\":\"pass\"}";
        client.sendRequest(loginJson);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Server phải nhận được dữ liệu trong 3 giây");
        assertEquals(loginJson, received.get(), "Nội dung gửi và nhận phải khớp nhau");
    }

    @Test
    @DisplayName("TC6: Client nhận phản hồi từ Server — callback được gọi đúng nội dung")
    void testReceiveServerMessage() throws Exception {
        String fakeResponse = "{\"status\":\"SUCCESS\",\"role\":\"BIDDER\"}";
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        Thread serverThread = new Thread(() -> {
            try (ServerSocket mockServer = new ServerSocket(TEST_PORT + 2)) {
                Socket conn = mockServer.accept();
                PrintWriter writer = new PrintWriter(conn.getOutputStream(), true);
                writer.println(fakeResponse); // Server gửi phản hồi cho client
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(100);
        client.connect("localhost", TEST_PORT + 2, message -> {
            received.set(message);
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Client phải nhận được phản hồi trong 3 giây");
        assertEquals(fakeResponse, received.get(), "Nội dung phản hồi phải đúng với những gì Server gửi");
    }

    @Test
    @DisplayName("TC7: Gửi JSON BID hợp lệ — định dạng đúng chuẩn giao thức")
    void testSendBidRequestFormat() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        Thread serverThread = new Thread(() -> {
            try (ServerSocket mockServer = new ServerSocket(TEST_PORT + 3)) {
                Socket conn = mockServer.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                received.set(reader.readLine());
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(100);
        client.connect("localhost", TEST_PORT + 3, r -> {});
        String bidJson = "{\"action\":\"BID\",\"itemId\":5,\"amount\":150.0,\"user\":\"alice\"}";
        client.sendRequest(bidJson);

        assertTrue(latch.await(3, TimeUnit.SECONDS));

        // Kiểm tra định dạng JSON đúng giao thức
        com.google.gson.JsonObject obj = new com.google.gson.Gson()
                .fromJson(received.get(), com.google.gson.JsonObject.class);
        assertEquals("BID", obj.get("action").getAsString());
        assertEquals(5, obj.get("itemId").getAsInt());
        assertEquals(150.0, obj.get("amount").getAsDouble(), 0.001);
        assertEquals("alice", obj.get("user").getAsString());
    }

    @Test
    @DisplayName("TC8: isConnected() trả về false sau khi closeConnection()")
    void testIsConnectedAfterClose() throws Exception {
        try (ServerSocket mockServer = new ServerSocket(TEST_PORT + 4)) {
            client.connect("localhost", TEST_PORT + 4, r -> {});
            assertTrue(client.isConnected(), "Phải connected sau khi connect()");
            client.closeConnection();
            Thread.sleep(100); // Đợi thread đóng hoàn toàn
            assertFalse(client.isConnected(), "Phải disconnected sau khi closeConnection()");
        }
    }

    // =====================================================================
    //  NHÓM 3: KIỂM TRA util.MessageFactory (TC9 – TC12)  — THÊM MỚI
    // =====================================================================

    @Test
    @DisplayName("TC9: MessageFactory.loginRequest() tạo JSON đúng chuẩn")
    void testMessageFactoryLoginRequest() {
        String json = util.MessageFactory.loginRequest("alice", "secret");
        com.google.gson.JsonObject obj = new com.google.gson.Gson()
                .fromJson(json, com.google.gson.JsonObject.class);

        assertEquals("LOGIN", obj.get("action").getAsString());
        assertEquals("alice", obj.get("username").getAsString());
        assertEquals("secret", obj.get("password").getAsString());
    }

    @Test
    @DisplayName("TC10: MessageFactory.registerRequest() tạo JSON đúng chuẩn với role SELLER")
    void testMessageFactoryRegisterRequest() {
        String json = util.MessageFactory.registerRequest("bob", "pass123", "SELLER");
        com.google.gson.JsonObject obj = new com.google.gson.Gson()
                .fromJson(json, com.google.gson.JsonObject.class);

        assertEquals("REGISTER", obj.get("action").getAsString());
        assertEquals("bob", obj.get("username").getAsString());
        assertEquals("SELLER", obj.get("role").getAsString());
    }

    @Test
    @DisplayName("TC11: MessageFactory.bidRequest() tạo JSON đúng chuẩn")
    void testMessageFactoryBidRequest() {
        String json = util.MessageFactory.bidRequest(3, 200.0, "carol");
        com.google.gson.JsonObject obj = new com.google.gson.Gson()
                .fromJson(json, com.google.gson.JsonObject.class);

        assertEquals("BID", obj.get("action").getAsString());
        assertEquals(3, obj.get("itemId").getAsInt());
        assertEquals(200.0, obj.get("amount").getAsDouble(), 0.001);
        assertEquals("carol", obj.get("user").getAsString());
    }

    @Test
    @DisplayName("TC12: MessageFactory.createItemRequest() tạo JSON đúng chuẩn")
    void testMessageFactoryCreateItemRequest() {
        String json = util.MessageFactory.createItemRequest("Laptop", "Electronics", 999.0, "seller1");
        com.google.gson.JsonObject obj = new com.google.gson.Gson()
                .fromJson(json, com.google.gson.JsonObject.class);

        assertEquals("CREATE_ITEM", obj.get("action").getAsString());
        assertEquals("Laptop", obj.get("name").getAsString());
        assertEquals("Electronics", obj.get("type").getAsString());
        assertEquals(999.0, obj.get("price").getAsDouble(), 0.001);
        assertEquals("seller1", obj.get("seller").getAsString());
    }
}
