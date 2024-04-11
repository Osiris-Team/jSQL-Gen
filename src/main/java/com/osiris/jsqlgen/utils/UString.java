package com.osiris.jsqlgen.utils;

public class UString {

    public static String firstToUpperCase(String s) {
        return ("" + s.charAt(0)).toUpperCase() + s.substring(1);
    }

    public static String firstToLowerCase(String s) {
        return ("" + s.charAt(0)).toLowerCase() + s.substring(1);
    }

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

    /**
     * Trims and removes only the quote(s) at the first position and/or at the last position. <br>
     * Quotes that will be detected are: " ' `
     */
    public static String removeOuterQuotes(String s) {
        if (s == null || s.isEmpty()) {
            return s; // return unchanged if the string is null or empty
        }
        s = s.trim();

        char firstChar = s.charAt(0);
        char lastChar = s.charAt(s.length() - 1);

        if ((firstChar == '\'' || firstChar == '"' || firstChar == '`') &&
                (lastChar == '\'' || lastChar == '"' || lastChar == '`')) {
            // Remove the outer quotes
            return s.substring(1, s.length() - 1);
        }

        return s; // No outer quotes to remove
    }
}
