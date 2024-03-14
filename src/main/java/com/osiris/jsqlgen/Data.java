package com.osiris.jsqlgen;

import com.google.gson.*;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.FileTypeAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Data {
    public static final File file = new File(Main.dir + "/data.yml");
    public static final DataJson instance;
    private static AtomicBoolean save = new AtomicBoolean(false);
    public static Gson parser = new GsonBuilder().registerTypeAdapter(File.class, new FileTypeAdapter())
            .setPrettyPrinting().create();

    public static void save(){
        save.set(true);
    }

    static {
        try{
            synchronized (file) {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    Files.writeString(file.toPath(), "{}");
                }
                instance = parser.fromJson(new BufferedReader(new FileReader(file)), DataJson.class);
                for (Database db : instance.databases) {
                    for (Table t : db.tables) {
                        for (Column col : t.columns) {
                            if(col.nameQuoted == null) col.updateName(col.name);
                        }
                    }
                }
            }
            new Thread(() -> {
                try{
                    while (true){
                        Thread.sleep(1000);
                        if(save.get()){
                            synchronized (file) {
                                if (!file.exists()) {
                                    file.getParentFile().mkdirs();
                                    file.createNewFile();
                                }
                                // Before writing, backup the existing file
                                Files.copy(file.toPath(), new File(file.getPath()+"_backup.json").toPath(),
                                        StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                                StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
                                parser.toJson(instance, sw);
                                String out = sw.toString();
                                //System.out.println(out);
                                Files.writeString(file.toPath(), out);
                            }

                            // Also update config
                            try{
                                Config c = new Config();
                                c.idCounter.setValues(Main.idCounter.get());
                                c.save();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

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
        return getDatabase(instance.databases, name);
    }

    public static Database getDatabase(List<Database> list, String name) {
        for (Database db : list) {
            if (Objects.equals(name, db.name))
                return db;
        }
        throw new NullPointerException("Failed to find database named '" + name + "' in " + Data.file);
    }

    public static void saveNow() {
        save();
        try {
            while (save.get()) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
