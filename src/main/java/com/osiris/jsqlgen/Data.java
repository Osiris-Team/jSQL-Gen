package com.osiris.jsqlgen;

import com.google.gson.*;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.generator.GetTableChange;
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

    public static List<String> seriousWarnings = new CopyOnWriteArrayList<>();

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
                        try {
                            File backup = backup(dbOld, "-pre-import-");
                            var s = "IMPORTED NEWER DATABASE STRUCTURE AT LAUNCH FROM: "+dbNew.structureFile+ " (BACKUP WAS CREATED AT "+backup+" RESTART THE APP TO GET RID OF THIS WARNING)";
                            seriousWarnings.add(s);
                            System.out.println(s);
                            return dbNew.db;
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("FAILED TO IMPORT NEWER DATABASE STRUCTURE, DUE TO FAILING TO BACKUP OLDER DATABASE STRUCTURE!");
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

    public static @NotNull File backup(Database dbOld, String actionResultingInBackupName) throws IOException {
        if(actionResultingInBackupName == null || actionResultingInBackupName.isEmpty()) actionResultingInBackupName = "-";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = getFormatter();
        File backup = new File(backupDir+"/backup-db-"+ dbOld.name + actionResultingInBackupName + now.format(formatter)+".json");
        Files.writeString(backup.toPath(), parser.toJson(dbOld, Database.class));
        return backup;
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
            Database dbOld = oldDBs.get(k);
            List<JavaProjectGenDir> javaProjectGenDirs = getJavaProjectGenDir(dbOld);
            List<File> databaseStructureFiles = getDatabaseStructureFile(dbOld, javaProjectGenDirs);
            for (File databaseStructureFile : databaseStructureFiles) {

                // Parse database from java project structure file
                Database dbNew = null;
                try{
                    dbNew = parser.fromJson(new BufferedReader(new FileReader(databaseStructureFile)), Database.class);
                } catch (Exception e) {}
                if(dbNew == null && newDBs != null){
                    for (Database dbNew1 : newDBs) {
                        if(dbNew1.name.equals(dbOld.name)) {
                            dbNew = dbNew1;
                            break;
                        }
                    }
                }
                if(dbNew == null) continue;

                // Determine if the database is really newer
                CopyOnWriteArrayList<Table> tablesNew = dbNew.tables;
                CopyOnWriteArrayList<Table> tablesOld = dbOld.tables;
                boolean isDBReallyNewer = false;

                // Support older jsqlgen data json formats
                if((!tablesNew.isEmpty() && !tablesOld.isEmpty()) &&
                    (tablesNew.get(0).changes != null && tablesOld.get(0).changes == null)){
                    isDBReallyNewer = true;
                }

                // If a single new table has more changes than in old, the complete database is determined as newer
                if(!isDBReallyNewer)
                    for (Table tNew : tablesNew) {
                        CopyOnWriteArrayList<Table> oldTables = dbOld.tables;
                        for (Table tOld : oldTables) {
                            if (tOld.id == tNew.id) {
                                if (tNew.changes.size() > tOld.changes.size()) isDBReallyNewer = true;
                                break;
                            }
                        }
                        if (isDBReallyNewer) break;
                    }

                // If there are missing changes add them (which might happen when importing databases generated by older jSQL-Gen versions).
                // For example if the table contains a column, but there is no change referencing that column, then it will be added to the first change.
                // Note that this should only be execute here and not for all tables, since there are issues if the user closes the app without generating
                // which then saves the table with the change object missing.
                if (isDBReallyNewer) {
                    for (Table tNew : dbNew.tables) {
                        if(tNew.changes.isEmpty()){
                            TableChange currentTableChange = GetTableChange.get(tNew, Data.instance.databases);
                            tNew.changes.add(currentTableChange);
                        }
                        for (Column col : tNew.columns) {
                            boolean isAddedOnce = false;
                            for (TableChange c : tNew.changes) {
                                if(c.addedColumnNames.contains(col.name)) {
                                    isAddedOnce = true;
                                    break;
                                }
                            }
                            boolean isRenamedOnce = false;
                            for (TableChange c : tNew.changes) {
                                if(c.newColumnNames.contains(col.name)) {
                                    isRenamedOnce = true;
                                    break;
                                }
                            }
                            if(!isAddedOnce && !isRenamedOnce){
                                TableChange firstChange = tNew.changes.get(0);
                                firstChange.addedColumnNames.add(col.name);
                                firstChange.addedColumnDefinitions.add(col.definition);
                                AL.warn("Failed to find column '"+col.name+"' in a change, thus added to first change.");
                            }
                        }
                    }

                    // Finally add the newer db to the list
                    oldAndNew.put(dbOld, new DBWrapper(dbNew, databaseStructureFile));
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
