package com.osiris.jsqlgen.utils;

public class UString {
    public static boolean startsWithWordIgnoreCase(String s, String query) {
        String firstWord;
        if(s.contains("(")) firstWord = s.substring(0, s.indexOf("(")); // Support enums
        else firstWord = s.split(" ")[0];

        firstWord = firstWord.trim();

        return firstWord.equalsIgnoreCase(query);
    }

    public static boolean containsIgnoreCase(String s, String query) {
        return s.toLowerCase().contains(query.toLowerCase());
    }
}
