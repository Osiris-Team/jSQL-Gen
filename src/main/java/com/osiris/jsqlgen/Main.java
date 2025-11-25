package com.osiris.jsqlgen;

import com.github.mvysny.vaadinboot.VaadinBoot;
import com.osiris.desku.App;
import com.osiris.jlib.logger.AL;

import com.osiris.jsqlgen.generator.JavaCodeGenerator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;
import com.osiris.jsqlgen.generator.GetTableChange;
import com.osiris.jsqlgen.utils.AsyncFileTailReader;
import com.osiris.jsqlgen.utils.AsyncReader;
import com.osiris.jsqlgen.utils.MariaDB4jLogSilencer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static AsyncFileTailReader asyncIn;
    public static AsyncFileTailReader asyncInErr;

    public static File dir = new File(System.getProperty("user.home") + "/jSQL-Gen");
    public static File generatedDir = new File(Main.dir + "/generated");
    public static AtomicInteger idCounter = new AtomicInteger(new Config().idCounter.asInt());
    public static boolean isInDepthDebugging = false;

    //public static MainRoute mainRoute = new MainRoute();

    public static void main(String[] _args) throws Exception {
//        App.name = "jSQL-Gen";
//        App.theme = new MyTheme();
//        App.LoggerParams loggerParams = new App.LoggerParams();
//        if(Arrays.asList(_args).contains("debug")){
//            loggerParams.debug = true;
//            App.isInDepthDebugging = true;
//        }

        //App.init(null, loggerParams);
        AL.start();
        File mirrorOut = new File(System.getProperty("user.dir")+"/logs/mirror-out.log");
        File mirrorErr = new File(System.getProperty("user.dir")+"/logs/mirror-err.log");
        AL.mirrorSystemStreams(mirrorOut, mirrorErr);
        MariaDB4jLogSilencer.silenceMariaDB4jLogs();

        // DUPLICATE SYSTEM.OUT AND ASYNC-READ FROM PIPE
        asyncIn = new AsyncFileTailReader(mirrorOut, 1000);
        asyncInErr = new AsyncFileTailReader(mirrorErr, 1000);
        AL.info("DB initialized at: "+com.osiris.jsqlgen.jsqlgen.Database.url); // Init DB by static constructor

        // Update id counter if there is an imported table with larger ids
        for (Database db : Data.instance.databases) {
            for (Table t : db.tables) {
                for (Column col : t.columns) {
                    if(col.id > idCounter.get())
                        idCounter.set((int) (col.id + 1));
                }
            }
        }

        var ids = new HashSet<Long>();
        for (Database db : Data.instance.databases) {
            // If there are missing ids set them
            for (Table t : db.tables) {
                if(t.id == 0) t.id = idCounter.getAndIncrement();
                for (Column c : t.columns) {
                    if(c.id == 0) c.id = idCounter.getAndIncrement();
                }
            }

            // If there are duplicate ids, set them to a new id
            for (Table table : db.tables) {
                if(ids.contains((long) table.id)){
                    table.id = Main.idCounter.getAndIncrement();
                    AL.info("Found duplicate id, for table "+ table.name+", updated to id: "+table.id);
                } else{
                    ids.add((long) table.id); // TODO make sure table.id is also long
                }

                for (Column column : table.columns) {
                    if(ids.contains(column.id)){
                        column.id = Main.idCounter.getAndIncrement();
                        AL.info("Found duplicate id, for column "+ table.name +"."+ column.name+", updated to id: "+column.id);
                    } else{
                        ids.add(column.id);
                    }
                }
            }

            // Cache current data
            JavaCodeGenerator.oldDatabases.add(db.duplicate());
        }


        new VaadinBoot().setPort(8081).run();
        // Create and show windows
//        try{
//            App.uis.create(mainRoute);
//            AL.info("Showing dir details to user:");
//            AL.info("workingDir = " + App.workingDir);
//            AL.info("tempDir = " + App.tempDir);
//            AL.info("userDir = " + App.userDir);
//            AL.info("htmlDir = " + App.htmlDir);
//        } catch (Exception e) {
//            AL.error(e);
//        }
    }
}
