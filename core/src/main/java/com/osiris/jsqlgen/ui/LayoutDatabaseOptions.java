package com.osiris.jsqlgen.ui;

import com.osiris.desku.Statics;
import com.osiris.desku.ui.Component;
import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.model.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class LayoutDatabaseOptions extends Component<LayoutDatabaseOptions, Database> {

    public LayoutDatabaseOptions(@UnknownNullability Database database, @NotNull Class<Database> databaseClass) {
        super(database, databaseClass, "c");
        childHorizontal().scrollable(true, "100%", "fit-content");
        setValue(database);
    }

    @Override
    public LayoutDatabaseOptions setValue(@Nullable Database db) {
        removeAll();
        if(db == null) return super.setValue(db);

        add(Statics.checkbox("MariaDB4j").setValue(db.isWithMariadb4j).grow(0).onValueChange(e -> {
            db.isWithMariadb4j = e.value;
            Data.save();
        }).setTooltip("Expects you to have MariaDB4j 3.0.1 or LOWER added as dependency. <br>" +
            "Lower because the newer versions do not really support persistent databases. <br>" +
            "Also expects a MariaDB driver/client present."));

        add(Statics.checkbox("Versioning").setValue(db.isVersioning).grow(0).onValueChange(e -> {
            db.isVersioning = e.value;
            Data.save();
        }).setTooltip("Creates the database, tables, columns if needed <br>" +
            "and also ensures any changes like renaming/adding/deleting of tables/columns are reflected in the actual database. <br>"));
        return super.setValue(db);
    }
}
