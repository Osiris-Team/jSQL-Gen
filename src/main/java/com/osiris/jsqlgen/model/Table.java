package com.osiris.jsqlgen.model;

import java.util.ArrayList;

public class Table {
    public String name;
    public ArrayList<Column> columns = new ArrayList<>();
    public boolean isDebug = false;
    public boolean isNoExceptions = true;
    public boolean isCache = false;
    public boolean isVaadinFlowUI = false;

    public Table() {
        Column idColumn = new Column("id");
        idColumn.definition = "INT NOT NULL PRIMARY KEY";
        columns.add(idColumn);
    }

    public Table duplicate() {
        Table t = new Table();
        t.name = name;
        t.columns.clear();
        for (int i = 0; i < columns.size(); i++) {
            t.columns.add(columns.get(i).duplicate());
        }
        t.isDebug = isDebug;
        t.isNoExceptions = isNoExceptions;
        t.isCache = isCache;
        t.isVaadinFlowUI = isVaadinFlowUI;
        return t;
    }
}
