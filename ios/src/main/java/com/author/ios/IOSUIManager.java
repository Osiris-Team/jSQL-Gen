package com.author.ios;

import com.osiris.desku.Route;
import com.osiris.desku.UI;
import com.osiris.desku.UIManager;

public class IOSUIManager extends UIManager {
    @Override
    public UI create(Route route) throws Exception {
        return new IOSUI(route);
    }

    @Override
    public UI create(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        return new IOSUI(route, isTransparent, widthPercent, heightPercent);
    }
}
