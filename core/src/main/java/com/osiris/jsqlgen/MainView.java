package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.osiris.desku.App;
import com.osiris.desku.Icon;
import com.osiris.desku.ui.DesktopUI;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.input.*;
import com.osiris.desku.ui.input.filechooser.FileAsRow;
import com.osiris.desku.ui.input.filechooser.FileChooser;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.Popup;
import com.osiris.desku.ui.layout.TabLayout;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.generator.GenDatabaseFile;
import com.osiris.jsqlgen.generator.JavaCodeGenerator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.ui.LayoutDatabaseOptions;
import com.osiris.jsqlgen.ui.timer.LayoutTimer;
import com.osiris.jsqlgen.utils.*;
import org.fusesource.jansi.utils.UtilsAnsiHtml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.osiris.jsqlgen.Data.*;

import static com.osiris.desku.Statics.*;

public class MainView extends Vertical {
    public static AsyncReader asyncIn;
    public static AsyncReader asyncInErr;

    static {
        try {
            // DUPLICATE SYSTEM.OUT AND ASYNC-READ FROM PIPE
            File mirrorOut = new File(System.getProperty("user.dir")+"/logs/mirror-out.log");
            File mirrorErr = new File(System.getProperty("user.dir")+"/logs/mirror-err.log");
            InputStream is = Files.newInputStream(mirrorOut.toPath(), StandardOpenOption.READ);
            asyncIn = new AsyncReader(is, 1000);
            InputStream isErr = Files.newInputStream(mirrorErr.toPath(), StandardOpenOption.READ);
            asyncInErr = new AsyncReader(isErr, 1000);
        } catch (IOException e) {
            AL.warn(e);
        }
    }

    public static Vertical vertical(){
        return com.osiris.desku.Statics.vertical().childGap(true).padding(true);
    }

    // Home panel
    private final Vertical lyHome = vertical();
    private final TextField dbName = textfield("Enter database name");
    private final Button btnCreateDatabase = button("Create");
    private final Button btnDeleteDatabase = button("Delete");
    private final Button btnImportDatabase = button("Import");
    private final Button btnExportDatabase = button("Export");
    private final Button btnMergeDatabasesFromDir = button("Merge from Projects");
    private final Button btnShowData = button("Open data folder");
    private final Vertical txtLogs = vertical().childGap(false)
        .scrollable(true, "100%", "25vh", "", "");
    // Database panel
    private final Vertical lyDatabase = vertical();
    private final OptionField dbSelector = optionfield("Select database to show",
        instance.databases.isEmpty() ? "Select" : instance.databases.get(0).name).onValueChange(e -> {
        try {
            changeDatabase(e.value);
            updateChooserJavaProjectDir();
        } catch (Exception ex) {
            AL.warn(ex);
        }
    });
    {
        dbSelector.button.sty("font-size", "xx-large")
            .sty("font-weight", "bolder");
    }
    private final Vertical listTables = vertical().scrollable(true, "100%", "90vh", "", "");
    private final TabLayout tabsCode = tablayout().grow(1);
    private final Button btnGenerate = button("Generate Code");
    private final FileChooser chooserJavaProjectDir;
    {
        chooserJavaProjectDir = getFileChooser("");
    }

    private @NotNull FileChooser getFileChooser(String defaultValue) {
        final FileChooser chooserJavaProjectDir;
        Consumer<FileAsRow> fileSelectListener = e -> {
            Database database = null;
            try {
                database = getDatabaseOrFail();
                CopyOnWriteArrayList<File> dirs = database.getJavaProjectDirs();
                if(e.checkBox.getValue())
                    dirs.addIfAbsent(e.file);
                else dirs.remove(e.file);
                database.setJavaProjectDirs(dirs);
                save();
                AL.info("Set Java project directory/ies for database '" + database.name + "' to: " + dirs);
            } catch (Exception ex) {
                AL.warn("Failed to save data for java project dir.", ex);
            }
        };
        chooserJavaProjectDir = filechooser("",  defaultValue).onFileSelected(fileSelectListener)
            .onFileDeselected(fileSelectListener);
        Component selectFilesBtn = chooserJavaProjectDir.btnsSelectedFiles.children.get(0);
        selectFilesBtn.setValue("Select Java project directory/ies");
        return chooserJavaProjectDir;
    }

