plugins {
    java
    // Plugin application giúp định nghĩa file chạy chính (chứa hàm main)
    application 
}

// Khai báo nơi chứa các thư viện dùng chung cho Java
repositories {
    mavenCentral()
}

dependencies {
    // Tạm thời để trống. 
    // Sau này bạn sẽ thêm các thư viện xử lý JSON hoặc kết nối Database vào đây
}

application {
    // Trỏ chính xác đến class chứa hàm main của bạn
    mainClass.set("network.AuctionServer") 
}