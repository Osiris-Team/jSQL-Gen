package com.osiris.jsqlgen.model;

import com.osiris.jsqlgen.Main;

import java.util.ArrayList;

public class Table {
    public int id = 0;
    public String name;
    public ArrayList<Column> columns = new ArrayList<>();
    public boolean isDebug = false;
    public boolean isNoExceptions = true;
    public boolean isCache = false;
    public boolean isVaadinFlowUI = false;
    public ArrayList<TableChange> changes = new ArrayList<>();


    public Table addIdColumn(){
        Column idColumn = new Column("id");
        idColumn.id = Main.idCounter.getAndIncrement();
        idColumn.definition = "INT NOT NULL PRIMARY KEY";
        columns.add(idColumn);
        return this;
    }

    public Table duplicate() {
        Table t = new Table();
        t.name = name;
        for (Column c : columns) {
            t.columns.add(c.duplicate());
        }
        t.isDebug = isDebug;
        t.isNoExceptions = isNoExceptions;
        t.isCache = isCache;
        t.isVaadinFlowUI = isVaadinFlowUI;
        t.changes.clear();
        t.changes.addAll(changes); // TODO is proper duplicate function required for this?
        return t;
    }
}
