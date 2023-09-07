package com.author.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

import com.osiris.desku.App;
import com.author.core.Main;

/** Launches the Android application. */
public class AndroidLauncher extends Activity {

    public static Handler mainHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidUIManager.mainActivity = this;
        App.workingDir = getApplicationContext().getFilesDir();
        App.tempDir = getApplicationContext().getCacheDir();
        App.userDir = App.workingDir;
        // Allow network operations on the main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
            .permitNetwork()
            .build();
        StrictMode.setThreadPolicy(policy);

        mainHandler = new Handler(Looper.getMainLooper());


        App.init(new AndroidUIManager());
        new Thread(() -> {
            Main.main(new String[]{});
        }).start();
    }
}
