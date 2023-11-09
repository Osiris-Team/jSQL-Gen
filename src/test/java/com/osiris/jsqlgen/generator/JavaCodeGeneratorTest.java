package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

class JavaCodeGeneratorTest {

    @Test
    void generate() throws Exception {
        Database db = new Database();
        db.name = "testDB";
        File dir = new File(System.getProperty("user.dir")+"/src/test/java/com/osiris/jsqlgen/testDB");
        db.javaProjectDir = dir;
        JavaCodeGenerator.generateDatabaseFile(db, new File(dir+"/Database.java"), "\"raw-url\"", "\"url\"", "testDB", "\"username\"", "\"password\"");

        Table t = new Table();
        db.tables.add(t);
        t.name = "Person";
        t.columns.add(new Column("name").definition("TEXT NOT NULL"));
        t.columns.add(new Column("age").definition("INT NOT NULL"));
        t.isCache = true;
        File javaFile = new File(dir + "/" + t.name + ".java");
        javaFile.createNewFile();
        Files.writeString(javaFile.toPath(), (db.javaProjectDir != null ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                JavaCodeGenerator.generateTableFile(javaFile, t));
    }
}