package com.osiris.jsqlgen.utils;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class MariaDB4jLogSilencer {

    public static void silenceMariaDB4jLogs() {
        silenceSlf4jSimple();
        silenceJavaUtilLogging();
    }

    /**
     * 🔇 SLF4J SimpleLogger (slf4j-simple.jar)
     */
    private static void silenceSlf4jSimple() {
        try {
            System.setProperty("org.slf4j.simpleLogger.log.ch.vorburger.mariadb4j", "off");
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
            System.out.println("✅ SLF4J SimpleLogger silenced for MariaDB4j");
        } catch (Exception ignored) {}
    }

    /**
     * 🔇 java.util.logging (JUL)
     */
    private static void silenceJavaUtilLogging() {
        try {
            Logger julLogger = LogManager.getLogManager().getLogger("ch.vorburger.mariadb4j");
            if (julLogger != null) {
                julLogger.setLevel(Level.OFF);
                System.out.println("✅ JUL logger silenced for MariaDB4j");
            }
        } catch (Exception ignored) {}
    }

    // Convenience method to call everything
    public static void init() {
        silenceMariaDB4jLogs();
    }

    // Example usage
    public static void main(String[] args) {
        MariaDB4jLogSilencer.init();
        System.out.println("MariaDB4j logging silenced across all frameworks.");
    }
}

