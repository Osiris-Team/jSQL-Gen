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
}
