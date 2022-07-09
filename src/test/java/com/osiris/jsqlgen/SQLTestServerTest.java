package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class SQLTestServerTest {
    @Test
    void test() throws ManagedProcessException, SQLException, InterruptedException {
        SQLTestServer db = SQLTestServer.buildAndRun();
        
    }
}
