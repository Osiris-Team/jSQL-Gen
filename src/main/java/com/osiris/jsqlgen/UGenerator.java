package com.osiris.jsqlgen;

import java.util.List;
import java.util.Objects;

public class UGenerator {

    public static class Constructor {
        public String asString;
        public String params;
        public String paramsWithoutTypes;
        public String fieldAssignments;
    }

    /**
     * Generates Java source code, for the provided table.
     */
    public static String generate(Table t) throws Exception {
        String tNameQuoted = "`"+t.name+"`";
        for (Column col : t.columns) {
            col.type = ColumnType.findBySQLDefinition(col.definition);
            if(col.type == null)
                throw new Exception("Failed to generate code, because failed to find matching java type of definition '"+col.definition
                    +"'. Make sure that the data type is the first word in your definition and that its a supported type by jSQL-Gen.");
        }
        Constructor constructor = genContructor(t.name, t.columns);

        StringBuilder importsBuilder = new StringBuilder();
        importsBuilder.append("import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import java.sql.*;\n");
        importsBuilder.append("\n");

        StringBuilder classContentBuilder = new StringBuilder();
        classContentBuilder.append("public class "+t.name+"{\n"); // Open class
        classContentBuilder.append("private static java.sql.Connection con;\n");
        classContentBuilder.append("private static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);\n");
        classContentBuilder.append("static {\n" +
                "try{\n" +
                "con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
                "try (Statement s = con.createStatement()) {\n" +
                "s.executeUpdate(\"CREATE TABLE IF NOT EXISTS "+ tNameQuoted +" ("+t.columns.get(0).name // EXPECTS ID
                +" "+t.columns.get(0).definition+")\");\n" );
        for (Column col : t.columns) {
            classContentBuilder.append("s.executeUpdate(\"ALTER TABLE "+tNameQuoted+" ADD COLUMN IF NOT EXISTS "+col.name+" "+col.definition+"\");\n");
            classContentBuilder.append("s.executeUpdate(\"ALTER TABLE "+tNameQuoted+" MODIFY IF EXISTS "+col.name+" "+col.definition+"\");\n");
        }
        classContentBuilder.append(
                "}\n" +
                        "" +
                        "try (PreparedStatement ps = con.prepareStatement(\"SELECT id FROM "+tNameQuoted+" ORDER BY id DESC LIMIT 1\")) {\n" +
                        "ResultSet rs = ps.executeQuery();\n" +
                        "if (rs.next()) idCounter.set(rs.getInt(1));\n" +
                        "}\n" +
                        "}\n" +
                        "catch(Exception e){ throw new RuntimeException(e); }\n" +
                        "}\n");

        // CONSTRUCTORS
        classContentBuilder.append("private "+t.name+"(){}\n");
        classContentBuilder.append(constructor.asString);

        // CREATE FIELDS AKA COLUMNS:
        for (Column col : t.columns) {
            boolean notNull = UString.containsIgnoreCase(col.definition, "NOT NULL");
            classContentBuilder.append("" +
                    "/**\n" +
                    "Database field/value. "+(notNull ? "Not null. " : "")+"<br>\n" +
                    (col.comment != null ? (col.comment+"\n") : "") +
                    "*/\n" +
                    "public "+col.type.inJava+" "+col.name+";\n");
        }

        // CREATE CREATE METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Increments the id and sets it for this object (basically reserves a space in the database).\n" +
                "@return object with latest id. Should be added to the database next by you.\n" +
                "*/\n");
        Column firstCol = t.columns.get(0);
        String idParam = firstCol.type.inJava+" "+firstCol.name+",";
        classContentBuilder.append(
                "public static "+t.name+" create("+constructor.params.replace(idParam, "")
                +") {\n" +
                        firstCol.type.inJava+" "+firstCol.name+" = idCounter.incrementAndGet();\n" +
                "" + t.name +" obj = new "+t.name+"("+constructor.paramsWithoutTypes+");\n"+
                "return obj;\n");
        classContentBuilder.append("}\n\n"); // Close create method

