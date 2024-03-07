package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Table;

public class GenRemoveMethods {
    public static String s(Table t, String tNameQuoted) {
        StringBuilder sb = new StringBuilder();
        sb.append("/**\n" +
                "Deletes the provided object from the database.\n" +
                "*/\n" +
                "public static void remove(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "remove(\"WHERE id = \"+obj.id);\n" +
                "onRemove.forEach(code -> code.accept(obj));\n" +
                "}\n" +
                "/**\n" +
                "Example: <br>\n" +
                "remove(\"WHERE username=?\", \"Peter\"); <br>\n" +
                "Deletes the objects that are found by the provided SQL WHERE statement, from the database.\n" +
                "@param where can NOT be null.\n" +
                "@param whereValues can be null. Your SQL WHERE statement values to set for '?'.\n" +
                "*/\n" +
                "public static void remove(String where, Object... whereValues) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "java.util.Objects.requireNonNull(where);\n" +
                "String sql = \"DELETE FROM " + tNameQuoted + " \"+where;\n" +
                (t.isDebug ? "long msGetCon = System.currentTimeMillis(); long msJDBC = 0;\n" : "") +
                "Connection con = Database.getCon();\n" +
                (t.isDebug ? "msGetCon = System.currentTimeMillis() - msGetCon;\n" : "") +
                (t.isDebug ? "msJDBC = System.currentTimeMillis();\n" : "") +
                "try (PreparedStatement ps = con.prepareStatement(sql)) {\n");// Open try/catch
        sb.append(
                "if(whereValues != null)\n" +
                        "                for (int i = 0; i < whereValues.length; i++) {\n" +
                        "                    Object val = whereValues[i];\n" +
                        "                    ps.setObject(i+1, val);\n" +
                        "                }\n" +
                        "ps.executeUpdate();\n" +
                        (t.isDebug ? "msJDBC = System.currentTimeMillis() - msJDBC;\n" : "") +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                        "finally{" +
                        (t.isDebug ? "System.err.println(sql+\" /* //// msGetCon=\"+msGetCon+\" msJDBC=\"+msJDBC+\" con=\"+con+\" minimalStack=\"+minimalStackString()+\" */\");\n" : "") +
                        "Database.freeCon(con);\n" +
                        (t.isCache ? "clearCache();\n" : "") +
                        "}\n"
        );
        sb.append("}\n\n"); // Close delete method

        sb.append("public static void removeAll() " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "String sql = \"DELETE FROM " + tNameQuoted + "\";\n" +
                (t.isDebug ? "long msGetCon = System.currentTimeMillis(); long msJDBC = 0;\n" : "") +
                "Connection con = Database.getCon();\n" +
                (t.isDebug ? "msGetCon = System.currentTimeMillis() - msGetCon;\n" : "") +
                (t.isDebug ? "msJDBC = System.currentTimeMillis();\n" : "") +
                "        try (PreparedStatement ps = con.prepareStatement(sql)) {\n" +
                "            ps.executeUpdate();\n" +
                (t.isDebug ? "msJDBC = System.currentTimeMillis() - msJDBC;\n" : "") +
                (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                "        finally{" +
                (t.isDebug ? "System.err.println(sql+\" /* //// msGetCon=\"+msGetCon+\" msJDBC=\"+msJDBC+\" con=\"+con+\" minimalStack=\"+minimalStackString()+\" */\");\n" : "") +
                "Database.freeCon(con);\n" +
                (t.isCache ? "clearCache();\n" : "") +
                "}\n" +
                "    }\n\n");
        return sb.toString();
    }
}
