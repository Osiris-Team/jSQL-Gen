package com.osiris.jsqlgen.utils;

import com.osiris.jsqlgen.utils.flexlayout.FlexBox;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.concurrent.Executors;

public class MyScroll extends ScrollPane {

    public MyScroll(VBox content) {
        super(content);
        FX.sizeFull(this);
        FX.sizeFull(content);
    }

    public MyScroll(HBox content) {
        super(content);
        FX.sizeFull(this);
        FX.sizeFull(content);
    }


    public void add(Node node){
        if(getContent() instanceof VBox)
            ((VBox)getContent()).getChildren().add(node);
        else
            ((HBox)getContent()).getChildren().add(node);
    }

    public void removeAll() {
        if(getContent() instanceof VBox)
            ((VBox)getContent()).getChildren().clear();
        else
            ((HBox)getContent()).getChildren().clear();
    }

    public FlexBox addRow(){
        FlexBox flexBox = new FlexBox();
        add(flexBox);

        return flexBox;
    }
}
