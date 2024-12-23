package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.osiris.jlib.UtilsFiles;

import java.io.File;
import java.io.IOException;

public class SQLTestServer {
    private final DBConfigurationBuilder properties;
    private final DB server;
    private boolean running = false;
    private String name;
    private String url;

    /**
     * Creates and opens a new MySQL server
     * (or loads already existing) at the default path: 'user dir'/db
     */
    private SQLTestServer() throws ManagedProcessException {
        this(getDefaultSQLProps());
    }

    private SQLTestServer(DBConfigurationBuilder props) throws ManagedProcessException {
        properties = props;
        server = DB.newEmbeddedDB(props.build());
        server.start();
        running = true;
    }

    public static SQLTestServer buildAndRun() throws ManagedProcessException, IOException {
        return buildAndRun("testDB", 3306);
    }

    public static SQLTestServer buildAndRun(String name, int port) throws ManagedProcessException, IOException {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(port); // OR, default: setPort(0); => autom. detect free port
        configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false); // this doesnt seem to work if true
        configBuilder.setBaseDir(System.getProperty("user.dir") + File.separator +
                "db" + File.separator + "base");
        configBuilder.setDataDir(System.getProperty("user.dir") + File.separator +
                "db" + File.separator + "databases" + File.separator + File.separator + name); // just an example
        configBuilder.setLibDir(System.getProperty("user.dir") + File.separator +
                "db" + File.separator + "libs");

        new UtilsFiles().forceDeleteDirectory(new File(configBuilder.getDataDir()));
        SQLTestServer server = new SQLTestServer(configBuilder);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try{
                while (server.isRunning()) Thread.yield();
                new UtilsFiles().forceDeleteDirectory(new File(configBuilder.getDataDir()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        server.setName(name);
        server.setUrl("jdbc:mysql://localhost/");// MUST BE TEST, see: https://github.com/vorburger/MariaDB4j#how-java
        return server;
    }

    private static DBConfigurationBuilder getDefaultSQLProps() {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(3306); // OR, default: setPort(0); => autom. detect free port
        configBuilder.setDataDir(System.getProperty("user.dir") + File.separator + "db" + File.separator + "testDB"); // just an example
        return configBuilder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DBConfigurationBuilder getProperties() {
        return properties;
    }

    public DB getServer() {
        return server;
    }

    public boolean isRunning() {
        return running;
    }
}
