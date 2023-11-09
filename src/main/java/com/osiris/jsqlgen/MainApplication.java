package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.osiris.jsqlgen.generator.JavaCodeGenerator;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final MyScroll lyHome = new MyScroll();
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
        Scene scene = new Scene(lyRoot);
        stage.setScene(scene);
        stage.show(); // RootPane has full window width and height

        stage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        if(Data.instance.window.isMaximized)
            stage.setMaximized(true);
        else{
            stage.setX(Data.instance.window.x);
            stage.setY(Data.instance.window.y);
            stage.setWidth(Data.instance.window.width);
            stage.setHeight(Data.instance.window.height);
        }
        lyRoot.getTabs().add(new MyTab("Home", lyHome).closable(false));
        lyRoot.getTabs().add(new MyTab("Database", lyDatabase).closable(false));

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
                stage.setTitle("jSQL-Gen v"+Const.getVersion());
            } catch (Exception e) {
                e.printStackTrace();
                stage.setTitle("jSQL-Gen");
            }

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
                File projectDir = database.javaProjectDir;
                while(projectDir != null){
                    if(!projectDir.exists()){
                        projectDir = projectDir.getParentFile();
                        continue;
                    }
                    try{
                        chooserJavaProjectDir.setInitialDirectory(projectDir);
                        break;
                    } catch (Exception ignored) {
                    }
                }
                if(projectDir != database.javaProjectDir){
                    database.javaProjectDir = projectDir;
                    Data.save();
                    System.out.println("Set Java project directory for database '" + database.name + "' to: " + database.javaProjectDir);
                }

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
                File jsonData = new File(dir.getParentFile() + "/"+db.name+"_structure.json");
                jsonData.createNewFile();
                StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
                Data.parser.toJson(db, sw);
                String out = sw.toString();
                //System.out.println(out);
                Files.writeString(jsonData.toPath(), out);
            }
            File databaseFile = new File(dir + "/Database.java");
            String rawUrl = "\"jdbc:mysql://localhost/\"";
            String url = "\"jdbc:mysql://localhost/" + db.name+"\"";
            String name = "\""+db.name+"\"";
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
                        else if (Objects.equals(var.getName().asString(), "name"))
                            if(varInit.isStringLiteralExpr()) name = "\""+varInit.asStringLiteralExpr().asString()+"\"";
                            else name = varInit.toString();
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
            JavaCodeGenerator.generateDatabaseFile(db, databaseFile, rawUrl, url, name, username, password);
            files.add(databaseFile);
            for (Table t : db.tables) {
                File javaFile = new File(dir + "/" + t.name + ".java");
                javaFile.createNewFile();
                files.add(javaFile);
                Files.writeString(javaFile.toPath(), (db.javaProjectDir != null ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                        JavaCodeGenerator.generateTableFile(javaFile, t));
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
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            HBox item = new HBox();
            listColumns.getChildren().add(item);
            Button btnMoveUp = new Button("˄");
            item.getChildren().add(btnMoveUp);
            if (Objects.equals(col.name, "id")) btnMoveUp.setDisable(true);
            AtomicInteger finalI = new AtomicInteger(i);
            btnMoveUp.setOnMouseClicked(event -> {
                try {
                    int oldI = finalI.get();
                    t.columns.remove(oldI);
                    int newIndex = finalI.decrementAndGet();
                    if(newIndex < 1) newIndex = 1; // 1 because id is always at index 0
                    else if(newIndex > t.columns.size()) newIndex = t.columns.size();
                    t.columns.add(newIndex, col);
                    Data.save();
                    updateColumnsList(listColumns, dbName, tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            Button btnMoveDown = new Button("˅");
            item.getChildren().add(btnMoveDown);
            if (Objects.equals(col.name, "id")) btnMoveDown.setDisable(true);
            btnMoveDown.setOnMouseClicked(event -> {
                try {
                    int oldI = finalI.get();
                    t.columns.remove(oldI);
                    int newIndex = finalI.incrementAndGet();
                    if(newIndex < 1) newIndex = 1; // 1 because id is always at index 0
                    else if(newIndex > t.columns.size()) newIndex = t.columns.size();
                    t.columns.add(newIndex, col);
                    Data.save();
                    updateColumnsList(listColumns, dbName, tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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
            FX.widthFull(colDefinition);
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