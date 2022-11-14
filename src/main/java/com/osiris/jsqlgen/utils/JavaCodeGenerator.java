package com.osiris.jsqlgen.utils;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Table;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JavaCodeGenerator {

    /**
     * Generates Java source code, for the provided table.
     */
    public static String generate(File oldGeneratedClass, Table t) throws Exception {
        String tNameQuoted = "`" + t.name.toLowerCase() + "`";
        for (Column col : t.columns) {
            col.type = ColumnType.findBySQLDefinition(col.definition);
            if (col.type == null)
                throw new Exception("Failed to generate code, because failed to find matching java type of definition '" + col.definition
                        + "'. Make sure that the data type is the first word in your definition and that its a supported type by jSQL-Gen.");
        }
        Constructor constructor = genConstructor(t.name, t.columns);
        Constructor minimalConstructor = genMinimalConstructor(t.name, t.columns);
        boolean hasMoreFields = genFieldAssignments(t.columns).length() != genOnlyNotNullFieldAssignments(t.columns).length();

        List<String> importsList = new ArrayList<>();
        importsList.add("import java.util.List;");
        importsList.add("import java.util.ArrayList;");
        importsList.add("import java.sql.*;");
        importsList.add("import java.util.Arrays;");

        StringBuilder classContentBuilder = new StringBuilder();
        classContentBuilder.append("/**\n" +
                "Generated class by <a href=\"https://github.com/Osiris-Team/jSQL-Gen\">jSQL-Gen</a>\n" +
                "that contains static methods for fetching/updating data from the \"" + t.name + "\" table.\n" +
                "A single object/instance of this class represents a single row in the table\n" +
                "and data can be accessed via its public fields. <p>\n" +
                "Its not recommended to modify this class but it should be OK to add new methods to it.\n" +
                "If modifications are really needed create a pull request directly to jSQL-Gen instead. <br>\n" +
                (t.isDebug ? "DEBUG is enabled, thus debug information will be printed out to System.err. <br>\n": "") +
                (t.isNoExceptions ? "NO EXCEPTIONS is enabled which makes it possible to use this methods outside of try/catch" +
                        " blocks because SQL errors will be caught and thrown as runtime exceptions instead. <br>\n": "") +
                (t.isCache ? "CACHE is enabled, note that now the results of get() are not thread safe anymore, because it could be the same list from the" +
                        " request before. <br>\n": "") +
                "*/\n" +
                "public class " + t.name + "{\n"); // Open class
        classContentBuilder.append("private static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);\n");
        classContentBuilder.append("static {\n" +
                "try{\n" + // Without this additional try/catch that encapsulates the complete code inside static constructor
                // we somehow get problems like class not found exception
                "Connection con = Database.getCon();\n" +
                "try{\n" +
                "try (Statement s = con.createStatement()) {\n" +
                "s.executeUpdate(\"CREATE TABLE IF NOT EXISTS " + tNameQuoted + " (" + t.columns.get(0).nameQuoted // EXPECTS ID
                + " " + t.columns.get(0).definition + ")\");\n");
        for (int i = 1; i < t.columns.size(); i++) { // Skip first column (id) to avoid "SQLSyntaxErrorException: Multiple primary key defined"
            Column col = t.columns.get(i);
            classContentBuilder.append("try{s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " ADD COLUMN " + col.nameQuoted + " " + col.definition + "\");}catch(Exception ignored){}\n");
            classContentBuilder.append("s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " MODIFY COLUMN " + col.nameQuoted + " " + col.definition + "\");\n");
        }
        classContentBuilder.append(
                        "}\n" +
                        "" +
                        "try (PreparedStatement ps = con.prepareStatement(\"SELECT id FROM " + tNameQuoted + " ORDER BY id DESC LIMIT 1\")) {\n" +
                        "ResultSet rs = ps.executeQuery();\n" +
                        "if (rs.next()) idCounter.set(rs.getInt(1) + 1);\n" +
                        "}\n" +
                        "}\n" +
                        "catch(Exception e){ throw new RuntimeException(e); }\n" +
                        "finally {Database.freeCon(con);}\n" +
                                "}catch(Exception e){\n" +
                                "e.printStackTrace();\n" +
                                "System.err.println(\"Something went really wrong during table ("+t.name+") initialisation, thus the program will exit!\");" +
                                "System.exit(1);}\n" +
                        "}\n\n");

        if(t.isCache)
            classContentBuilder.append("    private static final List<CachedResult> cachedResults = new ArrayList<>();\n" +
                    "    private static class CachedResult {\n" +
                    "        public final String sql;\n" +
                    "        public final Object[] whereValues;\n" +
                    "        public final List<"+t.name+"> results;\n" +
                    "        public CachedResult(String sql, Object[] whereValues, List<"+t.name+"> results) {\n" +
                    "            this.sql = sql;\n" +
                    "            this.whereValues = whereValues;\n" +
                    "            this.results = results;\n" +
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
            boolean notNull = UString.containsIgnoreCase(col.definition, "NOT NULL");
            classContentBuilder.append("" +
                    "/**\n" +
                    "Database field/value. " + (notNull ? "Not null. " : "") + "<br>\n" +
                    (col.comment != null ? (col.comment + "\n") : "") +
                    "*/\n" +
                    "public " + col.type.inJava + " " + col.name + ";\n");
        }

        // CREATE CREATE METHODS:
        classContentBuilder.append("""
                /**
                Creates and returns an object that can be added to this table.
                Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
                Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
                Also note that this method will NOT add the object to the table.
                */
                """);
        Column firstCol = t.columns.get(0);
        String idParam = firstCol.type.inJava + " " + firstCol.name + ",";
        classContentBuilder.append(
                "public static " + t.name + " create(" + minimalConstructor.params.replace(idParam, "")
                        + ") {\n" +
                        firstCol.type.inJava + " " + firstCol.name + " = idCounter.getAndIncrement();\n" +
                        "" + t.name + " obj = new " + t.name + "(" + minimalConstructor.paramsWithoutTypes + ");\n" +
                        "return obj;\n");
        classContentBuilder.append("}\n\n"); // Close create method


        if (hasMoreFields) {
            classContentBuilder.append("" +
                    "/**\n" +
                    "Creates and returns an object that can be added to this table.\n" +
                    "Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).\n" +
                    "Note that this method will NOT add the object to the table.\n" +
                    "*/\n");
            classContentBuilder.append(
                    "public static " + t.name + " create(" + genParams(t.columns).replace(idParam, "")
                            + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                            firstCol.type.inJava + " " + firstCol.name + " = idCounter.getAndIncrement();\n" +
                            "" + t.name + " obj = new " + t.name + "();\n" +
                            "" + genFieldAssignments("obj", t.columns) + "\n" +
                            "return obj;\n");
            classContentBuilder.append("}\n\n"); // Close create method
        }

        classContentBuilder.append("" +
                "/**\n" +
                "Convenience method for creating and directly adding a new object to the table.\n" +
                "Note that the parameters of this method represent \"NOT NULL\" fields in the table and thus should not be null.\n" +
                "*/\n");
        classContentBuilder.append(
                "public static " + t.name + " createAndAdd(" + minimalConstructor.params.replace(idParam, "")
                        + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                        firstCol.type.inJava + " " + firstCol.name + " = idCounter.getAndIncrement();\n" +
                        "" + t.name + " obj = new " + t.name + "(" + minimalConstructor.paramsWithoutTypes + ");\n" +
                        "add(obj);\n" +
                        "return obj;\n");
        classContentBuilder.append("}\n\n"); // Close method


        if (hasMoreFields) {
            classContentBuilder.append("" +
                    "/**\n" +
                    "Convenience method for creating and directly adding a new object to the table.\n" +
                    "*/\n");
            classContentBuilder.append(
                    "public static " + t.name + " createAndAdd(" + genParams(t.columns).replace(idParam, "")
                            + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                            firstCol.type.inJava + " " + firstCol.name + " = idCounter.getAndIncrement();\n" +
                            "" + t.name + " obj = new " + t.name + "();\n" +
                            "" + genFieldAssignments("obj", t.columns) + "\n" +
                            "add(obj);\n" +
                            "return obj;\n");
            classContentBuilder.append("}\n\n"); // Close method
        }


        // CREATE GET METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "@return a list containing all objects in this table.\n" +
                "*/\n" +
                "public static List<" + t.name + "> get() " + (t.isNoExceptions ? "" : "throws Exception") + " {return get(null);}\n" +
                "/**\n" +
                "@return object with the provided id or null if there is no object with the provided id in this table.\n" +
                "@throws Exception on SQL issues.\n" +
                "*/\n" +
                "public static " + t.name + " get(int id) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "try{\n" +
                "return get(\"id = \"+id).get(0);\n" +
                "}catch(IndexOutOfBoundsException ignored){}\n" +
                (t.isNoExceptions ? "catch(Exception e){throw new RuntimeException(e);}\n" : "") + // Close try/catch
                "return null;\n" + // Close tr
                "}\n" +
                "/**\n" +
                "Example: <br>\n" +
                "get(\"username=? AND age=?\", \"Peter\", 33);  <br>\n" +
                "@param where can be null. Your SQL WHERE statement (without the leading WHERE).\n" +
                "@param whereValues can be null. Your SQL WHERE statement values to set for '?'.\n" +
                "@return a list containing only objects that match the provided SQL WHERE statement (no matches = empty list).\n" +
                "if that statement is null, returns all the contents of this table.\n" +
                "*/\n" +
                "public static List<" + t.name + "> get(String where, Object... whereValues) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "Connection con = Database.getCon();\n" +
                "String sql = \"SELECT ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).nameQuoted + ",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).nameQuoted);
        classContentBuilder.append("\" +\n" +
                        "\" FROM " + tNameQuoted + "\" +\n" +
                        "(where != null ? (\"WHERE \"+where) : \"\");\n"+
                (t.isDebug ? "System.err.println(\"get: \"+where);\n" : "") +
                (t.isCache ? "synchronized(cachedResults){ CachedResult cachedResult = cacheContains(sql, whereValues);\n" +
                        "if(cachedResult != null) return cachedResult.results;\n" : "") +
                "List<" + t.name + "> list = new ArrayList<>();\n" +
                "try (PreparedStatement ps = con.prepareStatement(sql)) {\n" + // Open try/catch
                        "if(where!=null && whereValues!=null)\n" +
                        "for (int i = 0; i < whereValues.length; i++) {\n" +
                        "Object val = whereValues[i];\n" +
                        "ps.setObject(i+1, val);\n" +
                        "}\n" +
                        "ResultSet rs = ps.executeQuery();\n" +
                        "while (rs.next()) {\n" + // Open while
                        "" + t.name + " obj = new " + t.name + "();\n" +
                        "list.add(obj);\n");
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append("obj." + c.name + " = rs." + c.type.inJBDCGet + "(" + (i + 1) + ");\n");
        }
        classContentBuilder.append(
                "}\n" + // Close while
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                        "finally{Database.freeCon(con);}\n"+
                        (t.isCache ? """
                                cachedResults.add(new CachedResult(sql, whereValues, list));
                                return list;}
                                """ : "return list;\n")); // Close synchronized block if isCache
        classContentBuilder.append("}\n\n"); // Close get method

        // CREATE GETLAZY METHODS:


        // CREATE COUNT METHOD:
        classContentBuilder.append("" +
                "public static int count(String where, Object... whereValues) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                (t.isDebug ? "System.err.println(\"count: \"+where);\n" : "") +
                "Connection con = Database.getCon();\n"+
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"SELECT COUNT(`id`) AS recordCount FROM " + tNameQuoted + "\" +\n" +
                "(where != null ? (\"WHERE \"+where) : \"\"))) {\n" + // Open try/catch
                "if(where!=null && whereValues!=null)\n" +
                "for (int i = 0; i < whereValues.length; i++) {\n" +
                "Object val = whereValues[i];\n" +
                "ps.setObject(i+1, val);\n" +
                "}\n" +
                "ResultSet rs = ps.executeQuery();\n" +
                "if (rs.next()) return rs.getInt(\"recordCount\");\n" +
                (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                "finally {Database.freeCon(con);}\n" +
                        "return 0;\n");
        classContentBuilder.append("}\n\n"); // Close count method


        // CREATE UPDATE METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Searches the provided object in the database (by its id),\n" +
                "and updates all its fields.\n" +
                "@throws Exception when failed to find by id or other SQL issues.\n" +
                "*/\n" +
                "public static void update(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "Connection con = Database.getCon();\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"UPDATE " + tNameQuoted + " SET ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).nameQuoted + "=?,");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).nameQuoted + "=?");
        classContentBuilder.append(" WHERE id=\"+obj.id)) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append("ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ");\n");
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n" +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") +// Close try/catch
                "finally{Database.freeCon(con);}\n"
        );
        if(t.isCache) classContentBuilder.append("clearCache();\n");
        classContentBuilder.append("}\n\n"); // Close update method


        // CREATE ADD METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Adds the provided object to the database (note that the id is not checked for duplicates).\n" +
                "*/\n" +
                "public static void add(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "Connection con = Database.getCon();\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"INSERT INTO " + tNameQuoted + " (");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).nameQuoted + ",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).nameQuoted);
        classContentBuilder.append(") VALUES (");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append("?,");
        }
        classContentBuilder.append("?)");
        classContentBuilder.append(
                "\")) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append("ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ");\n");
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n" +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") +// Close try/catch
                "finally{Database.freeCon(con);}\n"
        );
        if(t.isCache) classContentBuilder.append("clearCache();\n");
        classContentBuilder.append("}\n\n"); // Close add method


        // CREATE DELETE/REMOVE METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Deletes the provided object from the database.\n" +
                "*/\n" +
                "public static void remove(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "remove(\"id = \"+obj.id);\n" +
                "}\n" +
                "/**\n" +
                "Example: <br>\n" +
                "remove(\"username=?\", \"Peter\"); <br>\n" +
                "Deletes the objects that are found by the provided SQL WHERE statement, from the database.\n" +
                "@param whereValues can be null. Your SQL WHERE statement values to set for '?'.\n" +
                "*/\n" +
                "public static void remove(String where, Object... whereValues) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                (t.isDebug ? "System.err.println(\"remove: \"+where);\n" : "") +
                "java.util.Objects.requireNonNull(where);\n" +
                "Connection con = Database.getCon();\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"DELETE FROM " + tNameQuoted + " WHERE \"+where)) {\n");// Open try/catch
        classContentBuilder.append(
                "if(whereValues != null)\n" +
                        "                for (int i = 0; i < whereValues.length; i++) {\n" +
                        "                    Object val = whereValues[i];\n" +
                        "                    ps.setObject(i+1, val);\n" +
                        "                }\n" +
                        "ps.executeUpdate();\n" +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                        "finally{Database.freeCon(con);}\n"
        );
        if(t.isCache) classContentBuilder.append("clearCache();\n");
        classContentBuilder.append("}\n\n"); // Close delete method

        classContentBuilder.append("public static void removeAll() "+(t.isNoExceptions ? "" : "throws Exception")+" {\n" +
                "        Connection con = Database.getCon();\n" +
                "        try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"DELETE FROM "+tNameQuoted+"\")) {\n" +
                "            ps.executeUpdate();\n" +
                (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                "        finally{Database.freeCon(con);}\n" +
                "    }");

        // CREATE CLONE METHOD
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

        // CREATE TOPRINTSTRING METHOD
        classContentBuilder.append("public String toPrintString(){\n" +
                "return  \"\"");
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            classContentBuilder.append("+\"" + col.name + "=\"+this." + col.name + "+\" \"");
        }
        classContentBuilder.substring(0, classContentBuilder.length() - 1);
        classContentBuilder.append(";\n}\n");

        for (Column col : t.columns) {
            classContentBuilder.append(
                    "public static WHERE where" + firstToUpperCase(col.name) + "() {\n" +
                            "return new WHERE(\"" + col.nameQuoted + "\");\n" +
                            "}\n");
        }
        classContentBuilder.append(generateWhereClass(t));
        classContentBuilder.append("// The code below will not be removed when re-generating this class.\n");

        // Handle additional code from existing/old generated class
        if(oldGeneratedClass.exists()){
            List<String> additionalLines = new ArrayList<>();
            List<String> lines = Files.readAllLines(oldGeneratedClass.toPath());
            List<String> oldImportsList = new ArrayList<>();
            boolean isAdditionalLine = false;
            for (String line : lines) {
                if(line.startsWith("import"))
                    oldImportsList.add(line);
                else if(line.contains("// Additional code start ->")){
                    isAdditionalLine = true;
                    continue;
                }
                else if(line.contains("// Additional code end <-"))
                    isAdditionalLine = false;

                if(isAdditionalLine)
                    additionalLines.add(line);
            }
            importsList = mergeListContents(importsList, oldImportsList);
            if(!additionalLines.isEmpty()){
                classContentBuilder.append("// Additional code start -> \n");
                for (String additionalLine : additionalLines) {
                    classContentBuilder.append(additionalLine).append("\n");
                }
                classContentBuilder.append("// Additional code end <- \n");
            } else{
                classContentBuilder.append("// Additional code start -> \n");
                classContentBuilder.append("private " + t.name + "(){}\n");
                classContentBuilder.append("// Additional code end <- \n");
            }
        } else{
            classContentBuilder.append("// Additional code start -> \n");
            classContentBuilder.append("private " + t.name + "(){}\n");
            classContentBuilder.append("// Additional code end <- \n");
        }

        classContentBuilder.append("}\n"); // Close class

        StringBuilder imports = new StringBuilder();
        for (String s : importsList) {
            imports.append(s).append("\n");
        }
        imports.append("\n");
        return imports.toString() + classContentBuilder;
    }

    private static List<String> mergeListContents(List<String>... lists) {
        List<String> unified = new ArrayList<>();
        for (List<String> list : lists) {
            for (String s : list) {
                if(!unified.contains(s))
                    unified.add(s);
            }
        }
        return unified;
    }

    private static String firstToUpperCase(String s) {
        return ("" + s.charAt(0)).toUpperCase() + s.substring(1);
    }

    public static Constructor genConstructor(String objName, List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        StringBuilder paramsWithoutTypesBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
            paramsWithoutTypesBuilder.append(col.name + ", ");
            fieldsBuilder.append("this." + col.name + " = " + col.name + ";");
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        if (constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length() - 2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "" +
                "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #id}.\n" +
                "*/\n" +
                "public " + objName + " (" + constructor.params + "){\n" + constructor.fieldAssignments + "\n}\n";

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
        for (Column col : columns) {
            if (UString.containsIgnoreCase(col.definition, "NOT NULL")) {
                paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
                paramsWithoutTypesBuilder.append(col.name + ", ");
                fieldsBuilder.append("this." + col.name + " = " + col.name + ";");
            }
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        if (constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length() - 2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "" +
                "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #id}.\n" +
                "*/\n" +
                "public " + objName + " (" + constructor.params + "){\n" + constructor.fieldAssignments + "\n}\n";

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
            if (UString.containsIgnoreCase(col.definition, "NOT NULL")) {
                fieldsBuilder.append(objName + "." + col.name + "=" + col.name + "; ");
            }
        }
        return fieldsBuilder.toString();
    }

    public static String generateWhereClass(Table table) {
        return "public static class WHERE {\n" +
                "        /**\n" +
                "         * Remember to prepend WHERE on the final SQL statement.\n" +
                "         * This is not done by this class due to performance reasons. <p>\n" +
                "         * <p>\n" +
                "         * Note that it excepts the generated SQL string to be used by a {@link java.sql.PreparedStatement}\n" +
                "         * to protect against SQL-Injection. <p>\n" +
                "         * <p>\n" +
                "         * Also note that the SQL query gets optimized by the database automatically,\n" +
                "         * thus It's recommended to make queries as readable as possible and\n" +
                "         * not worry that much about performance.\n" +
                "         */\n" +
                "        public StringBuilder sqlBuilder = new StringBuilder();\n" +
                "        public StringBuilder orderByBuilder = new StringBuilder();\n" +
                "        public StringBuilder limitBuilder = new StringBuilder();\n" +
                "        List<Object> whereObjects = new ArrayList<>();\n" +
                "        private final String columnName;\n" +
                "        public WHERE(String columnName) {\n" +
                "            this.columnName = columnName;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and returns a list of objects matching the query.\n" +
                "         */\n" +
                "        public List<" + table.name + "> get() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            String orderBy = orderByBuilder.toString();\n" +
                "            if(!orderBy.isEmpty()) orderBy = \" ORDER BY \"+orderBy.substring(0, orderBy.length()-2)+\" \";\n" +
                "            if(!whereObjects.isEmpty())\n" +
                "                return " + table.name + ".get(sqlBuilder.toString()+orderBy+limitBuilder.toString(), whereObjects.toArray());\n" +
                "            else\n" +
                "                return " + table.name + ".get(sqlBuilder.toString()+orderBy+limitBuilder.toString(), (Object[]) null);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and returns the size of the list of objects matching the query.\n" +
                "         */\n" +
                "        public int count() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            String orderBy = orderByBuilder.toString();\n" +
                "            if(!orderBy.isEmpty()) orderBy = \" ORDER BY \"+orderBy.substring(0, orderBy.length()-2)+\" \";\n" +
                "            if(!whereObjects.isEmpty())\n" +
                "                return " + table.name + ".count(sqlBuilder.toString()+orderBy+limitBuilder.toString(), whereObjects.toArray());\n" +
                "            else\n" +
                "                return " + table.name + ".count(sqlBuilder.toString()+orderBy+limitBuilder.toString(), (Object[]) null);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and removes the objects matching the query.\n" +
                "         */\n" +
                "        public void remove() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            String orderBy = orderByBuilder.toString();\n" +
                "            if(!orderBy.isEmpty()) orderBy = \" ORDER BY \"+orderBy.substring(0, orderBy.length()-2)+\" \";\n" +
                "            if(!whereObjects.isEmpty())\n" +
                "                " + table.name + ".remove(sqlBuilder.toString()+orderBy+limitBuilder.toString(), whereObjects.toArray());\n" +
                "            else\n" +
                "                " + table.name + ".remove(sqlBuilder.toString()+orderBy+limitBuilder.toString(), (Object[]) null);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * AND (...) <br>\n" +
                "         */\n" +
                "        public WHERE and(WHERE where) {\n" +
                "            String sql = where.sqlBuilder.toString();\n" +
                "            if(!sql.isEmpty()) {\n" +
                "            sqlBuilder.append(\"AND (\").append(sql).append(\") \");\n" +
                "            whereObjects.addAll(where.whereObjects);\n" +
                "            }\n" +
                "            orderByBuilder.append(where.orderByBuilder.toString());\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * OR (...) <br>\n" +
                "         */\n" +
                "        public WHERE or(WHERE where) {\n" +
                "            String sql = where.sqlBuilder.toString();\n" +
                "            if(!sql.isEmpty()) {\n" +
                "            sqlBuilder.append(\"OR (\").append(sql).append(\") \");\n" +
                "            whereObjects.addAll(where.whereObjects);\n" +
                "            }\n" +
                "            orderByBuilder.append(where.orderByBuilder.toString());\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName = ? <br>\n" +
                "         */\n" +
                "        public WHERE is(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" = ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName IN (?,?,...) <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_in.asp\">https://www.w3schools.com/mysql/mysql_in.asp</a>\n" +
                "         */\n" +
                "        public WHERE is(Object... objects) {\n" +
                "            String s = \"\";\n" +
                "            for (Object obj : objects) {\n" +
                "                s += \"?,\";\n" +
                "                whereObjects.add(obj);\n" +
                "            }\n" +
                "            s = s.substring(0, s.length() - 1); // Remove last ,\n" +
                "            sqlBuilder.append(columnName).append(\" IN (\" + s + \") \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName <> ? <br>\n" +
                "         */\n" +
                "        public WHERE isNot(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" <> ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName IS NULL <br>\n" +
                "         */\n" +
                "        public WHERE isNull() {\n" +
                "            sqlBuilder.append(columnName).append(\" IS NULL \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName IS NOT NULL <br>\n" +
                "         */\n" +
                "        public WHERE isNotNull() {\n" +
                "            sqlBuilder.append(columnName).append(\" IS NOT NULL \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName LIKE ? <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE like(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" LIKE ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName NOT LIKE ? <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE notLike(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" NOT LIKE ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName > ? <br>\n" +
                "         */\n" +
                "        public WHERE biggerThan(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" > ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName < ? <br>\n" +
                "         */\n" +
                "        public WHERE smallerThan(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" < ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName >= ? <br>\n" +
                "         */\n" +
                "        public WHERE biggerOrEqual(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" >= ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName <= ? <br>\n" +
                "         */\n" +
                "        public WHERE smallerOrEqual(Object obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" <= ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName BETWEEN ? AND ? <br>\n" +
                "         */\n" +
                "        public WHERE between(Object obj1, Object obj2) {\n" +
                "            sqlBuilder.append(columnName).append(\" BETWEEN ? AND ? \");\n" +
                "            whereObjects.add(obj1);\n" +
                "            whereObjects.add(obj2);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName ASC, <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE smallestFirst() {\n" +
                "            orderByBuilder.append(columnName + \" ASC, \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName DESC, <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE biggestFirst() {\n" +
                "            orderByBuilder.append(columnName + \" DESC, \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * LIMIT number <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_limit.asp\">https://www.w3schools.com/mysql/mysql_limit.asp</a>\n" +
                "         */\n" +
                "        public WHERE limit(int num) {\n" +
                "            limitBuilder.append(\"LIMIT \").append(num + \" \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "    }\n";
    }

    public static class Constructor {
        public String asString;
        public String params;
        public String paramsWithoutTypes;
        public String fieldAssignments;
    }

}
