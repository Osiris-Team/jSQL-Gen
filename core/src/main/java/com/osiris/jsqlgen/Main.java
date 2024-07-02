package com.osiris.jsqlgen;

import com.osiris.desku.App;
import com.osiris.jlib.logger.AL;

import com.osiris.jsqlgen.generator.JavaCodeGenerator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static File dir = new File(System.getProperty("user.home") + "/jSQL-Gen");
    public static File generatedDir = new File(Main.dir + "/generated");
    public static AtomicInteger idCounter = new AtomicInteger(new Config().idCounter.asInt());

    public static MainRoute mainRoute = new MainRoute();

    public static void main(String[] args) {

        for (Database db : Data.instance.databases) {
            // If there are missing ids set them
            for (Table t : db.tables) {
                if(t.id == 0) t.id = idCounter.getAndIncrement();
                for (Column c : t.columns) {
                    if(c.id == 0) c.id = idCounter.getAndIncrement();
                }
            }

            // Cache current data
            JavaCodeGenerator.oldDatabases.add(db.duplicate());
        }

        App.name = "jSQL-Gen";
        App.theme = new MyTheme();
        // Create and show windows
        try{
            App.uis.create(mainRoute);
        } catch (Exception e) {
            AL.error(e);
        }
    }
}
