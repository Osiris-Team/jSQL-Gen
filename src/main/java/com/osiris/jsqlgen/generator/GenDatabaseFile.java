package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.UString;
import org.apache.commons.collections4.map.LinkedMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.osiris.jsqlgen.generator.GenReferences.getAllDirectRefs;
import static com.osiris.jsqlgen.generator.GenReferences.getRefTable;
import static com.osiris.jsqlgen.generator.TranslationsHelper.appendJavaTranslation;
import static com.osiris.jsqlgen.utils.UString.containsIgnoreCase;

public class GenDatabaseFile {
    public static void s(Database db, File databaseFile, String rawUrl, String url, String name, String username, String password) throws IOException {
        databaseFile.getParentFile().mkdirs();
        databaseFile.createNewFile();

        LinkedHashSet<String> importsList = new LinkedHashSet<>();
        importsList.add("import java.sql.*;");
        importsList.add("import java.util.*;");
        importsList.add("import java.io.File;");
        importsList.add("import java.util.concurrent.CopyOnWriteArrayList;");
        importsList.add("import java.util.function.Consumer;");

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
                "* If true, complete rows containing a reference/id of the deleted row, will be deleted too.\n" +
                "*/\n" +
                "public static boolean isRemoveRefs = false;\n" +
                "/** \n" +
                "* True by default to ensure no old references are kept in other rows after a removal.\n" +
                "* If true rows with fields containing a reference/id of the deleted row, will be set to -1.\n" +
                "* If you want almost no data loss and granular control set this to false too.\n" +
                "*/\n" +
                "public static boolean isUnsetRefs = true;\n" +
                "/** \n" +
                "* Use synchronized on this before doing changes to it. \n" +
                "*/\n" +
                "public static final List<Connection> availableConnections = new ArrayList<>();\n" +
                    "public static final List<Consumer<TableMetaData>> beforeTableChange = new CopyOnWriteArrayList<>();\n" +
                    "public static final List<Consumer<TableMetaData>> afterTableChange = new CopyOnWriteArrayList<>();\n" +
                    "public static final int defaultInMemoryOnlyObjId = -1;\n"
                );

        CopyOnWriteArrayList<Table> tables = db.tables;
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            int latestTableVersion = t.changes.size(); // TODO t.changes has not latest change included at this point yet
            // TODO since this gets generated before the tables get generated.
            // id, tableVersion, steps
            s.append("public static TableMetaData t"+t.name+" = new TableMetaData("+t.id+", "+latestTableVersion+", 0, \""+t.name+"\", ");
            s.append("new String[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                s.append("\""+t.columns.get(j).name+"\"");
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("}, new long[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                s.append(
                    t.columns.get(j).id
                    );
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("}, new String[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                s.append("\""+
                    t.columns.get(j).definition.replace("\"", "\\\"") // Escape quotes
                    +"\"");
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("}, new String[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                String comment = t.columns.get(j).comment;
                s.append("\""+
                    (comment == null ? "" : comment).replace("\"", "\\\"") // Escape quotes
                    +"\"");
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("})");

            // Create overriding methods
            var idCol = t.columns.get(0);
            s.append("{");
            s.append("public Class<?> getTableClass(){return "+t.name+".class;}");
            s.append("public List<Database.Row> get(){List<Database.Row> l = new ArrayList<>(); for("+t.name+" obj : "+t.name+".get()) l.add(obj); return l;}");
            s.append("public Database.Row get(Object id){return "+t.name+".get(("+idCol.type.inJava+") id);}");
            s.append("public List<Database.Row> get(String where, Object... values){List<Database.Row> l = new ArrayList<>(); for("+t.name+" obj : "+t.name+".get(where, values)) l.add(obj); return l;}");
            s.append("public void update(Database.Row obj){"+t.name+".update(("+ t.name +")obj);}");

            String initialValues = "";
            ArrayList<Column> columns = t.columns;
            for (int j = 1; j < columns.size(); j++) { // skip id column, since we are trying to create params for the minimal create method
                Column col = columns.get(j);
                if (containsIgnoreCase(col.definition, "NOT NULL")) {
                    if(col.type.isBigDecimal())
                        initialValues += "null, ";
                    else if(col.type.isBitOrBoolean()){
                        initialValues += "false, ";
                    }
                    else if (col.type.isNumber() || col.type.isDecimalNumber()) { // Potential id field with ref to another table
                        var refTable = getRefTable(db, col.name);
                        if (refTable != null) initialValues += "defaultInMemoryOnlyObjId, ";
                        else initialValues += "("+col.type.inJava+") 0, ";
                    } else {
                        initialValues += "null, ";
                    }
                }
            }
            initialValues = UString.replaceLast(initialValues, ", ", "");

            s.append("public Database.Row createWithNulls(){ return "+t.name+".create("+initialValues+");}");
            s.append("public void add(Database.Row obj){"+t.name+".add(("+ t.name +")obj);}");
            s.append("public void remove(Database.Row obj){"+t.name+".remove(("+ t.name +")obj);}");
            s.append("}");

            s.append("; ");
        }

