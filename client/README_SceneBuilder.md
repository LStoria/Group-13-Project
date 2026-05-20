Hướng dẫn mở và chỉnh sửa các file FXML bằng Scene Builder

Mục tiêu: bạn chỉ cần thiết kế giao diện bằng JavaFX Scene Builder. Project đã có các file FXML trong `client/src/main/resources/view/` và controller tương ứng trong `client/src/main/java/controller/`.

1) Cài Scene Builder
- Tải Gluon Scene Builder từ: https://gluonhq.com/products/scene-builder/
- Cài trên Windows; nhớ đường dẫn tới `SceneBuilder.exe` (ví dụ `C:\Program Files\SceneBuilder\SceneBuilder.exe`).

2) Mở FXML bằng Scene Builder
- Cách nhanh (trong IntelliJ IDEA): Settings -> Languages & Frameworks -> JavaFX -> điền đường dẫn tới `SceneBuilder.exe`. Sau đó chuột phải file `.fxml` -> Open In SceneBuilder.
- Nếu không muốn cấu hình IDE, bạn có thể mở trực tiếp từ Scene Builder: File -> Open -> chọn file FXML (ví dụ `client/src/main/resources/view/login.fxml`).

3) Script tiện lợi (Windows)
- Trong thư mục `client/` có file `open_fxml_in_scene_builder.bat` để mở nhanh một file FXML bằng Scene Builder. Cách dùng:

  - Mở Command Prompt, chuyển đến thư mục project `Group-13-Project\client` và chạy:

    open_fxml_in_scene_builder.bat view\login.fxml

  - Nếu không truyền tham số, script sẽ mở `src\main\resources\view\login.fxml`.
  - Script tự tìm `SceneBuilder.exe` bằng biến môi trường `SCENEBUILDER_PATH` (nếu bạn đã cấu hình), hoặc thử các đường dẫn cài đặt thông dụng. Nếu không tìm thấy, script sẽ báo lỗi và yêu cầu bạn cài Scene Builder hoặc thiết lập biến môi trường `SCENEBUILDER_PATH`.

4) Lưu ý khi chỉnh sửa FXML
- Các file FXML đã được đặt under `client/src/main/resources/view/` và đã khai báo `fx:controller` trỏ tới các lớp trong package `controller`.
- Sau khi lưu FXML trong Scene Builder, build lại project hoặc chạy ứng dụng để xem thay đổi.

5) Chạy ứng dụng client (Gradle)
- Từ thư mục gốc project, bạn có thể chạy module client bằng Gradle (Windows):

  ```bat
  .\gradlew.bat :client:run
  ```

6) Nếu cần tôi có thể:
- Thêm FXML mẫu mới, thêm controller skeleton, hoặc tự động mở thêm script cho từng FXML. Nói cho tôi biết file nào bạn muốn chỉnh trực tiếp bằng Scene Builder và tôi sẽ chuẩn bị.

Chúc bạn thiết kế giao diện thuận lợi!
