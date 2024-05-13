module com.example.gestinfo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.fasterxml.jackson.databind;
    requires force.wsc;
    requires java.net.http;
    requires javafx.graphics;
    opens com.example.gestinfo to javafx.fxml;
    exports com.example.gestinfo;
}
