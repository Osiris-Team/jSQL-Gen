package com.author.ios;

import com.osiris.desku.Route;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.UIManager;

public class IOSUIManager extends UIManager {
    @Override
    public UI create(Route route) throws Exception {
        return new IOSUI(route);
    }

    @Override
    public UI create(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        return new IOSUI(route, isTransparent, isDecorated, widthPercent, heightPercent);
    }
}