    private final LayoutDatabaseOptions lyDatabaseOptions = new LayoutDatabaseOptions(null, Database.class);
    {
        try{
            lyDatabaseOptions.setValue(getDatabaseOrFail());
        } catch (Exception e) {
        }
    }

    private @Nullable JFrame frame;

    public MainView() {
        UI ui = UI.get();
        if(ui instanceof DesktopUI){
            DesktopUI desktopUI = (DesktopUI) ui;
            this.frame = desktopUI.frame;
            if(Data.instance.window.isMaximized) {
                frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
            else{
                frame.setLocation((int) instance.window.x, (int) instance.window.y);
                frame.setSize((int) instance.window.width, (int) instance.window.height);
            }
        }

        this.sty("zoom", "0.85");

        var tabs = new TabLayout();
        this.add(tabs);

        tabs.addTabAndPage("Home", new Vertical().add(lyHome, lyDatabase).padding(false).grow(1));
        tabs.addTabAndPage("Timer", new LayoutTimer().grow(1));

        if(!App.isInDepthDebugging){
            MainView.asyncIn.listeners.add(line -> {
                ui.access(() -> {
                    var comp = horizontal().padding(false);
                    try {
                        txtLogs.add(comp);
                        comp.executeJS("comp.innerHTML = `" + new UtilsAnsiHtml().convertAnsiToHtml(line)
                            .replace("\\", "\\\\") +"`");
                        txtLogs.scrollToBottom();
                    } catch (IOException e) {
                        AL.warn(e);
                    }
                });
            });

            MainView.asyncInErr.listeners.add(line -> {
                ui.access(() -> {
                    var comp = horizontal().padding(false);
                    try {
                        txtLogs.add(comp);
                        comp.executeJS("comp.innerHTML = `[!] " + new UtilsAnsiHtml().convertAnsiToHtml(line)
                            .replace("\\", "\\\\")+"`");
                        txtLogs.scrollToBottom();
                    } catch (IOException e) {
                        AL.warn(e);
                    }
                });
            });
            AL.info("Registered log listener.");
        }
        AL.info("Initialised jSQL-Gen successfully!");

        try {
            String version = Const.getVersion();
            if(startsWithNumber(version)) frame.setTitle("jSQL-Gen v"+version);
        } catch (Exception e) {
            e.printStackTrace();
            frame.setTitle("jSQL-Gen");
        }

        try {
            updateDatabaseSelector();
            if (!Data.instance.databases.isEmpty()) {
                dbSelector.setValue(Data.instance.databases.get(0).name);
            }
            // Layout stuff
            layoutHome();
            layoutDatabase();
        } catch (Exception e) {
            AL.warn(e);
        }

        if(frame != null){
            // Define the runnable to update Data.instance.window
            Runnable runnable = () -> {
                Data.instance.window.x = frame.getX();
                Data.instance.window.y = frame.getY();
                Data.instance.window.width = frame.getWidth();
                Data.instance.window.height = frame.getHeight();
                Data.save();
            };

            // Add component listener to handle size and position changes
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    runnable.run();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    runnable.run();
                }
            });

