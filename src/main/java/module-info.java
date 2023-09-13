module com.se233.chapter5 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    opens com.se233.chapter5 to javafx.fxml;
    exports com.se233.chapter5;
}