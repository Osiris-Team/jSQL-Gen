package com.osiris.jsqlgen.utils;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Table;

import java.util.List;

public class UGenerator {

    /**
     * Generates Java source code, for the provided table.
     */
    public static String generate(Table t) throws Exception {
        String tNameQuoted = "`" + t.name + "`";
        for (Column col : t.columns) {
            col.type = ColumnType.findBySQLDefinition(col.definition);
            if (col.type == null)
                throw new Exception("Failed to generate code, because failed to find matching java type of definition '" + col.definition
                        + "'. Make sure that the data type is the first word in your definition and that its a supported type by jSQL-Gen.");
        }
        Constructor constructor = genConstructor(t.name, t.columns);
        Constructor minimalConstructor = genMinimalConstructor(t.name, t.columns);
        boolean hasMoreFields = genFieldAssignments(t.columns).length() != genOnlyNotNullFieldAssignments(t.columns).length();

        StringBuilder importsBuilder = new StringBuilder();
        importsBuilder.append("import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import java.sql.*;\n");
        importsBuilder.append("\n");

        StringBuilder classContentBuilder = new StringBuilder();
        classContentBuilder.append("public class " + t.name + "{\n"); // Open class
        classContentBuilder.append("private static java.sql.Connection con;\n");
        classContentBuilder.append("private static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);\n");
        classContentBuilder.append("static {\n" +
                "try{\n" +
                "con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
                "try (Statement s = con.createStatement()) {\n" +
                "s.executeUpdate(\"CREATE TABLE IF NOT EXISTS " + tNameQuoted + " (" + t.columns.get(0).name // EXPECTS ID
                + " " + t.columns.get(0).definition + ")\");\n");
        for (int i = 1; i < t.columns.size(); i++) { // Skip first column (id) to avoid "SQLSyntaxErrorException: Multiple primary key defined"
            Column col = t.columns.get(i);
            classContentBuilder.append("try{s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " ADD COLUMN " + col.name + " " + col.definition + "\");}catch(Exception ignored){}\n");
            classContentBuilder.append("s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " MODIFY COLUMN " + col.name + " " + col.definition + "\");\n");
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
                        "}\n");

        // CONSTRUCTORS
        classContentBuilder.append("private " + t.name + "(){}\n");
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
        classContentBuilder.append("" +
                "/**\n" +
                "Creates and returns an object that can be added to this table.\n" +
                "Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).\n" +
                "Note that the parameters of this method represent \"NOT NULL\" fields in the table and thus should not be null.\n" +
                "Also note that this method will NOT add the object to the table.\n" +
                "*/\n");
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
                            + ") throws Exception {\n" +
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
                        + ") throws Exception {\n" +
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
                            + ") throws Exception {\n" +
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
                "public static List<" + t.name + "> get() throws Exception {return get(null);}\n" +
                "/**\n" +
                "@return object with the provided id.\n" +
                "@throws Exception on SQL issues, or if there is no object with the provided id in this table.\n" +
                "*/\n" +
                "public static " + t.name + " get(int id) throws Exception {\n" +
                "return get(\"id = \"+id).get(0);\n" +
                "}\n" +
                "/**\n" +
                "Example: <br>\n" +
                "get(\"username=? AND age=?\", \"Peter\", 33);  <br>\n" +
                "@param where can be null. Your SQL WHERE statement (without the leading WHERE).\n" +
                "@param whereValues can be null. Your SQL WHERE statement values to set for '?'.\n" +
                "@return a list containing only objects that match the provided SQL WHERE statement.\n" +
                "if that statement is null, returns all the contents of this table.\n" +
                "*/\n" +
                "public static List<" + t.name + "> get(String where, Object... whereValues) throws Exception {\n" +
                "List<" + t.name + "> list = new ArrayList<>();\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"SELECT ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).name + ",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).name);
        classContentBuilder.append(
                "\" +\n" +
                        "\" FROM " + tNameQuoted + "\" +\n" +
                        "(where != null ? (\"WHERE \"+where) : \"\"))) {\n" + // Open try/catch
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
                        "}\n" + // Close try/catch
                        "return list;\n");
        classContentBuilder.append("}\n\n"); // Close get method


        // CREATE UPDATE METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Searches the provided object in the database (by its id),\n" +
                "and updates all its fields.\n" +
                "@throws Exception when failed to find by id.\n" +
                "*/\n" +
                "public static void update(" + t.name + " obj) throws Exception {\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"UPDATE " + tNameQuoted + " SET ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).name + "=?,");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).name + "=?");
        classContentBuilder.append(
                "\")) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append("ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ");\n");
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n" +
                        "}\n" // Close try/catch
        );
        classContentBuilder.append("}\n\n"); // Close update method


        // CREATE ADD METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Adds the provided object to the database (note that the id is not checked for duplicates).\n" +
                "*/\n" +
                "public static void add(" + t.name + " obj) throws Exception {\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"INSERT INTO " + tNameQuoted + " (");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).name + ",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size() - 1).name);
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
                        "}\n" // Close try/catch
        );
        classContentBuilder.append("}\n\n"); // Close add method


        // CREATE DELETE METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Deletes the provided object from the database.\n" +
                "*/\n" +
                "public static void remove(" + t.name + " obj) throws Exception {\n" +
                "remove(\"id = \"+obj.id);\n" +
                "}\n" +
                "/**\n" +
                "Example: <br>\n" +
                "remove(\"username=?\", \"Peter\"); <br>\n" +
                "Deletes the objects that are found by the provided SQL WHERE statement, from the database.\n" +
                "@param whereValues can be null. Your SQL WHERE statement values to set for '?'.\n" +
                "*/\n" +
                "public static void remove(String where, Object... whereValues) throws Exception {\n" +
                "java.util.Objects.requireNonNull(where);\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"DELETE FROM " + tNameQuoted + " WHERE \"+where)) {\n");// Open try/catch
        classContentBuilder.append(
                "if(whereValues != null)\n" +
                        "                for (int i = 0; i < whereValues.length; i++) {\n" +
                        "                    Object val = whereValues[i];\n" +
                        "                    ps.setObject(i+1, val);\n" +
                        "                }\n" +
                        "ps.executeUpdate();\n" +
                        "}\n" // Close try/catch
        );
        classContentBuilder.append("}\n\n"); // Close delete method

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
                    "public static WHERE where"+firstToUpperCase(col.name)+"() {\n" +
                            "return new WHERE(\""+col.name+"\");\n"+
                            "}\n");
        }

        classContentBuilder.append("}\n"); // Close class
        return importsBuilder.toString() + classContentBuilder;
    }

    private static String firstToUpperCase(String s){
        return (""+s.charAt(0)).toUpperCase()+s.substring(1);
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

    public static String generateWhereClass() {
        return "";
    }

    public static class Constructor {
        public String asString;
        public String params;
        public String paramsWithoutTypes;
        public String fieldAssignments;
    }

}