        /* TODO delete dialog to allow unsetting/deletion of references/rows.

            public static class RowDeleteDialogVaadinComponent<ROW extends Row> extends Dialog {
        public ROW data;
        public Button btnDelete = new Button("Confirm Delete");
        public static class Ref{
            public TableMetaData table;
            public int columnIndex;

            public Ref(TableMetaData table, int columnIndex) {
                this.table = table;
                this.columnIndex = columnIndex;
            }
        }
        public RowDeleteDialogVaadinComponent(TableMetaData t){
            var idOfRowToDelete = data.getId();
            // TODO Check if this row/id is used/referenced in other tables rows
            for (TableMetaData t1 : Database.tables) {
                if(t1 == t) continue; // Skip self
                var potentialColumns = new ArrayList<Ref>();
                for (int i = 0; i < t1.columns.length; i++) {
                    TableMetaData colRefTable = t1.columnsRefs[i];
                    if(colRefTable == t){
                        // Found column containing a row with a potential reference of our data.id!
                        potentialColumns.add(new Ref(colRefTable, i));
                    }
                }
                // TODO fetch rows lazy and check potential columns
                for (Row row : t1.get()) {
                    // TODO display each row with all its data after 2 checkboxes where
                    // the first checkbox is named "Unset" and the other "Delete"
                    // "Unset" if checked will set the reference to -1
                    // "Delete" will delete the complete row, which must be used with caution
                }
            }
        }
    }

         */

        s.append("\n//Set table metadata references after all objects have been created\n" +
            "static {");
        for (Table t : tables) {
            s.append("t"+t.name+".columnsRefs = new TableMetaData[]{");
            for (int j = 0; j < t.columns.size(); j++) {
                var col = t.columns.get(j);
                if (col.type.isNumber() || col.type.isDecimalNumber()) { // Potential id field with ref to another table
                    var refTable = getRefTable(db, col.name);
                    if (refTable != null) s.append("t"+refTable.name);
                    else s.append("null");
                }
                else s.append("null");
                if(j != t.columns.size() - 1) s.append(", ");
            }
            s.append("};");
        }
        s.append("}\n");

