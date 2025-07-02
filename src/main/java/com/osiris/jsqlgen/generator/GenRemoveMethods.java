package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.osiris.jsqlgen.generator.GenReferences.*;
import static com.osiris.jsqlgen.generator.JavaCodeGenerator.*;
import static com.osiris.jsqlgen.utils.UString.firstToUpperCase;

public class GenRemoveMethods {
    /**
     * Reference Table, example: <br>
     * Order.id <br>
     * Person.orderId <-- Person is ref table of Order <br>
     */
    static class RefTable {
        public Table table;
        public List<Column> columns = new ArrayList<>();

        public RefTable(Table table) {
            this.table = table;
        }
    }

    static class TreeNode {
        public TreeNode parent;
        public List<RefTable> tables = new ArrayList<>();

        public TreeNode(TreeNode parent) {
            this.parent = parent;
        }
    }

    public static String s(Database db, Table t, String tNameQuoted) {

        Column idCol = t.columns.get(0);

        LinkedHashMap<Table, List<Column>> allRefs = getAllRefs(db, t);
        LinkedHashMap<Table, List<Column>> allDirectRefs = getAllDirectRefs(db, t);

        String params = genRefParams(t, allRefs);
        String paramsInvoke = genRefParamsInvoke(t, allRefs);

        String paramsDirect = genRefParams(t, allDirectRefs);
        String paramsInvokeDirect = genRefParamsInvoke(t, allDirectRefs);

        StringBuilder sb = new StringBuilder();
        sb.append(
                "/**\n" +
                        "Unsets its references (sets them to -1/'') and deletes the provided object from the database.\n" +
                        "*/\n" +
                        "public static void remove(" + t.name + " obj) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                        "remove(obj, true, Database.isRemoveRefs);\n" +
                        "}\n" +
                "/**\n" +
                        " * Deletes the provided object from the database.\n" +
                        " * @param unsetRefs If true, sets ids in other tables to -1/''.\n" +
                        " * @param removeRefs !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!! If true removes the complete obj/row(s) in all tables that reference/contain this id.\n" +
                        " *                   This is recursive. It's highly recommended to call removeRefs() before instead, which allows to explicitly exclude some tables.\n" +
                        "*/\n" +
                "public static void remove(" + t.name + " obj, boolean unsetRefs, boolean removeRefs) " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "if(unsetRefs) unsetRefs("+paramsInvokeDirect+");\n" +
                        "if(removeRefs) removeRefs("+paramsInvoke+");\n" +
                        "remove(\"WHERE "+idCol.name+" = \"+obj.getId());\n" +
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
                "getLazySync(objs -> {for("+t.name+" obj : objs) {obj.remove();}});\n" +
                "    }\n\n");

        sb.append("" +
                "/**\n" +
                "     * @see #remove("+t.name+", boolean, boolean) \n" +
                "     */\n" +
                "public static void unsetRefs("+paramsDirect+") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n");
        allDirectRefs.forEach((t1, columns) -> {
            for (Column refCol : columns) {
                String param = getParamName(t1, refCol);
                String s = "if (remove_"+ param + ") {"+t1.name+".getLazySync(results -> { \n" +
                        "  for("+t1.name+" refObj : results) {refObj."+refCol.name+" = " +
                        (refCol.type.isText() ? "\"\"" : "-1") +
                        "; refObj.update();};\n" +
                        "}, totalCount -> {}, 100, "+t1.name+".where"+firstToUpperCase(refCol.name)+"().is(obj."+idCol.name+"));}";
                if(!refCol.type.equals(idCol.type)) s = "/* Possibly not a primary id, since types do not match, thus ignored! " +
                        t1.name+"."+refCol.name+" "+refCol.type.inJava +" != "+t.name+"."+idCol.name+" "+idCol.type.inJava+" \n" + s + "*/";
                sb.append(s + "\n\n");
            }
        });
        sb.append("    }\n\n");

        sb.append("" +
                "/** !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!!\n" +
                "     * @see #remove("+t.name+", boolean, boolean) \n" +
                "     */\n" +
                "public static void removeRefs("+params+") " + (t.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "// Take care of direct refs and indirect refs\n");
        allDirectRefs.forEach((t1, columns) -> {
            for (Column refCol : columns) {
                String param = getParamName(t1, refCol);
                LinkedHashMap<Table, List<Column>> allRefs1 = getAllRefs(db, t1);
                String params1 = genRefParams(t1, allRefs1);
                params1 = params1.substring(params1.indexOf("obj"))
                        .replaceFirst("(obj)", "refObj")
                        .replaceAll("( boolean )", "");
                //String paramsInvoke1 = genRefParamsInvoke(t1, allRefs1).replaceFirst("(obj)", "obj1");
                String s = "if (remove_"+ param + ") {"+t1.name+".getLazySync(results -> { \n" +
                        "  for("+t1.name+" refObj : results) {"+t1.name+".removeRefs("+params1.replaceAll("Class<[^>]+>", "")+");refObj.remove();};\n" +
                        "}, totalCount -> {}, 100, "+t1.name+".where"+firstToUpperCase(refCol.name)+"().is(obj."+idCol.name+"));}\n\n";
                if(!refCol.type.equals(idCol.type)) s = "/* Possibly not a primary id, since types do not match, thus ignored! " +
                        t1.name+"."+refCol.name+" "+refCol.type.inJava +" != "+t.name+"."+idCol.name+" "+idCol.type.inJava+" \n" + s + "*/";
                sb.append(s + "\n\n");
                //sb.append(t1.name+".remove(\"WHERE "+col.name+"=?\", obj.getId());");
            }
        });
        sb.append("    }\n\n");

        return sb.toString();
    }


}
