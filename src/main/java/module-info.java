module com.logan.waterchat {
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires org.slf4j;

    exports com.logan.waterchat;
    opens com.logan.waterchat;
}