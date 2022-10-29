package com.osiris.jsqlgen.model;

import java.util.ArrayList;

public class Table {
    public String name;
    public ArrayList<Column> columns = new ArrayList<>();
    public boolean isDebug = false;
    public boolean isNoExceptions = true;
    public boolean isCache = false;

    public Table() {
        Column idColumn = new Column("id");
        idColumn.definition = "INT NOT NULL PRIMARY KEY";
        columns.add(idColumn);
    }

    public Table duplicate() {
        Table t = new Table();
        t.name = "COPY-"+name;
        for (Column column : columns) {
            t.columns.add(column.duplicate());
        }
        t.isDebug = isDebug;
        t.isNoExceptions = isNoExceptions;
        t.isCache = isCache;
        return t;
    }
}
