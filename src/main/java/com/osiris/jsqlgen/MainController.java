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

public class MainController {
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

    public MainController() {
        MainApplication.asyncIn.listeners.add(line -> {
            Platform.runLater(() -> {
                txtLogs.setText(txtLogs.getText() + line+"\n");
            });
        });
        MainApplication.asyncInErr.listeners.add(line -> {
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
        UFile.showInFileManager(Data.file);
        System.out.println("Showing file: "+Data.file);
    }

    @FXML
    public void generateCode() throws IOException {
        System.out.println("Generating code...");
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns a list of files (.java) that were generated.
     */
    public List<File> generateCode(List<Database> databases, File outputDir, boolean tablesInOneFile) throws Exception {
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
                    "// TODO: Insert credentials and update url.\n" +
                    "public static String url = \"jdbc:mysql://localhost/"+db.name+"\";\n" +
                    "public static String username;\n" +
                    "public static String password;\n" +
                    "}\n");
            files.add(databaseFile);
            for (Table t : db.tables) {
                File javaFile = new File(dir+"/"+t.name+".java");
                javaFile.createNewFile();
                files.add(javaFile);
                Files.writeString(javaFile.toPath(), UGenerator.generate(t));
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
            tableName.textProperty().addListener((o, oldVal, newVal)-> { // enter pressed event
                try {
                    renameTable(dbName, oldVal, newVal);
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
        System.out.println("Renaming table from '"+oldName+"' to '"+newName+"'.");
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
        System.out.println("OK!");
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
            colName.textProperty().addListener((o, oldVal, newVal) -> {
                try {
                    updateColumn(listColumns, dbName, t.name, oldVal, newVal, col.definition, colComment.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            item.getChildren().add(colDefinition);
            if(Objects.equals(col.name, "id")) colDefinition.setDisable(true);
            colDefinition.setPromptText("Column definition");
            colDefinition.setTooltip(new Tooltip("Column definition. Changes are auto-saved."));
            colDefinition.textProperty().addListener((o, oldVal, newVal) -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getText(), colName.getText(), newVal, colComment.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            item.getChildren().add(colComment);
            if(Objects.equals(col.name, "id")) colComment.setDisable(true);
            colComment.setPromptText("Column comment");
            colComment.setTooltip(new Tooltip("Column comment. Changes are auto-saved."));
            colComment.textProperty().addListener((o, oldVal, newVal) -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getText(), colName.getText(), colDefinition.getText(), newVal);
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
        System.out.println("Updating column...");
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
        System.out.println("OLD: "+col.name+" "+col.definition+" "+col.comment);
        col.name = newName;
        col.definition = newDefinition;
        col.comment = newComment;
        System.out.println("NEW: "+col.name+" "+col.definition+" "+col.comment);
        Data.updateDatabases(list);
        System.out.println("OK!");
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