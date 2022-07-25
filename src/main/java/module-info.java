
module com.osiris.jsqlgen {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires java.compiler;
    requires org.apache.commons.collections4;

    opens com.osiris.jsqlgen to javafx.fxml;
    exports com.osiris.jsqlgen;
    exports com.osiris.jsqlgen.model;
    opens com.osiris.jsqlgen.model to javafx.fxml;
    exports com.osiris.jsqlgen.utils;
    opens com.osiris.jsqlgen.utils to javafx.fxml;
}
