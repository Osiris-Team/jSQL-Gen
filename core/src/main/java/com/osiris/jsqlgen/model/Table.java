package com.osiris.jsqlgen.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public String name;
    public ArrayList<Column> columns = new ArrayList<>();
    public boolean isDebug = false;
    public boolean isNoExceptions = true;
    public boolean isCache = false;
    public List<String> removedColumnNames = new ArrayList<>();

    public Table() {
        Column idColumn = new Column("id");
        idColumn.definition = "INT NOT NULL PRIMARY KEY";
        columns.add(idColumn);
    }

    public Table duplicate() {
        Table t = new Table();
        t.name = "COPY-"+name;
        for (int i = 1; i < columns.size(); i++) { // Start at 1 to skip id column
            t.columns.add(columns.get(i).duplicate());
        }
        t.isDebug = isDebug;
        t.isNoExceptions = isNoExceptions;
        t.isCache = isCache;
        return t;
    }
}
