package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class SQLTestServerTest {
    @Test
    void test() throws ManagedProcessException, SQLException {
        SQLTestServer db = SQLTestServer.buildAndRun();
        System.out.println(db.getUrl());
        try(Connection con = DriverManager.getConnection(db.getUrl(), "root", "");
            PreparedStatement s = con.prepareStatement("CREATE DATABASE IF NOT EXISTS `test`")){
            s.executeUpdate();
        }
    }
}
