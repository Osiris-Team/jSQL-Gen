package com.osiris.jsqlgen;

import java.util.ArrayList;

public class Table {
    public String name;
    public ArrayList<Column> columns = new ArrayList<>();

    public Table() {
        Column idColumn = new Column();
        idColumn.name = "id";
        idColumn.definition = "INT NOT NULL PRIMARY KEY";
        columns.add(idColumn);
    }
}
