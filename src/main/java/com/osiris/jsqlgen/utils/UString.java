package com.osiris.jsqlgen.utils;

public class UString {
    public static boolean startsWithIgnoreCase(String s, String query) {
        return s.toLowerCase().startsWith(query.toLowerCase());
    }

    public static boolean containsIgnoreCase(String s, String query) {
        return s.toLowerCase().contains(query.toLowerCase());
    }
}
