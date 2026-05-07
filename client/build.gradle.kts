plugins {
    java
    application
    // Thêm các plugin JavaFX nếu bạn dùng giao diện đồ họa
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1") // Để đọc JSON từ server
    // Thêm thư viện JavaFX tại đây
}

application {
    mainClass.set("view.MainApp") // Trỏ đến file chạy giao diện của bạn
}