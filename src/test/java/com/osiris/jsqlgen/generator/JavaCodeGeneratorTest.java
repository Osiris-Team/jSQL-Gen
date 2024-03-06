package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.SQLTestServer;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.testDB.Person;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JavaCodeGeneratorTest {

    @Test
    void generate() throws Exception {
        Database db = new Database();
        db.name = "testDB";
        File dir = new File(System.getProperty("user.dir")+"/src/test/java/com/osiris/jsqlgen/testDB");
        db.javaProjectDir = dir;
        JavaCodeGenerator.generateDatabaseFile(db, new File(dir+"/Database.java"),
                "getRawDbUrlFrom(url)",
                "\"jdbc:mysql://localhost:3307/testDB\"",
                "\"testDB\"", "\"root\"", "\"\"");

        Table t = new Table();
        db.tables.add(t);
        t.name = "Person";
        t.columns.add(new Column("name").definition("TEXT NOT NULL"));
        t.columns.add(new Column("age").definition("INT NOT NULL"));
        t.columns.add(new Column("flair").definition("ENUM('COOL', 'CHILL', 'FLY') DEFAULT 'COOL'"));
        t.columns.add(new Column("lastName").definition("TEXT DEFAULT ''"));
        t.columns.add(new Column("parentAge").definition("INT DEFAULT 10"));
        t.columns.add(new Column("myblob").definition("BLOB DEFAULT ''"));
        t.isCache = true;
        t.isDebug = true;
        t.isVaadinFlowUI = true;
        t.isNoExceptions = true;
        File javaFile = new File(dir + "/" + t.name + ".java");
        javaFile.createNewFile();
        Files.writeString(javaFile.toPath(), (db.javaProjectDir != null ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                JavaCodeGenerator.generateTableFile(javaFile, t));

        System.err.println("""
                Remember that the above generated classes are source code which
                has to be recompiled at some point, thus the below tests probably run with the last compiled classes,
                not the current ones. So if encountering errors re-run this.""");

        SQLTestServer testDB = SQLTestServer.buildAndRun("testDB", 3307);

        Person john = Person.createAndAdd("John", 32);
        assertFalse(Person.whereName().is(john.name).get().isEmpty());
    }
}