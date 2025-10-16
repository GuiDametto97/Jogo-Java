module com.example.aulasfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.aulasfx to javafx.fxml;
    exports com.example.aulasfx;
}