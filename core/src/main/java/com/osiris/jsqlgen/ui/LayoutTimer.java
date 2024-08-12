package com.osiris.jsqlgen.ui;

import com.osiris.desku.ui.layout.Vertical;

public class LayoutTimer extends Vertical implements Refreshable {
    public LayoutTimer() {
        refresh();
    }

    @Override
    public void refresh() {
        this.removeAll();


    }
}
