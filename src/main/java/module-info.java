module com.example.serveresmfamil {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires netty.socketio;
    requires json;
    requires com.google.gson;

    opens com.example.serveresmfamil to javafx.fxml,com.google.gson;
    exports com.example.serveresmfamil;
    exports com.example.serveresmfamil.Models;
    opens com.example.serveresmfamil.Models to javafx.fxml, com.google.gson;
}