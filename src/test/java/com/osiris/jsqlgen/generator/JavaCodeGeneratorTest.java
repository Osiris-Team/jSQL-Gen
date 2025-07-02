package com.osiris.jsqlgen.generator;

import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.DBTablePrinter;
import com.osiris.jsqlgen.Main;
import com.osiris.jsqlgen.SQLTestServer;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.testDB.Person;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class JavaCodeGeneratorTest {

    @Test
    void generate() throws Exception {
        AL.start();

        Database db = new Database();
        db.name = "testDB";
        File dir = new File(System.getProperty("user.dir")+"/src/test/java/com/osiris/jsqlgen/testDB");
        db.setJavaProjectDirs(dir+";");

        Table t = new Table().addIdColumn();
        db.tables.add(t);
        t.name = "Person";
        t.id = 1;
        // Do not add directly to columns list, because we rely on versioning
        t.addCol(new Column("name").definition("TEXT NOT NULL"));
        t.addCol(new Column("age").definition("INT NOT NULL"));
        t.addCol(new Column("flair").definition("ENUM('COOL', 'CHILL', 'FLY') DEFAULT 'COOL'"));
        t.addCol(new Column("lastName").definition("TEXT DEFAULT ''"));
        t.addCol(new Column("parentAge").definition("INT DEFAULT 10"));
        t.addCol(new Column("myblob").definition("BLOB DEFAULT ''"));
        t.addCol(new Column("timestamp").definition("TIMESTAMP DEFAULT NOW()"));
        t.isCache = true;
        t.isDebug = true;
        t.isVaadinFlowUI = true;
        t.isNoExceptions = true;

        Table t2 = new Table().addIdColumn();
        db.tables.add(t2);
        t2.id = 2;
        t2.name = "PersonOrder";
        t2.addCol(new Column("personId").definition("INT"));
        t2.addCol(new Column("name").definition("TEXT DEFAULT ''"));
        t2.addCol(new Column("time").definition("INT DEFAULT 10000"));
        t2.isCache = true;
        t2.isDebug = true;
        t2.isVaadinFlowUI = true;
        t2.isNoExceptions = true;

        // Make sure these exist here


        // Expect error
        Exception e = null;
        Column errorCol = new Column("timestamp").definition("TIMESTAMP DEFAULTAAAA NOW()");
        try{
            t.columns.add(errorCol);
            JavaCodeGenerator.prepareTables(db);
        } catch (Exception e_) {
            e = e_;
            t.columns.remove(errorCol);
        }
        assertNotNull(e); // Expect error

        // No error
        JavaCodeGenerator.prepareTables(db);

        GenDatabaseFile.s(db, new File(dir+"/Database.java"),
            "getRawDbUrlFrom(url)",
            "\"jdbc:mysql://localhost:3307/testDB\"",
            "\"testDB\"", "\"root\"", "\"\"");

        File javaFile = new File(dir + "/" + t.name + ".java");
        javaFile.createNewFile();
        Files.writeString(javaFile.toPath(), (!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                GenTableFile.s(javaFile, db.tables.get(0), db));

        File javaFile2 = new File(dir + "/" + t2.name + ".java");
        javaFile2.createNewFile();
        Files.writeString(javaFile2.toPath(), (!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                GenTableFile.s(javaFile2, db.tables.get(1), db));

        System.err.println("""
                !>>>> Remember that the above generated classes are source code which
                !>>>> has to be recompiled at some point, thus the below tests probably run with the last compiled classes,
                !>>>> not the current ones. So if encountering errors re-run this.""");

        SQLTestServer testDB = SQLTestServer.buildAndRun("testDB", 3307);
        Person.removeAll();
        assertEquals(0, Person.count());
        Person john = Person.createAndAdd("John", 32);
        assertEquals(1, john.id);
        assertFalse(Person.whereName().is(john.name).get().isEmpty());
        john.remove();
        assertEquals(0, Person.count());
        john.add();
        john.add();

        var con = com.osiris.jsqlgen.testDB.Database.getCon();
        DBTablePrinter.printTable(con, "person");
        com.osiris.jsqlgen.testDB.Database.freeCon(con);

        assertEquals(2, Person.count());
        assertEquals(3, john.id);

        john.name = "Peter";
        john.update();
        assertEquals("Peter", Person.get(john.id).name);
        //TODO Person printer thread causes the last error messages and also probably causes the test not finishing
    }
}
