package com.osiris.jsqlgen;

import com.google.gson.*;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.generator.GetTableChange;
import com.osiris.jsqlgen.generator.JavaCodeGenerator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;
import com.osiris.jsqlgen.utils.FileTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Data {
    public static final File file = new File(Main.dir + "/data.yml");
    public static final File backupDir = new File(Main.dir + "/backup");
    public static final DataJson instance;
    private static AtomicBoolean save = new AtomicBoolean(false);
    public static Gson parser = new GsonBuilder().registerTypeAdapter(File.class, new FileTypeAdapter())
            .setPrettyPrinting().create();

    public static void save(){
        save.set(true);
    }

    static {
        try{
            backupDir.mkdirs();
            synchronized (file) {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    Files.writeString(file.toPath(), "{}");
                }
                instance = parser.fromJson(new BufferedReader(new FileReader(file)), DataJson.class);
                // Check if there is a project that contains a newer version of the database (aka with more changes)
                CopyOnWriteArrayList<Database> databases = instance.databases;
                Map<Database, DBWrapper> oldAndNew = getOldAndNewDBsMap(databases, null);
                // Backup before replacing, then replace
                databases.replaceAll(dbOld -> {
                    DBWrapper dbNew = oldAndNew.get(dbOld);
                    if(dbNew != null) {
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = getFormatter();
                        File backup = new File(backupDir+"/backup-db-"+dbOld.name+"-pre-import-"+now.format(formatter)+".json");
                        try {
                            Files.writeString(backup.toPath(), parser.toJson(dbOld, Database.class));
                            System.out.println("LOADED NEWER DATABASE STRUCTURE FROM: "+dbNew.structureFile);
                            return dbNew.db;
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("FAILED TO LOAD NEWER DATABASE STRUCTURE, DUE TO FAILING TO BACKUP OLDER DATABASE STRUCTURE!");
                            return dbOld;
                        }
                    }
                    else return dbOld;
                });
                // Update names
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

    @NotNull
    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    }

    public static class DBWrapper{
        public Database db;
        public File structureFile;

        public DBWrapper(Database db, File structureFile) {
            this.db = db;
            this.structureFile = structureFile;
        }
    }

    @NotNull
    public static Map<Database, DBWrapper> getOldAndNewDBsMap(@NotNull CopyOnWriteArrayList<Database> oldDBs,
                                                             @Nullable CopyOnWriteArrayList<Database> newDBs) {
        Map<Database, DBWrapper> oldAndNew = new HashMap<>();
        for (int k = 0; k < oldDBs.size(); k++) {
            Database db = oldDBs.get(k);
            List<JavaProjectGenDir> javaProjectGenDirs = getJavaProjectGenDir(db);
            List<File> databaseStructureFiles = getDatabaseStructureFile(db, javaProjectGenDirs);
            for (File databaseStructureFile : databaseStructureFiles) {
                Database dbNew = null;
                try{
                    dbNew = parser.fromJson(new BufferedReader(new FileReader(databaseStructureFile)), Database.class);
                } catch (Exception e) {}
                if(dbNew == null && newDBs != null){
                    for (Database dbNew1 : newDBs) {
                        if(dbNew1.name.equals(db.name)) {
                            dbNew = dbNew1;
                            break;
                        }
                    }
                }
                if(dbNew == null) continue;
                CopyOnWriteArrayList<Table> tablesNew = dbNew.tables;
                CopyOnWriteArrayList<Table> tablesOld = db.tables;
                boolean isNewer = false;
                if((!tablesNew.isEmpty() && !tablesOld.isEmpty()) &&
                    (tablesNew.get(0).changes != null && tablesOld.get(0).changes == null)){
                    // Support older jsqlgen data json formats
                    isNewer = true;
                }
                if(!isNewer)
                    for (Table tNew : tablesNew) {
                        CopyOnWriteArrayList<Table> oldTables = db.tables;
                        for (Table tOld : oldTables) {
                            if (tOld.id == tNew.id) {
                                if (tOld.changes.size() > tNew.changes.size()) isNewer = true;
                                break;
                            }
                        }
                        if (isNewer) break;
                    }
                if (isNewer) {

                    // If there are missing changes add them (which might happen when importing databases generated by older jSQL-Gen versions).
                    // For example if the table contains a column, but there is no change referencing that column, then it will be added to the first
                    // Note that this should only be execute here and not for all tables, since there are issues if the user closes the app without generating
                    // which then saves the table with the change object missing.
                    for (Table t : dbNew.tables) {
                        if(t.changes.isEmpty()){
                            TableChange currentTableChange = GetTableChange.get(t, Data.instance.databases);
                            t.changes.add(currentTableChange);
                        }
                        for (Column col : t.columns) {
                            boolean isAddedOnce = false;
                            for (TableChange c : t.changes) {
                                if(c.addedColumnNames.contains(col.name)) {
                                    isAddedOnce = true;
                                    break;
                                }
                            }
                            boolean isRenamedOnce = false;
                            for (TableChange c : t.changes) {
                                if(c.newColumnNames.contains(col.name)) {
                                    isRenamedOnce = true;
                                    break;
                                }
                            }
                            if(!isAddedOnce && !isRenamedOnce){
                                TableChange firstChange = t.changes.get(0);
                                firstChange.addedColumnNames.add(col.name);
                                firstChange.addedColumnDefinitions.add(col.definition);
                                AL.warn("Failed to find column '"+col.name+"' in a change, thus added to first change.");
                            }
                        }
                    }

                    // Finally add the newer db to the list
                    oldAndNew.put(db, new DBWrapper(dbNew, databaseStructureFile));
                }
            }
        }
        return oldAndNew;
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

    @NotNull
    public static List<JavaProjectGenDir> getJavaProjectGenDir(Database db) {
        List<JavaProjectGenDir> dirs = new ArrayList<>();
        for (File javaProjectDir : db.getJavaProjectDirs()) {
            dirs.add(new JavaProjectGenDir(javaProjectDir + "/src/main/java/com/osiris/jsqlgen/" + db.name));
        }
        return dirs;
    }

    public static class JavaProjectGenDir extends File{
        public JavaProjectGenDir(@NotNull String pathname) {
            super(pathname);
        }
    }

    @NotNull
    public static List<File> getDatabaseStructureFile(Database db, List<JavaProjectGenDir> javaProjectGenDirs) {
        List<File> files = new ArrayList<>();
        for (JavaProjectGenDir dir : javaProjectGenDirs) {
            files.add(new File(dir.getParentFile() + "/" + db.name + "_structure.json"));
        }
        return files;
    }

    @NotNull
    public static File getDatabaseFile(JavaProjectGenDir javaProjectDir) {
        return new File(javaProjectDir + "/Database.java");
    }
}
