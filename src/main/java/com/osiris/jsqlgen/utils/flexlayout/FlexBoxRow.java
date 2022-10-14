package com.osiris.jsqlgen.utils.flexlayout;

import java.util.ArrayList;

/**
 * Created by TB on 19.10.16.
 */
public class FlexBoxRow {
    public double rowMinWidth = 0;
    public double flexGrowSum = 0;
    private ArrayList<FlexBoxItem> nodes;

    public void addFlexBoxItem(FlexBoxItem flexBoxItem) {
        if (nodes == null) {
            nodes = new ArrayList<FlexBoxItem>();
        }
        nodes.add(flexBoxItem);
    }

    public ArrayList<FlexBoxItem> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<FlexBoxItem>();
        }
        return nodes;
    }
}
