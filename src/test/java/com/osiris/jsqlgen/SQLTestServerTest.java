package com.osiris.jsqlgen;

import ch.vorburger.exec.ManagedProcessException;
import org.junit.jupiter.api.Test;

public class SQLTestServerTest {
    @Test
    void test() throws ManagedProcessException {
        SQLTestServer.buildAndRun();
    }
}