        s.append("public static final TableMetaData[] tables = new TableMetaData[]{");
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            s.append("t"+t.name);
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
                "    public interface Row<T>{\n" +
                "        Object getId();\n" +
                "        T setId(Object id);\n" +
                "        T update();\n" +
                "        T add();\n" +
                "        T remove();\n" +
                "        String toPrintString();\n" +
                "        String toMinimalPrintString();\n" +
                "    }\n" +
                "\n" +
                "    public static class TableMetaData {\n" +
                "        public int id;\n" +
                "        public int version;\n" +
                "        public int steps;\n" +
                "        public String name;\n" +
                "/** The column names of this table. */\n" +
                "        public String[] columns;\n" +
                "/** The internal identifiers used for each column. For example relevant to find the correct translation. */\n" +
                "        public long[] columnsIds;\n" +
                "/** If a column is an id referencing another table its TableMetaData object is given, otherwise null is inserted. */\n" +
                "        public TableMetaData[] columnsRefs;\n" +
                "        public String[] definitions;\n" +
                "        public String[] comments;\n" +
                "\n" +
                "        public TableMetaData(int id, int version, int steps, String name, String[] columns, long[] columnsIds, String[] definitions, String[] comments) {\n" +
                "            this.id = id;\n" +
                "            this.version = version;\n" +
                "            this.steps = steps;\n" +
                "            this.name = name;\n" +
                "            this.columns = columns;\n" +
                "            this.columnsIds = columnsIds;\n" +
                "            this.definitions = definitions;\n" +
                "            this.comments = comments;\n" +
                "        }\n" +
                "\n" +
                "        // Implementations for the following methods are provided in the array initialisation of 'tables'\n" +
                "\n" +
                "        public Class<?> getTableClass(){throw new RuntimeException(\"Not implemented!\");}\n" + // Class is not provided as field to prevent static constructor execution
                "        public List<Database.Row> get(){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public Database.Row get(Object id){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public List<Database.Row> get(String where, Object... values){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public void update(Database.Row obj){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public void add(Database.Row obj){throw new RuntimeException(\"Not implemented!\");}\n" +
                "/** Creates a row object for this table by initialising default fields if any present. Note that NOT NULL fields without default values will be initialised as null or defaultInMemoryOnlyObjId if an id field or with 0 if simply a number/decimal. */\n"+
                "        public Database.Row createWithNulls(){throw new RuntimeException(\"Not implemented!\");}\n" +
                "        public void remove(Database.Row obj){throw new RuntimeException(\"Not implemented!\");}\n" +
                "    }\n");

        importsList.add("import com.vaadin.flow.component.UI;");
        importsList.add("import java.lang.reflect.Field;");
        importsList.add("import java.util.HashMap;");
        importsList.add("import java.util.Locale;");
        importsList.add("import java.util.Map;");
        importsList.add("import java.util.function.Function;");
        importsList.add("import java.util.concurrent.CopyOnWriteArrayList;");

        s.append("""

            """);

        StringBuilder s2 = new StringBuilder();
        s2.append((!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : ""));
        s2.append("""
                import java.lang.reflect.Field;
                import java.util.Locale;
                import java.util.function.Function;
                import java.util.concurrent.CopyOnWriteArrayList;
                import java.util.List;

                /**
                 * Translator.
                 * Example LLM prompt for your App: <br>
                 * <br>
                 * Extract all english/translatable strings from the provided MainView.java file below, and return 3 fully editted files:
                 * The first file (T.java, short for Translate.java) should contain all strings in english in public static fields
                 * like these public static TString EXAMPLE = new TString("EXAMPLE", "example-string");,
                 * do not implement TString just know that it expects the fieldName and stringValue in its constructor.
                 * The second file (TGerman.java) should contain all german translations in the same format as T.java,
                 * like public static TString EXAMPLE = new TString("EXAMPLE", "beispiel-text");.
                 * The third file (MainView.java) should be almost exactly the same as shown below,
                 * however replace all extracted strings like "example-string" with ""+T.EXAMPLE, for longer strings pick a shorter name.
                 */
                public class DatabaseTranslationBase {
                    public static final List<DatabaseTranslationBase> translations = new CopyOnWriteArrayList<>();
                    /**
                     * Logic to get the current locale object. String that will be translated is given for extra info.
                     * */
                    public static Function<DatabaseTranslationBase.TString, Locale> fnGetLocaleForTString = (s) -> {
                        // For example: return UI.getCurrent().getSession().getLocale();
                        return Locale.getDefault();
                    };

                    public static DatabaseTranslationBase defaultTranslation = new DatabaseTranslationBase("en");

                    static{
                        translations.add(defaultTranslation);
                        // Examples:
                        // z_internal_translations.add(new AdditionalEnglishTranslation("en"));
                        // z_internal_translations.add(new GermanTranslation("de"));
                    }

                    public String z_internal_localeString = "en";
                    public DatabaseTranslationBase(String localeString){
                      this.z_internal_localeString = localeString;
                    }

                    /**
                     * Short for translate.
                     */
                    public static String t(TString s){
                        Locale locale;
                        try{
                            locale = fnGetLocaleForTString.apply(s);
                        } catch (Exception ignored) {
                            locale = Locale.getDefault();
                        }
                        for(DatabaseTranslationBase translation : translations){
                            if(!translation.z_internal_localeString.equals(locale.getLanguage())) continue;
                            try {
                                Field tField = translation.getClass().getDeclaredField(s.fieldName);
                                tField.setAccessible(true); // Allow access to private fields
                                return ((TString) tField.get(translation)).value;
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                // try next translation object if exists, or default
                            }
                        }
                        return getFieldValueString(s.fieldName, defaultTranslation);
                    }

                    public static String getFieldValueString(String fieldName, Object translation){
                        try{
                            Field field = translation.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            return ((TString) field.get(translation)).value;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return fieldName;
                        }
                    }

                    /**
                     * Util function.
                     */
                    public static String tByFieldName(String fieldName, Object translation){
                        try{
                            Field field = translation.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            return t(((TString) field.get(translation)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return fieldName;
                        }
                    }

                    /**
                     * Util function.
                     */
                    public static String tByFieldName(String fieldName){
                        Locale locale;
                        try{
                            locale = fnGetLocaleForTString.apply(new TString(fieldName, ""));
                        } catch (Exception ignored) {
                            locale = Locale.getDefault();
                        }
                        for(DatabaseTranslationBase translation : translations){
                            if(!translation.z_internal_localeString.equals(locale.getLanguage())) continue;
                            try {
                                Field tField = translation.getClass().getDeclaredField(fieldName);
                                tField.setAccessible(true); // Allow access to private fields
                                return ((TString) tField.get(translation)).value;
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                // try next translation object if exists, or default
                            }
                        }
                        return getFieldValueString(fieldName, defaultTranslation);
                    }

                    public static class TString implements CharSequence{
                        public String fieldName;
                        public String value;

                        public TString(String fieldName, String value) {
                            this.fieldName = fieldName;
                            this.value = value;
                        }

                        // LLMs are a bit crazy sometimes
                        public TString(String fieldName, String value, String valueCopy) {
                            this.fieldName = fieldName;
                            this.value = value;
                        }

                        @Override
                        public int length() {
                            if(cachedValue == null) toString();
                            return cachedValue.length();
                        }

                        @Override
                        public char charAt(int index) {
                            if(cachedValue == null) toString();
                            return cachedValue.charAt(index);
                        }

                        @Override
                        public CharSequence subSequence(int start, int end) {
                            if(cachedValue == null) toString();
                            return cachedValue.subSequence(start, end);
                        }
                        public String cachedValue = null;

                        @Override
                        public String toString() {
                            var val = "";
                            try{
                                val = DatabaseTranslationBase.t(this);
                            } catch (Exception e) {
                                val = value;
                            }
                            this.cachedValue = val;
                            return val;
                        }
                    }

            """);

        for (Table t : tables) {
            s2.append("\n\n// "+t.name+"\n");
            for (Column col : t.columns) {
                appendJavaTranslation(t, col, s2);
            }
        }

        s2.append("\n}\n");

        var dbTranslationFile = new File(databaseFile.getParentFile(), "DatabaseTranslationBase.java");
        dbTranslationFile.getParentFile().mkdirs();
        dbTranslationFile.createNewFile();
        Files.writeString(dbTranslationFile.toPath(), s2.toString());

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

        var isOneTableVaadin = false;
        for (Table table : tables) {
            if(table.isVaadinFlowUI){
                isOneTableVaadin = true;
                break;
            }
        }
        if(isOneTableVaadin){
            importsList.add("import com.vaadin.flow.component.button.Button;");
            importsList.add("import com.vaadin.flow.component.formlayout.FormLayout;");
            importsList.add("import com.vaadin.flow.component.orderedlayout.HorizontalLayout;");
            importsList.add("import com.vaadin.flow.component.orderedlayout.VerticalLayout;");

            s.append("""
                public static abstract class RowCRUDVaadinComponent<ROW> extends VerticalLayout{
                  public ROW data;
                  public FormLayout form = new FormLayout();
                  public HorizontalLayout hlButtons = new HorizontalLayout();
                  public Button btnAdd = new Button("Add");
                  public Button btnSave = new Button("Save");
                  public Button btnDelete = new Button("Delete");

                  public abstract void updateFields();
                  public abstract void updateData();
                }
                """);
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
            configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
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

        importsList.add("import com.vaadin.flow.data.provider.CallbackDataProvider;");
        importsList.add("import com.vaadin.flow.data.provider.Query;");
        importsList.add("import java.util.stream.Stream;");
        importsList.add("import java.util.AbstractList;");
        importsList.add("import java.util.List;");
        importsList.add("import java.util.function.Supplier;");
        s.append("""
            // We use a custom LazyInitializingList to fetch the size and items exactly once upon first access,\s
            // avoiding Vaadin's default setItems(FetchCallback, CountCallback) or setItemsWithFilterConverter,
            // because Client & Server-Side filtering seems to be break when we use those
            public static class LazyInitializingList<T> extends AbstractList<T> {
                private List<T> internalList = null;
                private final Supplier<List<T>> fetcher;

                public LazyInitializingList(Supplier<List<T>> fetcher) {
                    this.fetcher = fetcher;
                }

                private void initialize() {
                    if (internalList == null) {
                        // This happens only once, the first time size() or get() is called
                        internalList = fetcher.get();
                    }
                }

                @Override
                public T get(int index) {
                    initialize();
                    return internalList.get(index);
                }

                @Override
                public int size() {
                    if(internalList == null) return 1;
                    return internalList.size();
                }
            }
            """);

        // Add other dependencies
        s.append(GenDefBlobClass.s(importsList));
        s.append(GenAsyncListClass.s(importsList));

        s.append("}\n");



        String sNoImports = s.toString();

        String finalS = (!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
            (db.isWithMariadb4j ? "" +
                "import ch.vorburger.exec.ManagedProcessException;\n" +
                "import ch.vorburger.mariadb4j.DB;\n" +
                "import ch.vorburger.mariadb4j.DBConfigurationBuilder;\n" : "")+
            "\n";
        for (String anImport : importsList) {
            finalS += anImport+"\n";
        }
        finalS += sNoImports;


        Files.writeString(databaseFile.toPath(), finalS.toString());
    }

}
