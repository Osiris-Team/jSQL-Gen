package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.osiris.desku.DesktopUI;
import com.osiris.desku.Route;
import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.input.*;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.SmartLayout;
import com.osiris.desku.ui.layout.TabLayout;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jsqlgen.generator.JavaCodeGenerator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.osiris.desku.Statics.*;

public class MainView extends Route {
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

    private final Vertical lyRoot = vertical();
    // Home panel
    private final Vertical lyHome = vertical();
    private final TextField dbName = textfield("Enter database name");
    private final Button btnCreateDatabase = button("Create");
    private final Button btnDeleteDatabase = button("Delete");
    private final Button btnImportDatabase = button("Import");
    private final Button btnExportDatabase = button("Export");
    private final Button btnShowData = button("Show data");
    private final TextField txtLogs = textfield();
    // Database panel
    private final Vertical lyDatabase = vertical();
    private final Select choiceDatabase = select();
    private final Vertical listTables = vertical().scrollable(true, "100%", "50vh", "100%", "2vh");
    private final TabLayout tabsCode = tablayout();
    private final Button btnGenerate = button("Generate Code");
    private final Button btnChooseJavaProjectDir = button("Project-Dir");
    private final FileChooser chooserJavaProjectDir = filechooser();
    private JFrame frame;

    public MainView() {
        super("/");
    }

