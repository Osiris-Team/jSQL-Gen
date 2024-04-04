package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetTableChange {
    @NotNull
    public static TableChange get(Table t, List<Database> oldDatabases) {
        Table oldT = null;
        for (Database oldDb : oldDatabases) {
            for (Table oldT_ : oldDb.tables) {
                if(oldT_.id == t.id) {
                    oldT = oldT_;
                    break;
                }
            }
        }
        // Generate current table change
        TableChange newChange = new TableChange();

        if(oldT == null || t.changes.isEmpty()){
            // Means this is a brand new table, or a table without the initial change to create it
            newChange.oldTableName = t.name;
            newChange.newTableName = t.name;
            for (Column c : t.columns) {
                newChange.addedColumnNames.add(c.name);
                newChange.addedColumnDefinitions.add(c.definition);
            }
        }
        else {
            if(!t.name.equals(oldT.name)){
                newChange.newTableName = t.name;
                newChange.oldTableName = oldT.name;
            }

            for (Column c : t.columns) {
                Column oldC = null;
                for (Column oldC_ : oldT.columns) {
                    if(oldC_.id == c.id){
                        oldC = oldC_;
                        break;
                    }
                }
                if(oldC == null){
                    // New column
                    newChange.addedColumnNames.add(c.name);
                    newChange.addedColumnDefinitions.add(c.definition);
                } else{
                    // Existing column, check for changes
                    if(!c.name.equals(oldC.name)) {
                        newChange.newColumnNames.add(c.name);
                        newChange.newColumnNames_Definitions.add(c.definition);
                        newChange.oldColumnNames.add(oldC.name);
                    }
                    if(!c.definition.equals(oldC.definition)) {
                        newChange.newColumnDefinitions.add(c.definition);
                        newChange.oldColumnDefinitions.add(oldC.definition);
                        newChange.newColumnDefinitions_Names.add(c.name);
                    }
                }
            }

            // Check for column deletions
            for (Column oldC : oldT.columns) {
                boolean stillExists = false;
                for (Column newC : t.columns) {
                    if(oldC.id == newC.id) {
                        stillExists = true;
                        break;
                    }
                }
                if(!stillExists) newChange.deletedColumnNames.add(oldC.name);
            }
        }

        if(newChange.newTableName.isEmpty()) newChange.newTableName = t.name;
        if(newChange.oldTableName.isEmpty()) newChange.oldTableName = t.name;
        return newChange;
    }
}
