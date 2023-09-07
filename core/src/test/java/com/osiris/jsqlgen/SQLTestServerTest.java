package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.Instant;


public class SQLTestServerTest {
    @Test
    void test() throws ManagedProcessException, SQLException, InterruptedException {
        SQLTestServer db = SQLTestServer.buildAndRun();
        while(true) {
            System.out.println(Instant.now().toString());
            System.out.println("url: "+db.getUrl()+" running: "+db.isRunning());
            try(Connection conn = DriverManager.getConnection(db.getUrl()+"velocity", "root", "root");){
                DBTablePrinter.printTable(conn, "FailedLogin");
            }
            Thread.sleep(30000);
        }
    }
}
