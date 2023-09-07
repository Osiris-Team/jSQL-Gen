package com.author.ios;

import com.osiris.desku.App;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.*;

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends UIApplicationDelegateAdapter {
    public static UIWindow window;
    public static UIViewController rootViewController = new UIViewController();
    public static UIWebView webView;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        window = new UIWindow(UIScreen.getMainScreen().getBounds());
        webView = new UIWebView(window.getBounds());
        rootViewController.setView(webView);
        window.setRootViewController(rootViewController);

        App.init(new IOSUIManager());
        com.osiris.jsqlgen.Main.main(new String[]{});

        // Make the window visible
        window.makeKeyAndVisible();
        return true;
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
