
module com.osiris.jsqlgen {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires com.squareup.javapoet;
    requires java.compiler;

    opens com.osiris.jsqlgen to javafx.fxml;
    exports com.osiris.jsqlgen;
}
