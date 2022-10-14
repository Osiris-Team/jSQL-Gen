package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MainApplication extends javafx.application.Application {
    public static MyTeeOutputStream outErr;
    public static MyTeeOutputStream out;
    public static AsyncReader asyncIn;
    public static AsyncReader asyncInErr;

    static {
        try {
            // DUPLICATE SYSTEM.OUT AND ASYNC-READ FROM PIPE
            PipedOutputStream pipeOut = new PipedOutputStream();
            PipedInputStream pipeIn = new PipedInputStream();
            pipeOut.connect(pipeIn);
            out = new MyTeeOutputStream(System.out, pipeOut);
            System.setOut(new PrintStream(out));
            asyncIn = new AsyncReader(pipeIn);

            // DUPLICATE SYSTEM.ERR AND ASYNC-READ FROM PIPE
            PipedOutputStream pipeOutErr = new PipedOutputStream();
            PipedInputStream pipeInErr = new PipedInputStream();
            pipeOutErr.connect(pipeInErr);
            outErr = new MyTeeOutputStream(System.err, pipeOutErr);
            System.setErr(new PrintStream(outErr));
            asyncInErr = new AsyncReader(pipeInErr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private Stage stage;
    private TabPane lyRoot = new TabPane();
    // Home panel
    private MyScroll lyHome = new MyScroll(new VBox());
    private TextField dbName = new TextField();
    private Button btnCreateDatabase = new Button("Create");
    private Button btnDeleteDatabase = new Button("Delete");
    private Button btnImportDatabase = new Button("Import");
    private Button btnExportDatabase = new Button("Export");
    private Button btnShowData = new Button("Show data");
    private TextArea txtLogs = new TextArea();
    // Database panel
    private MyScroll lyDatabase = new MyScroll(new VBox());
    private ChoiceBox<String> choiceDatabase = new ChoiceBox<>();
    private ListView<VBox> listTables = new ListView<>();
    private TabPane tabsCode = new TabPane();
    private Button btnGenerate = new Button("Generate Code");
    private CheckBox isDebug = new CheckBox("Debug");
    private CheckBox isNoExceptions = new CheckBox("No exceptions");
    private Button btnChooseJavaProjectDir = new Button("Project-Dir");
    private DirectoryChooser chooserJavaProjectDir = new DirectoryChooser();

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("jSQL-Gen");
        lyRoot.getTabs().add(new MyTab("Home", lyHome).closable(false));
        lyRoot.getTabs().add(new MyTab("Database", lyDatabase).closable(false));
        Scene scene = new Scene(lyRoot, stage.getMaxWidth(), stage.getMaxHeight());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show(); // RootPane has full window width and height

        List<String> newLines = new ArrayList<>();
        MainApplication.asyncIn.listeners.add(line -> {
            synchronized (newLines){
                newLines.add(line);
            }
            Platform.runLater(() -> {
                txtLogs.setText(txtLogs.getText() + line + "\n");
            });
        });
        List<String> newErrLines = new ArrayList<>();
        MainApplication.asyncInErr.listeners.add(line -> {
            synchronized (newErrLines){
                newErrLines.add(line);
            }
            Platform.runLater(() -> {
                txtLogs.setText(txtLogs.getText() + "[!] " + line + "\n");
            });
        });
        System.out.println("Registered log listener.");
        System.out.println("Initialised jSQL-Gen successfully!");
        new Thread(() -> {
           try{
               while (true){
                   Thread.sleep(1000);
                   synchronized (newLines){
                       if(!newLines.isEmpty()){
                           StringBuilder builder = new StringBuilder();
                           for (String l : newLines) {
                               builder.append(l + "\n");
                           }
                           newLines.clear();
                           Platform.runLater(() -> {
                               Notifications.create()
                                       .title("jSQL-Gen | Info")
                                       .text(builder.toString())
                                       .position(Pos.BOTTOM_RIGHT)
                                       .hideAfter(Duration.millis(10000))
                                       .show();
                           });
                       }
                   }
                   synchronized (newErrLines){
                       if(!newErrLines.isEmpty()){
                           StringBuilder builder = new StringBuilder();
                           for (String l : newErrLines) {
                               builder.append(l + "\n");
                           }
                           newErrLines.clear();
                           Platform.runLater(() -> {
                               Notifications.create()
                                       .title("jSQL-Gen | Error")
                                       .text(builder.toString())
                                       .position(Pos.BOTTOM_RIGHT)
                                       .hideAfter(Duration.millis(30000))
                                       .show();
                           });
                       }
                   }
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
        }).start();

        Platform.runLater(() -> {
            try {
                choiceDatabase.setOnAction(event -> { // value changed event
                    try {
                        changeDatabase(choiceDatabase.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                updateChoiceDatabase();
                List<Database> databases = Data.fetchDatabases();
                if (!databases.isEmpty()) {
                    choiceDatabase.setValue(databases.get(0).name);
                }
                isDebug.setTooltip(new MyTooltip("If selected generates additional debug logging to the error stream."));
                isDebug.setOnAction(event -> {
                    JavaCodeGenerator.isDebug = isDebug.isSelected();
                });
                isNoExceptions.setTooltip(new MyTooltip("If selected catches SQL exceptions and throws runtime exceptions instead," +
                        " which means that all methods of a generated class can be used outside of try/catch blocks."));
                isNoExceptions.setSelected(true);
                isNoExceptions.setOnAction(event -> {
                    JavaCodeGenerator.isNoExceptions = isNoExceptions.isSelected();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Layout stuff
        layoutHome();
        layoutDatabase();
    }

    private void layoutHome() {
        lyHome.removeAll();

        dbName.setPromptText("Enter database name");
        btnCreateDatabase.setOnMouseClicked(click -> {
            try {
                addDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnDeleteDatabase.setOnMouseClicked(click -> {
            try {
                deleteDatabase(dbName.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnImportDatabase.setTooltip(new MyTooltip("Imports a json file or text and either overrides the existing database or creates a new one."));
        btnImportDatabase.setOnMouseClicked(click -> {
            Popup popup = new Popup();

            MyScroll ly = new MyScroll(new VBox());
            FX.heightPercentScreen(ly, 10);
            FX.widthPercentScreen(ly, 20);
            Button btnClose = new Button("Close");
            ly.addRow().add(btnClose);
            btnClose.setOnMouseClicked(click2 -> popup.hide());
            ly.addRow().add(new Label("Currently in todo, will be available soon..."));

            popup.getContent().add(ly);
            popup.show(this.stage);
        });
        btnExportDatabase.setTooltip(new Tooltip("Exports the selected database in json format, which later can be imported again."));
        btnExportDatabase.setOnMouseClicked(click -> {
            Popup popup = new Popup();

            MyScroll ly = new MyScroll(new VBox());
            FX.heightPercentScreen(ly, 10);
            FX.widthPercentScreen(ly, 20);
            Button btnClose = new Button("Close");
            ly.addRow().add(btnClose);
            btnClose.setOnMouseClicked(click2 -> popup.hide());
            ly.addRow().add(new Label("Currently in todo, will be available soon..."));

            popup.getContent().add(ly);
            popup.show(this.stage);
        });
        btnShowData.setOnMouseClicked(click -> {
            try {
                showData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        lyHome.addRow().add(dbName, btnCreateDatabase, btnDeleteDatabase);
        lyHome.addRow().add(btnImportDatabase, btnExportDatabase, btnShowData);
        FX.widthFull(txtLogs);
        lyHome.addRow().add(txtLogs);
    }

    private void layoutDatabase() {
        lyDatabase.removeAll();

        btnGenerate.setOnMouseClicked(click -> {
            try {
                generateCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        chooserJavaProjectDir.setTitle("Select Java project directory");
        btnChooseJavaProjectDir.setTooltip(new Tooltip("Select the directory of your Java project. Classes then will be generated there" +
                " together with a copy of the schema. Everything gets overwritten, except critical information in the database class."));
        btnChooseJavaProjectDir.setOnMouseClicked(click -> {
            try{
                List<Database> databases = Data.fetchDatabases();
                Database database = Data.findDatabase(databases, choiceDatabase.getValue());
                if(database == null)
                    throw new Exception("Failed to find database '"+choiceDatabase.getValue()+"', make sure you created and selected one before.");
                if(database.javaProjectDir != null)
                    chooserJavaProjectDir.setInitialDirectory(database.javaProjectDir);
                Platform.runLater(() -> {
                    File selectedFile = chooserJavaProjectDir.showDialog(stage);
                    if (selectedFile != null) {
                        database.javaProjectDir = selectedFile;
                        try {
                            Data.updateDatabases(databases);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Set Java project directory for database '"+database.name+"' to: "+database.javaProjectDir);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        lyDatabase.addRow().add(choiceDatabase);
        FX.widthPercent(listTables, 100);
        FX.heightPercentWindow(listTables, 70);
        lyDatabase.addRow().add(listTables);
        lyDatabase.addRow().add(btnGenerate, isDebug, isNoExceptions, btnChooseJavaProjectDir);
        FX.widthPercentWindow(tabsCode, 70);
        FX.widthFull(tabsCode);
        lyDatabase.addRow().add(tabsCode);
    }

    private void changeDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.strip().isEmpty()) {
            System.err.println("Provided database name cannot be null or empty!");
            return;
        }
        List<Database> databases = Data.fetchDatabases();
        Database database = null;
        for (Database db : databases) {
            if (db.name.equals(dbName)) {
                database = db;
                break;
            }
        }
        if (database == null) {
            System.err.println("Failed to find '" + dbName + "' database in " + Data.file);
            return;
        }
        updateTablesList(dbName);
    }

    public void updateChoiceDatabase() throws IOException {
        List<Database> databases = Data.fetchDatabases();
        ObservableList<String> list = null;
        String lastValue = choiceDatabase.getValue();
        if (choiceDatabase.getItems() != null) list = choiceDatabase.getItems();
        else list = FXCollections.observableArrayList();
        list.clear();
        for (Database db : databases) {
            list.add(db.name);
        }
        choiceDatabase.setItems(list);
        if (lastValue != null && !lastValue.strip().isEmpty())
            choiceDatabase.setValue(lastValue);
    }

    protected void addDatabase() throws IOException {
        if (dbName.getText() == null || dbName.getText().strip().isEmpty()) {
            System.err.println("Database name cannot be null or empty!");
            return;
        }
        List<Database> updatedList = Data.fetchDatabases();
        Database db = new Database();
        db.name = dbName.getText();
        updatedList.add(db);
        Data.updateDatabases(updatedList);
        updateChoiceDatabase();
        System.out.println("Successfully added new database named '" + db.name + "'.");
    }

    private void deleteDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.strip().isEmpty()) {
            System.err.println("Database name cannot be null or empty!");
            return;
        }
        List<Database> databases = Data.fetchDatabases();
        Database db = Data.findDatabase(databases, dbName);
        if(db==null){
            System.err.println("Failed to find database named '"+dbName+"', thus deletion failed!");
            return;
        }
        databases.remove(db);
        Data.updateDatabases(databases);
        updateChoiceDatabase();
        System.out.println("Successfully deleted database named '" + db.name + "'.");
    }

    public void showData() throws IOException {
        UFile.showInFileManager(Data.file);
        System.out.println("Showing file: " + Data.file);
    }

    public void generateCode() throws IOException {
        System.out.println("Generating code...");
        try {
            List<File> files = generateCode(Collections.singletonList(Data.findDatabase(Data.fetchDatabases(), choiceDatabase.getValue())),
                    new File(Data.dir + "/generated"), true);
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
    public <T extends Node> List<File> generateCode(List<Database> databases, File outputDir, boolean tablesInOneFile) throws Exception {
        Objects.requireNonNull(databases);
        Objects.requireNonNull(outputDir);
        if (outputDir.isFile())
            throw new IllegalArgumentException("Outputdir file must be directory! " + outputDir);
        outputDir.mkdirs();

        List<File> files = new ArrayList<>();
        for (Database db : databases) {
            File dir = new File(outputDir + "/" + db.name);
            if(db.javaProjectDir != null) dir = new File(db.javaProjectDir+"/src/main/java/com/osiris/jsqlgen/"+db.name);
            dir.mkdirs();
            if(db.javaProjectDir != null){
                File jsonData = new File(dir.getParentFile()+"/data.json");
                jsonData.createNewFile();
                Files.copy(Data.file.toPath(), jsonData.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            File databaseFile = new File(dir + "/Database.java");
            String rawUrl = "jdbc:mysql://localhost/";
            String url = "jdbc:mysql://localhost/" + db.name;
            String username = "";
            String password = "";
            if(databaseFile.exists()){
                CompilationUnit unit = StaticJavaParser.parse(Files.readString(databaseFile.toPath()));
                for (FieldDeclaration field : unit.findAll(FieldDeclaration.class)) {
                    VariableDeclarator var = field.getVariable(0);
                    if(var.getInitializer().isPresent()){
                        if(Objects.equals(var.getName().asString(), "rawUrl"))
                            rawUrl = var.getInitializer().get().asStringLiteralExpr().asString();
                        else if(Objects.equals(var.getName().asString(), "url"))
                            url = var.getInitializer().get().asStringLiteralExpr().asString();
                        else if(Objects.equals(var.getName().asString(), "username"))
                            username = var.getInitializer().get().asStringLiteralExpr().asString();
                        else if(Objects.equals(var.getName().asString(), "password"))
                            password = var.getInitializer().get().asStringLiteralExpr().asString();
                    }
                }
            }
            databaseFile.createNewFile();
            Files.writeString(databaseFile.toPath(), "" +
                    (db.javaProjectDir != null ? "package com.osiris.jsqlgen."+db.name+";\n" : "") +
                    "import java.sql.Connection;\n" +
                    "import java.sql.DriverManager;\n" +
                    "import java.sql.SQLException;\n" +
                    "import java.sql.Statement;\n" +
                    "import java.util.Objects;\n\n" +
                    "public class Database{\n" +
                    "// TODO: Insert credentials and update url.\n" +
                    "public static String rawUrl = \""+rawUrl+"\";\n" +
                    "public static String url = \""+ url + "\";\n" +
                    "public static String name = \"" + db.name + "\";\n" +
                    "public static String username = \""+username+"\";\n" +
                    "public static String password = \""+password+"\";\n\n" +
                    "static{create();} // Create database if not exists\n" +
                    "    public static void create() {\n" +
                    "\n" +
                    "        // Do the below to avoid \"No suitable driver found...\" exception \n" +
                    "        String driverClassName = \"com.mysql.cj.jdbc.Driver\";\n" +
                    "        try {\n" +
                    "            Class<?> driverClass = Class.forName(driverClassName);\n" +
                    "            Objects.requireNonNull(driverClass);\n" +
                    "        } catch (ClassNotFoundException e) {\n" +
                    "            try {\n" +
                    "                driverClassName = \"com.mysql.jdbc.Driver\"; // Try deprecated driver as fallback\n" +
                    "                Class<?> driverClass = Class.forName(driverClassName);\n" +
                    "                Objects.requireNonNull(driverClass);\n" +
                    "            } catch (ClassNotFoundException ex) {\n" +
                    "                System.err.println(\"Failed to find critical database driver class: \"+driverClassName);\n" +
                    "                ex.printStackTrace();\n" +
                    "            }\n" +
                    "        }\n" +
                    "\n" +
                    "        // Create database if not exists\n" +
                    "        try(Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);\n" +
                    "            Statement s = c.createStatement();) {\n" +
                    "            s.executeUpdate(\"CREATE DATABASE IF NOT EXISTS `\"+Database.name+\"`\");\n" +
                    "        } catch (SQLException e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n\n");
            files.add(databaseFile);
            for (Table t : db.tables) {
                File javaFile = new File(dir + "/" + t.name + ".java");
                javaFile.createNewFile();
                files.add(javaFile);
                Files.writeString(javaFile.toPath(), (db.javaProjectDir != null ? "package com.osiris.jsqlgen."+db.name+";\n" : "") +
                        JavaCodeGenerator.generate(t));
            }
        }
        return files;
    }

    private void updateTablesList(String dbName) throws IOException {
        listTables.getItems().clear();
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
            return;
        }
        for (Table t : db.tables) {
            VBox wrapperTable = new VBox();
            listTables.getItems().add(wrapperTable);
            FlowPane paneTable = new FlowPane();
            paneTable.setBackground(new Background(new BackgroundFill(
                    new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.7),
                    null, null)));
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
            tableName.textProperty().addListener((o, oldVal, newVal) -> { // enter pressed event
                try {
                    renameTable(dbName, oldVal, newVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            VBox listColumns = new VBox();
            listTables.getItems().add(listColumns);
            listColumns.paddingProperty().setValue(new Insets(0, 0, 0, 50));
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

    private void renameTable(String dbName, String oldName, String newName) throws IOException {
        System.out.println("Renaming table from '" + oldName + "' to '" + newName + "'.");
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
            return;
        }
        Objects.requireNonNull(oldName);
        Table t = Data.findTable(db.tables, oldName);
        Objects.requireNonNull(t);
        t.name = newName;
        Data.updateDatabases(list);
        System.out.println("OK!");
    }

    private void addNewTable(String dbName, String tableName) throws IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
            return;
        }
        Table t = new Table();
        db.tables.add(t);
        t.name = tableName;
        Data.updateDatabases(list);
        updateTablesList(dbName);
    }

    private void deleteTable(String dbName, String tableName) throws IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
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
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        for (Column col : t.columns) {
            HBox item = new HBox();
            listColumns.getChildren().add(item);
            Button btnRemove = new Button("Delete");
            item.getChildren().add(btnRemove);
            if (Objects.equals(col.name, "id")) btnRemove.setDisable(true);
            btnRemove.setOnMouseClicked(event -> {
                try {
                    deleteColumn(listColumns, dbName, t.name, col.name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            TextField colName = new TextField(col.name);
            SuggestionTextField colDefinition = new SuggestionTextField(col.definition);
            for (ColumnType colType : ColumnType.allTypes) {
                colDefinition.getEntries().addAll(List.of(colType.inSQL));
            }
            TextField colComment = new TextField(col.comment);

            item.getChildren().add(colName);
            if (Objects.equals(col.name, "id")) colName.setDisable(true);
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
            if (Objects.equals(col.name, "id")) colDefinition.setDisable(true);
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
            if (Objects.equals(col.name, "id")) colComment.setDisable(true);
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

    private void updateColumn(VBox listColumns, String dbName, String tableName, String oldName, String newName, String newDefinition, String newComment) throws IOException {
        System.out.println("Updating column...");
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, oldName);
        Objects.requireNonNull(col);
        System.out.println("OLD: " + col.name + " " + col.definition + " " + col.comment);
        col.updateName(newName);
        col.definition = newDefinition;
        col.comment = newComment;
        System.out.println("NEW: " + col.name + " " + col.definition + " " + col.comment);
        Data.updateDatabases(list);
        System.out.println("OK!");
    }

    private void addNewColumn(VBox listColumns, String dbName, String tableName, String columnName, String columnDefinition) throws IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
            return;
        }
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = new Column(columnName);
        t.columns.add(col);
        col.definition = columnDefinition;
        Data.updateDatabases(list);
        updateColumnsList(listColumns, dbName, tableName);
    }

    private void deleteColumn(VBox listColumns, String dbName, String tableName, String columnName) throws IOException {
        List<Database> list = Data.fetchDatabases();
        Database db = Data.findDatabase(list, dbName);
        if (db == null) {
            System.err.println("Failed to find database named '" + dbName + "' in " + Data.file);
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