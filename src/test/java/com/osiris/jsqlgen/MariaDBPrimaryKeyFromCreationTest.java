package com.osiris.jsqlgen;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.jupiter.api.*;

import java.io.EOFException;
import java.sql.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MariaDBPrimaryKeyFromCreationTest {

    private DB db;
    private Connection conn;

    @BeforeAll
    public void startDatabase() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        db = DB.newEmbeddedDB(config.build());
        db.start();
        db.createDB("testdb");

        conn = DriverManager.getConnection("jdbc:mysql://localhost:" + config.getPort() + "/testdb", "root", "");
    }

    @AfterAll
    public void stopDatabase() throws Exception {
        if (conn != null) conn.close();
        if (db != null) db.stop();
    }

    @Test
    public void testCreatePrimaryKeyImmediately() throws Exception {
        try{
            Statement stmt = conn.createStatement();

            // 1. Create table with 'id'
            stmt.execute("CREATE TABLE test_table (id INT, example TEXT) "); // If id defined as PRIMARY KEY, the zero insertion below will be turned into a 1 automatically which is insane
            stmt.execute("INSERT INTO test_table (id, example) VALUES (0, 'zero'), (11, 'first'), (12, 'second'), (13, 'third')");

            // This fails with: Multiple primary key defined, nah this works without mentioning PRIMARY KEY, wtf?? and the primary key attribute isn't lost
            stmt.execute("SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';");
            stmt.execute("ALTER TABLE test_table MODIFY COLUMN id INT NOT NULL PRIMARY KEY AUTO_INCREMENT");
            stmt.execute("SET SESSION sql_mode='';");

//            // 2. Add 'newId' as the new id column with AUTO_INCREMENT, values will be set automatically on creation
//            stmt.execute("ALTER TABLE test_table ADD COLUMN newId INT NOT NULL UNIQUE AUTO_INCREMENT"); // replace PRIMARY KEY with UNIQUE
//
//            // 3. Copy 'id' values to 'newId'
//            stmt.execute("UPDATE test_table SET newId = id");
//
//            // 4. Drop old 'id' column
//            stmt.execute("ALTER TABLE test_table DROP COLUMN id");
//
//            // 5. Rename 'newId' to 'id'
//            stmt.execute("ALTER TABLE test_table CHANGE COLUMN newId id INT NOT NULL AUTO_INCREMENT PRIMARY KEY"); // Set UNIQUE back to PRIMARY KEY

            // 6. Verify result
            ResultSet rs = stmt.executeQuery("SHOW KEYS FROM test_table WHERE Key_name = 'PRIMARY'");
            Assertions.assertTrue(rs.next(), "Primary key should exist on id");

            rs = stmt.executeQuery("SELECT id FROM test_table ORDER BY id");
            int expected = 11;
            while (rs.next()) {
                Assertions.assertEquals(expected, rs.getInt("id"));
                expected++;
            }

            // 2. Test inserting new rows after modification
            stmt.execute("INSERT INTO test_table (example) VALUES ('fourth')"); // id should auto-increment
            stmt.execute("INSERT INTO test_table (example) VALUES ('fifth')"); // id should auto-increment

            // 3. Test update operation
            stmt.execute("UPDATE test_table SET example = 'updated second' WHERE id = 12");

            // 4. Test delete operation
            stmt.execute("DELETE FROM test_table WHERE id = 13");

            // 5. Verify results
            rs = stmt.executeQuery("SHOW KEYS FROM test_table WHERE Key_name = 'PRIMARY'");
            Assertions.assertTrue(rs.next(), "Primary key should exist on id");

            // Verify the remaining data
            rs = stmt.executeQuery("SELECT id, example FROM test_table ORDER BY id");
            int[] expectedIds = {11, 12, 14, 15}; // 13 was deleted, 14 and 15 are new auto-incremented values
            String[] expectedExamples = {"first", "updated second", "fourth", "fifth"};

            int i = 0;
            while (rs.next()) {
                Assertions.assertEquals(expectedIds[i], rs.getInt("id"));
                Assertions.assertEquals(expectedExamples[i], rs.getString("example"));
                i++;
            }
            Assertions.assertEquals(4, i, "Should have 4 rows remaining");

            stmt.close();
        } catch (Throwable e) {
            DBTablePrinter.printTable(conn, "test_table");
            throw e;
        }
    }

}
