package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;

import java.sql.*;
import java.time.Instant;


public class TestConst {
    public static SQLTestServer db;
    static{
        try {
            db = SQLTestServer.buildAndRun("testDB", 3307);
        } catch (ManagedProcessException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try{
                while(true) {
                    System.out.println(Instant.now().toString());
                    System.out.println("url: "+db.getUrl()+" running: "+db.isRunning());
                    try(Connection conn = DriverManager.getConnection(db.getUrl()+"velocity", "root", "root");){
                        DBTablePrinter.printTable(conn, "FailedLogin");
                    }
                    Thread.sleep(30000);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
