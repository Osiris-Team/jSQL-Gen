package com.osiris.jsqlgen.generator;

import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.model.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.osiris.jsqlgen.generator.JavaCodeGenerator.oldDatabases;
import static com.osiris.jsqlgen.utils.UString.containsIgnoreCase;
import static com.osiris.jsqlgen.utils.UString.firstToUpperCase;

public class GenTableFile {
    /**
     * Generates Java source code, for the provided table. <br>
     * Modifies the provided db object, for example to reset the {@link Table#currentChange}.
     */
    public static String s(File oldGeneratedClass, Table t, Database db) throws Exception {

        // GENERATE COLUMN TYPES
        LinkedHashSet<String> importsList = new LinkedHashSet<>();
        String tNameQuoted = JavaCodeGenerator.getSQLTableNameQuoted(t.name);
        List<String> generatedEnumClasses = new ArrayList<>();
        for (Column col : t.columns) {
            if (col.type.isEnum()) {
                String enumName = firstToUpperCase(col.name);
                col.type = new ColumnType(ColumnType.ENUM.inSQL, enumName, ColumnType.ENUM.inJBDCSet, ColumnType.ENUM.inJBDCGet);
                generatedEnumClasses.add(JavaCodeGenerator.genEnum(enumName, col.definition));
            }
            if (col.type.inJavaWithPackage != null)
                importsList.add("import " + col.type.inJavaWithPackage + ";");
        }

        JavaCodeGenerator.Constructor constructor = JavaCodeGenerator.genConstructor(t.name, t.columns);
        JavaCodeGenerator.Constructor minimalConstructor = JavaCodeGenerator.genMinimalConstructor(t.name, t.columns);
        boolean hasMoreFields = JavaCodeGenerator.genFieldAssignments(t.columns).length() != JavaCodeGenerator.genOnlyNotNullFieldAssignments(t.columns).length();

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
        classContentBuilder.append("/**\n");
        classContentBuilder.append("Table "+t.name+" with id "+t.id+" and "+t.changes.size()+" changes/version. <br>\n" +
            "Structure (" + t.columns.size() + " fields/columns): <br>\n");
        for (Column col : t.columns) {
            classContentBuilder.append("- " + col.type.inJava + " " + col.name + " = " + col.definition + " <br>\n");
        }
        classContentBuilder.append("\n");
        classContentBuilder.append(
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
                "<br>\n");
        classContentBuilder.append(
            "*/\n" +
                "public class " + t.name + " implements Database.Row{\n"); // Open class

        // Append public inner enum classes
        for (String generatedEnumClass : generatedEnumClasses) {
            classContentBuilder.append(generatedEnumClass);
        }

        var idCol = t.columns.get(0);

        // Add listeners
        classContentBuilder.append("/** Limitation: Not executed in constructor, but only the create methods. */\n" +
            "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onCreate = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
            "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onAdd = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
            "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onUpdate = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
            "public static CopyOnWriteArrayList<Consumer<" + t.name + ">> onRemove = new CopyOnWriteArrayList<Consumer<" + t.name + ">>();\n" +
            "\n" +
            "private static boolean isEqual("+t.name+" obj1, "+t.name+" obj2){ return obj1.equals(obj2) || obj1.getId() == obj2.getId(); }\n");

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
            "public Object getId(){return "+idCol.name+";}\n" +
            "public void setId(Object id){this."+idCol.name+" = ("+idCol.type.inJava+") id;}\n");

        // STATIC TABLE INIT METHOD
        // TODO also check if a type conversion was made and check if that conversion is valid.
        if(t.changes.isEmpty() || t.currentChange.hasChanges()) {
            if(t.currentChange.oldTableName.isEmpty()) t.currentChange.oldTableName = t.name;
            if(t.currentChange.newTableName.isEmpty()) t.currentChange.newTableName = t.name;
            t.changes.add(t.currentChange);
            t.currentChange = new TableChange();
            AL.info("Detected change in table '"+t.name+"' and added it.");
        }
        classContentBuilder.append(GenStaticTableConstructor.s(db, t, tNameQuoted));

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
            classContentBuilder.append("" +
                "/**\n" +
                "Database field/value: " + col.definition + ". <br>\n" +
                (col.comment != null ? (col.comment + "\n") : "") + "\n"+
                "Convenience builder-like setter with method-chaining.\n"+
                "*/\n" +
                "public "+t.name+" "+col.name+"("+col.type.inJava+" "+col.name+"){ this."+ col.name+" = "+col.name+"; return this;}\n");
        }

        // CREATE INIT DEFAULTS METHOD:
        classContentBuilder.append("""
                /**
                Initialises the DEFAULT fields with the provided default values mentioned in the columns definition.
                */
                """);
        classContentBuilder.append(
            "protected " + t.name + " initDefaultFields() {\n" +
                JavaCodeGenerator.genOnlyDefaultFieldAssignments(t.columns) +
                "return this;\n");
        classContentBuilder.append("}\n\n"); // Close create method

        // CREATE CREATE METHODS:
        classContentBuilder.append(GenCreateMethods.s(t, tNameQuoted, constructor, minimalConstructor, hasMoreFields));


        // CREATE COUNT METHOD:
        classContentBuilder.append("public static int count(){ return count(null, (Object[]) null); }\n" +
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
        classContentBuilder.append(" WHERE "+idCol.name+"=\"+obj.getId();\n");
        classContentBuilder.append(
            (t.isDebug ? "long msGetCon = System.currentTimeMillis(); long msJDBC = 0;\n" : "") +
                "Connection con = Database.getCon();\n" +
                (t.isDebug ? "msGetCon = System.currentTimeMillis() - msGetCon;\n" : "") +
                (t.isDebug ? "msJDBC = System.currentTimeMillis();\n" : "") +
                "try (PreparedStatement ps = con.prepareStatement(sql)) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append(JavaCodeGenerator.genJDBCSet(c, i));
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
            classContentBuilder.append(JavaCodeGenerator.genJDBCSet(c, i));
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
            "return "+idCol.name+" < 0;\n}\n");

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
            importsList = Utils.mergeListContents(importsList, oldImportsList);
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
}
