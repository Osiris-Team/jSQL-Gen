package com.osiris.jsqlgen.model;

import java.util.ArrayList;

public class TableChange {
    // Table rename
    public String oldTableName = "";
    public String newTableName = "";

    // Column rename
    public ArrayList<String> oldColumnNames = new ArrayList<>();
    public ArrayList<String> newColumnNames = new ArrayList<>();
    public ArrayList<String> newColumnNames_Definitions = new ArrayList<>();

    // Column definition change
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
