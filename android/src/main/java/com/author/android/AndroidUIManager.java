package com.author.android;

import android.app.Activity;

import com.osiris.desku.Route;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.UIManager;

public class AndroidUIManager extends UIManager {
    public static Activity mainActivity;

    @Override
    public UI create(Route route) throws Exception {
        return new AndroidUI(route);
    }

    @Override
    public UI create(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        return new AndroidUI(route, isTransparent, isDecorated, widthPercent, heightPercent);
    }
}
