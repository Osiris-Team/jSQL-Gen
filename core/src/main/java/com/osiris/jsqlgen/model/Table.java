package com.osiris.jsqlgen.model;

import com.osiris.jsqlgen.Main;

import java.util.ArrayList;

public class Table {
    public int id = 0;
    public String name;
    public ArrayList<Column> columns = new ArrayList<>();
    public boolean isDebug = false;
    public boolean isNoExceptions = true;
    public boolean isCache = false;
    public boolean isVaadinFlowUI = false;
    public ArrayList<TableChange> changes = new ArrayList<>();
    /**
     * Should be added to {@link #changes} before generating the code. <br>
     * Should be always up-to-date with the latest user changes (like adding/removing/changing columns). <br>
     */
    public TableChange currentChange = new TableChange();

    public Table addIdColumn(){
        Column idColumn = new Column("id");
        idColumn.id = Main.idCounter.getAndIncrement();
        idColumn.definition = "INT AUTO_INCREMENT NOT NULL PRIMARY KEY";
        addCol(idColumn);
        return this;
    }

    public Table addCol(Column col){
        columns.add(col);

        // Update current change
        if(!currentChange.addedColumnNames.contains(col.name)){
            currentChange.addedColumnNames.add(col.name);
            currentChange.addedColumnDefinitions.add(col.definition);
            currentChange.deletedColumnNames.remove(col.name);
        }

        return this;
    }

    public Table updateCol(Column col, String oldName, String newName, String newDefinition, String newComment){

        col.updateName(newName);
        String oldDefinition = col.definition;
        col.definition = newDefinition;
        col.comment = newComment;

        // Update current change
        if(currentChange.addedColumnNames.contains(oldName)){ // brand new column
            int i = currentChange.addedColumnNames.indexOf(oldName);
            currentChange.addedColumnNames.set(i, newName);
            currentChange.addedColumnDefinitions.set(i, newDefinition);
        } else{
            // Existing column, check for changes

            // NAMES
            if(!newName.equals(oldName) && !currentChange.oldColumnNamesIds.contains(col.id)){
                currentChange.oldColumnNamesIds.add(col.id);
                currentChange.oldColumnNames.add(oldName); // Only add once if renamed multiple times to ensure correct old name is used
            }

            int i = currentChange.newColumnNames.indexOf(oldName); // Ensure latest change is only present
            if(i >= 0){
                currentChange.newColumnNames.remove(i);
                currentChange.newColumnNames_Definitions.remove(i);
            }
            if(!newName.equals(oldName)) {
                currentChange.newColumnNames.add(newName);
                currentChange.newColumnNames_Definitions.add(newDefinition);
            }

            // DEFS
            if(!newDefinition.equals(oldDefinition) && !currentChange.oldColumnDefinitionsIds.contains(col.id)){
                currentChange.oldColumnDefinitionsIds.add(col.id);
                currentChange.oldColumnDefinitions.add(oldDefinition); // Only add once if changed multiple times to ensure correct old def is used
            }

            i = currentChange.newColumnDefinitions.indexOf(oldDefinition); // Ensure latest change is only present
            if(i >= 0){
                currentChange.newColumnDefinitions.remove(i);
                currentChange.newColumnDefinitions_Names.remove(i);
            }
            if(!newDefinition.equals(oldDefinition)) {
                currentChange.newColumnDefinitions.add(newDefinition);
                currentChange.newColumnDefinitions_Names.add(newName);
            }
        }
        return this;
    }

    public Table removeCol(Column col){
        columns.remove(col);

        // Update current change
        if(!currentChange.deletedColumnNames.contains(col.name)){
            currentChange.deletedColumnNames.add(col.name);
            int i = currentChange.addedColumnNames.indexOf(col.name);
            if(i >= 0) {
                currentChange.addedColumnNames.remove(i);
                currentChange.addedColumnDefinitions.remove(i);
            }
        }
        return this;
    }

    public Table duplicate() {
        Table t = new Table();
        t.id = id;
        t.name = name;
        for (Column c : columns) {
            t.columns.add(c.duplicate());
        }
        t.isDebug = isDebug;
        t.isNoExceptions = isNoExceptions;
        t.isCache = isCache;
        t.isVaadinFlowUI = isVaadinFlowUI;
        t.changes.clear();
        t.changes.addAll(changes); // TODO is proper duplicate function required for this?
        t.currentChange = currentChange;
        return t;
    }
}
