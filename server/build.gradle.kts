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
    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    // Trỏ chính xác đến class chứa hàm main của bạn
    mainClass.set("network.AuctionServer") 
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    // Dòng này giúp các lệnh System.out.println in được tiếng Việt
    jvmArgs("-Dfile.encoding=UTF-8")
}