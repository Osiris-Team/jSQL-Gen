package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class GenDatabaseFile {
    public static void s(Database db, File databaseFile, String rawUrl, String url, String name, String username, String password) throws IOException {
        databaseFile.getParentFile().mkdirs();
        databaseFile.createNewFile();

        LinkedHashSet<String> imports = new LinkedHashSet<>();
        imports.add("import java.sql.*;");
        imports.add("import java.util.*;");
        imports.add("import java.io.File;");

        StringBuilder s = new StringBuilder(
                "/**\n" +
                "Auto-generated class that is used by all table classes to create connections. <br>\n" +
                "It holds the database credentials (set by you at first run of jSQL-Gen).<br>\n" +
                "Note that the fields rawUrl, url, username and password do NOT get overwritten when re-generating this class. <br>\n" +
                "All tables use the cached connection pool in this class which has following advantages: <br>\n" +
                "- Ensures optimal performance (cpu and memory usage) for any type of database from small to huge, with millions of queries per second.<br>\n" +
                "- Connection status is checked before doing a query (since it could be closed or timed out and thus result in errors).<br>\n" +
                "*/\n" +
                "public class Database{\n" +
                "public static String url = " + url + ";\n" +
                "public static String rawUrl = " + rawUrl + ";\n" +
                "public static String name = " + name + ";\n" +
                "public static String username = " + username + ";\n" +
                "public static String password = " + password + ";\n" +
                "/** \n" +
                "* False by default to ensure minimal data loss when using default remove() function.\n" +
                "* If true rows containing a reference/id of the deleted row, will be deleted too.\n" +
                "*/\n" +
                "public static boolean isRemoveRefs = false;\n" +
                "/** \n" +
                "* Use synchronized on this before doing changes to it. \n" +
                "*/\n" +
                "public static final List<Connection> availableConnections = new ArrayList<>();\n" +
                    "public static final int defaultInMemoryOnlyObjId = -1;\n" +
                "public static final TableMetaData[] tables = new TableMetaData[]{");

        CopyOnWriteArrayList<Table> tables = db.tables;
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            int latestTableVersion = t.changes.size(); // TODO t.changes has not latest change included at this point yet
            // TODO since this gets generated before the tables get generated.
            // id, tableVersion, steps
            s.append("new TableMetaData("+t.id+", "+latestTableVersion+", 0, \""+t.name+"\", ");
            s.append("new String[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                s.append("\""+t.columns.get(j).name+"\"");
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("}, new String[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                s.append("\""+t.columns.get(j).definition+"\"");
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("})");

            // Create overriding methods
            var idCol = t.columns.get(0);
            s.append("{");
            s.append("public Class<?> getTableClass(){return "+t.name+".class;}");
            s.append("public List<Database.Row> get(){List<Database.Row> l = new ArrayList<>(); for("+t.name+" obj : "+t.name+".get()) l.add(obj); return l;}");
            s.append("public Database.Row get(Object id){return "+t.name+".get(("+idCol.type.inJava+") id);}");
            s.append("public void update(Database.Row obj){"+t.name+".update(("+ t.name +")obj);}");
            s.append("public void add(Database.Row obj){"+t.name+".add(("+ t.name +")obj);}");
            s.append("public void remove(Database.Row obj){"+t.name+".remove(("+ t.name +")obj);}");
            s.append("}");

            if(i != tables.size() - 1) s.append(", ");
        }

        s.append("};\n" +
                "\n" +
                "    static{" +
                (db.isWithMariadb4j ? "initIntegratedMariaDB();" : "")+
            "create();} // Create database if not exists\n" +
                "\n" +
                "public static void create() {\n" +
                "\n" +
                "        // Do the below to avoid \"No suitable driver found...\" exception\n" +
                "        String[] driversClassNames = new String[]{\"com.mysql.cj.jdbc.Driver\", \"com.mysql.jdbc.Driver\",\n" +
                "        \"oracle.jdbc.OracleDriver\", \"com.microsoft.sqlserver.jdbc.SQLServerDriver\", \"org.postgresql.Driver\",\n" +
                "        \"org.sqlite.JDBC\", \"org.h2.Driver\", \"com.ibm.db2.jcc.DB2Driver\", \"org.apache.derby.jdbc.ClientDriver\",\n" +
                "        \"org.mariadb.jdbc.Driver\", \"org.apache.derby.jdbc.ClientDriver\"};\n" +
                "        Class<?> driverClass = null;\n" +
                "        Exception lastException = null;\n" +
                "    for (int i = 0; i < driversClassNames.length; i++) {\n" +
                "        String driverClassName = driversClassNames[i];\n" +
                "        try {\n" +
                "            driverClass = Class.forName(driverClassName);\n" +
                "            Objects.requireNonNull(driverClass);\n" +
                "            break; // No need to continue, since registration was a success \n" +
                "        } catch (Exception e) {\n" +
                "            lastException = e;\n" +
                "        }\n" +
                "    }\n" +
                "    if(driverClass == null){\n" +
                "        if(lastException != null) lastException.printStackTrace();\n" +
                "        System.err.println(\"Failed to find critical database driver class, program will exit! Searched classes: \"+ Arrays.toString(driversClassNames));\n" +
                "        System.exit(1);\n" +
                "    }\n" +
                "\n");
        if(db.isVersioning)
            s.append(
                "        // Create database if not exists\n" +
            "        try(Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);\n" +
            "            Statement s = c.createStatement();) {\n" +
            "            s.executeUpdate(\"CREATE DATABASE IF NOT EXISTS `\"+Database.name+\"`\");\n" +
            "        } catch (SQLException e) {\n" +
            "            e.printStackTrace();\n" +
            "            System.err.println(\"Something went really wrong during database initialisation, program will exit.\");\n" +
            "            System.exit(1);\n" +
            "        }\n" +
            "        // Create metadata table if not exists\n" +
            "        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
            "             Statement s = c.createStatement()) {\n" +
            "            s.executeUpdate(\"CREATE TABLE IF NOT EXISTS `jsqlgen_metadata` (`tableId` INT NOT NULL PRIMARY KEY)\");\n" +
            "            try {s.executeUpdate(\"ALTER TABLE `jsqlgen_metadata` ADD COLUMN `tableVersion` INT NOT NULL\");} catch (Exception ignored) {}\n" +
            "            try {s.executeUpdate(\"ALTER TABLE `jsqlgen_metadata` ADD COLUMN `steps` INT NOT NULL\");} catch (Exception ignored) {}\n" +
            "\n" +
            "        } catch (SQLException e) {\n" +
            "            e.printStackTrace();\n" +
            "            System.err.println(\"Something went really wrong during database initialisation, program will exit.\");\n" +
            "            System.exit(1);\n" +
            "        }\n");
        else
            s.append("// No database creation and no creation of jsqlgen_metadata table because versioning is disabled!\n");
        s.append(
            "    }\n" +
                "\n" +
                "    public static Connection getCon() {\n" +
                "        synchronized (availableConnections){\n" +
                "            try{\n" +
                "                Connection availableCon = null;\n" +
                "                if (!availableConnections.isEmpty()) {\n" +
                "                    List<Connection> removableConnections = new ArrayList<>(0);\n" +
                "                    for (Connection con : availableConnections) {\n" +
                "                        if (!con.isValid(1)) {con.close(); removableConnections.add(con);}\n" +
                "                        else {availableCon = con; removableConnections.add(con); break;}\n" +
                "                    }\n" +
                "                    for (Connection removableConnection : removableConnections) {\n" +
                "                        availableConnections.remove(removableConnection); // Remove invalid or used connections\n" +
                "                    }\n" +
                "                }\n" +
                "                if (availableCon != null) return availableCon;\n" +
                "                else return DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
                "            } catch (Exception e) {\n" +
                "                throw new RuntimeException(e);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static void freeCon(Connection connection) {\n" +
                "        synchronized (availableConnections){\n" +
                "            availableConnections.add(connection);\n" +
                "        }\n" +
                "    }\n" +
                "    /**\n" +
                "     * Gets the raw database url without database name. <br>\n" +
                "     * Before: \"jdbc:mysql://localhost/my_database\" <br>\n" +
                "     * After: \"jdbc:mysql://localhost\" <br>\n" +
                "     */\n" +
                "    public static String getRawDbUrlFrom(String databaseUrl) {\n" +
                "        int index = 0;\n" +
                "        int count = 0;\n" +
                "        for (int i = 0; i < databaseUrl.length(); i++) {\n" +
                "            char c = databaseUrl.charAt(i);\n" +
                "            if(c == '/'){\n" +
                "                index = i;\n" +
                "                count++;\n" +
                "            }\n" +
                "            if(count == 3) break;\n" +
                "        }\n" +
                "        if(count != 3) return databaseUrl; // Means there is less than 3 \"/\", thus may already be raw url, or totally wrong url\n" +
                "        return databaseUrl.substring(0, index);\n" +
                "    }\n" +
                "" +
                "    public static TableMetaData getTableMetaData(int tableId) {\n" +
                "        TableMetaData t = null;\n" +
                "        for (TableMetaData t_ : tables) {\n" +
                "            if(t_.id == tableId){\n" +
                "                t = t_;\n" +
                "                break;\n" +
                "            }\n" +
                "        }\n" +
                "        Objects.requireNonNull(t);\n" +
                "        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
                "             Statement s = c.createStatement()) {\n" +
                "            try (ResultSet rs = s.executeQuery(\"SELECT `tableId`,`tableVersion`,`steps`\" +\n" +
                "                    \" FROM `jsqlgen_metadata` WHERE tableId=\"+tableId)) {\n" +
                "                boolean exists = false;\n" +
                "                while (rs.next()) {\n" +
                "                    exists = true;\n" +
                "                    tableId = rs.getInt(1);\n" +
                "                    int tableVersion = rs.getInt(2);\n" +
                "                    int steps = rs.getInt(3);\n" +
                "                    t.id = tableId;\n" +
                "                    t.version = tableVersion;\n" +
                "                    t.steps = steps;\n" +
                "                }\n" +
                "                if(!exists){\n" +
                "                    // Insert new row\n" +
                "                    String insertQuery = \"INSERT INTO `jsqlgen_metadata` (`tableId`, `tableVersion`, `steps`) VALUES (?, ?, ?)\";\n" +
                "                    try (PreparedStatement ps = c.prepareStatement(insertQuery)) {\n" +
                "                        ps.setInt(1, t.id);\n" +
                "                        t.version = 0;\n" +
                "                        ps.setInt(2, t.version);\n" +
                "                        t.steps = 0;\n" +
                "                        ps.setInt(3, t.steps);\n" +
                "                        ps.executeUpdate();\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "            // In each table at start, get this metadata object and compare with the actual table version\n" +
                "            // that was generated by jsqlgen, then execute all the SQL changes for missing inbetween versions.\n" +
                "\n" +
                "        } catch (SQLException e) {\n" +
                "            e.printStackTrace();\n" +
                "            System.err.println(\"Something went really wrong during database initialisation, program will exit.\");\n" +
                "            System.exit(1);\n" +
                "        }\n" +
                "        return t;\n" +
                "    }\n" +
                "\n" +
                "    public static void updateTableMetaData(TableMetaData t) {\n" +
                "        // Create metadata table if not exists\n" +
                "        try (Connection c = DriverManager.getConnection(Database.url, Database.username, Database.password)) {\n" +
                "            // Update existing row\n" +
                "            String updateQuery = \"UPDATE `jsqlgen_metadata` SET `tableId`=?, `tableVersion`=?, `steps`=? WHERE `tableId`=?\";\n" +
                "            try (PreparedStatement ps = c.prepareStatement(updateQuery)) {\n" +
                "                ps.setLong(1, t.id);\n" +
                "                ps.setLong(2, t.version);\n" +
                "                ps.setLong(3, t.steps);\n" +
                "                ps.setLong(4, t.id);\n" +
                "                ps.executeUpdate();\n" +
                "            } \n" +
                "        } catch (SQLException e) {\n" +
                "            e.printStackTrace();\n" +
                "            System.err.println(\"Something went really wrong during database initialisation, program will exit.\");\n" +
                "            System.exit(1);\n" +
                "        }\n" +
                "    }\n" +
                "    public interface Row{\n" +
                "        Object getId();\n" +
                "        void setId(Object id);\n" +
                "        void update();\n" +
                "        void add();\n" +
                "        void remove();\n" +
                "        String toPrintString();\n" +
                "        String toMinimalPrintString();\n" +
                "    }\n" +
                "\n" +
                "    public static class TableMetaData {\n" +
                "        public int id;\n" +
                "        public int version;\n" +
                "        public int steps;\n" +
                "        public String name;\n" +
                "        public String[] columns;\n" +
                "        public String[] definitions;\n" +
                "\n" +
                "        public TableMetaData(int id, int version, int steps, String name, String[] columns, String[] definitions) {\n" +
                "            this.id = id;\n" +
                "            this.version = version;\n" +
                "            this.steps = steps;\n" +
                "            this.name = name;\n" +
                "            this.columns = columns;\n" +
                "            this.definitions = definitions;\n" +
                "        }\n" +
                "\n" +
                "        // Implementations for the following methods are provided in the array initialisation of 'tables'\n" +
                "\n" +
                "        public Class<?> getTableClass(){throw new RuntimeException(\"Not implemented!\");}\n" + // Class is not provided as field to prevent static constructor execution
                "        public List<Database.Row> get(){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public Database.Row get(Object id){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public void update(Database.Row obj){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public void add(Database.Row obj){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public void remove(Database.Row obj){throw new RuntimeException(\"Not implemented!\");}\n" +
                "    }\n");
        for (Table t : tables) {
            if(t.isDebug){
                s.append("""
                                public static synchronized void printTable(TableMetaData table) {
                                    List<Row> rows = table.get();
                                    System.err.println("Printing table " + table.name+" with size = "+rows.size());
                                    for (Database.Row row : rows) {
                                        System.err.println(row.toPrintString());
                                    }
                                }""");
                break;
            }
        }

        if(db.isWithMariadb4j){
            s.append("""
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
    """);
        }

        // Add other dependencies
        s.append(GenDefBlobClass.s(imports));

        s.append("}\n");



        String sNoImports = s.toString();

        String finalS = (!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
            (db.isWithMariadb4j ? "" +
                "import ch.vorburger.exec.ManagedProcessException;\n" +
                "import ch.vorburger.mariadb4j.DB;\n" +
                "import ch.vorburger.mariadb4j.DBConfigurationBuilder;\n" : "")+
            "\n";
        for (String anImport : imports) {
            finalS += anImport+"\n";
        }
        finalS += sNoImports;


        Files.writeString(databaseFile.toPath(), finalS.toString());
    }
}
