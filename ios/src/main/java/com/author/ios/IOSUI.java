package com.author.ios;

import com.osiris.desku.Route;
import com.osiris.desku.UI;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLRequest;

public class IOSUI extends UI {
    public IOSUI(Route route) throws Exception {
        super(route);
    }

    public IOSUI(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        super(route, isTransparent, widthPercent, heightPercent);
    }

    @Override
    public void init(String startURL, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        NSURL url = new NSURL(startURL);
        NSURLRequest request = new NSURLRequest(url);
        IOSLauncher.webView.loadRequest(request);

        //TODO IOSLauncher.webView.setBackgroundColor(new UIColor());

        // JavaScript cannot be executed before the page is loaded
        while (!IOSLauncher.webView.isLoading()) Thread.yield();
    }

    @Override
    public void width(int widthPercent) {

    }

    @Override
    public void height(int heightPercent) {

    }

    @Override
    public void plusX(int x) {

    }

    @Override
    public void plusY(int y) {

    }

    @Override
    public void executeJavaScript(String jsCode, String jsCodeSourceName, int jsCodeStartingLineNumber) {
        IOSLauncher.webView.evaluateJavaScript(jsCode);
    }
}
