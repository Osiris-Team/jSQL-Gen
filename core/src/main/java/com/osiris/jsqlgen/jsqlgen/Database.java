package com.osiris.jsqlgen.jsqlgen;
import java.sql.*;
import java.util.*;
import java.io.File;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/*
Auto-generated class that is used by all table classes to create connections. <br>
It holds the database credentials (set by you at first run of jSQL-Gen).<br>
Note that the fields rawUrl, url, username and password do NOT get overwritten when re-generating this class. <br>
All tables use the cached connection pool in this class which has following advantages: <br>
- Ensures optimal performance (cpu and memory usage) for any type of database from small to huge, with millions of queries per second.
- Connection status is checked before doing a query (since it could be closed or timed out and thus result in errors).*/
public class Database{
public static String url = "jdbc:mysql://localhost:3306/jsqlgen";
public static String rawUrl = getRawDbUrlFrom(url);
public static String name = "jsqlgen";
public static String username = "";
public static String password = "";
/** 
* False by default to ensure minimal data loss when using default remove() function.
* If true rows containing a reference/id of the deleted row, will be deleted too.
*/
public static boolean isRemoveRefs = false;
/** 
* Use synchronized on this before doing changes to it. 
*/
public static final List<Connection> availableConnections = new ArrayList<>();
public static final TableMetaData[] tables = new TableMetaData[]{new TableMetaData(594, 1, 0, "Timer", new String[]{"id", "start", "end"}, new String[]{"INT NOT NULL PRIMARY KEY", "DATETIME NOT NULL", "DATETIME NOT NULL"}){public Class<?> getTableClass(){return Timer.class;}public List<Database.Row> get(){List<Database.Row> l = new ArrayList<>(); for(Timer obj : Timer.get()) l.add(obj); return l;}public Database.Row get(int i){return Timer.get(i);}public void update(Database.Row obj){Timer.update((Timer)obj);}public void add(Database.Row obj){Timer.add((Timer)obj);}public void remove(Database.Row obj){Timer.remove((Timer)obj);}}, new TableMetaData(598, 1, 0, "TimerTask", new String[]{"id", "timerId", "taskId", "percentageOfTimer", "changelog"}, new String[]{"INT NOT NULL PRIMARY KEY", "INT NOT NULL", "INT NOT NULL", "DOUBLE NOT NULL", "TEXT DEFAULT ''"}){public Class<?> getTableClass(){return TimerTask.class;}public List<Database.Row> get(){List<Database.Row> l = new ArrayList<>(); for(TimerTask obj : TimerTask.get()) l.add(obj); return l;}public Database.Row get(int i){return TimerTask.get(i);}public void update(Database.Row obj){TimerTask.update((TimerTask)obj);}public void add(Database.Row obj){TimerTask.add((TimerTask)obj);}public void remove(Database.Row obj){TimerTask.remove((TimerTask)obj);}}, new TableMetaData(601, 1, 0, "Task", new String[]{"id", "name"}, new String[]{"INT NOT NULL PRIMARY KEY", "TEXT DEFAULT 'New Task'"}){public Class<?> getTableClass(){return Task.class;}public List<Database.Row> get(){List<Database.Row> l = new ArrayList<>(); for(Task obj : Task.get()) l.add(obj); return l;}public Database.Row get(int i){return Task.get(i);}public void update(Database.Row obj){Task.update((Task)obj);}public void add(Database.Row obj){Task.add((Task)obj);}public void remove(Database.Row obj){Task.remove((Task)obj);}}};

    static{initIntegratedMariaDB();create();} // Create database if not exists

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
            try {s.executeUpdate("ALTER TABLE `jsqlgen_metadata` ADD COLUMN `steps` INT NOT NULL");} catch (Exception ignored) {}

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
        TableMetaData t = null;
        for (TableMetaData t_ : tables) {
            if(t_.id == tableId){
                t = t_;
                break;
            }
        }
        Objects.requireNonNull(t);
        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password);
             Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT `tableId`,`tableVersion`,`steps`" +
                    " FROM `jsqlgen_metadata` WHERE tableId="+tableId)) {
                boolean exists = false;
                while (rs.next()) {
                    exists = true;
                    tableId = rs.getInt(1);
                    int tableVersion = rs.getInt(2);
                    int steps = rs.getInt(3);
                    t.id = tableId;
                    t.version = tableVersion;
                    t.steps = steps;
                }
                if(!exists){
                    // Insert new row
                    String insertQuery = "INSERT INTO `jsqlgen_metadata` (`tableId`, `tableVersion`, `steps`) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = c.prepareStatement(insertQuery)) {
                        ps.setInt(1, t.id);
                        t.version = 0;
                        ps.setInt(2, t.version);
                        t.steps = 0;
                        ps.setInt(3, t.steps);
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
            String updateQuery = "UPDATE `jsqlgen_metadata` SET `tableId`=?, `tableVersion`=?, `steps`=? WHERE `tableId`=?";
            try (PreparedStatement ps = c.prepareStatement(updateQuery)) {
                ps.setLong(1, t.id);
                ps.setLong(2, t.version);
                ps.setLong(3, t.steps);
                ps.setLong(4, t.id);
                ps.executeUpdate();
            } 
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Something went really wrong during database initialisation, program will exit.");
            System.exit(1);
        }
    }
    public interface Row{
        int getId();
        void setId(int id);
        void update();
        void add();
        void remove();
        String toPrintString();
        String toMinimalPrintString();
    }

    public static class TableMetaData {
        public int id;
        public int version;
        public int steps;
        public String name;
        public String[] columns;
        public String[] definitions;

        public TableMetaData(int id, int version, int steps, String name, String[] columns, String[] definitions) {
            this.id = id;
            this.version = version;
            this.steps = steps;
            this.name = name;
            this.columns = columns;
            this.definitions = definitions;
        }

        // Implementations for the following methods are provided in the array initialisation of 'tables'

        public Class<?> getTableClass(){throw new RuntimeException("Not implemented!");}
        public List<Database.Row> get(){throw new RuntimeException("Not implemented!");}
        public Database.Row get(int i){throw new RuntimeException("Not implemented!");}
        public void update(Database.Row obj){throw new RuntimeException("Not implemented!");}
        public void add(Database.Row obj){throw new RuntimeException("Not implemented!");}
        public void remove(Database.Row obj){throw new RuntimeException("Not implemented!");}
    }
public static DB mariaDB;
/**
 * Creates or uses the database inside ./db and runs it via MariaDB4j on a random available port. <br>
 * MariaDB4j handles downloading of MariaDB and launching it. <br>
 * Returns once fully launched or throws exception on fail. <br>
 */
public static void initIntegratedMariaDB() {
    try{
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(0); // OR, default: setPort(0); => autom. detect free port
        configBuilder.setDataDir(new File(System.getProperty("user.dir") + "/db").getAbsolutePath());
        mariaDB = DB.newEmbeddedDB(configBuilder.build());
        mariaDB.start();
        String port = url.substring(url.lastIndexOf(":"), url.lastIndexOf("/"));
        url = url.replace(port, ":"+mariaDB.getConfiguration().getPort());
        rawUrl = getRawDbUrlFrom(url);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Stopping database...");
                mariaDB.stop();
                System.out.println("Stopped database successfully.");
            } catch (ManagedProcessException e) {
                throw new RuntimeException(e);
            }
        }));
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
}
