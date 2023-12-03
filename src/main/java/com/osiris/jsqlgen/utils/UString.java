package com.osiris.jsqlgen.utils;

public class UString {
    public static boolean startsWithWordIgnoreCase(String s, String query) {
        String firstWord = s.split(" ")[0];
        firstWord = firstWord.trim();

        if(firstWord.equalsIgnoreCase(query)) return true;
        else {
            // Support enums
            if(s.contains("(")) firstWord = s.substring(0, s.indexOf("("));
            return firstWord.equalsIgnoreCase(query);
        }
    }

    public static boolean containsIgnoreCase(String s, String query) {
        return s.toLowerCase().contains(query.toLowerCase());
    }
}
