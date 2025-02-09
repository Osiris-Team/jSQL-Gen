package com.osiris.jsqlgen.model;

import com.osiris.jsqlgen.utils.UString;

import java.util.Objects;

public class Column {
    public long id = 0; // Gson always sets to 0 if field doesn't exist yet, no matter what we write here
    public String name;
    public String nameQuoted;
    public String definition;
    public String comment;
    public transient ColumnType type;

    public Column(String name) {
        updateName(name);
    }

    public void updateName(String newName) {
        this.name = newName;
        this.nameQuoted = "`" + newName + "`";
    }

    public Column definition(String s){
        definition = s;
        return this;
    }

    public Column duplicate() {
        Column col = new Column(name);
        col.id = id;
        col.definition = definition;
        col.comment = comment;
        col.type = type;
        return col;
    }

    public String getDefaultValue() {
        String val = definition.substring(definition.indexOf("DEFAULT"));
        val = val.substring(val.indexOf(" ") + 1);

        if(type.isText())
            return Objects.requireNonNull(UString.getContentWithinQuotes(val));

        if(val.contains(" ")) val = val.substring(0, val.indexOf(" "));
        return UString.removeOuterQuotes(val);
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", nameQuoted='" + nameQuoted + '\'' +
                ", definition='" + definition + '\'' +
                ", comment='" + comment + '\'' +
                ", type=" + type +
                '}';
    }
}
