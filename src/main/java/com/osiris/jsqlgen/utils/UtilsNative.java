package com.osiris.jsqlgen.utils;

import org.jline.utils.OSUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * A clean, library-friendly autostart utility supporting
 * Windows, Linux (systemd), and macOS (launchd).
 *
 * ✔ No config file needed
 * ✔ Detects autostart state directly from OS
 * ✔ Sensible defaults for library usage
 *
 * Example:
 *     UtilsNative auto = new UtilsNative("MyApp");
 *     auto.enableStartOnBootIfNeeded(new File("MyApp.jar"));
 */
public class UtilsNative {

    private final String serviceName;
    private final String javaCommand;
    private final File startScriptFolder;

    /* ---------------------------------------------------------
       CONSTRUCTORS
       --------------------------------------------------------- */

    /** Default constructor with sensible library-ready defaults */
    public UtilsNative() {
        this(
            "JavaApp",
            new File(System.getProperty("user.home")),
            OSUtils.IS_WINDOWS ? "javaw" : "java"
        );
    }

    /** Constructor with custom service name */
    public UtilsNative(String serviceName) {
        this(
            serviceName,
            new File(System.getProperty("user.home")),
            OSUtils.IS_WINDOWS ? "javaw" : "java"
        );
    }

    /** Full constructor */
    public UtilsNative(String serviceName, File startScriptFolder, String javaCommand) {
        this.serviceName = serviceName;
        this.startScriptFolder = startScriptFolder;
        this.javaCommand = javaCommand;
    }

    /* ---------------------------------------------------------
       REGISTRATION STATE (NO CONFIG FILE)
       --------------------------------------------------------- */

    public boolean isRegistered() {
        if (OSUtils.IS_WINDOWS) {
            return isRegisteredWindows();
        } else if (OSUtils.IS_OSX) {
            return isRegisteredMacOS();
        } else {
            return isRegisteredLinux();
        }
    }

