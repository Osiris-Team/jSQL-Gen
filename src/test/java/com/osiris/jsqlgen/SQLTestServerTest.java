package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class SQLTestServerTest {
    @Test
    void aaaa() throws ManagedProcessException, SQLException {
        SQLTestServer db = SQLTestServer.buildAndRun();
        try(Connection con = DriverManager.getConnection(db.getUrl(), "root", "")){

        }

        try(Connection conn = DriverManager.getConnection("DB_URL", "USER", "PASS");
            Statement stm = conn.createStatement();) {
            String sql = "CREATE DATABASE STUDENTS";
            stm.executeUpdate(sql);
            System.out.println("Database created successfully...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
