package com.osiris.jsqlgen.ui;

import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.generator.GetTableChange;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;


public class LayoutDatabaseOptions extends FlexLayout { // Changed from Desku Component to Vaadin FlexLayout

    private Database currentDatabase; // Store the database instance

    public LayoutDatabaseOptions() {
        // Initial setup for FlexLayout (equivalent to Desku's childHorizontal)
        setFlexDirection(FlexDirection.ROW);
        setFlexWrap(FlexWrap.WRAP); // Allow items to wrap
        setJustifyContentMode(JustifyContentMode.START);
        setAlignItems(Alignment.START);
        getStyle().set("gap", "var(--lumo-space-m)"); // Add spacing between checkboxes
        setWidthFull(); // Take full width
        getStyle().set("overflow-x", "auto"); // Scrollable horizontally
        addClassName("layout-database-options"); // Add CSS class
    }

    // Since this is a Spring component, inject any dependencies if needed
    // The original Desku constructor took (Database database, Class<Database> databaseClass)
    // For Vaadin, we'll set the database via setValue after injection.

    public LayoutDatabaseOptions setValue(@Nullable Database db) {
        this.currentDatabase = db; // Store the current database
        removeAll(); // Clear existing components

        if (db == null) {
            return this;
        }

        Checkbox mariadb4jCheckbox = new Checkbox("MariaDB4j"); // Vaadin Checkbox
        mariadb4jCheckbox.setValue(db.isWithMariadb4j);
        mariadb4jCheckbox.addValueChangeListener(e -> {
            db.isWithMariadb4j = e.getValue();
            Data.save();
        });
        // Tooltip for Vaadin component
        mariadb4jCheckbox.setTooltipText("Expects you to have MariaDB4j 3.0.1 or LOWER added as dependency. <br>" +
                "Lower because the newer versions do not really support persistent databases. <br>" +
                "Also expects a MariaDB driver/client present.");
        add(mariadb4jCheckbox);


        Checkbox versioningCheckbox = new Checkbox("Versioning"); // Vaadin Checkbox
        versioningCheckbox.setValue(db.isVersioning);
        versioningCheckbox.addValueChangeListener(e -> {
            db.isVersioning = e.getValue();
            Data.save();
        });
        versioningCheckbox.setTooltipText("Creates the database, tables, columns if needed <br>" +
                "and also ensures any changes like renaming/adding/deleting of tables/columns are reflected in the actual database. <br>");
        add(versioningCheckbox);

        Button btnResetVersions = new Button("Reset Versions");
        btnResetVersions.setTooltipText("!!CAUTION!! This will remove all changes of all tables and create a default version 1 change object for all tables with their current columns." +
            " Meaning its as if you created the tables right now for the first time. A backup will be created." +
            " Note that you might need to delete the jsqlgen_metadata table manually (it contains version information, thus the table init code for version 1 might not be run if not deleted).");
        btnResetVersions.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnResetVersions.addClickListener(e -> {
            try {
                File backup = Data.backup(db, "-pre-reset-changes-");
                AL.info("Backup success, file: "+backup);
            } catch (Exception ex) {
                AL.warn("Failed to create backup, thus aborted reset!", ex);
                return;
            }
            try{
                for (Table t : db.tables) {
                    t.changes.clear();
                    TableChange currentTableChange = GetTableChange.get(t, Data.instance.databases);
                    t.changes.add(currentTableChange);
                }
                Data.save();
                AL.info("Reset success for db: "+db.name);
            } catch (Exception ex) {
                AL.warn(ex);
            }
        });
        add(btnResetVersions);

        return this; // Return the component itself for chaining
    }
}