    private boolean isRegisteredWindows() {
        try {
            Process p = new ProcessBuilder(
                "REG", "QUERY",
                "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run",
                "/V", serviceName
            ).start();

            while (p.isAlive()) Thread.sleep(30);

            return p.exitValue() == 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isRegisteredLinux() {
        File serviceFile = new File(System.getProperty("user.home") +
            "/.config/systemd/user/" + serviceName + ".service");
        return serviceFile.exists();
    }

    private boolean isRegisteredMacOS() {
        File plist = new File(System.getProperty("user.home") +
            "/Library/LaunchAgents/" + serviceName + ".plist");
        return plist.exists();
    }

    /* ---------------------------------------------------------
       PUBLIC API
       --------------------------------------------------------- */

    public void enableStartOnBootIfNeeded(File jar) throws Exception {
        if (!isRegistered())
            register(jar);
    }

    public void disableStartOnBootIfNeeded() throws Exception {
        if (isRegistered())
            remove();
    }

    public void register(File jar) throws Exception {
        if (OSUtils.IS_WINDOWS) {
            registerWindows(jar);
        } else if (OSUtils.IS_OSX) {
            registerMacOS(jar);
        } else {
            registerLinux(jar);
        }
    }

    public void remove() throws Exception {
        if (OSUtils.IS_WINDOWS) {
            removeWindows();
        } else if (OSUtils.IS_OSX) {
            removeMacOS();
        } else {
            removeLinux();
        }
    }

    /* ---------------------------------------------------------
       WINDOWS (Registry Run Key)
       --------------------------------------------------------- */

    private void registerWindows(File jar) throws Exception {
        File script = new File(startScriptFolder, serviceName + ".bat");
        script.getParentFile().mkdirs();
        script.createNewFile();

        String content =
            "@echo off\n" +
                "timeout /t 1 /nobreak\n" +
                "cd /d \"" + jar.getParentFile().getAbsolutePath() + "\"\n" +
                "start \"\" " + javaCommand + " -jar \"" + jar.getAbsolutePath() + "\"\n";

        Files.write(script.toPath(), content.getBytes(StandardCharsets.UTF_8));

        Process p = new ProcessBuilder(
            "REG", "ADD",
            "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run",
            "/V", serviceName,
            "/t", "REG_SZ",
            "/F",
            "/D", "\"" + script.getAbsolutePath() + "\""
        ).start();

        while (p.isAlive()) Thread.sleep(30);
        if (p.exitValue() != 0)
            throw new IOException("Failed to add registry autostart entry");
    }

    private void removeWindows() throws Exception {
        Process p = new ProcessBuilder(
            "REG", "DELETE",
            "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run",
            "/V", serviceName,
            "/F"
        ).start();

        while (p.isAlive()) Thread.sleep(30);
        if (p.exitValue() != 0)
            throw new IOException("Failed to remove Windows autostart entry");
    }

    /* ---------------------------------------------------------
       LINUX (systemd user service)
       --------------------------------------------------------- */

    private void registerLinux(File jar) throws Exception {
        File script = new File(startScriptFolder, serviceName + ".sh");
        script.getParentFile().mkdirs();
        script.createNewFile();

        String scriptContent =
            "#!/bin/sh\n" +
                "sleep 1\n" +
                "cd \"" + jar.getParentFile().getAbsolutePath() + "\"\n" +
                javaCommand + " -jar \"" + jar.getAbsolutePath() + "\"\n";

        Files.write(script.toPath(), scriptContent.getBytes(StandardCharsets.UTF_8));
        script.setExecutable(true);

        File serviceFile = new File(System.getProperty("user.home") +
            "/.config/systemd/user/" + serviceName + ".service");

        serviceFile.getParentFile().mkdirs();
        serviceFile.createNewFile();

        String serviceContent =
            "[Unit]\n" +
                "Description=" + serviceName + " autostart\n" +
                "\n" +
                "[Service]\n" +
                "ExecStart=" + script.getAbsolutePath() + "\n" +
                "\n" +
                "[Install]\n" +
                "WantedBy=default.target\n";

        Files.write(serviceFile.toPath(), serviceContent.getBytes(StandardCharsets.UTF_8));

        run("systemctl", "--user", "daemon-reload");
        run("systemctl", "--user", "enable", serviceName + ".service");
    }

    private void removeLinux() throws Exception {
        run("systemctl", "--user", "disable", serviceName + ".service");
        run("systemctl", "--user", "daemon-reload");

        File serviceFile = new File(System.getProperty("user.home") +
            "/.config/systemd/user/" + serviceName + ".service");
        serviceFile.delete();
    }

    /* ---------------------------------------------------------
       macOS (launchd)
       --------------------------------------------------------- */

    private void registerMacOS(File jar) throws Exception {
        File plist = new File(System.getProperty("user.home") +
            "/Library/LaunchAgents/" + serviceName + ".plist");

        plist.getParentFile().mkdirs();
        plist.createNewFile();

        String plistContent =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"\n" +
                "\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "  <key>Label</key><string>" + serviceName + "</string>\n" +
                "  <key>ProgramArguments</key>\n" +
                "  <array>\n" +
                "    <string>" + javaCommand + "</string>\n" +
                "    <string>-jar</string>\n" +
                "    <string>" + jar.getAbsolutePath() + "</string>\n" +
                "  </array>\n" +
                "  <key>RunAtLoad</key><true/>\n" +
                "</dict>\n" +
                "</plist>";

        Files.write(plist.toPath(), plistContent.getBytes(StandardCharsets.UTF_8));

        run("launchctl", "load", plist.getAbsolutePath());
    }

    private void removeMacOS() throws Exception {
        File plist = new File(System.getProperty("user.home") +
            "/Library/LaunchAgents/" + serviceName + ".plist");

        run("launchctl", "unload", plist.getAbsolutePath());
        plist.delete();
    }

    /* ---------------------------------------------------------
       UTILITY
       --------------------------------------------------------- */

    private void run(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).start();
        while (p.isAlive()) Thread.sleep(30);
        if (p.exitValue() != 0)
            throw new IOException("Command failed: " + String.join(" ", cmd));
    }
}
