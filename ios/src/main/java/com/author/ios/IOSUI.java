package com.author.ios;

import com.osiris.desku.Route;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.utils.Rectangle;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class IOSUI extends UI {
    public IOSUI(Route route) throws Exception {
        super(route);
    }

    public IOSUI(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        super(route, isTransparent, isDecorated, widthPercent, heightPercent);
    }

    @Override
    public void init(String startURL, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
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

    @Override
    public void maximize(boolean b) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public void minimize(boolean b) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public void fullscreen(boolean b) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public void onSizeChange(Consumer<Rectangle> code) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public Rectangle getScreenSize() throws InterruptedException, InvocationTargetException {
        return null;
    }

    @Override
    public Rectangle getScreenSizeWithoutTaskBar() throws InterruptedException, InvocationTargetException {
        return null;
    }

    @Override
    public void decorate(boolean b) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public void allwaysOnTop(boolean b) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public void focus(boolean b) throws InterruptedException, InvocationTargetException {

    }

    @Override
    public void background(String hexColor) throws InterruptedException, InvocationTargetException {

    }
}