            // Add window state listener to handle maximize state changes
            frame.addWindowStateListener(e -> {
                if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                    Data.instance.window.isMaximized = true;
                } else {
                    Data.instance.window.isMaximized = false;
                }
                runnable.run();
            });
        }
    }

    private boolean startsWithNumber(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        char firstChar = s.charAt(0);
        return Character.isDigit(firstChar);
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

        btnImportDatabase.setTooltip("Imports a json file or text and either overrides the existing database or creates a new one.");
        btnImportDatabase.onClick(click -> {
            var popup = popup();
            popup.add(text("Currently in todo, will be available soon..."));
            lyHome.add(popup);
        });

        btnMergeDatabasesFromDir.setTooltip("Searches the provided directory and \nsub-directories for databases and imports them.\n" +
            "If a database with the same name exists its replaced by the imported one, thus proceed with caution.\n" +
            "A backup of the current structure will be created though.");
        btnMergeDatabasesFromDir.onClick(click -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = getFormatter();
            File backup = new File(backupDir+"/backup-all-databases-"+now.format(formatter)+".json");
            try {
                Files.writeString(backup.toPath(), parser.toJson(instance, DataJson.class));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to proceed, due to failed backup!");
                return;
            }

            FileChooser chooser = filechooser();
            Popup chooserPopup = popup();
            chooserPopup.content.add(chooser);
            lyHome.add(chooserPopup);

            chooser.onFileSelected(fileAsRow -> {
                File selectedFile = fileAsRow.file;
                if(!selectedFile.isDirectory()){
                    selectedFile = selectedFile.getParentFile();
                }
                AL.info("Importing, please stand by... Dir: " + selectedFile);
                if (selectedFile != null) {
                    File finalSelectedFile = selectedFile;
                    AtomicInteger counter = new AtomicInteger();
                    AtomicLong checkedFilesCounter = new AtomicLong();
                    Thread t1 = new Thread(() -> {
                        walkRecursive(finalSelectedFile, file -> {
                            checkedFilesCounter.incrementAndGet();
                            if (file.getName().endsWith("_structure.json")) {
                                AL.info(file.getAbsolutePath());
                                Database db;
                                try {
                                    db = parser.fromJson(new BufferedReader(new FileReader(file)), Database.class);
                                } catch (Exception e) {
                                    return;
                                }
                                CopyOnWriteArrayList<Database> list = new CopyOnWriteArrayList<>();
                                list.add(db);
                                Map<Database, DBWrapper> oldAndNewDBsMap;
                                oldAndNewDBsMap = getOldAndNewDBsMap(instance.databases, list);
                                if(oldAndNewDBsMap.isEmpty()){
                                    boolean exists = false;
                                    for (Database db_ : instance.databases) {
                                        if(db.name.equals(db_.name)) exists = true;
                                    }
                                    if(exists){
                                        AL.info("Db " + db.name + " already exists and seems to be up-to-date.");
                                    } else{
                                        // New database, thus add/import
                                        instance.databases.add(db);
                                        Data.save();
                                        counter.incrementAndGet();
                                        AL.info("Added new db " + db.name + " from: " + file.getAbsolutePath());
                                        lyHome.later((___) -> updateDatabaseSelector());
                                    }
                                } else // There is a newer version of the db, thus replace
                                    instance.databases.replaceAll(dbOld -> {
                                        DBWrapper dbNew = oldAndNewDBsMap.get(dbOld);
                                        if (dbNew != null) {
                                            counter.incrementAndGet();
                                            AL.info("Replaced " + dbOld.name + " with newer db (has table with more changes) from: " + file.getAbsolutePath());
                                            return dbNew.db;
                                        } else return dbOld;
                                    });
                            }
                        });
                        AL.info("Success! Added or replaced " + counter.get() + " databases.");
                    });
                    t1.start();
                    new Thread(() -> {
                        try{
                            while (t1.isAlive()){
                                Thread.sleep(3000);
                                AL.info("Scanned "+checkedFilesCounter.get()+" files.");
                            }
                            AL.info("Details printer thread stopped.");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            });
        });

        btnExportDatabase.setTooltip(("Exports the selected database in json format, which later can be imported again."));
        btnExportDatabase.onClick(click -> {
            var popup = popup();
            popup.add(text("Currently in todo, will be available soon..."));
            lyHome.add(popup);
        });
        btnShowData.onClick(click -> {
            try {
                showData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        lyHome.horizontalCL().childGap(true).add(btnImportDatabase, btnMergeDatabasesFromDir, btnExportDatabase, btnShowData);
        lyHome.horizontalCL().childGap(true).add(dbName);
        lyHome.horizontalCL().childGap(true).add(btnCreateDatabase, btnDeleteDatabase);
        lyHome.add(txtLogs);
    }

    private void walkRecursive(File file, Consumer<File> code) {
        if(file.isFile()) {
            code.accept(file);
            return;
        } else {
            File[] files = file.listFiles();
            if(files != null)
                for (File f : files) {
                    walkRecursive(f, code);
                }
        }
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
        chooserJavaProjectDir.tfSelectedFiles.setTooltip(("(Optional) Select the directory of your Java project. Classes then will be generated there" +
                " together with a copy of the schema. Everything gets overwritten, except critical information in the database class." +
            " If not selected, files will be only shown in the \"Code\" tab.\n" +
            "Supports multiple directories."));
        updateChooserJavaProjectDir();

        lyDatabase.add(dbSelector);
        lyDatabase.add(btnGenerate);
        lyDatabase.add(chooserJavaProjectDir, lyDatabaseOptions);
        TabLayout tabs = new TabLayout();
        tabs.addTabAndPage("Tables", listTables);
        tabs.addTabAndPage("Code", tabsCode);
        lyDatabase.add(tabs);
        //lyDatabase.add(listTables);
        //lyDatabase.add(tabsCode);
    }

    private void updateChooserJavaProjectDir() {
        try {
            Database database = getDatabaseOrFail();
            CopyOnWriteArrayList<File> javaProjectDirs = database.getJavaProjectDirs();
            boolean isChanged = false;
            for (int i = 0; i < javaProjectDirs.size(); i++) {
                File projectDir = javaProjectDirs.get(i);
                File p2 = projectDir;
                while (p2 != null) {
                    if (!p2.exists()) {
                        p2 = p2.getParentFile();
                        continue;
                    }
                    break;
                }
                if(p2 == null || !p2.exists()){
                    AL.warn("Removed directory since it doesn't exist: "+projectDir);
                    isChanged = true;
                    javaProjectDirs.remove(projectDir);
                }
                else if (p2 != projectDir) {
                    AL.warn("Changed directory since original doesn't exist, new: "+p2+" old: "+projectDir);
                    isChanged = true;
                    javaProjectDirs.set(i, p2);
                }
                if(p2 != null){
                    int count = 0;
                    for (File anotherDir : javaProjectDirs) {
                        if(anotherDir.getAbsolutePath().equals(p2.getAbsolutePath())) count++;
                    }
                    if(count > 1){
                        AL.warn("Removed directory since it already exists in list (duplicate): "+p2);
                        isChanged = true;
                        javaProjectDirs.remove(p2);
                    }
                }
            }
            if(isChanged && !javaProjectDirs.isEmpty()){
                database.setJavaProjectDirs(javaProjectDirs);
                Data.save();
                AL.info("Set Java project directory for database '" + database.name + "' to: " + javaProjectDirs);
            }
            if(!javaProjectDirs.isEmpty()){ // Update UI
                String s = "";
                for (File javaProjectDir : javaProjectDirs) {
                    s += javaProjectDir+"; ";
                }
                chooserJavaProjectDir.setValue(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private @NotNull Database getDatabaseOrFail() throws Exception {
        Database database = Data.getDatabase(dbSelector.getValue());
        if (database == null)
            throw new Exception("Failed to find database '" + dbSelector.getValue() + "', make sure you created and selected one before.");
        return database;
    }

    private void changeDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.strip().isEmpty()) {
            System.err.println("Provided database name cannot be null or empty!");
            return;
        }
        Database db = Data.getDatabase(dbName);
        updateTablesList(dbName);
        lyDatabaseOptions.setValue(db);
    }

    public void updateDatabaseSelector() {
        dbSelector.items.body.removeAll();
        String lastValue = dbSelector.getValue();
        String[] dbNames = new String[instance.databases.size()];
        CopyOnWriteArrayList<Database> databases = instance.databases;
        for (int i = 0; i < databases.size(); i++) {
            Database db = databases.get(i);
            dbNames[i] = db.name;
        }
        dbSelector.addItems(dbNames);
        if (lastValue != null && !lastValue.strip().isEmpty())
            dbSelector.setValue(lastValue);
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
        updateDatabaseSelector();
        AL.info("Successfully added new database named '" + db.name + "'.");
    }

    private void deleteDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.strip().isEmpty()) {
            System.err.println("Database name cannot be null or empty!");
            return;
        }
        Database db = Data.getDatabase(dbName);
        Data.instance.databases.remove(db);
        Data.save();
        updateDatabaseSelector();
        AL.info("Successfully deleted database named '" + db.name + "'.");
    }

    public void showData() throws IOException {
        UFile.showInFileManager(Data.file);
        AL.info("Showing file: " + Data.file);
    }

    public void generateCode() throws IOException {
        AL.info("Generating code...");
        try {
            List<File> files = generateCode(Collections.singletonList(Data.getDatabase(dbSelector.getValue())),
                    Main.generatedDir, true);
            AL.info("Generated code/files: ");
            for (File f : files) {
                AL.info(f.getAbsolutePath());
            }
            // Refresh table view
            updateTablesList(dbSelector.getValue());

            // Refresh tabs
            tabsCode.pages.removeAll();
            tabsCode.tabs.removeAll();
            //tabsCode.removeAll();
            for (File f : files) {
                tabsCode.addTabAndPage(f.getName(), textarea("", Files.readString(f.toPath())).width("100%").height("70vh"));
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
            List<Data.JavaProjectGenDir> dirs = new ArrayList<>();
            dirs.add(new Data.JavaProjectGenDir(outputDir + "/" + db.name));
            if (!db.getJavaProjectDirs().isEmpty()){
                dirs = getJavaProjectGenDir(db);
            }
            for (JavaProjectGenDir dir : dirs) {
                dir.mkdirs();
            }


            // Write Database class files and Tables files
            for (JavaProjectGenDir javaProjectGenDir : dirs) {
                File databaseFile = getDatabaseFile(javaProjectGenDir);
                String url = "\"jdbc:mysql://localhost:3306/" + db.name+"\"";
                String rawUrl = "getRawDbUrlFrom(url)";
                String name = "\""+db.name+"\"";
                String username = "\"\"";
                String password = "\"\"";
                if (databaseFile.exists()) {
                    AL.info("Reading database class file at: "+databaseFile);
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
                    AL.info("Success, database class file is valid.");
                }
                databaseFile.createNewFile();
                GenDatabaseFile.s(db, databaseFile, rawUrl, url, name, username, password);
                files.add(databaseFile);
                JavaCodeGenerator.prepareTables(db);
                for (Table t : db.tables) {
                    File javaFile = new File(javaProjectGenDir + "/" + t.name + ".java");
                    javaFile.createNewFile();
                    files.add(javaFile);
                    Files.writeString(javaFile.toPath(), (!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                        JavaCodeGenerator.generateTableFile(javaFile, t, db));
                }
            }

            // After generation, since db object might still change
            if (!db.getJavaProjectDirs().isEmpty()) {
                // Write json structure data
                for (File jsonData : getDatabaseStructureFile(db, dirs)) {
                    AL.info(jsonData.getAbsolutePath());
                    jsonData.createNewFile();
                    StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
                    Data.parser.toJson(db, sw);
                    String out = sw.toString();
                    //AL.info(out);
                    Files.writeString(jsonData.toPath(), out);
                }
            }
        }
        return files;
    }

    private void updateTablesList(String dbName) throws IOException {
        listTables.removeAll();
        Database db = Data.getDatabase(dbName);
        CopyOnWriteArrayList<Table> tables = db.tables;
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            Vertical wrapperTable = new Vertical().padding(false);
            listTables.add(wrapperTable);
            Horizontal hl = new Horizontal().padding(false);
            hl.sty("background-color",
                "rgba("+new Random().nextInt(250)+","+ new Random().nextInt(250)+","+ new Random().nextInt(250)+","+ 0.7+")"
            ).addClass("rounded");
            wrapperTable.add(hl);
            var choiceAction = optionfield("", "").sty("max-width", "40px");
            hl.add(choiceAction);
            choiceAction.addItems("Delete", "Duplicate");
            int finalI = i;
            choiceAction.onValueChange(event -> {
                try {
                    String command = event.value;
                    if(command == null || command.isEmpty()) return;
                    if (command.equals("Delete"))
                        deleteTable(dbName, t.name);
                    else if (command.equals("Duplicate")) {
                        Table newT = t.duplicate();
                        newT.name = "COPY_"+t.name;
                        db.tables.add(finalI, newT);
                        updateTablesList(dbName);
                        Data.save();
                    } else
                        throw new Exception("Unknown command '" + command + "' to modify table!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            TextField tableName = new TextField().setValue(t.name).width("60%");
            tableName.input.sty("font-weight", "bold").sty("font-size", "larger");
            hl.add(tableName);
            tableName.setTooltip("The table name. Changes are auto-saved.");
            tableName.onValueChange(e -> {
                try {
                    renameTable(dbName, e.valueBefore, e.value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            final CheckBox isDebug = new CheckBox("Debug");
            hl.add(isDebug);
            isDebug.setTooltip("If selected generates additional debug logging to the error stream.");
            isDebug.setValue(t.isDebug);
            isDebug.onValueChange(event -> {
                t.isDebug = event.value;
                Data.save();
            });

            final CheckBox isNoExceptions = new CheckBox("No exceptions");
            hl.add(isNoExceptions);
            isNoExceptions.setTooltip("If selected catches SQL exceptions and throws runtime exceptions instead," +
                    " which means that all methods of a generated class can be used outside of try/catch blocks.");
            isNoExceptions.setValue(t.isNoExceptions);
            isNoExceptions.onValueChange(event -> {
                t.isNoExceptions = event.value;
                Data.save();
            });

            final CheckBox isCache = new CheckBox("Cache");
            hl.add(isCache);
            isCache.setValue(t.isCache);
            isCache.onValueChange(event -> {
                t.isCache = event.value;
                Data.save();
            });

            final CheckBox isVaadinFlow = new CheckBox("Vaadin-Flow");
            hl.add(isVaadinFlow);
            isVaadinFlow.setValue(t.isVaadinFlowUI);
            isVaadinFlow.onValueChange(event -> {
                t.isVaadinFlowUI = event.value;
                Data.save();
            });

            Vertical listColumns = new Vertical();
            listTables.add(listColumns);
            listColumns.paddingLeft("50px");
            try {
                updateColumnsList(listColumns, dbName, t.name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Vertical wrapper = new Vertical();
        listTables.add(wrapper);
        Horizontal lastItem = new Horizontal();
        wrapper.add(lastItem);
        TextField tableName = new TextField().grow(1);
        var btnAddNewTable = new Button("Add");
        lastItem.add(new Horizontal().add(btnAddNewTable, tableName).childGap(true));
        tableName.label.setValue("New table name");
        btnAddNewTable.onClick(event -> { // enter pressed event
            try {
                addNewTable(dbName, tableName.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void renameTable(String dbName, String oldName, String newName) throws IOException {
        AL.info("Renaming table from '" + oldName + "' to '" + newName + "'.");
        Database db = Data.getDatabase(dbName);
        Objects.requireNonNull(oldName);
        Table t = Data.findTable(db.tables, oldName);
        Objects.requireNonNull(t);
        t.name = newName;

        // Update current change
        t.currentChange.newTableName = newName;

        Data.save();
        AL.info("OK!");
    }

    private void addNewTable(String dbName, String tableName) throws IOException {
        Database db = Data.getDatabase(dbName);
        for (Table table : db.tables) {
            if(table.name.equalsIgnoreCase(tableName))
                throw new IOException("Table '"+tableName.toLowerCase()+"' already exists for this database!");
        }
        Table t = new Table();
        t.id = Main.idCounter.getAndIncrement();
        t.addIdColumn();
        db.tables.add(t);
        t.name = tableName;

        // Update current change
        t.currentChange.newTableName = tableName;
        t.currentChange.oldTableName = tableName;

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
        listColumns.removeAll();
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            Horizontal hl = new Horizontal().childGap(true).padding(false);
            listColumns.add(hl);
            Button btnMoveUp = button("").add(Icon.solid_arrow_up()).secondary();
            hl.add(btnMoveUp);
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
            Button btnMoveDown = button("").add(Icon.solid_arrow_down()).secondary();
            hl.add(btnMoveDown);
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
            Button btnRemove = button("").add(Icon.solid_trash()).danger();
            hl.add(btnRemove);
            if (Objects.equals(col.name, "id")) btnRemove.enable(false);
            btnRemove.onClick(event -> {
                try {
                    deleteColumn(listColumns, dbName, t.name, col.name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            TextField colName = new TextField("Column name", col.name);
            colName.input.sty("font-weight", "bold");
            SuggestionTextField colDefinition = new SuggestionTextField("Column definition", col.definition);
            for (ColumnType colType : ColumnType.allTypes) {
                colDefinition.getEntries().addAll(List.of(colType.inSQL));
            }
            TextField colComment = new TextField("Column comment", col.comment);

            hl.add(colName);
            if (Objects.equals(col.name, "id")) colName.enable(false);
            colName.setTooltip(("Column name. Changes are auto-saved."));
            colName.onValueChange(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, event.valueBefore, event.value, col.definition, colComment.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            hl.add(colDefinition);
            colDefinition.setTooltip("Column definition. Changes are auto-saved.");
            colDefinition.textField.onValueChange(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getValue(), colName.getValue(), event.value, colComment.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            hl.add(colComment);
            colComment.setTooltip("Column comment. Changes are auto-saved.");
            colComment.onValueChange(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getValue(), colName.getValue(), colDefinition.textField.getValue(), event.value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        var btnAddNewCol = button("Add").width("fit-content");
        TextField colName = new TextField("New column name", "");
        var colDef = new TextField("New column definition", "");
        var colComment = new TextField("New column comment", "");
        listColumns.add(horizontal().add(btnAddNewCol, colName, colDef, colComment));
        btnAddNewCol.onClick(event -> { // enter pressed event
            try {
                Column col = new Column(colName.getValue());
                col.definition = colDef.getValue();
                col.comment = colComment.getValue();
                addNewColumn(listColumns, dbName, t.name, col);
                colName.setValue("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateColumn(Vertical listColumns, String dbName, String tableName, String oldName, String newName, String newDefinition, String newComment) throws IOException {
        AL.info("Updating column "+oldName+" -> "+newName);
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, oldName);
        Objects.requireNonNull(col);
        Column colOld = col.duplicate();

        t.updateCol(col, oldName, newName, newDefinition, newComment);

        if(!colOld.definition.equals(newDefinition))
            AL.info("Updating column definition "+colOld.definition+ " -> " + newDefinition);
        Data.save();
        AL.info("OK!");
    }

    private void addNewColumn(Vertical listColumns, String dbName, String tableName, Column col) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        col.id = Main.idCounter.getAndIncrement();
        t.addCol(col);

        Data.save();
        updateColumnsList(listColumns, dbName, tableName);
    }

    private void deleteColumn(Vertical listColumns, String dbName, String tableName, String columnName) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, columnName);
        Objects.requireNonNull(col);

        t.removeCol(col);

        Data.save();
        updateColumnsList(listColumns, dbName, tableName);
    }
}
