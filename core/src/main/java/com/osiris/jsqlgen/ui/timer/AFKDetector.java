package com.osiris.jsqlgen.ui.timer;

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
        t = new Thread(() -> {
            try {
                Point lastMousePosition = MouseInfo.getPointerInfo().getLocation();

                while (true) {
                    // Check keyboard activity
                    boolean keyPressed = com.sun.jna.platform.KeyboardUtils.isPressed(KeyEvent.KEY_PRESSED); // This method should check if any key is pressed

                    // Check mouse activity
                    Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();
                    boolean mouseMoved = !currentMousePosition.equals(lastMousePosition);
                    lastMousePosition = currentMousePosition;

                    long now = System.currentTimeMillis();
                    if (keyPressed || mouseMoved) {
                        if (wasAFK && onBack != null) {
                            onBack.accept(lastActivityTime); // User returned from AFK
                        }
                        lastActivityTime = now;
                        wasAFK = false;
                    } else if (!wasAFK && now - lastActivityTime > INACTIVITY_THRESHOLD) {
                        onAFK.accept(lastActivityTime); // User became AFK
                        wasAFK = true;
                    }

                    Thread.sleep(500); // Check every 500ms
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
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
