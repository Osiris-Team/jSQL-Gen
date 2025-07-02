package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.osiris.jsqlgen.utils.UString.containsIgnoreCase;

public class GenReferences {

    public static Table getRefTable(Database db, String colName) {
        Table refTable = null;
        if(colName.toLowerCase().endsWith("id"))
            for (Table t2 : db.tables) {
                if(containsIgnoreCase(colName, t2.name)) {
                    refTable = t2;
                    break;
                }
            }
        return refTable;
    }

    /**
     * Goes through all tables and their columns in this database (except provided table)
     * and searches for references of provided table and returns those columns.
     */
    public static LinkedHashMap<Table, List<Column>> getAllDirectRefs(Database db, Table t) {
        LinkedHashMap<Table, List<Column>> map = new LinkedHashMap<>();
        for (Table t2 : db.tables) {
            if(t2.name.equals(t.name)) continue;
            for (Column col : t2.columns) {
                String colName = col.name.toLowerCase();
                if(colName.equalsIgnoreCase(t.name+"id")){
                    List<Column> columns = map.get(t2);
                    if(columns == null) {
                        columns = new ArrayList<>();
                        map.put(t2, columns);
                    }
                    columns.add(col);
                }
            }
        }
        return map;
    }

    /**
     * Recursively searches for all tables referencing the provided table directly or indirectly.
     */
    public static LinkedHashMap<Table, List<Column>> getAllRefs(Database db, Table t) {
        LinkedHashMap<Table, List<Column>> allRefs = new LinkedHashMap<>();
        Set<Table> visitedTables = new HashSet<>();
        getAllRefsRecursive(db, t, allRefs, visitedTables);
        return allRefs;
    }

    private static void getAllRefsRecursive(Database db, Table t, LinkedHashMap<Table, List<Column>> allRefs, Set<Table> visitedTables) {
        if (visitedTables.contains(t)) {
            // Already visited this table, stop recursion
            return;
        }
        visitedTables.add(t);

        LinkedHashMap<Table, List<Column>> directRefs = getAllDirectRefs(db, t);
        for (Map.Entry<Table, List<Column>> entry : directRefs.entrySet()) {
            Table refTable = entry.getKey();
            List<Column> refColumns = entry.getValue();
            List<Column> columns = allRefs.get(refTable);
            if(columns == null) {
                columns = new ArrayList<>();
                allRefs.put(refTable, columns);
            }
            columns.addAll(refColumns);
        }
        for (Map.Entry<Table, List<Column>> entry : directRefs.entrySet()) {
            Table refTable = entry.getKey();
            List<Column> refColumns = entry.getValue();
            getAllRefsRecursive(db, refTable, allRefs, visitedTables);
        }
    }

    @NotNull
    public static String getParamName(Table t1, Column col) {
        return col.name + "_in_"+ t1.name;
    }

    public static String genRefParams(Table t, Map<Table, List<Column>> map) {
        StringBuilder paramsBuilder = new StringBuilder(t.name + " obj, ");
        map.forEach((t1, columns) -> {
            if(t1 == t) return;

            for (Column col : columns) {
                String paramName = getParamName(t1, col);
                paramsBuilder.append("Class<"+ t1.name + "> " + paramName + ", boolean remove_"+paramName+", ");
            }
        });
        String params = paramsBuilder.toString();
        if (params.endsWith(", "))
            params = params.substring(0, params.length() - 2);
        return params;
    }

    public static String genRefParamsInvoke(Table t, Map<Table, List<Column>> map) {
        StringBuilder paramsBuilder = new StringBuilder("obj, ");
        map.forEach((t1, columns) -> {
            if(t1 == t) return;

            for (Column col : columns) {
                paramsBuilder.append(t1.name+".class, true, ");
            }
        });
        String params = paramsBuilder.toString();
        if (params.endsWith(", "))
            params = params.substring(0, params.length() - 2);
        return params;
    }
}
