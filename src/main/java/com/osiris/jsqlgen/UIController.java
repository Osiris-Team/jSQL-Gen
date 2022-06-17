package com.osiris.jsqlgen;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UIController {
    @FXML
    private TabPane rootPane;
    @FXML
    private TextArea txtLogs;
    @FXML
    private TextField dbName;
    @FXML
    private ChoiceBox<String> choiceTable;
    @FXML
    private ListView<VBox> listTables;
    @FXML
    private TabPane tabsCode;

    public UIController() {
        UIApplication.asyncIn.listeners.add(line -> {
            Platform.runLater(() -> {
                txtLogs.setText(txtLogs.getText() + line+"\n");
            });
        });
        UIApplication.asyncInErr.listeners.add(line -> {
            Platform.runLater(() -> {
                txtLogs.setText(txtLogs.getText() + "[!] "+line+"\n");
            });
        });
        System.out.println("Registered log listener.");
        System.out.println("Initialised jSQL-Gen successfully!");

        Platform.runLater(() -> {
            try{
                choiceTable.setOnAction(event -> { // value changed event
                    try {
                        changeDatabase(choiceTable.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                updateChoiceDatabase();
                List<Database> databases = Data.fetchDatabases();
                if(!databases.isEmpty()){
                    choiceTable.setValue(databases.get(0).name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void changeDatabase(String dbName) throws IOException {
        if(dbName==null || dbName.strip().isEmpty()){
            System.err.println("Provided database name cannot be null or empty!");
            return;
        }
        List<Database> databases = Data.fetchDatabases();
        Database database = null;
        for (Database db : databases) {
            if(db.name.equals(dbName)){
                database = db;
                break;
            }
        }
        if(database==null){
            System.err.println("Failed to find '"+dbName+"' database in "+Data.file);
            return;
        }
        updateTablesList(dbName);
    }

    public void updateChoiceDatabase() throws  IOException {
        List<Database> databases = Data.fetchDatabases();
        ObservableList<String> list = null;
        String lastValue = choiceTable.getValue();
        if(choiceTable.getItems() != null) list = choiceTable.getItems();
        else list = FXCollections.observableArrayList();
        list.clear();
        for (Database db : databases) {
            list.add(db.name);
        }
        choiceTable.setItems(list);
        if(lastValue!=null && !lastValue.strip().isEmpty())
            choiceTable.setValue(lastValue);
    }

    @FXML
    protected void addDatabase() throws  IOException {
        if(dbName.getText() == null || dbName.getText().strip().isEmpty()){
            System.err.println("Database name cannot be null or empty!");
            return;
        }
        List<Database> updatedList = Data.fetchDatabases();
        Database db = new Database();
        db.name = dbName.getText();
        updatedList.add(db);
        Data.updateDatabases(updatedList);
        updateChoiceDatabase();
        System.out.println("Successfully added new database named '"+db.name+"'.");
    }

    @FXML
    public void showData() throws IOException {
        UtilsFiles.showInFileManager(Data.file);
        System.out.println("Showing file: "+Data.file);
    }

    @FXML
    public void generateCode() throws IOException {
        System.out.println("Generating code...");
        List<File> files = generateCode(Collections.singletonList(Data.findDatabase(Data.fetchDatabases(), choiceTable.getValue())),
                new File(Data.dir+"/generated"), true);
        System.out.println("Generated code/files: ");
        for (File f : files) {
            System.out.println(f);
        }
        tabsCode.getTabs().clear();
        for (File f : files) {
            Tab tab = new Tab();
            tabsCode.getTabs().add(tab);
            TextArea txtCode = new TextArea();
            tab.setContent(txtCode);
            tab.setText(f.getName());
            txtCode.setText(Files.readString(f.toPath()));
        }
    }


    /**
     * Returns a list of files (.java) that were generated.
     */
    public List<File> generateCode(List<Database> databases, File outputDir, boolean tablesInOneFile) throws IOException {
        Objects.requireNonNull(databases);
        Objects.requireNonNull(outputDir);
        if(outputDir.isFile())
            throw new IllegalArgumentException("Outputdir file must be directory! "+outputDir);
        outputDir.mkdirs();

        List<File> files = new ArrayList<>();
        for (Database db : databases) {
            File dir = new File(outputDir+"/"+db.name);
            dir.mkdirs();
            File databaseFile = new File(dir+"/Database.java");
            databaseFile.createNewFile();
            Files.writeString(databaseFile.toPath(), "public class Database{\n" +
                    "public static String url;\n" +
                    "public static String username;\n" +
                    "public static String password;\n" +
                    "}\n");
            files.add(databaseFile);
            for (Table t : db.tables) {
                String tNameQuoted = "`"+t.name+"`";
                File javaFile = new File(dir+"/"+t.name+".java");
                javaFile.createNewFile();

                StringBuilder importsBuilder = new StringBuilder();
                importsBuilder.append("import java.util.List;\n" +
                        "import java.util.ArrayList;\n" +
                        "import java.sql.*;\n");
                importsBuilder.append("\n");

                StringBuilder classContentBuilder = new StringBuilder();
                classContentBuilder.append("public class "+t.name+"{\n"); // Open class
                classContentBuilder.append("public "+t.name+"(){}\n"); // Public constructor with no params
                classContentBuilder.append("private static java.sql.Connection con;\n");
                classContentBuilder.append("private static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);\n");
                classContentBuilder.append("static {\n" +
                        "try{\n" +
                        "con = java.sql.DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
                        "try (Statement s = con.createStatement()) {\n" +
                        "s.executeUpdate(\"CREATE TABLE IF NOT EXISTS "+ tNameQuoted +" ("+t.columns.get(0).name // EXPECTS ID
                        +" "+t.columns.get(0).definition+")\");\n" );
                for (Column col : t.columns) {
                    classContentBuilder.append("s.executeUpdate(\"ALTER TABLE "+tNameQuoted+" ADD COLUMN IF NOT EXISTS "+col.name+" "+col.definition+"\");\n");
                    classContentBuilder.append("s.executeUpdate(\"ALTER TABLE "+tNameQuoted+" MODIFY IF EXISTS "+col.name+" "+col.definition+"\");\n");
                }
                classContentBuilder.append(
                        "}\n" +
                        "" +
                        "try (PreparedStatement ps = con.prepareStatement(\"SELECT id FROM "+tNameQuoted+" ORDER BY id DESC LIMIT 1\")) {\n" +
                        "ResultSet rs = ps.executeQuery();\n" +
                        "if (rs.next()) idCounter.set(rs.getInt(1));\n" +
                        "}\n" +
                        "}\n" +
                        "catch(Exception e){ throw new RuntimeException(e); }\n" +
                        "}\n");
                // CREATE FIELDS AKA COLUMNS:
                for (Column col : t.columns) {
                    CoolType type = CoolType.findBySQLType(col.definition);
                    if(type==null){
                        System.err.println("Failed to generate code, because failed to find matching java type of definition '"+col.definition
                                +"'. Make sure that the data type is the first word in your definition and that its a supported type by jSQL-Gen.");
                        return files;
                    }
                    classContentBuilder.append("" +
                            "/**\n" +
                            "Database field/value. <br>\n" +
                            (col.comment != null ? (col.comment+"\n") : "") +
                            "*/\n" +
                            "public "+type.inJava+" "+col.name+";\n");
                }

                // CREATE CREATE METHOD:
                classContentBuilder.append("" +
                        "/**\n" +
                        "Increments the id and sets it for this object (basically reserves a space in the database).\n" +
                        "@return object with latest id. Should be added to the database next by you.\n" +
                        "*/\n" +
                        "public static "+t.name+" create() {\n" +
                        "" + t.name +" obj = new "+t.name+"();\n"+
                        "obj.id = idCounter.incrementAndGet();\n"+
                        "return obj;\n");
                classContentBuilder.append("}\n\n"); // Close create method

                // CREATE GET METHOD:
                classContentBuilder.append("" +
                        "public static List<"+t.name+"> get() throws Exception {return get(null);}\n" +
                        "/**\n" +
                        "@return a list containing only objects that match the provided SQL WHERE statement.\n" +
                        "if that statement is null, returns all the contents of this table.\n" +
                        "*/\n" +
                        "public static List<"+t.name+"> get(String where) throws Exception {\n" +
                        "List<"+t.name+"> list = new ArrayList<>();\n" +
                        "try (PreparedStatement ps = con.prepareStatement(\n" +
                        "                \"SELECT ");
                for (int i = 0; i < t.columns.size() - 1; i++) {
                    classContentBuilder.append(t.columns.get(i).name+",");
                }
                classContentBuilder.append(t.columns.get(t.columns.size()-1).name);
                classContentBuilder.append(
                        "\" +\n" +
                        "\" FROM "+tNameQuoted+"\" +\n" +
                        "(where != null ? (\"WHERE \"+where) : \"\"))) {\n" + // Open try/catch
                        "ResultSet rs = ps.executeQuery();\n" +
                        "while (rs.next()) {\n" + // Open while
                                ""+t.name +" obj = new "+t.name+"();\n" +
                                "list.add(obj);\n");
                for (int i = 0; i < t.columns.size(); i++) {
                    Column c = t.columns.get(i);
                    CoolType type = CoolType.findBySQLType(c.definition); // TODO create map with columns/types to only do this once
                    Objects.requireNonNull(type);
                    classContentBuilder.append("obj."+c.name+" = rs."+type.inJBDCGet+"("+(i+1)+");\n");
                }
                classContentBuilder.append(
                        "}\n" + // Close while
                        "}\n" + // Close try/catch
                        "return list;\n");
                classContentBuilder.append("}\n\n"); // Close get method



                // CREATE UPDATE METHOD:
                classContentBuilder.append("" +
                        "/**\n" +
                        "Searches the provided object in the database (by its id),\n" +
                        "and updates all its fields.\n" +
                        "@throws Exception when failed to find by id.\n" +
                        "*/\n" +
                        "public static void update("+t.name+" obj) throws Exception {\n" +
                        "try (PreparedStatement ps = con.prepareStatement(\n" +
                        "                \"UPDATE "+tNameQuoted+" SET ");
                for (int i = 0; i < t.columns.size() - 1; i++) {
                    classContentBuilder.append(t.columns.get(i).name+"=?,");
                }
                classContentBuilder.append(t.columns.get(t.columns.size()-1).name+"=?");
                classContentBuilder.append(
                                "\")) {\n" // Open try/catch
                                );
                for (int i = 0; i < t.columns.size(); i++) {
                    Column c = t.columns.get(i);
                    CoolType type = CoolType.findBySQLType(c.definition); // TODO create map with columns/types to only do this once
                    Objects.requireNonNull(type);
                    classContentBuilder.append("ps."+type.inJBDCSet+"("+(i+1)+", obj."+c.name+");\n");
                }
                classContentBuilder.append(
                        "ps.executeUpdate();\n"+
                                "}\n" // Close try/catch
                                );
                classContentBuilder.append("}\n\n"); // Close update method


                // CREATE ADD METHOD:
                classContentBuilder.append("" +
                        "/**\n" +
                        "Adds the provided object to the database (note that the id is not checked for duplicates).\n" +
                        "*/\n" +
                        "public static void add("+t.name+" obj) throws Exception {\n" +
                        "try (PreparedStatement ps = con.prepareStatement(\n" +
                        "                \"INSERT INTO "+tNameQuoted+" (");
                for (int i = 0; i < t.columns.size() - 1; i++) {
                    classContentBuilder.append(t.columns.get(i).name+",");
                }
                classContentBuilder.append(t.columns.get(t.columns.size()-1).name);
                classContentBuilder.append(") VALUES (");
                for (int i = 0; i < t.columns.size() - 1; i++) {
                    classContentBuilder.append("?,");
                }
                classContentBuilder.append("?)");
                classContentBuilder.append(
                        "\")) {\n" // Open try/catch
                );
                for (int i = 0; i < t.columns.size(); i++) {
                    Column c = t.columns.get(i);
                    CoolType type = CoolType.findBySQLType(c.definition); // TODO create map with columns/types to only do this once
                    Objects.requireNonNull(type);
                    classContentBuilder.append("ps."+type.inJBDCSet+"("+(i+1)+", obj."+c.name+");\n");
                }
                classContentBuilder.append(
                        "ps.executeUpdate();\n"+
                                "}\n" // Close try/catch
                );
                classContentBuilder.append("}\n\n"); // Close add method



                // CREATE DELETE METHOD:
                classContentBuilder.append("" +
                                "/**\n" +
                                "Deletes the provided object from the database.\n" +
                                "*/\n" +
                                "public static void delete("+t.name+" obj) throws Exception {\n" +
                                "delete(\"id IS \"+obj.id);\n" +
                        "}\n"+
                        "/**\n" +
                        "Deletes the objects that are found by the provided SQL WHERE statement, from the database.\n" +
                        "*/\n" +
                        "public static void delete(String where) throws Exception {\n" +
                        "java.util.Objects.requireNonNull(where);\n" +
                        "try (PreparedStatement ps = con.prepareStatement(\n" +
                        "                \"DELETE FROM "+tNameQuoted+" WHERE \"+where)) {\n");// Open try/catch
                classContentBuilder.append(
                        "ps.executeUpdate();\n"+
                                "}\n" // Close try/catch
                );
                classContentBuilder.append("}\n\n"); // Close delete method


                classContentBuilder.append("}\n"); // Close class

                files.add(javaFile);
                Files.writeString(javaFile.toPath(), importsBuilder.toString() + classContentBuilder.toString());
            }
        }
        return files;
    }

    private void updateTablesList(String dbName) throws IOException {
        listTables.getItems().clear();
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        for (Table t : db.tables) {
            VBox wrapperTable = new VBox();
            listTables.getItems().add(wrapperTable);
            FlowPane paneTable = new FlowPane();
            wrapperTable.getChildren().add(paneTable);
            Button btnRemove = new Button("Delete");
            paneTable.getChildren().add(btnRemove);
            btnRemove.setOnMouseClicked(event -> {
                try {
                    deleteTable(dbName, t.name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            TextField tableName = new TextField(t.name);
            paneTable.getChildren().add(tableName);
            tableName.setTooltip(new Tooltip("The table name. Changes are auto-saved."));
            tableName.setOnKeyTyped(event -> { // enter pressed event
                try {
                    renameTable(dbName, t.name, tableName.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            VBox listColumns = new VBox();
            listTables.getItems().add(listColumns);
            listColumns.paddingProperty().setValue(new Insets(0,0,0, 50));
            try {
                updateColumnsList(listColumns, dbName, t.name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        VBox wrapper = new VBox();
        listTables.getItems().add(wrapper);
        FlowPane lastItem = new FlowPane();
        wrapper.getChildren().add(lastItem);
        TextField tableName = new TextField();
        lastItem.getChildren().add(tableName);
        tableName.setPromptText("New table name");
        tableName.setOnAction(event -> { // enter pressed event
            try {
                addNewTable(dbName, tableName.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void renameTable(String dbName, String oldName, String newName) throws  IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Objects.requireNonNull(oldName);
        Table t = Data.findTable(db.tables, oldName);
        Objects.requireNonNull(t);
        t.name = newName;
        Data.updateDatabases(list);
    }

    private void addNewTable(String dbName, String tableName) throws  IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Table t = new Table();
        db.tables.add(t);
        t.name = tableName;
        Data.updateDatabases(list);
        updateTablesList(dbName);
    }

    private void deleteTable(String dbName, String tableName) throws  IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        db.tables.remove(t);
        Data.updateDatabases(list);
        updateTablesList(dbName);
    }



    private void updateColumnsList(VBox listColumns, String dbName, String tableName) throws IOException {
        listColumns.getChildren().clear();
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        for (Column col : t.columns) {
            HBox item = new HBox();
            listColumns.getChildren().add(item);
            Button btnRemove = new Button("Delete");
            item.getChildren().add(btnRemove);
            if(Objects.equals(col.name, "id")) btnRemove.setDisable(true);
            btnRemove.setOnMouseClicked(event -> {
                try {
                    deleteColumn(listColumns, dbName, t.name, col.name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            TextField colName = new TextField(col.name);
            TextField colDefinition = new TextField(col.definition);
            TextField colComment = new TextField(col.comment);

            item.getChildren().add(colName);
            if(Objects.equals(col.name, "id")) colName.setDisable(true);
            colName.setPromptText("Column name");
            colName.setTooltip(new Tooltip("Column name. Changes are auto-saved."));
            colName.setOnKeyTyped(event -> { // enter pressed event
                try {
                    updateColumn(listColumns, dbName, t.name, col.name, colName.getText(), col.definition, colComment.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            item.getChildren().add(colDefinition);
            if(Objects.equals(col.name, "id")) colDefinition.setDisable(true);
            colDefinition.setPromptText("Column definition");
            colDefinition.setTooltip(new Tooltip("Column definition. Changes are auto-saved."));
            colDefinition.setOnKeyTyped(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getText(), colName.getText(), colDefinition.getText(), colComment.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            item.getChildren().add(colComment);
            if(Objects.equals(col.name, "id")) colComment.setDisable(true);
            colComment.setPromptText("Column comment");
            colComment.setTooltip(new Tooltip("Column comment. Changes are auto-saved."));
            colComment.setOnKeyTyped(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getText(), colName.getText(), colDefinition.getText(), colComment.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        FlowPane lastItem = new FlowPane();
        listColumns.getChildren().add(lastItem);
        TextField colName = new TextField();
        lastItem.getChildren().add(colName);
        colName.setPromptText("New column name");
        colName.setOnAction(event -> { // enter pressed event
            try {
                addNewColumn(listColumns, dbName, t.name, colName.getText(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateColumn(VBox listColumns, String dbName, String tableName, String oldName, String newName, String newDefinition, String newComment) throws  IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, oldName);
        Objects.requireNonNull(col);
        col.name = newName;
        col.definition = newDefinition;
        col.comment = newComment;
        Data.updateDatabases(list);
    }

    private void addNewColumn(VBox listColumns, String dbName, String tableName, String columnName, String columnDefinition) throws  IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = new Column();
        t.columns.add(col);
        col.name = columnName;
        col.definition = columnDefinition;
        Data.updateDatabases(list);
        updateColumnsList(listColumns, dbName, tableName);
    }

    private void deleteColumn(VBox listColumns, String dbName, String tableName, String columnName) throws  IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"' in "+Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, columnName);
        Objects.requireNonNull(col);
        t.columns.remove(col);
        Data.updateDatabases(list);
        updateColumnsList(listColumns, dbName, tableName);
    }
}