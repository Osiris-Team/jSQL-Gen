package com.osiris.jsqlgen.model;

public class Column {
    public String name;
    public String nameQuoted;
    public String definition;
    public String comment;
    public ColumnType type;

    public Column(String name) {
        updateName(name);
    }

    public void updateName(String newName) {
        this.name = newName;
        this.nameQuoted = "`" + newName + "`";
    }

    public Column duplicate() {
        Column col = new Column(name);
        col.definition = definition;
        col.comment = comment;
        col.type = type;
        return col;
    }
}
