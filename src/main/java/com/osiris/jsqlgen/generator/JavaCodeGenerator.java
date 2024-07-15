package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.model.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static com.osiris.jsqlgen.utils.UString.*;

public class JavaCodeGenerator {

    public static void prepareTables(Database db) throws Exception {
        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // MAKE EVERYTHING THAT HAS NOT "DEFAULT" OR ONLY "NULL" IN THEIR DEFINITION NOT NULL
                if (containsIgnoreCase(col.definition, "DEFAULT")) continue;
                if (containsIgnoreCase(col.definition, "NOT NULL")) continue;
                if (containsIgnoreCase(col.definition, "NULL")) {
                    throw new Exception("Found suspicious definition using NULL! Please use the DEFAULT keyword instead!");
                } else {
                    System.out.println("Found suspicious definition without NOT NULL, appended it.");
                    col.definition = col.definition + " NOT NULL";
                }
            }
        }

        /*
        // TODO maybe another time we change int id to long, since this needs also further changes, since you cannot use
        // direct int values for example this: Folder.whereId().is(0).remove(); wouldnt work, instead you must write
        // 0L instead of 0, or provide another method that accepts integers too.
        // This directly could be expanded to all datatypes and generate methods of that accept smaller datatypes.
        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // MAKE SURE TYPE OF ID IS ALWAYS BIGINT
                if(!col.name.equals("id")) continue;
                if (!containsIgnoreCase(col.definition, "BIGINT")) {
                    System.out.println("Found suspicious id definition (at "+t.name+"."+col.name+") that is not BIGINT, fixed it.");
                    col.definition = col.definition.replace(col.definition.split(" ")[0], "BIGINT");
                }
            }
        }

         */

        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // GENERATE TYPES
                col.type = ColumnType.findBySQLDefinition(col.definition);
                if (col.type == null)
                    throw new Exception("Failed to generate code, because failed to find matching java type of definition '" + col.definition
                            + "'. Make sure that the data type is the first word in your definition and that its a supported type by jSQL-Gen.");
            }
        }

        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // CHECK SQL
                try{
                    Alter sql = (Alter) CCJSqlParserUtil.parse("ALTER TABLE `table` ADD COLUMN `column` " + col.definition);
                    for (AlterExpression alterExpression : sql.getAlterExpressions()) {
                        for (AlterExpression.ColumnDataType columnDataType : alterExpression.getColDataTypeList()) {
                            for (String columnSpec : columnDataType.getColumnSpecs()) {
                                // TODO compare each constraint with a list of all supported/valid constraints, since CCJSqlParserUtil does not do that
                                // or instead launch a MySQL server for example and run the SQL to see if it works
                                // to make this perfect, we would need to run other other servers like PostgreSQL too, if the user wants to use that instead
                            }
                        }
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Invalid SQL found in "+db.name+"."+t.name+"."+col.name+": "+e.getMessage(), e);
                }
            }
        }

    }

    /**
     * Contains a copy of the databases once jSQL-Gen was started and gets
     * updated at the end of {@link #generateTableFile(File, Table, Database)} if the generation of the table was a success. <br>
     * This is for versioning databases and keeping track of changes between the last and current database. <br>
     * This means that each time you press "Generate Files" the version is incremented by one (if there were changes). <br>
     */
    public static List<Database> oldDatabases = new ArrayList<>();

    /**
     * Generates Java source code, for the provided table.
     */
    public static String generateTableFile(File oldGeneratedClass, Table t, Database db) throws Exception {

        // GENERATE COLUMN TYPES
        LinkedHashSet<String> importsList = new LinkedHashSet<>();
        String tNameQuoted = getSQLTableNameQuoted(t.name);
        List<String> generatedEnumClasses = new ArrayList<>();
        for (Column col : t.columns) {
            if (col.type.isEnum()) {
                String enumName = firstToUpperCase(col.name);
                col.type = new ColumnType(ColumnType.ENUM.inSQL, enumName, ColumnType.ENUM.inJBDCSet, ColumnType.ENUM.inJBDCGet);
                generatedEnumClasses.add(genEnum(enumName, col.definition));
            }
            if (col.type.inJavaWithPackage != null)
                importsList.add("import " + col.type.inJavaWithPackage + ";");
        }

        Constructor constructor = genConstructor(t.name, t.columns);
        Constructor minimalConstructor = genMinimalConstructor(t.name, t.columns);
        boolean hasMoreFields = genFieldAssignments(t.columns).length() != genOnlyNotNullFieldAssignments(t.columns).length();

        importsList.add("import java.sql.Connection;");
        importsList.add("import java.sql.PreparedStatement;");
        importsList.add("import java.sql.ResultSet;");
        importsList.add("import java.sql.Statement;");
        importsList.add("import java.sql.Blob;");

        importsList.add("import java.util.List;");
        importsList.add("import java.util.ArrayList;");
        importsList.add("import java.util.concurrent.CopyOnWriteArrayList;");
        importsList.add("import java.util.function.Consumer;");
        if (t.isCache)
            importsList.add("import java.util.Arrays;");

        StringBuilder classContentBuilder = new StringBuilder();
        classContentBuilder.append("/**\n" +
                "Generated class by <a href=\"https://github.com/Osiris-Team/jSQL-Gen\">jSQL-Gen</a>\n" +
                "that contains static methods for fetching/updating data from the " + tNameQuoted + " table.\n" +
                "A single object/instance of this class represents a single row in the table\n" +
                "and data can be accessed via its public fields. <br>\n" +
                "<br>\n" +
                "You can add your own code to the bottom of this class. <br>\n" +
                "Do not modify the rest of this class since those changes will be removed at regeneration.\n" +
                "If modifications are really needed create a pull request directly to jSQL-Gen instead. <br>\n" +
                "<br>\n" +
                "Enabled modifiers: <br>\n" +
                (t.isDebug ? "- DEBUG is enabled, thus debug information will be printed out to System.err. <br>\n" : "") +
                (t.isNoExceptions ? "- NO EXCEPTIONS is enabled which makes it possible to use this methods outside of try/catch" +
                        " blocks because SQL errors will be caught and thrown as runtime exceptions instead. <br>\n" : "") +
                (t.isCache ? """
                        - CACHE is enabled, which means that results of get() are saved in memory <br>
                        and returned the next time the same request is made. <br>
                        The returned list is a deep copy, thus you can modify the list and its elements fields in your thread safely. <br>
                        The cache gets cleared/invalidated at any update/insert/delete. <br>
                        """ : "") +
                (t.isVaadinFlowUI ? """
                        - VAADIN FLOW is enabled, which means that an additional obj.toComp() method<br>
                        will be generated that returns a Vaadin Flow UI Form representation that allows creating/updating/deleting a row/object. <br>
                        """ : "") +
                "<br>\n" +
                "Structure (" + t.columns.size() + " fields/columns): <br>\n");
        for (Column col : t.columns) {
            classContentBuilder.append("- " + col.type.inJava + " " + col.name + " = " + col.definition + " <br>\n");
        }
        classContentBuilder.append(
                "*/\n" +
                        "public class " + t.name + " implements Database.Row{\n"); // Open class

        // Append public inner enum classes
        for (String generatedEnumClass : generatedEnumClasses) {
            classContentBuilder.append(generatedEnumClass);
        }

        // Add other dependencies
        classContentBuilder.append(GenDefBlobClass.s(importsList));

        // Add listeners
        classContentBuilder.append("/** Limitation: Not executed in constructor, but only the create methods. */\n" +
                "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onCreate = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
                "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onAdd = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
                "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onUpdate = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
                "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onRemove = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
                "\n" +
                "private static boolean isEqual("+t.name+" obj1, "+t.name+" obj2){ return obj1.equals(obj2) || obj1.id == obj2.id; }\n");;

        if (t.isDebug)
            classContentBuilder.append("    /**\n" +
                    "     * Only works correctly if the package name is com.osiris.jsqlgen.\n" +
                    "     */\n" +
                    "    private static String minimalStackString(){\n" +
                    "        StackTraceElement[] stack = new Exception().getStackTrace();\n" +
                    "        String s = \"\";\n" +
                    "        for (int i = stack.length - 1; i >= 1; i--) {\n" +
                    "            StackTraceElement el = stack[i];\n" +
                    "            if(el.getClassName().startsWith(\"java.\") || " +
                    "            el.getClassName().startsWith(\"com.osiris.jsqlgen\")) continue;\n" +
                    "            s = el.toString();\n" +
                    "            break;\n" +
                    "        }\n" +
                    "        return s +\"...\"+ stack[1].toString(); //stack[0] == current method, gets ignored\n" +
                    "    }\n");
        classContentBuilder.append("public static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);\n" +
                "public int getId(){return id;}\n" +
                "public void setId(int id){this.id = id;}\n");

        // STATIC TABLE INIT METHOD
        TableChange currentTableChange = GetTableChange.get(t, oldDatabases);
        if(t.changes.isEmpty() || currentTableChange.hasChanges()) t.changes.add(currentTableChange);
        classContentBuilder.append(GenStaticTableConstructor.s(t, tNameQuoted));

        if (t.isCache)
            classContentBuilder.append("    private static final List<CachedResult> cachedResults = new ArrayList<>();\n" +
                    "    private static class CachedResult {\n" +
                    "        public final String sql;\n" +
                    "        public final Object[] whereValues;\n" +
                    "        public final List<" + t.name + "> results;\n" +
                    "        public CachedResult(String sql, Object[] whereValues, List<" + t.name + "> results) {\n" +
                    "            this.sql = sql;\n" +
                    "            this.whereValues = whereValues;\n" +
                    "            this.results = results;\n" +
                    "        }\n" +
                    "        public List<" + t.name + "> getResultsCopy(){\n" +
                    "            synchronized (results){\n" +
                    "                List<" + t.name + "> list = new ArrayList<>(results.size());\n" +
                    "                for (" + t.name + " obj : results) {\n" +
                    "                    list.add(obj.clone());\n" +
                    "                }\n" +
                    "                return list;\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "    private static CachedResult cacheContains(String sql, Object[] whereValues){\n" +
                    "        synchronized (cachedResults){\n" +
                    "            for (CachedResult cr : cachedResults) {\n" +
                    "                if(cr.sql.equals(sql) && Arrays.equals(cr.whereValues, whereValues)){\n" +
                    "                    return cr;\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "        return null;\n" +
                    "    }\n" +
                    "    public static void clearCache(){\n" +
                    "        synchronized (cachedResults){ // Invalidate cache\n" +
                    "            cachedResults.clear();\n" +
                    "        }\n" +
                    "    }\n\n");

        // CONSTRUCTORS
        classContentBuilder.append(minimalConstructor.asString);
        if (hasMoreFields)
            classContentBuilder.append(constructor.asString);

        // CREATE FIELDS AKA COLUMNS:
        for (Column col : t.columns) {
            boolean notNull = containsIgnoreCase(col.definition, "NOT NULL");
            classContentBuilder.append("/**\n" +
                    "Database field/value: " + col.definition + ". <br>\n" +
                    (col.comment != null ? (col.comment + "\n") : "") +
                    "*/\n" +
                    "public " + col.type.inJava + " " + col.name + ";\n");
        }

        // CREATE INIT DEFAULTS METHOD:
        classContentBuilder.append("""
                /**
                Initialises the DEFAULT fields with the provided default values mentioned in the columns definition.
                */
                """);
        classContentBuilder.append(
                "protected " + t.name + " initDefaultFields() {\n" +
                        genOnlyDefaultFieldAssignments(t.columns) +
                        "return this;\n");
        classContentBuilder.append("}\n\n"); // Close create method

        // CREATE CREATE METHODS:
        classContentBuilder.append(GenCreateMethods.s(t, tNameQuoted, constructor, minimalConstructor, hasMoreFields));


        // CREATE COUNT METHOD:
        classContentBuilder.append("public static int count(){ return count(null, null); }\n" +
                "\n" +
                "public static int count(String where, Object... whereValues) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "String sql = \"SELECT COUNT(`id`) AS recordCount FROM " + tNameQuoted + "\" +\n" +
                "(where != null ? where : \"\"); \n" +
                (t.isDebug ? "long msGetCon = System.currentTimeMillis(); long msJDBC = 0;\n" : "") +
                "Connection con = Database.getCon();\n" +
                (t.isDebug ? "msGetCon = System.currentTimeMillis() - msGetCon;\n" : "") +
                (t.isDebug ? "msJDBC = System.currentTimeMillis();\n" : "") +
                "try (PreparedStatement ps = con.prepareStatement(sql)) {\n" + // Open try/catch
                "if(where!=null && whereValues!=null)\n" +
                "for (int i = 0; i < whereValues.length; i++) {\n" +
                "Object val = whereValues[i];\n" +
                "ps.setObject(i+1, val);\n" +
                "}\n" +
                "ResultSet rs = ps.executeQuery();\n" +
                "if (rs.next()) return rs.getInt(\"recordCount\");\n" +
                (t.isDebug ? "msJDBC = System.currentTimeMillis() - msJDBC;\n" : "") +
                (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                "finally {" +
                (t.isDebug ? "System.err.println(sql+\" /* //// msGetCon=\"+msGetCon+\" msJDBC=\"+msJDBC+\" con=\"+con+\" minimalStack=\"+minimalStackString()+\" */\");\n" : "") +
                "Database.freeCon(con);\n" +
                "}\n" +
                "return 0;\n");
        classContentBuilder.append("}\n\n"); // Close count method


        // CREATE UPDATE METHOD:
        classContentBuilder.append("/**\n" +
                "Searches the provided object in the database (by its id),\n" +
                "and updates all its fields.\n" +
                "@throws Exception when failed to find by id or other SQL issues.\n" +
                "*/\n" +
                "public static void update(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "String sql = \"UPDATE " + tNameQuoted + " SET ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).nameQuoted + "=?,");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).nameQuoted + "=?");
        classContentBuilder.append(" WHERE id=\"+obj.id;\n");
        classContentBuilder.append(
                (t.isDebug ? "long msGetCon = System.currentTimeMillis(); long msJDBC = 0;\n" : "") +
                        "Connection con = Database.getCon();\n" +
                        (t.isDebug ? "msGetCon = System.currentTimeMillis() - msGetCon;\n" : "") +
                        (t.isDebug ? "msJDBC = System.currentTimeMillis();\n" : "") +
                        "try (PreparedStatement ps = con.prepareStatement(sql)) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append(genJDBCSet(c, i));
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n" +
                        (t.isDebug ? "msJDBC = System.currentTimeMillis() - msJDBC;\n" : "") +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") +// Close try/catch
                        "finally{" +
                        (t.isDebug ? "System.err.println(sql+\" /* //// msGetCon=\"+msGetCon+\" msJDBC=\"+msJDBC+\" con=\"+con+\" minimalStack=\"+minimalStackString()+\" */\");\n" : "") +
                        "Database.freeCon(con);\n" +
                        (t.isCache ? "clearCache();\n" : "") +
                        "onUpdate.forEach(code -> code.accept(obj));\n" +
                        "}\n"
        );
        classContentBuilder.append("}\n\n"); // Close update method


        // CREATE ADD METHOD:
        classContentBuilder.append("/**\n" +
                "Adds the provided object to the database (note that the id is not checked for duplicates).\n" +
                "*/\n" +
                "public static void add(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "String sql = \"INSERT INTO " + tNameQuoted + " (");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).nameQuoted + ",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).nameQuoted);
        classContentBuilder.append(") VALUES (");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append("?,");
        }
        classContentBuilder.append("?)\";\n");
        classContentBuilder.append(
                (t.isDebug ? "long msGetCon = System.currentTimeMillis(); long msJDBC = 0;\n" : "") +
                        "Connection con = Database.getCon();\n" +
                        (t.isDebug ? "msGetCon = System.currentTimeMillis() - msGetCon;\n" : "") +
                        (t.isDebug ? "msJDBC = System.currentTimeMillis();\n" : "") +
                        "try (PreparedStatement ps = con.prepareStatement(sql)) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append(genJDBCSet(c, i));
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n" +
                        (t.isDebug ? "msJDBC = System.currentTimeMillis() - msJDBC;\n" : "") +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") +// Close try/catch
                        "finally{" +
                        (t.isDebug ? "System.err.println(sql+\" /* //// msGetCon=\"+msGetCon+\" msJDBC=\"+msJDBC+\" con=\"+con+\" minimalStack=\"+minimalStackString()+\" */\");\n" : "") +
                        "Database.freeCon(con);\n" +
                        (t.isCache ? "clearCache();\n" : "") +
                        "onAdd.forEach(code -> code.accept(obj));\n" +
                        "}\n"
        );
        classContentBuilder.append("}\n\n"); // Close add method


        // CREATE DELETE/REMOVE METHOD:
        classContentBuilder.append(GenRemoveMethods.s(db, t, tNameQuoted));

        // CREATE OBJ CLONE METHOD
        classContentBuilder.append("public " + t.name + " clone(){\n" +
                "return new " + t.name + "(");
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            classContentBuilder.append("this." + col.name);
            if (i != t.columns.size() - 1)
                classContentBuilder.append(",");
        }
        classContentBuilder.substring(0, classContentBuilder.length() - 1);
        classContentBuilder.append(");\n}\n");

        // CREATE OBJ ADD METHOD
        classContentBuilder.append("public void add(){\n" +
                t.name + ".add(this);\n" +
                "}\n");

        // CREATE OBJ UPDATE METHOD
        classContentBuilder.append("public void update(){\n" +
                t.name + ".update(this);\n" +
                "}\n");

        // CREATE OBJ REMOVE METHOD
        classContentBuilder.append("public void remove(){\n" +
                t.name + ".remove(this);\n" +
                "}\n");
        classContentBuilder.append("public void remove(boolean unsetRefs, boolean removeRefs){\n" +
                t.name + ".remove(this, unsetRefs, removeRefs);\n" +
                "}\n");

        // CREATE OBJ TOPRINTSTRING METHOD
        classContentBuilder.append("public String toPrintString(){\n" +
                "return  \"\"");
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            classContentBuilder.append("+\"" + col.name + "=\"+this." + col.name + "+\" \"");
        }
        classContentBuilder.substring(0, classContentBuilder.length() - 1);
        classContentBuilder.append(";\n}\n");

        classContentBuilder.append("public String toMinimalPrintString(){\n");
        String valAsString = "\"\"+";
        for (Column refCol : t.columns) {
            if (refCol.type.isBlob() || refCol.type.isDateOrTime()) continue;
            valAsString += "this." + refCol.name;
            valAsString += "+\"; \"+";
        }
        valAsString += "\"\"";
        classContentBuilder.append("return "+valAsString+";\n}\n");

        // CREATE OBJ TOVAADINCOMPONENT METHOD
        if (t.isVaadinFlowUI)
            classContentBuilder.append(GenVaadinFlow.s(db, t, importsList));

        // CREATE OBJ ISONLYINMEMORY METHOD
        classContentBuilder.append("public boolean isOnlyInMemory(){\n" +
                "return id < 0;\n}\n");

        // SHORTCUT FOR WHERE METHODS
        for (Column col : t.columns) {
            String colType = col.type.inJava.equals("int") ? "Integer" : col.type.inJava; // We need the
            if (col.type.isEnum()) colType = "String"; // Workaround to support enums right now
            // TODO find a better solution for the above, since now typesafety for enums is gone
            classContentBuilder.append(
                    "public static WHERE<" + firstToUpperCase(colType) + "> where" + firstToUpperCase(col.name) + "() {\n" +
                            "return new WHERE<" + firstToUpperCase(colType) + ">(\"" + col.nameQuoted + "\");\n" +
                            "}\n");
        }
        classContentBuilder.append(GenWhereClass.s(t));

        // Handle additional code from existing/old generated class
        classContentBuilder.append("// The code below will not be removed when re-generating this class.\n");
        if (oldGeneratedClass.exists()) {
            List<String> additionalLines = new ArrayList<>();
            List<String> lines = Files.readAllLines(oldGeneratedClass.toPath());
            LinkedHashSet<String> oldImportsList = new LinkedHashSet<>();
            boolean isAdditionalLine = false;
            for (String line : lines) {
                if (line.startsWith("import"))
                    oldImportsList.add(line);
                else if (line.contains("// Additional code start ->")) {
                    isAdditionalLine = true;
                    continue;
                } else if (line.contains("// Additional code end <-"))
                    isAdditionalLine = false;

                if (isAdditionalLine)
                    additionalLines.add(line);
            }
            importsList = mergeListContents(importsList, oldImportsList);
            if (!additionalLines.isEmpty()) {
                classContentBuilder.append("// Additional code start -> \n");
                for (String additionalLine : additionalLines) {
                    classContentBuilder.append(additionalLine).append("\n");
                }
                classContentBuilder.append("// Additional code end <- \n");
            } else {
                classContentBuilder.append("// Additional code start -> \n");
                classContentBuilder.append("    private " + t.name + "(){}\n");
                classContentBuilder.append("// Additional code end <- \n");
            }
        } else {
            classContentBuilder.append("// Additional code start -> \n");
            classContentBuilder.append("    private " + t.name + "(){}\n");
            classContentBuilder.append("// Additional code end <- \n");
        }


        classContentBuilder.append("}\n"); // Close class

        StringBuilder imports = new StringBuilder();
        for (String s : importsList) {
            imports.append(s).append("\n");
        }
        imports.append("\n");

        // SUCCESS, thus update this table in oldDatabases (not original, but duplicate is created)
        for (Database oldDB : oldDatabases) {
            Table oldT = null;
            for (Table oldT_ : oldDB.tables) {
                if(oldT_.id == t.id) {
                    oldT = oldT_;
                    break;
                }
            }
            if(oldT == null){
                // New table
                oldDB.tables.add(t.duplicate());
            } else{
                oldDB.tables.replaceAll(oldT_ -> {
                    if(oldT_.id == t.id) return t.duplicate();
                    else return oldT_;
                });
            }
        }
        // SUCCESS, thus save the data
        Data.save();

        return imports.toString() + classContentBuilder;
    }

    public static String getSQLTableNameQuoted(String s) {
        return "`" + s.toLowerCase() + "`";
    }

    private static String genJDBCSet(Column c, int i) {
        if (c.type.isEnum())
            return "ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ".name());\n";
        else
            return "ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ");\n";
    }

    private static String genEnum(String enumName, String definition) {
        definition = definition.substring(definition.indexOf("(") + 1, definition.lastIndexOf(")"));
        String[] enumTypeRawNames = definition.split(",");
        String s = "public enum " + enumName + " {";
        for (String enumTypeRawName : enumTypeRawNames) {
            s += enumTypeRawName.replace("\"", "")
                    .replace("'", "")
                    .replace("`", "")
                    + ",";
        }
        s += "}\n";
        return s;
    }

    private static List<String> mergeListContents(List<String>... lists) {
        List<String> unified = new ArrayList<>();
        for (List<String> list : lists) {
            for (String s : list) {
                if (!unified.contains(s))
                    unified.add(s);
            }
        }
        return unified;
    }

    private static LinkedHashSet<String> mergeListContents(LinkedHashSet<String>... lists) {
        LinkedHashSet<String> unified = new LinkedHashSet<>();
        for (LinkedHashSet<String> list : lists) {
            for (String s : list) {
                unified.add(s);
            }
        }
        return unified;
    }

    public static Constructor genConstructor(String objName, List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        StringBuilder paramsWithoutTypesBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();
        Column idCol = columns.get(0);
        for (Column col : columns) {
            paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
            paramsWithoutTypesBuilder.append(col.name + ", ");
            fieldsBuilder.append("this." + col.name + " = " + col.name + ";");
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        constructor.paramsWithoutId = constructor.params.replace((idCol.type.inJava + " " + idCol.name + ", "), "");
        if (constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length() - 2);
        if (constructor.paramsWithoutId.endsWith(", "))
            constructor.paramsWithoutId = constructor.paramsWithoutId.substring(0, constructor.paramsWithoutId.length() - 2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #id}.\n" +
                "*/\n" +
                "public " + objName + " (" + constructor.params + "){\ninitDefaultFields();\n" + constructor.fieldAssignments + "\n}\n";

        constructor.paramsWithoutTypes = paramsWithoutTypesBuilder.toString();
        if (constructor.paramsWithoutTypes.endsWith(", "))
            constructor.paramsWithoutTypes = constructor.paramsWithoutTypes.substring(0, constructor.paramsWithoutTypes.length() - 2);

        return constructor;
    }

    /**
     * Generates a constructor with only the not-null fields as parameters.
     */
    public static Constructor genMinimalConstructor(String objName, List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        StringBuilder paramsWithoutTypesBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();
        Column idCol = columns.get(0);
        for (Column col : columns) {
            if (containsIgnoreCase(col.definition, "NOT NULL")) {
                paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
                paramsWithoutTypesBuilder.append(col.name + ", ");
                fieldsBuilder.append("this." + col.name + " = " + col.name + ";");
            }
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        constructor.paramsWithoutId = constructor.params.replace((idCol.type.inJava + " " + idCol.name + ", "), "");
        if (constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length() - 2);
        if (constructor.paramsWithoutId.endsWith(", "))
            constructor.paramsWithoutId = constructor.paramsWithoutId.substring(0, constructor.paramsWithoutId.length() - 2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #id}.\n" +
                "*/\n" +
                "public " + objName + " (" + constructor.params + "){\ninitDefaultFields();\n" + constructor.fieldAssignments + "\n}\n";

        constructor.paramsWithoutTypes = paramsWithoutTypesBuilder.toString();
        if (constructor.paramsWithoutTypes.endsWith(", "))
            constructor.paramsWithoutTypes = constructor.paramsWithoutTypes.substring(0, constructor.paramsWithoutTypes.length() - 2);

        return constructor;
    }

    public static String genParams(List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        for (Column col : columns) {
            paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
        }
        String params = paramsBuilder.toString();
        if (params.endsWith(", "))
            params = params.substring(0, params.length() - 2);
        return params;
    }

    public static String genFieldAssignments(List<Column> columns) {
        return genFieldAssignments("this", columns);
    }

    public static String genFieldAssignments(String objName, List<Column> columns) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            fieldsBuilder.append(objName + "." + col.name + "=" + col.name + "; ");
        }
        return fieldsBuilder.toString();
    }

    public static String genOnlyNotNullFieldAssignments(List<Column> columns) {
        return genOnlyNotNullFieldAssignments("this", columns);
    }

    public static String genOnlyNotNullFieldAssignments(String objName, List<Column> columns) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            if (containsIgnoreCase(col.definition, "NOT NULL")) {
                fieldsBuilder.append(objName + "." + col.name + "=" + col.name + "; ");
            }
        }
        return fieldsBuilder.toString();
    }

    public static String genOnlyDefaultFieldAssignments(List<Column> columns) {
        return genOnlyDefaultFieldAssignments("this", columns);
    }

    public static String genOnlyDefaultFieldAssignments(String objName, List<Column> columns) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            if (containsIgnoreCase(col.definition, "DEFAULT")) {
                String val = col.getDefaultValue();
                if (col.type.isEnum())
                    fieldsBuilder.append(objName + "." + col.name + "=" + col.type.inJava + "." + val + "; ");
                else if (col.type.isText()) {
                    fieldsBuilder.append(objName + "." + col.name + "=\"" + val + "\"; ");
                } else if (col.type.isDateOrTime()) {
                    if(containsIgnoreCase(val, "NOW")
                            || containsIgnoreCase(val, "CURDATE")
                            || containsIgnoreCase(val, "CURTIME")) val = "System.currentTimeMillis()";
                    if (col.type == ColumnType.YEAR) fieldsBuilder.append(objName + "." + col.name + "=" + val + "; ");
                    else fieldsBuilder.append(objName + "." + col.name + "=new " + col.type.inJava + "(" + val + "); ");
                } else if (col.type.isBlob()) {
                    fieldsBuilder.append(objName + "." + col.name + "=new DefaultBlob(new byte[0]); ");
                    // This is not directly supported by SQL
                } else if (col.type.isNumber() || col.type.isDecimalNumber()) {
                    fieldsBuilder.append(objName + "." + col.name + "=" + val + "; ");
                } else {
                    fieldsBuilder.append(objName + "." + col.name + "=new " + col.type.inJava + "(" + val + "); ");
                }
            }
        }
        return fieldsBuilder.toString();
    }

    public static class Constructor {
        public String asString;
        public String params;
        public String paramsWithoutId;
        public String paramsWithoutTypes;
        public String fieldAssignments;
    }

}
