package com.osiris.jsqlgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Rectangle;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.FileTypeAdapter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataJsonTest {
    @Test
    void serialize() {
        Rectangle window = new Rectangle(10, 10, 600, 400);
        CopyOnWriteArrayList<Database> databases = new CopyOnWriteArrayList<>();
        Database db = new Database();
        databases.add(db);
        db.name = "mydatabase";
        db.tables.add(new Table().addIdColumn());

        // Java -> Json
        DataJson dataJson = new DataJson();
        dataJson.window = window;
        dataJson.databases = databases;
        StringWriter sw = new StringWriter();
        Gson gson = new GsonBuilder().registerTypeAdapter(File.class, new FileTypeAdapter()).setPrettyPrinting().create();
        gson.toJson(dataJson, sw);
        String out = sw.toString();
        System.out.println(out);

        // Json -> Java
        dataJson = gson.fromJson(out, DataJson.class);
        assertNotNull(dataJson.window);
        assertNotNull(dataJson.databases);
        assertTrue(dataJson.window.x == 10);
        assertTrue(dataJson.window.y == 10);
    }
}