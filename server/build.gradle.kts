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
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.8")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
    useJUnitPlatform()
}
