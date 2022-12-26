package com.osiris.jsqlgen.utils;

import com.osiris.jsqlgen.utils.flexlayout.FlexBox;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class MyScroll extends VBox {
    private final ListView<Node> content = FX.smoothScrollingList();

    public MyScroll() {
        super.getChildren().add(content);
        FX.sizeFull(this);
        FX.sizeFull(content);
    }

    public MyScroll(Node... children) {
        super.getChildren().add(content);
        FX.sizeFull(this);
        FX.sizeFull(content);
        add(children);
    }

    public void add(Node... node) {
        for (Node n : node) {
            content.getItems().add(n);
        }
    }

    public void add(Node node) {
        content.getItems().add(node);
    }

    public void removeAll() {
        content.getItems().clear();
    }

    public FlexBox addRow() {
        FlexBox flexBox = new FlexBox();
        add(flexBox);
        return flexBox;
    }
}
