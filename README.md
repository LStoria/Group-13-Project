# 🔨 HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - NHÓM 13 (ONLINE AUCTION SYSTEM)

Dự án này là một **Hệ thống Đấu giá Trực tuyến (Online Auction System)** được xây dựng theo mô hình **Client-Server** sử dụng kết nối **Socket TCP** thời gian thực. Hệ thống hỗ trợ đa luồng xử lý đồng thời, đồng bộ hóa dữ liệu thời gian thực giữa các phiên đấu giá và cung cấp giao diện đồ họa trực quan cho người dùng.

---

## 📌 1. Mô tả bài toán và Phạm vi hệ thống

### 1.1. Bài toán đặt ra
Trong môi trường thương mại điện tử, việc đấu giá trực tuyến đòi hỏi tính chính xác cực kỳ cao về mặt thời gian, tính đồng bộ dữ liệu tức thời và tính công bằng trong cạnh tranh. Dự án này giải quyết bài toán đấu giá thời gian thực thông qua kết nối Socket TCP, truyền dữ liệu dạng JSON, đếm ngược thời gian tập trung tại máy chủ (Server-side Timer) và tự động cập nhật giá cả, trạng thái phòng đấu giá đến tất cả người dùng đang trực tuyến.

### 1.2. Phạm vi hệ thống
Hệ thống phục vụ 3 vai trò người dùng (Role) chính với các chức năng cụ thể:
*   **Người tham gia đấu giá (Bidder)**:
    *   Xem danh sách các phòng đấu giá (đang diễn ra, đã kết thúc) theo thời gian thực.
    *   Xem thông tin mô tả chi tiết và hình ảnh sản phẩm.
    *   Thực hiện đặt giá (Bid) hợp lệ (giá mới phải cao hơn giá hiện tại).
*   **Người bán (Seller)**:
    *   Đăng bán sản phẩm mới: Đặt tên, phân loại, giá khởi điểm, tải ảnh minh họa trực tiếp từ máy tính lên hệ thống, thiết lập thời gian phiên đấu giá (tính theo giây).
    *   Theo dõi danh sách sản phẩm thuộc sở hữu cá nhân và nhận cảnh báo (Alert popup) tức thời khi có người dùng khác tham gia trả giá.
*   **Quản trị viên (Admin)**:
    *   Quản lý danh sách tài khoản người dùng đăng ký trên hệ thống.
    *   Giám sát tất cả sản phẩm đấu giá và trạng thái hoạt động của chúng.

---

## 🏗️ 2. Công nghệ sử dụng, Môi trường chạy & Yêu cầu cài đặt

### 2.1. Công nghệ sử dụng
*   **Ngôn ngữ lập trình**: Java (Java SE 17 trở lên, khuyên dùng Java 21).
*   **Giao diện đồ họa (GUI)**: JavaFX 21 (sử dụng FXML và CSS styling `app.css`).
*   **Mạng & Giao tiếp (Networking)**: Java TCP Socket, Multi-threading (Thread Pool tối đa 50 kết nối đồng thời), Google Gson (chuẩn hóa thông tin truyền tải dưới dạng chuỗi JSON).
*   **Cơ sở dữ liệu (Database)**: SQLite (`auction.db`) kết nối qua SQLite JDBC Driver.
*   **Thư viện hỗ trợ**: Logback/SLF4J (Log hệ thống), JUnit 5 (Kiểm thử đơn vị).
*   **Hệ thống xây dựng (Build Tool)**: Gradle với Kotlin DSL (`build.gradle.kts`).

### 2.2. Yêu cầu cài đặt
Để chạy chương trình, máy tính của bạn cần được cài đặt sẵn:
1.  **Java Development Kit (JDK)**: Phiên bản 17 hoặc cao hơn (JDK 21 được khuyến nghị).
    *   *Kiểm tra phiên bản Java hiện tại bằng lệnh:* `java -version`
2.  **Cơ sở dữ liệu SQLite**: Không cần cài đặt riêng biệt vì dự án sử dụng thư viện SQLite JDBC tự động kết nối và quản lý file `auction.db` cục bộ.

---

## 📁 3. Cấu trúc thư mục và các Module chính

Dự án được chia thành hai module chính là `server` (máy chủ) và `client` (máy khách) chạy độc lập:

