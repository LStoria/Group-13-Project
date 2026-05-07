plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral() 
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    // Các thư viện JavaFX khác của bạn...
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("view.MainApp")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    // Dòng này giúp các lệnh System.out.println in được tiếng Việt
    jvmArgs("-Dfile.encoding=UTF-8")
}