        // CREATE GET METHOD:
        classContentBuilder.append("" +
                "public static List<"+t.name+"> get() throws Exception {return get(null);}\n" +
                "/**\n" +
                "@return a list containing only objects that match the provided SQL WHERE statement.\n" +
                "if that statement is null, returns all the contents of this table.\n" +
                "*/\n" +
                "public static List<"+t.name+"> get(String where) throws Exception {\n" +
                "List<"+t.name+"> list = new ArrayList<>();\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"SELECT ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).name+",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size()-1).name);
        classContentBuilder.append(
                "\" +\n" +
                        "\" FROM "+tNameQuoted+"\" +\n" +
                        "(where != null ? (\"WHERE \"+where) : \"\"))) {\n" + // Open try/catch
                        "ResultSet rs = ps.executeQuery();\n" +
                        "while (rs.next()) {\n" + // Open while
                        ""+t.name +" obj = new "+t.name+"();\n" +
                        "list.add(obj);\n");
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append("obj."+c.name+" = rs."+c.type.inJBDCGet+"("+(i+1)+");\n");
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
                "public static void update("+t.name+" obj) throws Exception {\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"UPDATE "+tNameQuoted+" SET ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).name+"=?,");
        }
        classContentBuilder.append(t.columns.get(t.columns.size()-1).name+"=?");
        classContentBuilder.append(
                "\")) {\n" // Open try/catch
        );
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            classContentBuilder.append("ps."+c.type.inJBDCSet+"("+(i+1)+", obj."+c.name+");\n");
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n"+
                        "}\n" // Close try/catch
        );
        classContentBuilder.append("}\n\n"); // Close update method


        // CREATE ADD METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Adds the provided object to the database (note that the id is not checked for duplicates).\n" +
                "*/\n" +
                "public static void add("+t.name+" obj) throws Exception {\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"INSERT INTO "+tNameQuoted+" (");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            classContentBuilder.append(t.columns.get(i).name+",");
        }
        classContentBuilder.append(t.columns.get(t.columns.size()-1).name);
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
            classContentBuilder.append("ps."+c.type.inJBDCSet+"("+(i+1)+", obj."+c.name+");\n");
        }
        classContentBuilder.append(
                "ps.executeUpdate();\n"+
                        "}\n" // Close try/catch
        );
        classContentBuilder.append("}\n\n"); // Close add method



        // CREATE DELETE METHOD:
        classContentBuilder.append("" +
                "/**\n" +
                "Deletes the provided object from the database.\n" +
                "*/\n" +
                "public static void remove("+t.name+" obj) throws Exception {\n" +
                "remove(\"id = \"+obj.id);\n" +
                "}\n"+
                "/**\n" +
                "Deletes the objects that are found by the provided SQL WHERE statement, from the database.\n" +
                "*/\n" +
                "public static void remove(String where) throws Exception {\n" +
                "java.util.Objects.requireNonNull(where);\n" +
                "try (PreparedStatement ps = con.prepareStatement(\n" +
                "                \"DELETE FROM "+tNameQuoted+" WHERE \"+where)) {\n");// Open try/catch
        classContentBuilder.append(
                "ps.executeUpdate();\n"+
                        "}\n" // Close try/catch
        );
        classContentBuilder.append("}\n\n"); // Close delete method
        classContentBuilder.append("}\n"); // Close class

        return importsBuilder.toString() + classContentBuilder.toString();
    }

    public static Constructor genContructor(String objName, List<Column> columns){
        StringBuilder paramsBuilder = new StringBuilder();
        StringBuilder paramsWithoutTypesBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col:columns) {
            if(UString.containsIgnoreCase(col.definition, "NOT NULL")){
                paramsBuilder.append(col.type.inJava+" "+col.name+", ");
                paramsWithoutTypesBuilder.append(col.name+", ");
                fieldsBuilder.append("this."+col.name+" = "+col.name+";");
            }
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        if(constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length()-2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "" +
                "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #id}.\n" +
                "*/\n" +
                "public "+objName+" (" + constructor.params + "){\n" + constructor.fieldAssignments + "\n}\n";

        constructor.paramsWithoutTypes = paramsWithoutTypesBuilder.toString();
        if(constructor.paramsWithoutTypes.endsWith(", "))
            constructor.paramsWithoutTypes = constructor.paramsWithoutTypes.substring(0, constructor.paramsWithoutTypes.length()-2);

        return constructor;
    }

}