```text
Group-13-Project/
├── client/                               # Module Máy khách (Client Application)
│   ├── src/main/java/
│   │   ├── app/                          # Điểm khởi chạy ứng dụng (MainApp.java)
│   │   ├── controller/                   # Điều khiển logic giao diện (Login, Seller, Bid...)
│   │   ├── model/                        # JavaFX Properties Model phục vụ liên kết dữ liệu (Data Binding)
│   │   ├── network/                      # Luồng Socket Client kết nối Server
│   │   ├── service/                      # Dịch vụ Socket quản lý kết nối và nhận/gửi yêu cầu
│   │   ├── util/                         # Bộ sinh yêu cầu JSON (MessageFactory.java)
│   │   └── view/                         # Giao diện người dùng
│   └── src/main/resources/
│       ├── styles/                       # Tệp giao diện CSS (app.css)
│       └── view/                         # Các tệp FXML định nghĩa bố cục (login.fxml, home.fxml, admin.fxml...)
│
├── server/                               # Module Máy chủ (Server Application)
│   ├── src/main/java/
│   │   ├── database/                     # Kết nối SQLite & Khởi tạo các bảng DB (DatabaseManager.java, DatabaseInitializer.java)
│   │   ├── exception/                    # Các ngoại lệ nghiệp vụ đấu giá (InvalidBidException.java...)
│   │   ├── model/                        # Các thực thể dữ liệu (User, Item, Auction, BidTransaction...)
│   │   ├── network/                      # Socket Server lắng nghe, Thread Pool xử lý kết nối, ClientHandler và AuctionTimer
│   │   ├── repository/                   # Tầng lưu trữ dữ liệu (SQLiteRepository & InMemoryRepository)
│   │   └── service/                      # Logic xử lý dịch vụ trung gian
│   └── src/test/                         # Các ca kiểm thử tự động (Unit Test bằng JUnit 5)
│
├── doc/                                  # Tài liệu báo cáo của dự án
│   └── BaoCao_Nhom13.md                  # Báo cáo chi tiết đề tài
├── build.gradle.kts                      # Cấu hình Gradle gốc
├── settings.gradle.kts                   # Khai báo các module (client, server)
├── gradlew                               # Kịch bản chạy Gradle cho Linux/macOS
└── gradlew.bat                           # Kịch bản chạy Gradle cho Windows
```

---

## 💻 4. Câu lệnh dòng lệnh chạy chương trình (Đa hệ điều hành)

Để đảm bảo tính tương thích đa nền tảng, hệ thống sử dụng **Gradle Wrapper** (`gradlew` và `gradlew.bat`). Các câu lệnh dưới đây có thể chạy trực tiếp trên bất kỳ hệ điều hành nào (Windows, Linux, macOS) mà không cần cài đặt sẵn Gradle trên hệ thống.

### 4.1. Đối với hệ điều hành Windows
Bạn có thể sử dụng Command Prompt (CMD) hoặc PowerShell:

*   **Chạy phía Server**:
    ```bash
    # Sử dụng CMD hoặc PowerShell
    gradlew :server:run
    ```
*   **Chạy phía Client**:
    ```bash
    # Sử dụng CMD hoặc PowerShell
    gradlew :client:run
    ```

### 4.2. Đối với hệ điều hành Linux và macOS
Trước tiên, bạn cần cấp quyền thực thi cho file chạy `gradlew` (chỉ cần thực hiện lần đầu tiên):
```bash
chmod +x gradlew
```

*   **Chạy phía Server**:
    ```bash
    ./gradlew :server:run
    ```
*   **Chạy phía Client**:
    ```bash
    ./gradlew :client:run
    ```

### 4.3. Các lệnh hữu ích khác (Dùng chung cho các hệ điều hành)
*   **Dọn dẹp thư mục build**:
    *   Windows: `gradlew clean`
    *   Linux/macOS: `./gradlew clean`
*   **Biên dịch toàn bộ dự án**:
    *   Windows: `gradlew build -x test`
    *   Linux/macOS: `./gradlew build -x test`
*   **Chạy toàn bộ Unit Test**:
    *   Windows: `gradlew test`
    *   Linux/macOS: `./gradlew test`

---

## 🚀 5. Hướng dẫn chạy Server/Client theo thứ tự cụ thể

Để hệ thống hoạt động chính xác, bạn cần tuân thủ thứ tự khởi chạy dưới đây:

