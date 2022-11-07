package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private Stage stage;
    private final TabPane lyRoot = new TabPane();
    // Home panel
    private final MyScroll lyHome = new MyScroll(new VBox());
    private final TextField dbName = new TextField();
    private final Button btnCreateDatabase = new Button("Create");
    private final Button btnDeleteDatabase = new Button("Delete");
    private final Button btnImportDatabase = new Button("Import");
    private final Button btnExportDatabase = new Button("Export");
    private final Button btnShowData = new Button("Show data");
    private final TextArea txtLogs = new TextArea();
    // Database panel
    private final MyScroll lyDatabase = new MyScroll(new VBox());
    private final ChoiceBox<String> choiceDatabase = new ChoiceBox<>();
    private final ListView<VBox> listTables = new ListView<>();
    private final TabPane tabsCode = new TabPane();
    private final Button btnGenerate = new Button("Generate Code");
    private final Button btnChooseJavaProjectDir = new Button("Project-Dir");
    private final DirectoryChooser chooserJavaProjectDir = new DirectoryChooser();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.setTitle("jSQL-Gen v"+Const.getVersion());
        if(Data.instance.window.isMaximized)
            stage.setMaximized(true);
        else{
            stage.setX(Data.instance.window.x);
            stage.setY(Data.instance.window.y);
            stage.setWidth(Data.instance.window.width);
            stage.setHeight(Data.instance.window.height);
            stage.setMaxWidth(Data.instance.window.width);
            stage.setMinHeight(Data.instance.window.height);
        }
        lyRoot.getTabs().add(new MyTab("Home", lyHome).closable(false));
        lyRoot.getTabs().add(new MyTab("Database", lyDatabase).closable(false));
        Scene scene = new Scene(lyRoot, stage.getMaxWidth(), stage.getMaxHeight());
        stage.setScene(scene);
        stage.show(); // RootPane has full window width and height

        Platform.runLater(() -> {
            List<String> newLines = new ArrayList<>();
            MainApplication.asyncIn.listeners.add(line -> {
                synchronized (newLines) {
                    newLines.add(line);
                }
                Platform.runLater(() -> {
                    txtLogs.setText(txtLogs.getText() + line + "\n");
                });
            });
            List<String> newErrLines = new ArrayList<>();
            MainApplication.asyncInErr.listeners.add(line -> {
                synchronized (newErrLines) {
                    newErrLines.add(line);
                }
                Platform.runLater(() -> {
                    txtLogs.setText(txtLogs.getText() + "[!] " + line + "\n");
                });
            });
            System.out.println("Registered log listener.");
            System.out.println("Initialised jSQL-Gen successfully!");
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        synchronized (newLines) {
                            if (!newLines.isEmpty()) {
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
                        synchronized (newErrLines) {
                            if (!newErrLines.isEmpty()) {
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

            try {
                choiceDatabase.setOnAction(event -> { // value changed event
                    try {
                        changeDatabase(choiceDatabase.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                updateChoiceDatabase();
                if (!Data.instance.databases.isEmpty()) {
                    choiceDatabase.setValue(Data.instance.databases.get(0).name);
                }
                // Layout stuff
                layoutHome();
                layoutDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Runnable runnable = () -> {
                Data.instance.window.x = stage.getX();
                Data.instance.window.y = stage.getY();
                Data.instance.window.width = stage.getWidth();
                Data.instance.window.height = stage.getHeight();
                Data.save();
            };
            stage.xProperty().addListener((observableValue, number, t1) -> {
                runnable.run();
            });
            stage.yProperty().addListener((observableValue, number, t1) -> {
                runnable.run();
            });
            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                runnable.run();
            });
            stage.heightProperty().addListener((obs, oldVal, newVal) -> {
                runnable.run();
            });
            stage.maximizedProperty().addListener((observableValue, aBoolean, t1) -> {
                Data.instance.window.isMaximized = observableValue.getValue();
                runnable.run();
            });
        });
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
        FX.heightPercent(txtLogs, 70);
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
            try {
                Database database = Data.getDatabase(choiceDatabase.getValue());
                if (database == null)
                    throw new Exception("Failed to find database '" + choiceDatabase.getValue() + "', make sure you created and selected one before.");
                if (database.javaProjectDir != null)
                    chooserJavaProjectDir.setInitialDirectory(database.javaProjectDir);
                Platform.runLater(() -> {
                    File selectedFile = chooserJavaProjectDir.showDialog(stage);
                    if (selectedFile != null) {
                        database.javaProjectDir = selectedFile;
                        Data.save();
                    }
                    System.out.println("Set Java project directory for database '" + database.name + "' to: " + database.javaProjectDir);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        lyDatabase.addRow().add(choiceDatabase, btnChooseJavaProjectDir);
        FX.widthPercent(listTables, 100);
        FX.heightPercentWindow(listTables, 70);
        lyDatabase.addRow().add(listTables);
        lyDatabase.addRow().add(btnGenerate);
        FX.widthPercentWindow(tabsCode, 70);
        FX.widthFull(tabsCode);
        lyDatabase.addRow().add(tabsCode);
    }

    private void changeDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.strip().isEmpty()) {
            System.err.println("Provided database name cannot be null or empty!");
            return;
        }
        updateTablesList(dbName);
    }

    public void updateChoiceDatabase() throws IOException {
        ObservableList<String> list = null;
        String lastValue = choiceDatabase.getValue();
        if (choiceDatabase.getItems() != null) list = choiceDatabase.getItems();
        else list = FXCollections.observableArrayList();
        list.clear();
        for (Database db : Data.instance.databases) {
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
        Database db = new Database();
        db.name = dbName.getText();
        Data.instance.databases.add(db);
        Data.save();
        updateChoiceDatabase();
        System.out.println("Successfully added new database named '" + db.name + "'.");
    }

    private void deleteDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.strip().isEmpty()) {
            System.err.println("Database name cannot be null or empty!");
            return;
        }
        Database db = Data.getDatabase(dbName);
        Data.instance.databases.remove(db);
        Data.save();
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
            List<File> files = generateCode(Collections.singletonList(Data.getDatabase(choiceDatabase.getValue())),
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
            if (db.javaProjectDir != null)
                dir = new File(db.javaProjectDir + "/src/main/java/com/osiris/jsqlgen/" + db.name);
            dir.mkdirs();
            if (db.javaProjectDir != null) {
                File jsonData = new File(dir.getParentFile() + "/data.json");
                jsonData.createNewFile();
                Files.copy(Data.file.toPath(), jsonData.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            File databaseFile = new File(dir + "/Database.java");
            String rawUrl = "\"jdbc:mysql://localhost/\"";
            String url = "\"jdbc:mysql://localhost/" + db.name+"\"";
            String username = "\"\"";
            String password = "\"\"";
            if (databaseFile.exists()) {
                CompilationUnit unit = StaticJavaParser.parse(Files.readString(databaseFile.toPath()));
                for (FieldDeclaration field : unit.findAll(FieldDeclaration.class)) {
                    VariableDeclarator var = field.getVariable(0);
                    if (var.getInitializer().isPresent()) {
                        Expression varInit = var.getInitializer().get();
                        if (Objects.equals(var.getName().asString(), "rawUrl"))
                            if(varInit.isStringLiteralExpr()) rawUrl = "\""+varInit.asStringLiteralExpr().asString()+"\"";
                            else rawUrl = varInit.toString();
                        else if (Objects.equals(var.getName().asString(), "url"))
                            if(varInit.isStringLiteralExpr()) url = "\""+varInit.asStringLiteralExpr().asString()+"\"";
                            else url = varInit.toString();
                        else if (Objects.equals(var.getName().asString(), "username"))
                            if(varInit.isStringLiteralExpr()) username = "\""+varInit.asStringLiteralExpr().asString()+"\"";
                            else username = varInit.toString();
                        else if (Objects.equals(var.getName().asString(), "password"))
                            if(varInit.isStringLiteralExpr()) password = "\""+varInit.asStringLiteralExpr().asString()+"\"";
                            else password = varInit.toString();
                    }
                }
            }
            databaseFile.createNewFile();
            Files.writeString(databaseFile.toPath(), "" +
                    (db.javaProjectDir != null ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                    "import java.sql.Connection;\n" +
                    "import java.sql.DriverManager;\n" +
                    "import java.sql.SQLException;\n" +
                    "import java.sql.Statement;\n" +
                    "import java.util.Objects;\n" +
                    "import java.util.ArrayList;\n" +
                    "import java.util.List;\n\n" +
                    "/*\n" +
                    "Auto-generated class that is used by all table classes to create connections. <br>\n" +
                    "It holds the database credentials (set by you at first run of jSQL-Gen).<br>\n" +
                    "Note that the fields rawUrl, url, username and password do NOT get overwritten when re-generating this class. <br>\n" +
                    "All tables use the cached connection pool in this class which has following advantages: <br>\n" +
                    "- Ensures optimal performance (cpu and memory usage) for any type of database from small to huge, with millions of queries per second.\n" +
                    "- Connection status is checked before doing a query (since it could be closed or timed out and thus result in errors)."+
                    "*/\n" +
                    "public class Database{\n" +
                    "public static String rawUrl = " + rawUrl + ";\n" +
                    "public static String url = " + url + ";\n" +
                    "public static String name = \"" + db.name + "\";\n" +
                    "public static String username = " + username + ";\n" +
                    "public static String password = " + password + ";\n" +
                    "private static final List<Connection> availableConnections = new ArrayList<>();\n" +
                    "\n" +
                    "    static{create();} // Create database if not exists\n" +
                    "\n" +
                    "public static void create() {\n" +
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
                    "                ex.printStackTrace();\n" +
                    "                System.err.println(\"Failed to find critical database driver class: \"+driverClassName+\" program will exit.\");\n" +
                    "                System.exit(1);\n" +
                    "            }\n" +
                    "        }\n" +
                    "\n" +
                    "        // Create database if not exists\n" +
                    "        try(Connection c = DriverManager.getConnection(Database.rawUrl, Database.username, Database.password);\n" +
                    "            Statement s = c.createStatement();) {\n" +
                    "            s.executeUpdate(\"CREATE DATABASE IF NOT EXISTS `\"+Database.name+\"`\");\n" +
                    "        } catch (SQLException e) {\n" +
                    "            e.printStackTrace();\n" +
                    "            System.err.println(\"Something went really wrong during database initialisation, program will exit.\");\n" +
                    "            System.exit(1);\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    public static Connection getCon() {\n" +
                    "        synchronized (availableConnections){\n" +
                    "            try{\n" +
                    "                if (!availableConnections.isEmpty()) {\n" +
                    "                    List<Connection> removableConnections = new ArrayList<>(0);\n" +
                    "                    for (Connection con : availableConnections) {\n" +
                    "                        if (con.isValid(1)) return con;\n" +
                    "                        else removableConnections.add(con);\n" +
                    "                    }\n" +
                    "                    for (Connection removableConnection : removableConnections) {\n" +
                    "                        removableConnection.close();\n" +
                    "                        availableConnections.remove(removableConnection); // Remove invalid connections\n" +
                    "                    }\n" +
                    "                }\n" +
                    "                return DriverManager.getConnection(Database.url, Database.username, Database.password);\n" +
                    "            } catch (Exception e) {\n" +
                    "                throw new RuntimeException(e);\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    public static void freeCon(Connection connection) {\n" +
                    "        synchronized (availableConnections){\n" +
                    "            availableConnections.add(connection);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n");
            files.add(databaseFile);
            for (Table t : db.tables) {
                File javaFile = new File(dir + "/" + t.name + ".java");
                javaFile.createNewFile();
                files.add(javaFile);
                Files.writeString(javaFile.toPath(), (db.javaProjectDir != null ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                        JavaCodeGenerator.generate(javaFile, t));
            }
        }
        return files;
    }

    private void updateTablesList(String dbName) throws IOException {
        listTables.getItems().clear();
        Database db = Data.getDatabase(dbName);
        ArrayList<Table> tables = db.tables;
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            VBox wrapperTable = new VBox();
            listTables.getItems().add(wrapperTable);
            FlowPane paneTable = new FlowPane();
            paneTable.setHgap(10);
            paneTable.setBackground(new Background(new BackgroundFill(
                    new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.7),
                    null, null)));
            wrapperTable.getChildren().add(paneTable);
            ChoiceBox<String> choiceAction = new ChoiceBox<>();
            paneTable.getChildren().add(choiceAction);
            FX.widthPercentScreen(choiceAction, 1);
            ObservableList<String> list = FXCollections.observableArrayList();
            list.add("Delete");
            list.add("Duplicate");
            choiceAction.setItems(list);
            int finalI = i;
            choiceAction.setOnAction(event -> {
                try {
                    String command = choiceAction.getValue();
                    if(command == null || command.isEmpty()) return;
                    if (command.equals("Delete"))
                        deleteTable(dbName, t.name);
                    else if (command.equals("Duplicate")) {
                        db.tables.add(finalI, t.duplicate());
                        updateTablesList(dbName);
                        Data.save();
                    } else
                        throw new Exception("Unknown command '" + command + "' to modify table!");
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
            final CheckBox isDebug = new CheckBox("Debug");
            paneTable.getChildren().add(isDebug);
            isDebug.setTooltip(new MyTooltip("If selected generates additional debug logging to the error stream."));
            isDebug.setSelected(t.isDebug);
            isDebug.setOnAction(event -> {
                t.isDebug = isDebug.isSelected();
                Data.save();
            });

            final CheckBox isNoExceptions = new CheckBox("No exceptions");
            paneTable.getChildren().add(isNoExceptions);
            isNoExceptions.setTooltip(new MyTooltip("If selected catches SQL exceptions and throws runtime exceptions instead," +
                    " which means that all methods of a generated class can be used outside of try/catch blocks."));
            isNoExceptions.setSelected(t.isNoExceptions);
            isNoExceptions.setOnAction(event -> {
                t.isNoExceptions = isNoExceptions.isSelected();
                Data.save();
            });

            final CheckBox isCache = new CheckBox("Cache");
            paneTable.getChildren().add(isCache);
            isCache.setSelected(t.isCache);
            isCache.setOnAction(event -> {
                t.isCache = isCache.isSelected();
                Data.save();
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
        Database db = Data.getDatabase(dbName);
        Objects.requireNonNull(oldName);
        Table t = Data.findTable(db.tables, oldName);
        Objects.requireNonNull(t);
        t.name = newName;
        Data.save();
        System.out.println("OK!");
    }

    private void addNewTable(String dbName, String tableName) throws IOException {
        Database db = Data.getDatabase(dbName);
        for (Table table : db.tables) {
            if(table.name.equalsIgnoreCase(tableName))
                throw new IOException("Table '"+tableName.toLowerCase()+"' already exists for this database!");
        }
        Table t = new Table();
        db.tables.add(t);
        t.name = tableName;
        Data.save();
        updateTablesList(dbName);
    }

    private void deleteTable(String dbName, String tableName) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        db.tables.remove(t);
        Data.save();
        updateTablesList(dbName);
    }


    private void updateColumnsList(VBox listColumns, String dbName, String tableName) throws IOException {
        listColumns.getChildren().clear();
        Database db = Data.getDatabase(dbName);
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
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, oldName);
        Objects.requireNonNull(col);
        System.out.println("OLD: " + col.name + " " + col.definition + " " + col.comment);
        col.updateName(newName);
        col.definition = newDefinition;
        col.comment = newComment;
        System.out.println("NEW: " + col.name + " " + col.definition + " " + col.comment);
        Data.save();
        System.out.println("OK!");
    }

    private void addNewColumn(VBox listColumns, String dbName, String tableName, String columnName, String columnDefinition) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = new Column(columnName);
        t.columns.add(col);
        col.definition = columnDefinition;
        Data.save();
        updateColumnsList(listColumns, dbName, tableName);
    }

    private void deleteColumn(VBox listColumns, String dbName, String tableName, String columnName) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, columnName);
        Objects.requireNonNull(col);
        t.columns.remove(col);
        Data.save();
        updateColumnsList(listColumns, dbName, tableName);
    }
}