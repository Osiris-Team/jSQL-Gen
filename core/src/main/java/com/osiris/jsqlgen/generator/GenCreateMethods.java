package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Table;

public class GenCreateMethods {
    public static String s(Table t, String tNameQuoted, JavaCodeGenerator.Constructor constructor, JavaCodeGenerator.Constructor minimalConstructor, boolean hasMoreFields) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                /**
                Creates and returns an object that can be added to this table.
                Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
                Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
                Also note that this method will NOT add the object to the table.
                */
                """);
        Column idCol = t.columns.get(0); // always first
        sb.append(
                "public static " + t.name + " create(" + minimalConstructor.paramsWithoutId
                        + ") {\n" +
                        idCol.type.inJava + " " + idCol.name + " = idCounter.getAndIncrement();\n" + t.name + " obj = new " + t.name + "(" + minimalConstructor.paramsWithoutTypes + ");\n" +
                        "onCreate.forEach(code -> code.accept(obj));\n" +
                        "return obj;\n");
        sb.append("}\n\n"); // Close create method


        if (hasMoreFields) {
            sb.append("/**\n" +
                    "Creates and returns an object that can be added to this table.\n" +
                    "Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).\n" +
                    "Note that this method will NOT add the object to the table.\n" +
                    "*/\n");
            sb.append(
                    "public static " + t.name + " create(" + constructor.paramsWithoutId
                            + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                            idCol.type.inJava + " " + idCol.name + " = idCounter.getAndIncrement();\n" + t.name + " obj = new " + t.name + "();\n" + JavaCodeGenerator.genFieldAssignments("obj", t.columns) + "\n" +
                            "onCreate.forEach(code -> code.accept(obj));\n" +
                            "return obj;\n");
            sb.append("}\n\n"); // Close create method
        }

        sb.append("""
                /**
                Creates and returns an in-memory object with -1 as id, that can be added to this table
                AFTER you manually did obj.id = idCounter.getAndIncrement().
                This is useful for objects that may never be added to the table.
                Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
                Also note that this method will NOT add the object to the table.
                */
                """);
        sb.append(
                "public static " + t.name + " createInMem(" + minimalConstructor.paramsWithoutId
                        + ") {\n" +
                        idCol.type.inJava + " " + idCol.name + " = -1;\n" + t.name + " obj = new " + t.name + "(" + minimalConstructor.paramsWithoutTypes + ");\n" +
                        "onCreate.forEach(code -> code.accept(obj));\n" +
                        "return obj;\n");
        sb.append("}\n\n"); // Close create method

        if (hasMoreFields) {
            sb.append("""
                /**
                Creates and returns an in-memory object with -1 as id, that can be added to this table
                AFTER you manually did obj.id = idCounter.getAndIncrement().
                This is useful for objects that may never be added to the table.
                Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
                Also note that this method will NOT add the object to the table.
                */
                """);
            sb.append(
                    "public static " + t.name + " createInMem(" + constructor.paramsWithoutId
                            + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                            idCol.type.inJava + " " + idCol.name + " = -1;\n" + t.name + " obj = new " + t.name + "();\n" + JavaCodeGenerator.genFieldAssignments("obj", t.columns) + "\n" +
                            "onCreate.forEach(code -> code.accept(obj));\n" +
                            "return obj;\n");
            sb.append("}\n\n"); // Close create method
        }


        sb.append("/**\n" +
                "Convenience method for creating and directly adding a new object to the table.\n" +
                "Note that the parameters of this method represent \"NOT NULL\" fields in the table and thus should not be null.\n" +
                "*/\n");
        sb.append(
                "public static " + t.name + " createAndAdd(" + minimalConstructor.paramsWithoutId
                        + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                        idCol.type.inJava + " " + idCol.name + " = idCounter.getAndIncrement();\n" + t.name + " obj = new " + t.name + "(" + minimalConstructor.paramsWithoutTypes + ");\n" +
                        "onCreate.forEach(code -> code.accept(obj));\n" +
                        "add(obj);\n" +
                        "return obj;\n");
        sb.append("}\n\n"); // Close method


        if (hasMoreFields) {
            sb.append("/**\n" +
                    "Convenience method for creating and directly adding a new object to the table.\n" +
                    "*/\n");
            sb.append(
                    "public static " + t.name + " createAndAdd(" + constructor.paramsWithoutId
                            + ") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                            idCol.type.inJava + " " + idCol.name + " = idCounter.getAndIncrement();\n" + t.name + " obj = new " + t.name + "();\n" + JavaCodeGenerator.genFieldAssignments("obj", t.columns) + "\n" +
                            "onCreate.forEach(code -> code.accept(obj));\n" +
                            "add(obj);\n" +
                            "return obj;\n");
            sb.append("}\n\n"); // Close method
        }


        // CREATE GET METHOD:
        sb.append("/**\n" +
                "@return a list containing all objects in this table.\n" +
                "*/\n" +
                "public static List<" + t.name + "> get() " + (t.isNoExceptions ? "" : "throws Exception") + " {return get(null);}\n" +
                "/**\n" +
                "@return object with the provided id or null if there is no object with the provided id in this table.\n" +
                "@throws Exception on SQL issues.\n" +
                "*/\n" +
                "public static " + t.name + " get(int id) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "try{\n" +
                "return get(\"WHERE id = \"+id).get(0);\n" +
                "}catch(IndexOutOfBoundsException ignored){}\n" +
                (t.isNoExceptions ? "catch(Exception e){throw new RuntimeException(e);}\n" : "") + // Close try/catch
                "return null;\n" + // Close tr
                "}\n" +
                "/**\n" +
                "Example: <br>\n" +
                "get(\"WHERE username=? AND age=?\", \"Peter\", 33);  <br>\n" +
                "@param where can be null. Your SQL WHERE statement (with the leading WHERE).\n" +
                "@param whereValues can be null. Your SQL WHERE statement values to set for '?'.\n" +
                "@return a list containing only objects that match the provided SQL WHERE statement (no matches = empty list).\n" +
                "if that statement is null, returns all the contents of this table.\n" +
                "*/\n" +
                "public static List<" + t.name + "> get(String where, Object... whereValues) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "String sql = \"SELECT ");
        for (int i = 0; i < t.columns.size() - 1; i++) {
            sb.append(t.columns.get(i).nameQuoted + ",");
        }
        sb.append(t.columns.get(t.columns.size() - 1).nameQuoted);
        // Open while
        sb.append("\" +\n" +
                "\" FROM " + tNameQuoted + "\" +\n" +
                "(where != null ? where : \"\");\n" +

                (t.isCache ? "synchronized(cachedResults){ CachedResult cachedResult = cacheContains(sql, whereValues);\n" +
                        "if(cachedResult != null) return cachedResult.getResultsCopy();\n" : "") +
                "List<" + t.name + "> list = new ArrayList<>();\n" +
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
                "while (rs.next()) {\n" + t.name + " obj = new " + t.name + "();\n" +
                "list.add(obj);\n");
        for (int i = 0; i < t.columns.size(); i++) {
            Column c = t.columns.get(i);
            if (c.type.isEnum())
                sb.append("obj." + c.name + " = " + c.type.inJava + ".valueOf(rs." + c.type.inJBDCGet + "(" + (i + 1) + "));\n");
            else
                sb.append("obj." + c.name + " = rs." + c.type.inJBDCGet + "(" + (i + 1) + ");\n");
        }
        sb.append(
                "}\n" + // Close while
                        (t.isDebug ? "msJDBC = System.currentTimeMillis() - msJDBC;\n" : "") +
                        (t.isNoExceptions ? "}catch(Exception e){throw new RuntimeException(e);}\n" : "}\n") + // Close try/catch
                        "finally{" +
                        (t.isDebug ? "System.err.println(sql+\" /* //// msGetCon=\"+msGetCon+\" msJDBC=\"+msJDBC+\" con=\"+con+\" minimalStack=\"+minimalStackString()+\" */\");\n" : "") +
                        "Database.freeCon(con);}\n" +
                        (t.isCache ? """
                                cachedResults.add(new CachedResult(sql, whereValues, list));
                                return list;}
                                """ : "return list;\n")); // Close synchronized block if isCache
        sb.append("}\n\n"); // Close get method

        // CREATE GETLAZY METHODS:
        sb.append("    /**\n" +
                "     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.\n" +
                "     */\n" +
                "    public static Thread getLazy(Consumer<List<" + t.name + ">> onResultReceived)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                "        return getLazy(onResultReceived, null, 500, null);\n" +
                "    }\n" +
                "    /**\n" +
                "     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.\n" +
                "     */\n" +
                "    public static Thread getLazy(Consumer<List<" + t.name + ">> onResultReceived, int limit)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                "        return getLazy(onResultReceived, null, limit, null);\n" +
                "    }\n" +
                "    /**\n" +
                "     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.\n" +
                "     */\n" +
                "    public static Thread getLazy(Consumer<List<" + t.name + ">> onResultReceived, Consumer<Long> onFinish)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                "        return getLazy(onResultReceived, onFinish, 500, null);\n" +
                "    }\n" +
                "    /**\n" +
                "     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.\n" +
                "     */\n" +
                "    public static Thread getLazy(Consumer<List<" + t.name + ">> onResultReceived, Consumer<Long> onFinish, int limit)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                "        return getLazy(onResultReceived, onFinish, limit, null);\n" +
                "    }\n" +
                "    /**\n" +
                "     * Loads results lazily in a new thread. <br>\n" +
                "     * Add {@link Thread#sleep(long)} at the end of your onResultReceived code, to sleep between fetches.\n" +
                "     * @param onResultReceived can NOT be null. Gets executed until there are no results left, thus the results list is never empty.\n" +
                "     * @param onFinish can be null. Gets executed when finished receiving all results. Provides the total amount of received elements as parameter.\n" +
                "     * @param limit the maximum amount of elements for each fetch.\n" +
                "     * @param where can be null. This WHERE is not allowed to contain LIMIT and should not contain order by id.\n" +
                "     */\n" +
                "    public static Thread getLazy(Consumer<List<" + t.name + ">> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) " + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                "        Thread thread = new Thread(() -> {\n" +
                "            WHERE finalWhere;\n" +
                "            if(where == null) finalWhere = new WHERE(\"\");\n" +
                "            else finalWhere = where;\n" +
                "            List<" + t.name + "> results;\n" +
                "            int lastId = -1;\n" +
                "            long count = 0;\n" +
                "            while(true){\n" +
                "                results = whereId().biggerThan(lastId).and(finalWhere).limit(limit).get();\n" +
                "                if(results.isEmpty()) break;\n" +
                "                lastId = results.get(results.size() - 1).id;\n" +
                "                count += results.size();\n" +
                "                onResultReceived.accept(results);\n" +
                "            }\n" +
                "            if(onFinish!=null) onFinish.accept(count);\n" +
                "        });\n" +
                "        thread.start();\n" +
                "        return thread;\n" +
                "    }\n\n");

        sb.append(
                "    /**\n" +
                        "     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.\n" +
                        "     */\n" +
                        "    public static Thread getLazySync(Consumer<List<" + t.name + ">> onResultReceived)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                        "        return getLazySync(onResultReceived, null, 500, null);\n" +
                        "    }\n" +
                        "    /**\n" +
                        "     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.\n" +
                        "     */\n" +
                        "    public static Thread getLazySync(Consumer<List<" + t.name + ">> onResultReceived, int limit)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                        "        return getLazySync(onResultReceived, null, limit, null);\n" +
                        "    }\n" +
                        "    /**\n" +
                        "     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.\n" +
                        "     */\n" +
                        "    public static Thread getLazySync(Consumer<List<" + t.name + ">> onResultReceived, Consumer<Long> onFinish)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                        "        return getLazySync(onResultReceived, onFinish, 500, null);\n" +
                        "    }\n" +
                        "    /**\n" +
                        "     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.\n" +
                        "     */\n" +
                        "    public static Thread getLazySync(Consumer<List<" + t.name + ">> onResultReceived, Consumer<Long> onFinish, int limit)" + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                        "        return getLazySync(onResultReceived, onFinish, limit, null);\n" +
                        "    }\n" +
                        "    /**\n" +
                        "     * Waits until finished, then returns. <br>" +
                        "     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.\n" +
                        "     */\n" +
                        "    public static Thread getLazySync(Consumer<List<" + t.name + ">> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) " + (t.isNoExceptions ? "" : "throws Exception") + "{\n" +
                "        Thread thread = getLazy(onResultReceived, onFinish, limit, where);\n" +
                "        while(thread.isAlive()) Thread.yield();\n" +
                "        return thread;\n" +
                "    }\n\n");
        return sb.toString();
    }
}