### ⚙️ Bước 1: Khởi động Server
Server phải được khởi động trước để mở Socket lắng nghe kết nối tại cổng `8080`.
*   Mở terminal tại thư mục gốc của dự án.
*   Chạy lệnh khởi chạy Server (tương ứng với hệ điều hành của bạn ở Mục 4).
*   Khi khởi chạy thành công, console sẽ hiển thị thông tin log:
    ```text
    [main] INFO network.AuctionServer - Server dang chay tren port 8080...
    [main] INFO database.DatabaseInitializer - Khoi tao CSDL thanh cong (neu chua ton tai)
    ```

### 🖥️ Bước 2: Khởi động Client
Sau khi Server đã chạy ổn định, bạn khởi động một hoặc nhiều Client:
*   Mở một cửa sổ terminal mới tại thư mục gốc của dự án.
*   Chạy lệnh khởi chạy Client (tương ứng với hệ điều hành của bạn ở Mục 4).
*   Giao diện đăng nhập JavaFX sẽ hiển thị.

### 🔑 Bước 3: Đăng nhập và Kết nối
*   Trên màn hình Đăng nhập (Login):
    *   **Server Host**: Mặc định là `localhost` (hoặc nhập địa chỉ IP của Server nếu chạy khác máy).
    *   **Server Port**: Mặc định là `8080`.
    *   **Username**: Nhập tên đăng nhập của bạn.
    *   **Password**: Nhập mật khẩu (tối thiểu 3 ký tự).
    *   **Role**: Chọn `BIDDER` (Người mua) hoặc `SELLER` (Người bán).
*   Nhấp **Register** nếu muốn tạo tài khoản mới, hoặc **Login** để đăng nhập trực tiếp. Giao diện ứng dụng sẽ chuyển hướng dựa trên vai trò tài khoản được đăng kí, không cần phải chỉnh về đúng vai trò trong những lần đăng nhập tiếp theo

---

## 🏆 6. Danh sách chức năng đã hoàn thành

Hệ thống đã triển khai đầy đủ các tính năng then chốt của một nền tảng đấu giá trực tuyến thời gian thực:

1.  **Xác thực và Phân quyền người dùng (Security & Roles)**:
    *   Đăng ký / Đăng nhập tài khoản.
    *   Mật khẩu được băm một chiều an toàn bằng thuật toán **SHA-256 kết hợp Salt** (chuỗi muối ngẫu nhiên) trước khi lưu SQLite DB.
    *   Phân quyền người dùng chặt chẽ trên Server thành 2 nhóm quyền: `BIDDER`, `SELLER`.
2.  **Đăng bán sản phẩm (Seller Upload)**:
    *   Seller đăng sản phẩm trực tiếp từ giao diện đồ họa.
3.  **Đấu giá trực tuyến thời gian thực (Real-time Bidding)**:
    *   Sử dụng cơ chế kết nối Socket TCP liên tục.
    *   Khi có Bidder đặt giá hợp lệ, Server cập nhật DB và ngay lập tức **broadcast (phát sóng)** thông tin `UPDATE_PRICE` đến tất cả các client đang trực tuyến để cập nhật tức thì màn hình giao diện.
4.  **Đồng bộ đếm ngược & Gia hạn tự động (Server-side Countdown & Anti-Sniper)**:
    *   Đếm ngược thời gian của mỗi phiên đấu giá được xử lý tập trung trên Server thông qua `ScheduledExecutorService` (mỗi giây một chu kỳ), chống gian lận thay đổi giờ trên client.
    *   Cơ chế **Anti-Sniper**: Nếu có lượt đặt giá hợp lệ trong **15 giây cuối cùng** của phiên đấu giá, Server tự động cộng thêm thời gian còn lại của sản phẩm đó thêm **15 giây**, tạo môi trường cạnh tranh lành mạnh.
5.  **Cảnh báo thông minh thời gian thực (Seller Real-time Alert)**:
    *   Server tự động nhận dạng chủ sở hữu (Seller) của sản phẩm đang được đấu giá.
    *   Khi có người trả giá mới cho sản phẩm của mình, Seller sở hữu sản phẩm sẽ lập tức nhận được hộp thoại cảnh báo (Popup Alert) trên màn hình để kịp thời theo dõi phiên đấu giá.
6.  **Giao diện quản trị hệ thống (Admin Panel)**:
    *   Giao diện dành riêng cho tài khoản quyền `ADMIN`.
    *   Xem danh sách tất cả người dùng và tất cả sản phẩm đấu giá trên hệ thống.
    *   Dữ liệu được Server kiểm tra xác thực quyền hạn trước khi trả về từ cơ sở dữ liệu SQLite dưới dạng JSON.
