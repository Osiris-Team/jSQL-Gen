package com.osiris.jsqlgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;


import java.util.ArrayList;
import java.util.List;

/**
 * Popup dialog for exporting Database -> Table -> Column
 * with hierarchical checkboxes.
 */
public class DatabaseExportDialog extends Dialog {
    private final Database database;
    private final TextArea jsonOutput = new TextArea("Generated JSON");

    private final List<Checkbox> dbFieldCheckboxes = new ArrayList<>();
    private final List<TableBlock> tableBlocks = new ArrayList<>();

    // Global column field checkboxes (Database layer)
    private final Checkbox includeAllColIds = new Checkbox("Include all column IDs", true);
    private final Checkbox includeAllColNames = new Checkbox("Include all column names", true);
    private final Checkbox includeAllColDefs = new Checkbox("Include all column definitions", true);
    private final Checkbox includeAllColComments = new Checkbox("Include all column comments", true);

    public DatabaseExportDialog(Database database) {
        this.database = database;

        setWidth("950px");
        setHeight("750px");

        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();

        rootLayout.add(new H3("Database Export"));

        // === Database level checkboxes ===
        VerticalLayout dbLayout = new VerticalLayout();
        dbLayout.setPadding(false);
        dbLayout.setSpacing(false);

        Checkbox dbName = new Checkbox("Database.name", true);
        Checkbox dbVersioning = new Checkbox("Database.isVersioning", true);
        Checkbox dbMariadb = new Checkbox("Database.isWithMariadb4j", true);
        Checkbox dbTables = new Checkbox("Database.tables", true);

        dbFieldCheckboxes.add(dbName);
        dbFieldCheckboxes.add(dbVersioning);
        dbFieldCheckboxes.add(dbMariadb);
        dbFieldCheckboxes.add(dbTables);

        dbLayout.add(dbName, dbVersioning, dbMariadb, dbTables);

        // Add global column field checkboxes
        dbLayout.add(includeAllColIds, includeAllColNames, includeAllColDefs, includeAllColComments);

        rootLayout.add(dbLayout);

        // === Tables level ===
        if (database.tables != null && !database.tables.isEmpty()) {
            for (Table table : database.tables) {
                TableBlock block = new TableBlock(table);
                tableBlocks.add(block);

                // tie parent dbTables to control all table checkboxes
                dbTables.addValueChangeListener(e -> block.setEnabledRecursively(e.getValue()));

                // tie global db column checkboxes to each table
                includeAllColIds.addValueChangeListener(e -> block.setColIdEnabled(e.getValue()));
                includeAllColNames.addValueChangeListener(e -> block.setColNameEnabled(e.getValue()));
                includeAllColDefs.addValueChangeListener(e -> block.setColDefEnabled(e.getValue()));
                includeAllColComments.addValueChangeListener(e -> block.setColCommentEnabled(e.getValue()));

                rootLayout.add(block);
            }
        }

        // === Output section ===
        Button generateBtn = new Button("Generate JSON", e -> generateJson());
        Button closeBtn = new Button("Close", e -> close());

        jsonOutput.setWidthFull();
        jsonOutput.setHeight("300px");
        jsonOutput.setReadOnly(true);

        rootLayout.add(generateBtn, jsonOutput, closeBtn);

        add(rootLayout);
    }

