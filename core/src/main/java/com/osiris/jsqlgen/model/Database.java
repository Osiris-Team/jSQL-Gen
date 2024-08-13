package com.osiris.jsqlgen.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {
    public String name;
    public CopyOnWriteArrayList<Table> tables = new CopyOnWriteArrayList<>();
    /**
     * Can be multiple paths seperated by ;
     */
    protected String javaProjectDir;
    public boolean isWithMariadb4j;

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

    public CopyOnWriteArrayList<File> getJavaProjectDirs(){
        CopyOnWriteArrayList<File> files = new CopyOnWriteArrayList<>();
        if(javaProjectDir == null || javaProjectDir.isEmpty()) return files;
        for (String s : javaProjectDir.split(";")) {
            files.add(new File(s.trim()));
        }
        return files;
    }

    /**
     * @param dirs semicolon ";" separated list of directories
     */
    public void setJavaProjectDirs(String dirs){
        this.javaProjectDir = dirs;
    }

    public String setJavaProjectDirs(List<File> dirs){
        String s = "";
        for (File dir : dirs) {
            s += dir.getAbsolutePath() + " ;";
        }
        this.javaProjectDir = s;
        return s;
    }
}
