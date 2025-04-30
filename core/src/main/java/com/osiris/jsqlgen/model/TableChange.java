package com.osiris.jsqlgen.model;

import java.util.ArrayList;

public class TableChange {
    // Table rename
    public String oldTableName = "";
    public String newTableName = "";

    // Column rename
    public ArrayList<Long> oldColumnNamesIds = new ArrayList<>();
    public ArrayList<String> oldColumnNames = new ArrayList<>();
    public ArrayList<String> newColumnNames = new ArrayList<>();
    public ArrayList<String> newColumnNames_Definitions = new ArrayList<>();

    // Column definition change
    public ArrayList<Long> oldColumnDefinitionsIds = new ArrayList<>();
    public ArrayList<String> oldColumnDefinitions = new ArrayList<>();
    public ArrayList<String> newColumnDefinitions = new ArrayList<>();
    public ArrayList<String> newColumnDefinitions_Names = new ArrayList<>();

    // Deleted column
    public transient ArrayList<Long> deletedColumnIds = new ArrayList<>();
    public ArrayList<String> deletedColumnNames = new ArrayList<>();

    // Added column
    public ArrayList<String> addedColumnNames = new ArrayList<>();
    public ArrayList<String> addedColumnDefinitions = new ArrayList<>();

    public boolean hasChanges(){
        return !oldTableName.equals(newTableName) ||
                !oldColumnNames.isEmpty() ||
                !oldColumnDefinitions.isEmpty() ||
                !deletedColumnNames.isEmpty() ||
                !addedColumnNames.isEmpty() ||
                !addedColumnDefinitions.isEmpty();
    }
}
//
// Lessons
//
// To prevent something like this:
//               "oldColumnDefinitions": [
//                "TEXT NOT NULL",
//                "TEXT DEFAULT ''",
//                "TEXT DEFAULT ''",
// -> application restart happens here, thus resulting in transient oldColumnDefinitionsIds being cleared and causing additional unwanted entries:
//                "TEXT NOT NULL CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
//                "TEXT DEFAULT '' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
//                "TEXT DEFAULT '' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
//              ],
//              "newColumnDefinitions": [
//                "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL ",
//                "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT ''",
//                "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT ''"
//              ],
//              "newColumnDefinitions_Names": [
//                "name",
//                "address",
//                "city"
//              ],
// => thus oldColumnDefinitionsIds cannot be transient
