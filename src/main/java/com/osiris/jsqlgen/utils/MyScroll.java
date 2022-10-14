package com.osiris.jsqlgen.utils;

import com.osiris.jsqlgen.utils.flexlayout.FlexBox;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class MyScroll extends ScrollPane {

    public MyScroll() {
        this(new VBox());
    }

    public MyScroll(VBox content) {
        super(content);
        FX.sizeFull(this);
        FX.sizeFull(content);
    }

    public void add(Node node) {
        ((VBox) getContent()).getChildren().add(node);
    }

    public void removeAll() {
        ((VBox) getContent()).getChildren().clear();
    }

    public FlexBox addRow() {
        FlexBox flexBox = new FlexBox();
        add(flexBox);

        return flexBox;
    }
}
