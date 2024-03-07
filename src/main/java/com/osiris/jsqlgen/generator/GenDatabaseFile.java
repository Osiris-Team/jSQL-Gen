package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GenDatabaseFile {
    public static void s(Database db, File databaseFile, String rawUrl, String url, String name, String username, String password) throws IOException {
        databaseFile.getParentFile().mkdirs();
        databaseFile.createNewFile();
        Files.writeString(databaseFile.toPath(), (db.javaProjectDir != null ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                "import java.sql.Connection;\n" +
                "import java.sql.DriverManager;\n" +
                "import java.sql.SQLException;\n" +
                "import java.sql.Statement;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Objects;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n\n" +
                "/*\n" +
                "Auto-generated class that is used by all table classes to create connections. <br>\n" +
                "It holds the database credentials (set by you at first run of jSQL-Gen).<br>\n" +
                "Note that the fields rawUrl, url, username and password do NOT get overwritten when re-generating this class. <br>\n" +
                "All tables use the cached connection pool in this class which has following advantages: <br>\n" +
                "- Ensures optimal performance (cpu and memory usage) for any type of database from small to huge, with millions of queries per second.\n" +
                "- Connection status is checked before doing a query (since it could be closed or timed out and thus result in errors)." +
                "*/\n" +
                "public class Database{\n" +
                "public static String url = " + url + ";\n" +
                "public static String rawUrl = " + rawUrl + ";\n" +
                "public static String name = " + name + ";\n" +
                "public static String username = " + username + ";\n" +
                "public static String password = " + password + ";\n" +
                "/** \n" +
                "* Use synchronized on this before doing changes to it. \n" +
                "*/\n" +
                "public static final List<Connection> availableConnections = new ArrayList<>();\n" +
                "\n" +
                "    static{create();} // Create database if not exists\n" +
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
                "\n" +
                "        // Create database if not exists\n" +
                "        try(Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);\n" +
                "            Statement s = c.createStatement();) {\n" +
                "            s.executeUpdate(\"CREATE DATABASE IF NOT EXISTS `\"+Database.name+\"`\");\n" +
                "        } catch (SQLException e) {\n" +
                "            e.printStackTrace();\n" +
                "            System.err.println(\"Something went really wrong during database initialisation, program will exit.\");\n" +
                "            System.exit(1);\n" +
                "        }\n" +
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
                "    }" +
                "}\n");
    }
}
