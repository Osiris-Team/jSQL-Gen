package com.osiris.jsqlgen.model;

import java.io.File;
import java.util.ArrayList;

public class Database {
    public String name;
    public ArrayList<Table> tables = new ArrayList<>();
    public File javaProjectDir;

    public Database duplicate() {
        Database db = new Database();
        db.name = name;
        db.tables.clear();
        for (Table t : tables) {
            db.tables.add(t.duplicate());
        }
        db.javaProjectDir = javaProjectDir;
        return db;
    }
}
