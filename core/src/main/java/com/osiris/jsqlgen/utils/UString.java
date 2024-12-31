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

    /**
     * Extracts and returns the content within the outermost quotes (if present). <br>
     * The function does not require the string to start or end with a quote. <br>
     * It removes everything before the first quote (inclusive) and everything after the last quote (inclusive). <br>
     * Quotes that will be detected are: " ' ` <br>
     *
     * Examples: <br>
     * - Input: hello 'world'! -> Output: world <br>
     * - Input: say "hello" -> Output: hello <br>
     * - Input: no quotes -> Output: null <br>
     *
     * @param s the input string to process
     * @return the content within the outermost quotes, or null if no valid quotes are found
     */
    public static String getContentWithinQuotes(String s) {
        if (s == null || s.isEmpty()) {
            return null; // No content if the string is null or empty
        }
        s = s.trim();

        int firstQuoteIndex = -1;
        int lastQuoteIndex = -1;

        // Find the first quote
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                firstQuoteIndex = i;
                break;
            }
        }

        // Find the last quote
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c == '\'' || c == '"' || c == '`') {
                lastQuoteIndex = i;
                break;
            }
        }

        // If both quotes are found and valid, extract the content
        if (firstQuoteIndex != -1 && lastQuoteIndex != -1 && firstQuoteIndex < lastQuoteIndex) {
            return s.substring(firstQuoteIndex + 1, lastQuoteIndex);
        }

        return null; // No valid quote pairs found
    }

    /**
     * Inserts a string into another string at a specific position.
     *
     * @param original The original string.
     * @param position The position where the string should be inserted (0-based index).
     * @param toInsert The string to insert.
     * @return The resulting string after insertion.
     * @throws IllegalArgumentException if the position is out of bounds.
     */
    public static String insertAt(String original, int position, String toInsert) {
        if (original == null || toInsert == null) {
            throw new IllegalArgumentException("Neither the original string nor the string to insert can be null.");
        }
        if (position < 0 || position > original.length()) {
            throw new IllegalArgumentException("Position out of bounds: " + position);
        }

        return original.substring(0, position) + toInsert + original.substring(position);
    }
}