    private void generateJson() {
        try {
            JsonObject dbObj = new JsonObject();

            for (Checkbox cb : dbFieldCheckboxes) {
                if (!cb.getValue()) continue;

                switch (cb.getLabel()) {
                    case "Database.name":
                        dbObj.addProperty("name", database.name);
                        break;
                    case "Database.isVersioning":
                        dbObj.addProperty("isVersioning", database.isVersioning);
                        break;
                    case "Database.isWithMariadb4j":
                        dbObj.addProperty("isWithMariadb4j", database.isWithMariadb4j);
                        break;
                    case "Database.tables":
                        JsonArray tablesArr = new JsonArray();
                        for (TableBlock tb : tableBlocks) {
                            JsonObject tObj = tb.toJson();
                            if (tObj != null) tablesArr.add(tObj);
                        }
                        dbObj.add("tables", tablesArr);
                        break;
                }
            }

            Gson pretty = new GsonBuilder().setPrettyPrinting().create();
            jsonOutput.setValue(pretty.toJson(dbObj));

        } catch (Exception ex) {
            jsonOutput.setValue("Error generating JSON: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * UI block for a Table and its columns.
     */
    private static class TableBlock extends VerticalLayout {
        private final Table table;
        private final Checkbox tableCheckbox;
        private final Checkbox idCheckbox;
        private final Checkbox nameCheckbox;
        private final Checkbox colsCheckbox;

        // Table-level column field toggles
        private final Checkbox includeColIds = new Checkbox("Include column IDs", true);
        private final Checkbox includeColNames = new Checkbox("Include column names", true);
        private final Checkbox includeColDefs = new Checkbox("Include column definitions", true);
        private final Checkbox includeColComments = new Checkbox("Include column comments", true);

        private final List<ColumnBlock> colBlocks = new ArrayList<>();

        public TableBlock(Table table) {
            this.table = table;
            setPadding(false);
            setSpacing(false);
            getStyle().set("border-left", "2px solid #ccc");
            getStyle().set("margin-left", "10px");
            getStyle().set("padding-left", "10px");

            tableCheckbox = new Checkbox("Table " + table.name, true);
            idCheckbox = new Checkbox("id", true);
            nameCheckbox = new Checkbox("name", true);
            colsCheckbox = new Checkbox("columns", true);

            add(tableCheckbox, idCheckbox, nameCheckbox, colsCheckbox);
            add(includeColIds, includeColNames, includeColDefs, includeColComments);

            // columns
            if (table.columns != null && !table.columns.isEmpty()) {
                for (Column col : table.columns) {
                    ColumnBlock cb = new ColumnBlock(col);
                    colBlocks.add(cb);

                    // table->columns check controls all column blocks
                    colsCheckbox.addValueChangeListener(e -> cb.setEnabled(e.getValue()));

                    // tie table-level column toggles
                    includeColIds.addValueChangeListener(e -> cb.setIdEnabled(e.getValue()));
                    includeColNames.addValueChangeListener(e -> cb.setNameEnabled(e.getValue()));
                    includeColDefs.addValueChangeListener(e -> cb.setDefEnabled(e.getValue()));
                    includeColComments.addValueChangeListener(e -> cb.setCommentEnabled(e.getValue()));

                    add(cb);
                }
            }

            // table parent controls all
            tableCheckbox.addValueChangeListener(e -> setEnabledRecursively(e.getValue()));
        }

        public void setEnabledRecursively(boolean enabled) {
            idCheckbox.setValue(enabled);
            nameCheckbox.setValue(enabled);
            colsCheckbox.setValue(enabled);
            includeColIds.setValue(enabled);
            includeColNames.setValue(enabled);
            includeColDefs.setValue(enabled);
            includeColComments.setValue(enabled);
            for (ColumnBlock cb : colBlocks) {
                cb.setEnabled(enabled);
            }
        }

        public void setColIdEnabled(boolean enabled) {
            includeColIds.setValue(enabled);
        }

        public void setColNameEnabled(boolean enabled) {
            includeColNames.setValue(enabled);
        }

        public void setColDefEnabled(boolean enabled) {
            includeColDefs.setValue(enabled);
        }

        public void setColCommentEnabled(boolean enabled) {
            includeColComments.setValue(enabled);
        }

        public JsonObject toJson() {
            if (!tableCheckbox.getValue()) return null;

            JsonObject obj = new JsonObject();
            if (idCheckbox.getValue()) obj.addProperty("id", table.id);
            if (nameCheckbox.getValue()) obj.addProperty("name", table.name);

            if (colsCheckbox.getValue()) {
                JsonArray colsArr = new JsonArray();
                for (ColumnBlock cb : colBlocks) {
                    JsonObject cObj = cb.toJson();
                    if (cObj != null) colsArr.add(cObj);
                }
                obj.add("columns", colsArr);
            }
            return obj;
        }
    }

    /**
     * UI block for a Column (final layer).
     */
    private static class ColumnBlock extends VerticalLayout {
        private final Column column;
        private final Checkbox colCheckbox;
        private final Checkbox idCheckbox;
        private final Checkbox nameCheckbox;
        private final Checkbox defCheckbox;
        private final Checkbox commentCheckbox;

        public ColumnBlock(Column column) {
            this.column = column;
            setPadding(false);
            setSpacing(false);
            getStyle().set("border-left", "2px dotted #ddd");
            getStyle().set("margin-left", "10px");
            getStyle().set("padding-left", "10px");

            colCheckbox = new Checkbox("Column " + column.name, true);
            idCheckbox = new Checkbox("id", true);
            nameCheckbox = new Checkbox("name", true);
            defCheckbox = new Checkbox("definition", true);
            commentCheckbox = new Checkbox("comment", true);

            add(colCheckbox, idCheckbox, nameCheckbox, defCheckbox, commentCheckbox);
        }

        public void setEnabled(boolean enabled) {
            colCheckbox.setValue(enabled);
            idCheckbox.setValue(enabled);
            nameCheckbox.setValue(enabled);
            defCheckbox.setValue(enabled);
            commentCheckbox.setValue(enabled);
        }

        public void setIdEnabled(boolean enabled) {
            idCheckbox.setValue(enabled);
        }

        public void setNameEnabled(boolean enabled) {
            nameCheckbox.setValue(enabled);
        }

        public void setDefEnabled(boolean enabled) {
            defCheckbox.setValue(enabled);
        }

        public void setCommentEnabled(boolean enabled) {
            commentCheckbox.setValue(enabled);
        }

        public JsonObject toJson() {
            if (!colCheckbox.getValue()) return null;

            JsonObject obj = new JsonObject();
            if (idCheckbox.getValue()) obj.addProperty("id", column.id);
            if (nameCheckbox.getValue()) obj.addProperty("name", column.name);
            if (defCheckbox.getValue() && column.definition != null) obj.addProperty("definition", column.definition);
            if (commentCheckbox.getValue() && column.comment != null) obj.addProperty("comment", column.comment);
            return obj;
        }
    }
}
