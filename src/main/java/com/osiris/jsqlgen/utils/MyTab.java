package com.osiris.jsqlgen.utils;

import javafx.scene.Node;
import javafx.scene.control.Tab;

public class MyTab {
    public Tab tab = new Tab();

    public MyTab(String text, Node content) {
        tab.setText(text);
        tab.setContent(content);
    }

    public Tab closable(boolean val){
        tab.closableProperty().set(val);
        return tab;
    }
}
