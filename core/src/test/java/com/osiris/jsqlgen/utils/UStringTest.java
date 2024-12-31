package com.osiris.jsqlgen.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UStringTest {

    @Test
    void getContentWithinQuotes() {
        String actual = "hello there`";
        String s = "DAMN '"+actual+"'";
        assertEquals(actual, UString.getContentWithinQuotes(s));
        s = "`"+actual+"`";
        assertEquals(actual, UString.getContentWithinQuotes(s));
        s = "\""+actual+"\"";
        assertEquals(actual, UString.getContentWithinQuotes(s));
    }
}
