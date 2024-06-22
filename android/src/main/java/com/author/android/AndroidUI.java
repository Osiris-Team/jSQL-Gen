package com.author.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.osiris.desku.Route;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.utils.Rectangle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AndroidUI extends UI {

    private class MyWebViewClient extends WebViewClient {
        public final AtomicBoolean isPageLoaded = new AtomicBoolean(false);
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Return false to let WebView handle the URL loading
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            isPageLoaded.set(true);
        }
    }
    public WebView webView;

    public AndroidUI(Route route) throws Exception {
        super(route);
    }

    public AndroidUI(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        super(route, isTransparent, isDecorated, widthPercent, heightPercent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void init(String startURL, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        // Remove first / to ensure file:///path and not file:////path is used as url.

        MyWebViewClient myWebViewClient = new MyWebViewClient();
        String finalStartURL = startURL;
        AndroidLauncher.mainHandler.post(() -> {
            // Create a new WebView instance
            Context context = AndroidUIManager.mainActivity.getApplicationContext(); // Replace with your context reference
            webView = new WebView(context);
            // Configure WebView settings
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            if (isTransparent) {
                webView.setBackgroundColor(0x00000000); // Transparent background
            }

            // Attach WebViewClient to handle page loading

            webView.setWebViewClient(myWebViewClient);

            // Attach WebChromeClient for JavaScript console logging
            webView.setWebChromeClient(new WebChromeClient());

            // Load the provided startURL
            webView.loadUrl(finalStartURL);

            // Set the dimensions of the WebView
            // Note: You may need to adjust the widthPercent and heightPercent calculation based on your requirements
            //int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            //int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            //int width = (int) (screenWidth * (widthPercent / 100.0));
            //int height = (int) (screenHeight * (heightPercent / 100.0));
            //ebView.setLayoutParams(new LayoutParams(width, height));

            // Add the WebView to your layout or view hierarchy
            // Replace 'yourContainer' with the appropriate container view
            AndroidUIManager.mainActivity.setContentView(webView);
        });

        // JavaScript cannot be executed before the page is loaded
        while (!myWebViewClient.isPageLoaded.get()) Thread.yield();
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
        AndroidLauncher.mainHandler.post(() -> {
            webView.loadUrl("javascript:(function() { " + jsCode + " })()");
        });
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
