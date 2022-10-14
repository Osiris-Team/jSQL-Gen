package com.osiris.jsqlgen;

import com.google.gson.*;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Data {
    public static File dir = new File(System.getProperty("user.home") + "/jSQL-Gen");
    public static final File file = new File(dir + "/data.yml");
    public static final CopyOnWriteArrayList<Database> databases;
    private static AtomicBoolean save = new AtomicBoolean(false);

    public static void save(){
        save.set(true);
    }

    static {
        try{
            databases = fetchDatabases();
            new Thread(() -> {
                try{
                    while (true){
                        Thread.sleep(1000);
                        if(save.get()){
                            updateDatabases(databases);
                            System.out.println("Saved/Updated data.");
                            save.set(false);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Data() throws IOException {
        super();
    }

    public static Column findColumn(List<Column> list, String name) throws IOException {
        for (Column c : list) {
            if (Objects.equals(name, c.name))
                return c;
        }
        return null;
    }

    public static Table findTable(Database db, String name){
        return findTable(db.tables, name);
    }

    public static Table findTable(List<Table> list, String name) {
        for (Table t : list) {
            if (Objects.equals(name, t.name))
                return t;
        }
        return null;
    }

    public static Database getDatabase(String name){
        return getDatabase(databases, name);
    }

    public static Database getDatabase(List<Database> list, String name) {
        for (Database db : list) {
            if (Objects.equals(name, db.name))
                return db;
        }
        throw new NullPointerException("Failed to find database named '" + name + "' in " + Data.file);
    }

    private static CopyOnWriteArrayList<Database> fetchDatabases() throws IOException {
        CopyOnWriteArrayList<Database> list = new CopyOnWriteArrayList<>();
        synchronized (file) {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            JsonElement el = JsonParser.parseReader(new FileReader(file));
            if (el == null || el.isJsonNull()) return list;
            JsonObject obj = el.getAsJsonObject();
            if (obj.get("databases") == null) return list;
            for (JsonElement elDatabase : obj.get("databases").getAsJsonArray()) {
                JsonObject objDatabase = elDatabase.getAsJsonObject();
                Database database = new Database();
                list.add(database);
                database.name = objDatabase.get("name").getAsString();
                if (objDatabase.get("javaProjectDir") != null)
                    database.javaProjectDir = new File(objDatabase.get("javaProjectDir").getAsString());
                database.tables = new ArrayList<Table>();
                for (JsonElement elTable : objDatabase.get("tables").getAsJsonArray()) {
                    JsonObject objTable = elTable.getAsJsonObject();
                    Table table = new Table();
                    database.tables.add(table);
                    table.name = objTable.get("name").getAsString();
                    if (objTable.get("isDebug") != null)
                        table.isDebug = objTable.get("isDebug").getAsBoolean();
                    if (objTable.get("isNoExceptions") != null)
                        table.isNoExceptions = objTable.get("isNoExceptions").getAsBoolean();
                    if (objTable.get("isCache") != null)
                        table.isCache = objTable.get("isCache").getAsBoolean();
                    table.columns = new ArrayList<Column>();
                    for (JsonElement elColumn : objTable.get("columns").getAsJsonArray()) {
                        JsonObject objColumn = elColumn.getAsJsonObject();
                        Column column = new Column(objColumn.get("name").getAsString());
                        table.columns.add(column);
                        if (objColumn.get("definition") != null)
                            column.definition = objColumn.get("definition").getAsString();
                        if (objColumn.get("comment") != null)
                            column.comment = objColumn.get("comment").getAsString();
                    }
                }
            }
        }
        return list;
    }

    private static void updateDatabases(List<Database> list) throws IOException {
        synchronized (file) {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            JsonObject obj = new JsonObject();
            JsonArray arrDatabases = new JsonArray();
            obj.add("databases", arrDatabases);
            for (Database database : list) {
                JsonObject dbObj = new JsonObject();
                arrDatabases.add(dbObj);
                dbObj.addProperty("name", database.name);
                if (database.javaProjectDir != null)
                    dbObj.addProperty("javaProjectDir", database.javaProjectDir.getAbsolutePath());

                JsonArray arrTables = new JsonArray();
                dbObj.add("tables", arrTables);
                for (Table table : database.tables) {
                    JsonArray arrColumns = new JsonArray();
                    JsonObject tableObj = new JsonObject();
                    arrTables.add(tableObj);
                    tableObj.addProperty("name", table.name);
                    dbObj.addProperty("isDebug", table.isDebug);
                    dbObj.addProperty("isNoExceptions", table.isNoExceptions);
                    dbObj.addProperty("isCache", table.isCache);

                    tableObj.add("columns", arrColumns);
                    for (Column column : table.columns) {
                        JsonObject columnObj = new JsonObject();
                        arrColumns.add(columnObj);
                        columnObj.addProperty("name", column.name);
                        columnObj.addProperty("definition", column.definition);
                        columnObj.addProperty("comment", column.comment);
                    }
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
            }

        }
    }


}
