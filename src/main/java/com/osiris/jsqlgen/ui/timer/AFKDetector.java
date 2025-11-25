package com.osiris.jsqlgen.ui.timer;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class AFKDetector implements AutoCloseable {

    private static final long INACTIVITY_THRESHOLD = 5 * 60 * 1000; // 5 minutes in milliseconds
    private volatile long lastActivityTime;
    public Consumer<Long> onAFK;
    public Consumer<Long> onBack;

    private boolean wasAFK = false;
    private Thread t;

    public AFKDetector(Consumer<Long> onAFK, Consumer<Long> onBack) {
        this.onAFK = onAFK;
        this.onBack = onBack;
        lastActivityTime = System.currentTimeMillis();
    }

    public void startActivityMonitorIfNeeded() {
        if(t != null && t.isAlive()) return;
        startActivityMonitor(onAFK, onBack);
    }

    public void startActivityMonitor(Consumer<Long> onAFK, Consumer<Long> onBack) {
        this.onAFK = onAFK;
        this.onBack = onBack;

        if (t != null) t.interrupt();
        Thread t = new Thread(() -> {
            try {
                WinDef.POINT lastMouse = new WinDef.POINT();
                User32.INSTANCE.GetCursorPos(lastMouse);

                while (true) {
                    boolean active = false;

                    // 1) Check mouse movement
                    WinDef.POINT current = new WinDef.POINT();
                    User32.INSTANCE.GetCursorPos(current);

                    boolean mouseMoved = (current.x != lastMouse.x ||
                        current.y != lastMouse.y);

                    if (mouseMoved) {
                        active = true;
                        lastMouse.x = current.x;
                        lastMouse.y = current.y;
                    }

                    // 2) Check keyboard activity
                    if (isAnyKeyPressed()) {
                        active = true;
                    }

                    long now = System.currentTimeMillis();

                    if (active) {
                        if (wasAFK && onBack != null) {
                            onBack.accept(lastActivityTime);
                        }
                        lastActivityTime = now;
                        wasAFK = false;
                    } else if (!wasAFK && now - lastActivityTime > INACTIVITY_THRESHOLD) {
                        if (onAFK != null) {
                            onAFK.accept(lastActivityTime);
                        }
                        wasAFK = true;
                    }

                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private boolean isAnyKeyPressed() {
        for (int key = 0x08; key <= 0xFE; key++) {
            if ((User32.INSTANCE.GetAsyncKeyState(key) & 0x8000) != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        if (t != null) t.interrupt();
    }

    public static void main(String[] args) {
        new AFKDetector(
            (v) -> {
                System.out.println("User has been AFK for more than 5 minutes!");
                // Add any other logic to handle AFK here
            },
            (v) -> {
                System.out.println("User has returned from AFK!");
                // Add any other logic to handle returning from AFK here
            }
        );
    }
}