    @Override
    public Component<?> loadContent() {
        this.frame = ((DesktopUI)UI.get()).frame; // TODO support mobile

        if(Data.instance.window.isMaximized)
            frame.setMaximized(true);
        else{
            frame.setX(Data.instance.window.x);
            frame.setY(Data.instance.window.y);
            frame.setWidth(Data.instance.window.width);
            frame.setHeight(Data.instance.window.height);
        }
        lyRoot.add(
            tablayout().addTabAndPage("Home", lyHome)
                .addTabAndPage("Database", lyDatabase)
        );

        lyRoot.later(root -> {
            List<String> newLines = new ArrayList<>();
            MainView.asyncIn.listeners.add(line -> {
                synchronized (newLines) {
                    newLines.add(line);
                }
                lyRoot.later(root2 -> {
                    txtLogs.setValue(txtLogs.getValue() + line + "\n");
                });
            });
            List<String> newErrLines = new ArrayList<>();
            MainView.asyncInErr.listeners.add(line -> {
                synchronized (newErrLines) {
                    newErrLines.add(line);
                }
                lyRoot.later(root2 -> {
                    txtLogs.setValue(txtLogs.getValue() + "[!] " + line + "\n");
                });
            });
            System.out.println("Registered log listener.");
            System.out.println("Initialised jSQL-Gen successfully!");
            lyRoot.later(root_ -> {
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
                                lyRoot.later(root__ -> {
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
                                lyRoot.later(root__ -> {
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
            });

            try {
                frame.setTitle("jSQL-Gen v"+Const.getVersion());
            } catch (Exception e) {
                e.printStackTrace();
                frame.setTitle("jSQL-Gen");
            }

            try {
                choiceDatabase.onSelectedChange(event -> { // value changed event
                    try {
                        changeDatabase(event.value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                updateChoiceDatabase();
                if (!Data.instance.databases.isEmpty()) {
                    choiceDatabase.setSelected(Data.instance.databases.get(0).name);
                }
                // Layout stuff
                layoutHome();
                layoutDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Runnable runnable = () -> {
                Data.instance.window.x = frame.getX();
                Data.instance.window.y = frame.getY();
                Data.instance.window.width = frame.getWidth();
                Data.instance.window.height = frame.getHeight();
                Data.save();
            };
            frame.xProperty().addListener((observableValue, number, t1) -> {
                runnable.run();
            });
            frame.yProperty().addListener((observableValue, number, t1) -> {
                runnable.run();
            });
            frame.widthProperty().addListener((obs, oldVal, newVal) -> {
                runnable.run();
            });
            frame.heightProperty().addListener((obs, oldVal, newVal) -> {
                runnable.run();
            });
            frame.maximizedProperty().addListener((observableValue, aBoolean, t1) -> {
                Data.instance.window.isMaximized = observableValue.getValue();
                runnable.run();
            });
        });
        return lyRoot;
    }

    private void layoutHome() {
        lyHome.removeAll();

        btnCreateDatabase.onClick(click -> {
            try {
                addDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnDeleteDatabase.onClick(click -> {
            try {
                deleteDatabase(dbName.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnImportDatabase.setTooltip(new MyTooltip("Imports a json file or text and either overrides the existing database or creates a new one."));
        btnImportDatabase.onClick(click -> {
            Popup popup = new Popup();

            MyScroll ly = new MyScroll(new Vertical());
            FX.heightPercentScreen(ly, 10);
            FX.widthPercentScreen(ly, 20);
            Button btnClose = button("Close");
            ly.addRow().add(btnClose);
            btnClose.onClick(click2 -> popup.hide());
            ly.addRow().add(new Label("Currently in todo, will be available soon..."));

            popup.getContent().add(ly);
            popup.show(this.frame);
        });
        btnExportDatabase.setTooltip(new Tooltip("Exports the selected database in json format, which later can be imported again."));
        btnExportDatabase.onClick(click -> {
            Popup popup = new Popup();

            MyScroll ly = new MyScroll(new Vertical());
            FX.heightPercentScreen(ly, 10);
            FX.widthPercentScreen(ly, 20);
            Button btnClose = button("Close");
            ly.addRow().add(btnClose);
            btnClose.onClick(click2 -> popup.hide());
            ly.addRow().add(new Label("Currently in todo, will be available soon..."));

            popup.getContent().add(ly);
            popup.show(this.frame);
        });
        btnShowData.onClick(click -> {
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

        btnGenerate.onClick(click -> {
            try {
                generateCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        chooserJavaProjectDir.setTitle("Select Java project directory");
        btnChooseJavaProjectDir.setTooltip(new Tooltip("Select the directory of your Java project. Classes then will be generated there" +
                " together with a copy of the schema. Everything gets overwritten, except critical information in the database class."));
        btnChooseJavaProjectDir.onClick(click -> {
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

                lyRoot.later(root -> {
                    File selectedFile = chooserJavaProjectDir.showDialog(frame);
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
        if (dbName.getValue() == null || dbName.getValue().strip().isEmpty()) {
            System.err.println("Database name cannot be null or empty!");
            return;
        }
        Database db = new Database();
        db.name = dbName.getValue();
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
                tab.setValue(f.getName());
                txtCode.setValue(Files.readString(f.toPath()));
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
            JavaCodeGenerator.generateDatabaseFile(db, databaseFile, rawUrl, url, username, password);
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
            Vertical wrapperTable = new Vertical();
            listTables.getItems().add(wrapperTable);
            SmartLayout paneTable = new SmartLayout();
            paneTable.putStyle("background-color",
                "rgba("+new Random().nextFloat()+","+ new Random().nextFloat()+","+ new Random().nextFloat()+","+ 0.7+")"
            );
            wrapperTable.add(paneTable);
            Select choiceAction = new Select();
            paneTable.add(choiceAction);
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
            paneTable.add(tableName);
            tableName.setTooltip(new Tooltip("The table name. Changes are auto-saved."));
            tableName.textProperty().addListener((o, oldVal, newVal) -> { // enter pressed event
                try {
                    renameTable(dbName, oldVal, newVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            final CheckBox isDebug = new CheckBox("Debug");
            paneTable.add(isDebug);
            isDebug.setTooltip(new MyTooltip("If selected generates additional debug logging to the error stream."));
            isDebug.setSelected(t.isDebug);
            isDebug.setOnAction(event -> {
                t.isDebug = isDebug.isSelected();
                Data.save();
            });

            final CheckBox isNoExceptions = new CheckBox("No exceptions");
            paneTable.add(isNoExceptions);
            isNoExceptions.setTooltip(new MyTooltip("If selected catches SQL exceptions and throws runtime exceptions instead," +
                    " which means that all methods of a generated class can be used outside of try/catch blocks."));
            isNoExceptions.setSelected(t.isNoExceptions);
            isNoExceptions.setOnAction(event -> {
                t.isNoExceptions = isNoExceptions.isSelected();
                Data.save();
            });

            final CheckBox isCache = new CheckBox("Cache");
            paneTable.add(isCache);
            isCache.setSelected(t.isCache);
            isCache.setOnAction(event -> {
                t.isCache = isCache.isSelected();
                Data.save();
            });

            Vertical listColumns = new Vertical();
            listTables.getItems().add(listColumns);
            listColumns.paddingProperty().setValue(new Insets(0, 0, 0, 50));
            try {
                updateColumnsList(listColumns, dbName, t.name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Vertical wrapper = new Vertical();
        listTables.getItems().add(wrapper);
        SmartLayout lastItem = new SmartLayout();
        wrapper.add(lastItem);
        TextField tableName = new TextField();
        lastItem.add(tableName);
        tableName.setPromptText("New table name");
        tableName.setOnAction(event -> { // enter pressed event
            try {
                addNewTable(dbName, tableName.getValue());
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


    private void updateColumnsList(Vertical listColumns, String dbName, String tableName) throws IOException {
        listColumns.children.clear();
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            Horizontal item = new Horizontal();
            listColumns.add(item);
            Button btnMoveUp = button("˄");
            item.add(btnMoveUp);
            if (Objects.equals(col.name, "id")) btnMoveUp.enable(false);
            AtomicInteger finalI = new AtomicInteger(i);
            btnMoveUp.onClick(event -> {
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
            Button btnMoveDown = button("˅");
            item.add(btnMoveDown);
            if (Objects.equals(col.name, "id")) btnMoveDown.enable(false);
            btnMoveDown.onClick(event -> {
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
            Button btnRemove = button("Delete");
            item.add(btnRemove);
            if (Objects.equals(col.name, "id")) btnRemove.enable(false);
            btnRemove.onClick(event -> {
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

            item.add(colName);
            if (Objects.equals(col.name, "id")) colName.enable(false);
            colName.setPromptText("Column name");
            colName.setTooltip(new Tooltip("Column name. Changes are auto-saved."));
            colName.textProperty().addListener((o, oldVal, newVal) -> {
                try {
                    updateColumn(listColumns, dbName, t.name, oldVal, newVal, col.definition, colComment.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            item.add(colDefinition);
            colDefinition.setPromptText("Column definition");
            colDefinition.setTooltip(new Tooltip("Column definition. Changes are auto-saved."));
            colDefinition.textProperty().addListener((o, oldVal, newVal) -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getValue(), colName.getValue(), newVal, colComment.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            item.add(colComment);
            colComment.setPromptText("Column comment");
            colComment.setTooltip(new Tooltip("Column comment. Changes are auto-saved."));
            colComment.textProperty().addListener((o, oldVal, newVal) -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getValue(), colName.getValue(), colDefinition.getValue(), newVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        FlowPane lastItem = new FlowPane();
        listColumns.add(lastItem);
        TextField colName = new TextField();
        lastItem.add(colName);
        colName.setPromptText("New column name");
        colName.setOnAction(event -> { // enter pressed event
            try {
                addNewColumn(listColumns, dbName, t.name, colName.getValue(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateColumn(Vertical listColumns, String dbName, String tableName, String oldName, String newName, String newDefinition, String newComment) throws IOException {
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

    private void addNewColumn(Vertical listColumns, String dbName, String tableName, String columnName, String columnDefinition) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = new Column(columnName);
        t.columns.add(col);
        col.definition = columnDefinition;
        Data.save();
        updateColumnsList(listColumns, dbName, tableName);
    }

    private void deleteColumn(Vertical listColumns, String dbName, String tableName, String columnName) throws IOException {
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
