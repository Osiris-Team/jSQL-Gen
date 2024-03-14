package com.osiris.jsqlgen.testDB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/*
Auto-generated class that is used by all table classes to create connections. <br>
It holds the database credentials (set by you at first run of jSQL-Gen).<br>
Note that the fields rawUrl, url, username and password do NOT get overwritten when re-generating this class. <br>
All tables use the cached connection pool in this class which has following advantages: <br>
- Ensures optimal performance (cpu and memory usage) for any type of database from small to huge, with millions of queries per second.
- Connection status is checked before doing a query (since it could be closed or timed out and thus result in errors).*/
public class Database{
public static String url = "jdbc:mysql://localhost:3307/testDB";
public static String rawUrl = getRawDbUrlFrom(url);
public static String name = "testDB";
public static String username = "root";
public static String password = "";
/** 
* Use synchronized on this before doing changes to it. 
*/
public static final List<Connection> availableConnections = new ArrayList<>();
public static final TableMetaData[] tables = new TableMetaData[]{};

    static{create();} // Create database if not exists

public static void create() {

        // Do the below to avoid "No suitable driver found..." exception
        String[] driversClassNames = new String[]{"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver",
        "oracle.jdbc.OracleDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "org.postgresql.Driver",
        "org.sqlite.JDBC", "org.h2.Driver", "com.ibm.db2.jcc.DB2Driver", "org.apache.derby.jdbc.ClientDriver",
        "org.mariadb.jdbc.Driver", "org.apache.derby.jdbc.ClientDriver"};
        Class<?> driverClass = null;
        Exception lastException = null;
    for (int i = 0; i < driversClassNames.length; i++) {
        String driverClassName = driversClassNames[i];
        try {
            driverClass = Class.forName(driverClassName);
            Objects.requireNonNull(driverClass);
            break; // No need to continue, since registration was a success 
        } catch (Exception e) {
            lastException = e;
        }
    }
    if(driverClass == null){
        if(lastException != null) lastException.printStackTrace();
        System.err.println("Failed to find critical database driver class, program will exit! Searched classes: "+ Arrays.toString(driversClassNames));
        System.exit(1);
    }

        // Create database if not exists
        try(Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);
            Statement s = c.createStatement();) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `"+Database.name+"`");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Something went really wrong during database initialisation, program will exit.");
            System.exit(1);
        }
        // Create metadata table if not exists
        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS `jsqlgen_metadata` (`tableId` INT NOT NULL PRIMARY KEY)");
            try {s.executeUpdate("ALTER TABLE `jsqlgen_metadata` ADD COLUMN `tableVersion` INT NOT NULL");} catch (Exception ignored) {}

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Something went really wrong during database initialisation, program will exit.");
            System.exit(1);
        }
    }

    public static Connection getCon() {
        synchronized (availableConnections){
            try{
                Connection availableCon = null;
                if (!availableConnections.isEmpty()) {
                    List<Connection> removableConnections = new ArrayList<>(0);
                    for (Connection con : availableConnections) {
                        if (!con.isValid(1)) {con.close(); removableConnections.add(con);}
                        else {availableCon = con; removableConnections.add(con); break;}
                    }
                    for (Connection removableConnection : removableConnections) {
                        availableConnections.remove(removableConnection); // Remove invalid or used connections
                    }
                }
                if (availableCon != null) return availableCon;
                else return DriverManager.getConnection(Database.url, Database.username, Database.password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void freeCon(Connection connection) {
        synchronized (availableConnections){
            availableConnections.add(connection);
        }
    }
    /**
     * Gets the raw database url without database name. <br>
     * Before: "jdbc:mysql://localhost/my_database" <br>
     * After: "jdbc:mysql://localhost" <br>
     */
    public static String getRawDbUrlFrom(String databaseUrl) {
        int index = 0;
        int count = 0;
        for (int i = 0; i < databaseUrl.length(); i++) {
            char c = databaseUrl.charAt(i);
            if(c == '/'){
                index = i;
                count++;
            }
            if(count == 3) break;
        }
        if(count != 3) return databaseUrl; // Means there is less than 3 "/", thus may already be raw url, or totally wrong url
        return databaseUrl.substring(0, index);
    }
    public static TableMetaData getTableMetaData(int tableId) {
        TableMetaData t = new TableMetaData(tableId, 0);
        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password);
             Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT `tableId`,`tableVersion`" +
                    " FROM `jsqlgen_metadata` WHERE tableId="+tableId)) {
                boolean exists = false;
                while (rs.next()) {
                    exists = true;
                    tableId = rs.getInt(1);
                    int tableVersion = rs.getInt(2);
                    t.id = tableId;
                    t.version = tableVersion;
                }
                if(!exists){
                    // Insert new row
                    String insertQuery = "INSERT INTO `jsqlgen_metadata` (`tableId`, `tableVersion`) VALUES (?, ?)";
                    try (PreparedStatement ps = c.prepareStatement(insertQuery)) {
                        ps.setLong(1, t.id);
                        ps.setLong(2, t.version);
                        ps.executeUpdate();
                    }
                }
            }
            // In each table at start, get this metadata object and compare with the actual table version
            // that was generated by jsqlgen, then execute all the SQL changes for missing inbetween versions.

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Something went really wrong during database initialisation, program will exit.");
            System.exit(1);
        }
        return t;
    }

    public static void updateTableMetaData(TableMetaData t) {
        // Create metadata table if not exists
        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password)) {
            // Update existing row
            String updateQuery = "UPDATE `jsqlgen_metadata` SET `tableId`=?, `tableVersion`=? WHERE `tableId`=?";
            try (PreparedStatement ps = c.prepareStatement(updateQuery)) {
                ps.setLong(1, t.id);
                ps.setLong(2, t.version);
                ps.setLong(3, t.id);
                ps.executeUpdate();
            } 
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Something went really wrong during database initialisation, program will exit.");
            System.exit(1);
        }
    }

    public static class TableMetaData {
        public int id;
        public int version;

        public TableMetaData(int id, int version) {
            this.id = id;
            this.version = version;
        }
    }
}
