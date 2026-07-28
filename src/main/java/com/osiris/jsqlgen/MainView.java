package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.generator.*;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;
import com.osiris.jsqlgen.ui.LayoutDatabaseOptions;
import com.osiris.jsqlgen.ui.hours.HoursOrganizerView;
import com.osiris.jsqlgen.ui.timer.ButtonsTasks;
import com.osiris.jsqlgen.ui.timer.Sliders;
import com.osiris.jsqlgen.ui.timer.Timer;
import com.osiris.jsqlgen.utils.UFile;
import com.osiris.osiris_vaadin_utils.ui.popups.Popup;
import com.osiris.osiris_vaadin_utils.ui.tabs.LayoutTabs;
import com.osiris.osiris_vaadin_utils.ui.texts.Text;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.fusesource.jansi.utils.UtilsAnsiHtml;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.osiris.jsqlgen.Data.backupDir;
import static com.osiris.jsqlgen.Data.getDatabaseFile;
import static com.osiris.jsqlgen.Data.getDatabaseStructureFile;
import static com.osiris.jsqlgen.Data.getFormatter;
import static com.osiris.jsqlgen.Data.getJavaProjectGenDir;
import static com.osiris.jsqlgen.Data.getOldAndNewDBsMap;
import static com.osiris.jsqlgen.Data.instance;
import static com.osiris.jsqlgen.Data.parser;
import static com.osiris.jsqlgen.Data.save;


@Route("") // This makes it the default view in Vaadin Flow
public class MainView extends VerticalLayout { // Changed from Desku Vertical to Vaadin VerticalLayout

    // Home panel
    private final VerticalLayout lyHome = new VerticalLayout();
    private final TextField dbName = new TextField();
    {
        dbName.setPlaceholder("Enter database name");
    }
    private final Button btnCreateDatabase = new Button("Create");
    private final Button btnDeleteDatabase = new Button("Delete");
    private final Button btnImportDatabase = new Button("Import");
    private final Button btnExportDatabase = new Button("Export");
    private final Button btnMergeDatabasesFromDir = new Button("Merge from Projects");
    private final Button btnShowData = new Button("Open data folder");
    private final Div txtLogs = new Div(); // Vaadin Div for logs
    {
        txtLogs.setId("log-container");
    }
    // Database panel
    private final Select<String> dbSelector = new Select<>(); // Vaadin Select for dropdown
    private final VerticalLayout listTables = new VerticalLayout();
    private final LayoutTabs tabsTableAndCode = new LayoutTabs(); // Vaadin Tabs for code display
    private final LayoutTabs tabsCode = new LayoutTabs(); // Div to hold tab content
    private final Button btnGenerate = new Button("Generate Code");
    {
        btnGenerate.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    }
    private final FileChooserVaadin chooserJavaProjectDir;

    // Injected Spring components
    private final LayoutDatabaseOptions lyDatabaseOptions = new LayoutDatabaseOptions();

    private final Sliders sliders = new Sliders(); // For SlidersPopup constructor
    private final ButtonsTasks buttonsTasks = new ButtonsTasks(sliders); // For SlidersPopup constructor
    private final Timer timer = new Timer(sliders, buttonsTasks);

    public MainView() {
        // Vaadin CSS is typically handled in shared-styles.html or themes
        // No direct equivalent of Desku's App.appendToGlobalCSS within a component
        // You would define these styles in frontend/themes/your-theme-name/styles.css
        // or using @CssImport in your component if they are specific.
        // For demonstration, I'll add a dummy class name.
        listTables.addClassName("list-tables-custom");

        // UI Initialization from Desku's DesktopUI specific parts.
        // Vaadin Flow is web-based, so JFrame specific logic is removed.
        // Window state (maximize, position, size) is managed by the browser.

        // Initial setup for the main layout
        setPadding(true);
        setSpacing(true); // Equivalent to Desku's childGap
        setSizeFull(); // Make MainView take 100% of the viewport

        // Setup log area
        txtLogs.getStyle().set("height", "25vh");
        txtLogs.getStyle().set("overflow-y", "auto");
        txtLogs.getStyle().set("width", "100%");
        txtLogs.getStyle().set("background-color", "var(--lumo-contrast-5pct)"); // Light background for logs
        txtLogs.getStyle().set("font-family", "monospace");
        txtLogs.getStyle().set("padding", "var(--lumo-space-s)");
        txtLogs.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        chooserJavaProjectDir = getFileChooserVaadin();

        // Initial setup for the database selector
        dbSelector.setLabel("Select database to show");
        dbSelector.setPlaceholder("Select");
        dbSelector.addValueChangeListener(event -> {
            try {
                if (event.getValue() != null) {
                    changeDatabase(event.getValue());
                    updateChooserJavaProjectDir();
                }
            } catch (Exception ex) {
                AL.warn(ex);
            }
        });
        dbSelector.getElement().getStyle().set("font-size", "xx-large").set("font-weight", "bolder");


        tabsCode.getStyle().set("flex-grow", "1"); // Make pages take available space
        tabsCode.getStyle().set("display", "flex"); // Ensure children fill horizontal space
        tabsCode.getStyle().set("flex-direction", "column"); // Ensure children fill vertical space

        for (String seriousWarning : Data.seriousWarnings) {
            add(new Text(seriousWarning).setColor("red"));
        }

        LayoutTabs mainTabs = new LayoutTabs();
        mainTabs.addTabAndPage("Home", lyHome);
        mainTabs.addTabAndPage("Timer", timer);
        mainTabs.addTabAndPage("Hours", new HoursOrganizerView());
        add(mainTabs); // Put all content in a div and control visibility

        // Configure layouts
        layoutTop();
        layoutBottom();

        // Initial population of database selector
        updateDatabaseSelector();
        if (!Data.instance.databases.isEmpty()) {
            dbSelector.setValue(Data.instance.databases.get(0).name);
        }

        // Redirect console output to log Div
        UI ui = UI.getCurrent();
        ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        if (!Main.isInDepthDebugging) {
            String fn = """
                window.appendLogLine = function (html) {
                    const logContainer = document.querySelector('#log-container');
                    if (!logContainer) return;

                    const div = document.createElement('div');
                    div.innerHTML = html;
                    logContainer.appendChild(div);

                    logContainer.scrollTop = logContainer.scrollHeight;
                };
                """;
            var listener = new Consumer<String>() {
                @Override
                public void accept(String line) {
                    if(ui.isClosing()) {
                        Main.asyncIn.listeners.remove((Consumer<String>) this);
                        Main.asyncInErr.listeners.remove((Consumer<String>) this);
                        return;
                    }
                    String htmlLine = "<div></div>";
                    try {
                        htmlLine = new UtilsAnsiHtml().convertAnsiToHtml(line)
                            .replace("\"", "\\\"") // Escape quotes
                            .replace("\n", "")
                            .replace("\r", "");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String finalHtmlLine = htmlLine;
                    ui.access(() -> {
                        ui.getPage().executeJs(
                            fn+
                                "console.log('WOW');\n"+
                                "\nwindow.appendLogLine && window.appendLogLine(\"" + finalHtmlLine + "\");\n" +
                                "document.querySelector('#log-container').scrollTop = document.querySelector('#log-container').scrollHeight;"
                        );
                    });
                };
            };
            Main.asyncIn.listeners.add(listener);
            Main.asyncInErr.listeners.add(listener);
            AL.info("Registered log listeners.");
        }
        AL.info("Initialised jSQL-Gen Window successfully!");

        String version = "-";
        try {
            version = Const.getVersion();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
        String finalVersion = version;
        if (startsWithNumber(version)) {
            // In Vaadin, you'd typically set the page title in the @PageTitle annotation
            // or by VaadinSession.getCurrent().getUI().getPage().setTitle().
            // For a single view application, setting it on the UI object is fine.
            ui.getPage().setTitle("jSQL-Gen v" + finalVersion);
        } else {
            ui.getPage().setTitle("jSQL-Gen");
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Ensure initial data is loaded and UI is consistent on attach
        try {
            updateDatabaseSelector();
            if (!Data.instance.databases.isEmpty() && dbSelector.getValue() == null) {
                dbSelector.setValue(Data.instance.databases.get(0).name);
            }
            // If dbSelector already has a value, ensure associated layouts are updated
            if (dbSelector.getValue() != null) {
                changeDatabase(dbSelector.getValue());
                updateChooserJavaProjectDir();
            }
        } catch (Exception e) {
            AL.warn(e);
        }
    }

    private boolean startsWithNumber(String s) {
        return s != null && !s.isEmpty() && Character.isDigit(s.charAt(0));
    }

    private FileChooserVaadin getFileChooserVaadin() {
        FileChooserVaadin chooser = new FileChooserVaadin();
        chooser.addFileSelectedListener(file -> {
            try {
                Database database = getDatabaseOrFail();
                CopyOnWriteArrayList<File> dirs = database.getJavaProjectDirs();
                if (file.isDirectorySelected()) {
                    dirs.addIfAbsent(file.getFile());
                } else {
                    dirs.remove(file.getFile());
                }
                database.setJavaProjectDirs(dirs);
                save();
                AL.info("Set Java project directory/ies for database '" + database.name + "' to: " + dirs);
            } catch (Exception ex) {
                AL.warn("Failed to save data for java project dir.", ex);
            }
        });
        chooser.getSelectButton().setText("Select Java project directory/ies"); // Renamed button
        return chooser;
    }

    private void layoutTop() {
        lyHome.setPadding(true);
        lyHome.setSpacing(true);
        lyHome.setWidthFull();
        lyHome.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Buttons row 1
        HorizontalLayout buttonRow1 = new HorizontalLayout(btnImportDatabase, btnMergeDatabasesFromDir, btnExportDatabase, btnShowData);
        buttonRow1.setSpacing(true);
        btnImportDatabase.setTooltipText("Imports a json file or text and either overrides the existing database or creates a new one.");
        btnExportDatabase.setTooltipText("Exports the selected database in json format, which later can be imported again.");
        btnMergeDatabasesFromDir.setTooltipText("Searches the provided directory and \nsub-directories for databases and imports them.\n" +
                "If a database with the same name exists its replaced by the imported one, thus proceed with caution.\n" +
                "A backup of the current structure will be created though.");
        btnImportDatabase.addClickListener(e -> showComingSoonPopup("Import Database"));
        btnExportDatabase.addClickListener(e -> {
            Database db = Data.getDatabase(dbSelector.getValue());
            if (db == null) {
                AL.warn("Database '" + dbName + "' not found.");
                return;
            }
            new DatabaseExportDialog(db).open();
        });
        btnMergeDatabasesFromDir.addClickListener(this::handleMergeDatabasesFromDir);
        btnShowData.addClickListener(e -> {
            try {
                showData();
            } catch (IOException ex) {
                AL.warn("Failed to show data folder.", ex);
            }
        });

        // Database name input
        dbName.setWidthFull(); // Make it grow

        // Buttons row 2
        HorizontalLayout dbRow2 = new HorizontalLayout();
        dbRow2.setAlignItems(Alignment.END);
        dbRow2.add(btnCreateDatabase, btnDeleteDatabase);
        dbRow2.addAndExpand(dbName);
        dbRow2.setSpacing(true);
        btnCreateDatabase.addClickListener(e -> {
            try {
                addDatabase();
            } catch (IOException ex) {
                AL.warn("Failed to create database.", ex);
            }
        });
        btnDeleteDatabase.addClickListener(e -> {
            try {
                deleteDatabase(dbName.getValue());
            } catch (IOException ex) {
                AL.warn("Failed to delete database.", ex);
            }
        });

        lyHome.add(buttonRow1, dbRow2, txtLogs);
        lyHome.setFlexGrow(1, txtLogs); // Logs div takes remaining space
    }

    private void showComingSoonPopup(String featureName) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(featureName + " - Coming Soon!");
        dialog.add(new Paragraph("This feature (" + featureName + ") is currently in todo and will be available soon..."));
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void handleMergeDatabasesFromDir(ClickEvent<Button> clickEvent) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = getFormatter();
        File backup = new File(backupDir + "/backup-all-databases-" + now.format(formatter) + ".json");
        try {
            Files.writeString(backup.toPath(), parser.toJson(instance, DataJson.class));
            AL.info("Created backup: " + backup.getAbsolutePath());
        } catch (IOException e) {
            AL.warn("Failed to proceed, due to failed backup!", e);
            return;
        }

        Dialog chooserDialog = new Dialog();
        chooserDialog.setHeaderTitle("Select Directory to Merge Databases From");
        FileChooserVaadin localFileChooser = getFileChooserVaadin(); // Use local instance for dialog
        localFileChooser.getSelectButton().setText("Select directory");
        localFileChooser.addFileSelectedListener(fileSelection -> {
            File selectedFile = fileSelection.getFile();
            if (!selectedFile.isDirectory()) {
                selectedFile = selectedFile.getParentFile();
            }
            if (selectedFile == null) {
                AL.warn("No directory selected.");
                return;
            }

            AL.info("Importing, please stand by... Dir: " + selectedFile);
            File finalSelectedFile = selectedFile;
            AtomicInteger counter = new AtomicInteger();
            AtomicLong checkedFilesCounter = new AtomicLong();

            // Perform merge in a background thread to keep UI responsive
            Thread t1 = new Thread(() -> {
                walkRecursive(finalSelectedFile, file -> {
                    checkedFilesCounter.incrementAndGet();
                    if (file.getName().endsWith("_structure.json")) {
                        AL.info("Processing: " + file.getAbsolutePath());
                        Database db;
                        try {
                            db = parser.fromJson(new BufferedReader(new FileReader(file)), Database.class);
                        } catch (Exception e) {
                            AL.warn("Failed to parse database from " + file.getAbsolutePath(), e);
                            return;
                        }

                        // Check if a database with this name already exists in current data
                        Database existingDb = Data.getDatabase(db.name);
                        if (existingDb != null) {
                            // Compare and replace if the imported one is "newer" (e.g., more changes)
                            // The original code uses getOldAndNewDBsMap, which assumes a list of *new* DBs
                            // Here, we have just one `db` from file, so direct comparison might be simpler.
                            // A simple heuristic for "newer": more tables, or more columns in corresponding tables.
                            // Or, the original Desku code used getOldAndNewDBsMap to find if the *imported* db
                            // has a newer version of tables. I'll replicate that.
                            CopyOnWriteArrayList<Database> singleDbList = new CopyOnWriteArrayList<>();
                            singleDbList.add(db);
                            Map<Database, Data.DBWrapper> oldAndNewDBsMap = getOldAndNewDBsMap(new CopyOnWriteArrayList<>(Collections.singletonList(existingDb)), singleDbList);

                            if (!oldAndNewDBsMap.isEmpty()) {
                                // There is a newer version of existingDb in oldAndNewDBsMap
                                // Replace the existing database with the new one
                                instance.databases.replaceAll(d -> {
                                    if (d.name.equals(existingDb.name)) {
                                        counter.incrementAndGet();
                                        AL.info("Replaced " + existingDb.name + " with newer db from: " + file.getAbsolutePath());
                                        return oldAndNewDBsMap.get(existingDb).db; // Get the newer DB from the map
                                    }
                                    return d;
                                });
                                save();
                                getUI().ifPresent(ui -> ui.access(this::updateDatabaseSelector));
                            } else {
                                AL.info("Db " + db.name + " already exists and seems to be up-to-date or older.");
                            }
                        } else {
                            // New database, thus add/import
                            instance.databases.add(db);
                            save();
                            counter.incrementAndGet();
                            AL.info("Added new db " + db.name + " from: " + file.getAbsolutePath());
                            getUI().ifPresent(ui -> ui.access(this::updateDatabaseSelector));
                        }
                    }
                });
                getUI().ifPresent(ui -> ui.access(() -> {
                    AL.info("Merge complete! Added or replaced " + counter.get() + " databases. Scanned " + checkedFilesCounter.get() + " files.");
                    chooserDialog.close(); // Close dialog on completion
                }));
            });
            t1.start();
            // Provide feedback during merge process
            new Thread(() -> {
                try {
                    while (t1.isAlive()) {
                        Thread.sleep(3000);
                        getUI().ifPresent(ui -> ui.access(() -> {
                            AL.info("Scanned " + checkedFilesCounter.get() + " files...");
                        }));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    AL.warn("Merge progress monitor interrupted.", e);
                } catch (Exception e) {
                    AL.warn("Error in merge progress monitor.", e);
                }
            }).start();
        });
        chooserDialog.add(localFileChooser);
        chooserDialog.open();
    }


    private void walkRecursive(File file, Consumer<File> code) {
        if (file.isFile()) {
            code.accept(file);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    walkRecursive(f, code);
                }
            }
        }
    }

    private void layoutBottom() {
        chooserJavaProjectDir.getLabel().setTooltipText("(Optional) Select the directory of your Java project. Classes then will be generated there" +
                " together with a copy of the schema. Everything gets overwritten, except critical information in the database class." +
                " If not selected, files will be only shown in the \"Code\" tab.\n" +
                "Supports multiple directories.");

        // Add components to the database layout
        lyHome.add(dbSelector, btnGenerate, chooserJavaProjectDir, lyDatabaseOptions);

        btnGenerate.addClickListener(e -> {
            try {
                generateCode();
            } catch (IOException ex) {
                AL.warn("Failed to generate code.", ex);
            }
        });

        // Add tabs
        tabsTableAndCode.addTabAndPage("Tables", listTables);
        tabsTableAndCode.addTabAndPage("Code", tabsCode);

        // Make listTables scrollable
        listTables.getStyle().set("overflow-y", "auto");
        listTables.getStyle().set("max-height", "90vh"); // Limit height if needed
        listTables.setSpacing(true); // Add spacing between table entries
        listTables.setPadding(true);

        lyHome.add(tabsTableAndCode);
    }

    private void updateChooserJavaProjectDir() {
        try {
            Database database = getDatabaseOrFail();
            CopyOnWriteArrayList<File> javaProjectDirs = database.getJavaProjectDirs();
            boolean isChanged = false;
            CopyOnWriteArrayList<File> verifiedDirs = new CopyOnWriteArrayList<>();

            for (File projectDir : javaProjectDirs) {
                File p2 = projectDir;
                while (p2 != null && !p2.exists()) { // Find existing parent if original doesn't exist
                    p2 = p2.getParentFile();
                }

                if (p2 == null || !p2.exists()) {
                    AL.warn("Removed directory since it doesn't exist: " + projectDir);
                    isChanged = true;
                } else if (!verifiedDirs.contains(p2)) { // Avoid duplicates
                    verifiedDirs.add(p2);
                } else {
                    AL.warn("Removed duplicate directory: " + p2);
                    isChanged = true;
                }
            }

            if (isChanged || javaProjectDirs.size() != verifiedDirs.size()) {
                database.setJavaProjectDirs(verifiedDirs);
                Data.save();
                AL.info("Set Java project directory for database '" + database.name + "' to: " + verifiedDirs);
            }

            // Update UI with current values
            chooserJavaProjectDir.setValue(verifiedDirs.stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.joining("; ")));

        } catch (Exception e) {
            AL.warn("Error updating Java project directory chooser.", e);
        }
    }

    private @NotNull Database getDatabaseOrFail() throws Exception {
        String selectedDbName = dbSelector.getValue();
        if (selectedDbName == null || selectedDbName.trim().isEmpty()) {
            throw new Exception("No database selected. Please create and select one.");
        }
        Database database = Data.getDatabase(selectedDbName);
        if (database == null) {
            throw new Exception("Failed to find database '" + selectedDbName + "', make sure you created and selected one before.");
        }
        return database;
    }

    private void changeDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.trim().isEmpty()) {
            AL.warn("Provided database name cannot be null or empty!");
            return;
        }
        Database db = Data.getDatabase(dbName);
        if (db == null) {
            AL.warn("Database '" + dbName + "' not found.");
            return;
        }
        updateTablesList(dbName);
        lyDatabaseOptions.setValue(db);
        AL.info("Selected database: " + dbName);
    }

    public void updateDatabaseSelector() {
        dbSelector.setItems(Data.instance.databases.stream().map(d -> d.name).collect(Collectors.toList()));
        // Attempt to restore previous selection if it still exists
        String lastValue = dbSelector.getValue();
        if (lastValue != null && Data.getDatabase(lastValue) != null) {
            dbSelector.setValue(lastValue);
        } else if (!Data.instance.databases.isEmpty()) {
            dbSelector.setValue(Data.instance.databases.get(0).name);
        } else {
            dbSelector.clear(); // No databases to select
        }
    }

    protected void addDatabase() throws IOException {
        String newDbName = dbName.getValue();
        if (newDbName == null || newDbName.trim().isEmpty()) {
            AL.warn("Database name cannot be null or empty!");
            return;
        }
        for (Database existingDb : Data.instance.databases) {
            if (existingDb.name.equalsIgnoreCase(newDbName)) {
                AL.warn("Database named '" + newDbName + "' already exists.");
                return;
            }
        }
        Database db = new Database();
        db.name = newDbName;
        Data.instance.databases.add(db);
        Data.save();
        updateDatabaseSelector();
        dbSelector.setValue(db.name); // Select the newly created database
        AL.info("Successfully added new database named '" + db.name + "'.");
    }

    private void deleteDatabase(String dbName) throws IOException {
        if (dbName == null || dbName.trim().isEmpty()) {
            AL.warn("Database name cannot be null or empty!");
            return;
        }
        Database db = Data.getDatabase(dbName);
        if (db == null) {
            AL.warn("Database '" + dbName + "' not found to delete.");
            return;
        }
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
            Database selectedDb = getDatabaseOrFail();
            List<File> files = generateCode(Collections.singletonList(selectedDb), Main.generatedDir, true);
            AL.info("Generated code/files: ");
            for (File f : files) {
                AL.info(f.getAbsolutePath());
            }
            // Refresh table view
            updateTablesList(dbSelector.getValue());

            // Refresh tabs
            tabsCode.getTabsAndPages().forEach((tab, page) -> {
                tab.removeFromParent();
                page.removeFromParent();
            });
            // Re-add Tables tab
            for (File f : files) {
                TextArea fileContent = new TextArea(); // Vaadin TextArea
                fileContent.setValue(Files.readString(f.toPath()));
                fileContent.setReadOnly(true); // Make it read-only
                fileContent.setSizeFull(); // Take full space
                //fileTab.setComponent(fileContent); // Attach component to tab

                tabsCode.addTabAndPage(f.getName(), fileContent);
            }
            AL.info("Code generation complete.");
        } catch (Exception e) {
            AL.warn("Failed to generate code.", e);
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
            if (!db.getJavaProjectDirs().isEmpty()) {
                dirs = getJavaProjectGenDir(db);
            }
            for (Data.JavaProjectGenDir dir : dirs) {
                dir.mkdirs();
            }


            // Write Database class files and Tables files
            for (Data.JavaProjectGenDir javaProjectGenDir : dirs) {
                JavaCodeGenerator.prepareTables(db);

                File databaseFile = getDatabaseFile(javaProjectGenDir);
                String url = "\"jdbc:mysql://localhost:3306/" + db.name + "\"";
                String rawUrl = "getRawDbUrlFrom(url)";
                String name = "\"" + db.name + "\"";
                String username = "\"\"";
                String password = "\"\"";
                if (databaseFile.exists()) {
                    AL.info("Reading database class file at: " + databaseFile);
                    CompilationUnit unit = StaticJavaParser.parse(Files.readString(databaseFile.toPath()));
                    for (FieldDeclaration field : unit.findAll(FieldDeclaration.class)) {
                        VariableDeclarator var = field.getVariable(0);
                        if (var.getInitializer().isPresent()) {
                            Expression varInit = var.getInitializer().get();
                            if (Objects.equals(var.getName().asString(), "rawUrl"))
                                if (varInit.isStringLiteralExpr()) rawUrl = "\"" + varInit.asStringLiteralExpr().asString() + "\"";
                                else rawUrl = varInit.toString();
                            else if (Objects.equals(var.getName().asString(), "url"))
                                if (varInit.isStringLiteralExpr()) url = "\"" + varInit.asStringLiteralExpr().asString() + "\"";
                                else url = varInit.toString();
                            else if (Objects.equals(var.getName().asString(), "name"))
                                if (varInit.isStringLiteralExpr()) name = "\"" + varInit.asStringLiteralExpr().asString() + "\"";
                                else name = varInit.toString();
                            else if (Objects.equals(var.getName().asString(), "username"))
                                if (varInit.isStringLiteralExpr()) username = "\"" + varInit.asStringLiteralExpr().asString() + "\"";
                                else username = varInit.toString();
                            else if (Objects.equals(var.getName().asString(), "password"))
                                if (varInit.isStringLiteralExpr()) password = "\"" + varInit.asStringLiteralExpr().asString() + "\"";
                                else password = varInit.toString();
                        }
                    }
                    AL.info("Success, database class file is valid.");
                }
                databaseFile.createNewFile();
                GenDatabaseFile.s(db, databaseFile, rawUrl, url, name, username, password);
                files.add(databaseFile);

                new Thread(() -> {
                    try{
                        AL.info("Running translation check in async... - "+javaProjectGenDir);
                        TranslationsHelper.check(javaProjectGenDir, db);
                        AL.info("Finished translation check! - "+javaProjectGenDir);
                    } catch (Exception e) {
                        AL.warn(e);
                    }
                }).start();


                for (Table t : db.tables) {
                    File javaFile = new File(javaProjectGenDir + "/" + t.name + ".java");
                    javaFile.createNewFile();
                    files.add(javaFile);
                    Files.writeString(javaFile.toPath(), (!db.getJavaProjectDirs().isEmpty() ? "package com.osiris.jsqlgen." + db.name + ";\n" : "") +
                            GenTableFile.s(javaFile, t, db));
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
        if (db == null) return;

        CopyOnWriteArrayList<Table> tables = db.tables;
        for (int i = 0; i < tables.size(); i++) {
            Table t = tables.get(i);
            VerticalLayout wrapperTable = new VerticalLayout();
            wrapperTable.setPadding(false);
            wrapperTable.setSpacing(false); // No spacing for wrapper itself
            listTables.add(wrapperTable);

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.setWidthFull();
            hl.getStyle().set("background-color", "rgba(" + new java.util.Random().nextInt(250) + "," + new java.util.Random().nextInt(250) + "," + new java.util.Random().nextInt(250) + "," + 0.7 + ")");
            hl.addClassName("rounded"); // Apply Vaadin styles for rounded corners if defined

            // Action Select
            Select<String> choiceAction = new Select<>();
            choiceAction.setItems("Delete", "Duplicate", "Insert above", "Insert below");
            choiceAction.setPlaceholder("Actions");
            choiceAction.setWidth("100px"); // Adjust width as needed
            int finalI = i;
            choiceAction.addValueChangeListener(event -> {
                String command = event.getValue();
                if (command == null || command.isEmpty()) return;
                try {
                    if ("Delete".equals(command)) {
                        deleteTable(dbName, t.name);
                    } else if ("Duplicate".equals(command)) {
                        Table newT = t.duplicate();
                        var p = new Popup();
                        var tf = new TextField("", "Enter Table Name");
                        tf.setWidthFull();
                        tf.setValue("COPY_" + t.name);
                        p.setContent(tf);
                        var btn = new Button("Create");
                        btn.addClickListener(e -> {
                            p.close();
                            newT.name = tf.getValue();
                            newT.changes.clear();
                            TableChange currentTableChange = GetTableChange.get(newT, Data.instance.databases);
                            newT.changes.add(currentTableChange);
                            newT.currentChange = new TableChange(); // Reset too, since actual table object always has latest data
                            db.tables.add(finalI, newT);
                            try {
                                updateTablesList(dbName);
                            } catch (IOException ex) {
                                AL.warn(ex);
                            }
                            Data.save();
                        });
                        p.setYesBtn(btn);
                        p.buildAndOpen();
                    }
                    else if("Insert above".equals(command)){
                        Table newT = Table.create();
                        newT.name = "NEW_TABLE_" + t.id;
                        db.tables.add(finalI, newT);
                        updateTablesList(dbName);
                        Data.save();
                    } else if("Insert below".equals(command)){
                        Table newT = Table.create();
                        newT.name = "NEW_TABLE_" + t.id;
                        db.tables.add(finalI + 1, newT);
                        updateTablesList(dbName);
                        Data.save();
                    }
                    else {
                        throw new Exception("Unknown command '" + command + "' to modify table!");
                    }
                } catch (Exception e) {
                    AL.warn("Error performing table action.", e);
                } finally {
                    choiceAction.clear(); // Clear selection after action
                }
            });
            hl.add(choiceAction);

            TextField tableName = new TextField();
            tableName.setValue(t.name);
            tableName.setWidth("60%"); // Fixed width, adjust as needed
            tableName.getElement().getStyle().set("font-weight", "bold").set("font-size", "larger");
            tableName.setTooltipText("The table name. Changes are auto-saved.");
            tableName.addValueChangeListener(e -> {
                try {
                    renameTable(dbName, t.name, e.getValue());
                } catch (Exception ex) {
                    AL.warn("Failed to rename table.", ex);
                }
            });
            hl.add(tableName);

            Checkbox isDebug = new Checkbox("Debug");
            isDebug.setTooltipText("If selected generates additional debug logging to the.warn stream.");
            isDebug.setValue(t.isDebug);
            isDebug.addValueChangeListener(event -> {
                t.isDebug = event.getValue();
                Data.save();
            });
            hl.add(isDebug);

            Checkbox isNoExceptions = new Checkbox("No exceptions");
            isNoExceptions.setTooltipText("If selected catches SQL exceptions and throws runtime exceptions instead," +
                    " which means that all methods of a generated class can be used outside of try/catch blocks.");
            isNoExceptions.setValue(t.isNoExceptions);
            isNoExceptions.addValueChangeListener(event -> {
                t.isNoExceptions = event.getValue();
                Data.save();
            });
            hl.add(isNoExceptions);

            Checkbox isCache = new Checkbox("Cache");
            isCache.setValue(t.isCache);
            isCache.addValueChangeListener(event -> {
                t.isCache = event.getValue();
                Data.save();
            });
            hl.add(isCache);

            Checkbox isVaadinFlow = new Checkbox("Vaadin-Flow");
            isVaadinFlow.setValue(t.isVaadinFlowUI);
            isVaadinFlow.addValueChangeListener(event -> {
                t.isVaadinFlowUI = event.getValue();
                Data.save();
            });
            hl.add(isVaadinFlow);

            wrapperTable.add(hl);

            VerticalLayout listColumns = new VerticalLayout();
            listColumns.setPadding(false); // No internal padding
            listColumns.setSpacing(false); // Spacing between columns
            listColumns.getStyle().set("padding-left", "50px"); // Indent columns
            wrapperTable.add(listColumns);

            try {
                updateColumnsList(listColumns, dbName, t.name);
            } catch (Exception e) {
                AL.warn("Failed to update columns list for table " + t.name, e);
            }
        }
        // Add New Table section
        VerticalLayout addNewTableWrapper = new VerticalLayout();
        addNewTableWrapper.setPadding(true);
        addNewTableWrapper.setSpacing(true);
        addNewTableWrapper.setWidthFull();
        listTables.add(addNewTableWrapper);

        HorizontalLayout addNewTableLayout = new HorizontalLayout();
        addNewTableLayout.setSpacing(true);
        addNewTableLayout.setWidthFull();
        addNewTableLayout.setAlignItems(FlexComponent.Alignment.BASELINE); // Align textfields and button

        TextField newTableNameField = new TextField("New table name");
        newTableNameField.setWidthFull();

        Button btnAddNewTable = new Button("Add");
        btnAddNewTable.addClickListener(event -> {
            try {
                addNewTable(dbName, newTableNameField.getValue());
                newTableNameField.clear(); // Clear field after adding
            } catch (Exception e) {
                AL.warn("Failed to add new table.", e);
            }
        });

        addNewTableLayout.add(btnAddNewTable, newTableNameField);
        addNewTableLayout.setFlexGrow(1, newTableNameField); // Make textfield grow
        addNewTableWrapper.add(addNewTableLayout);
    }

    private void renameTable(String dbName, String oldName, String newName) throws IOException {
        AL.info("Renaming table from '" + oldName + "' to '" + newName + "'.");
        Database db = Data.getDatabase(dbName);
        Objects.requireNonNull(oldName);
        Table t = Data.findTable(db.tables, oldName);
        Objects.requireNonNull(t);
        t.name = newName;

        // Update current change
        t.currentChange.oldTableName = oldName;
        t.currentChange.newTableName = newName;

        Data.save();
        AL.info("OK!");
        // Refresh UI to reflect change
        updateTablesList(dbName);
    }

    private void addNewTable(String dbName, String tableName) throws IOException {
        Database db = Data.getDatabase(dbName);
        for (Table table : db.tables) {
            if (table.name.equalsIgnoreCase(tableName))
                throw new IOException("Table '" + tableName.toLowerCase() + "' already exists for this database!");
        }
        Table t = new Table();
        t.id = Main.idCounter.getAndIncrement();
        t.addIdColumn();
        db.tables.add(t);
        t.name = tableName;

        // Update current change
        t.currentChange.newTableName = tableName;
        t.currentChange.oldTableName = tableName; // For new tables, old name is also new name

        Data.save();
        updateTablesList(dbName);
        AL.info("Added new table: " + tableName);
    }

    private void deleteTable(String dbName, String tableName) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        db.tables.remove(t);
        Data.save();
        updateTablesList(dbName);
        AL.info("Deleted table: " + tableName);
    }

    private void updateColumnsList(VerticalLayout listColumns, String dbName, String tableName) throws IOException {
        listColumns.removeAll();
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        for (int i = 0; i < t.columns.size(); i++) {
            Column col = t.columns.get(i);
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.setPadding(false);
            hl.setWidthFull();
            hl.addClassName("table-hover-item"); // For CSS hover effect

            Button btnMoveUp = new Button(VaadinIcon.ARROW_UP.create());
            btnMoveUp.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_CONTRAST); // Secondary style
            hl.add(btnMoveUp);
            if (Objects.equals(col.name, "id")) btnMoveUp.setEnabled(false);
            int finalI = i; // Effective final variable for lambda
            btnMoveUp.addClickListener(event -> {
                try {
                    int oldI = finalI;
                    t.columns.remove(oldI);
                    int newIndex = oldI - 1;
                    if (newIndex < 1) newIndex = 1; // 1 because id is always at index 0
                    t.columns.add(newIndex, col);
                    Data.save();
                    updateColumnsList(listColumns, dbName, tableName);
                } catch (Exception e) {
                    AL.warn("Failed to move column up.", e);
                }
            });

            Button btnMoveDown = new Button(VaadinIcon.ARROW_DOWN.create());
            btnMoveDown.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_CONTRAST); // Secondary style
            hl.add(btnMoveDown);
            if (Objects.equals(col.name, "id")) btnMoveDown.setEnabled(false);
            btnMoveDown.addClickListener(event -> {
                try {
                    int oldI = finalI;
                    t.columns.remove(oldI);
                    int newIndex = oldI + 1;
                    if (newIndex > t.columns.size()) newIndex = t.columns.size();
                    t.columns.add(newIndex, col);
                    Data.save();
                    updateColumnsList(listColumns, dbName, tableName);
                } catch (Exception e) {
                    AL.warn("Failed to move column down.", e);
                }
            });

            Button btnRemove = new Button(VaadinIcon.TRASH.create());
            btnRemove.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR); // Danger style
            hl.add(btnRemove);
            if (Objects.equals(col.name, "id")) btnRemove.setEnabled(false);
            btnRemove.addClickListener(event -> {
                try {
                    deleteColumn(listColumns, dbName, t.name, col.name);
                } catch (Exception e) {
                    AL.warn("Failed to delete column.", e);
                }
            });

            TextField colName = new TextField("", "Column name");
            colName.setValue(col.name == null ? "" : col.name);
            colName.getElement().getStyle().set("font-weight", "bold");
            colName.setWidthFull(); // Make it grow
            if (Objects.equals(col.name, "id")) colName.setEnabled(false);
            colName.setTooltipText("Column name. Changes are auto-saved.");

            // Using Vaadin's TextField with a custom suggestion provider or a ComboBox might be options.
            // For simplicity, a regular TextField for definition for now, assuming user knows SQL types.
            // A custom component could extend TextField to add suggestions.
            TextField colDefinition = new TextField("","Column definition");
            colDefinition.setValue(col.definition == null ? "" : col.definition);
            colDefinition.setWidthFull(); // Make it grow
            colDefinition.setTooltipText("Column definition. Changes are auto-saved.");

            TextField colComment = new TextField("","Column comment");
            colComment.setValue(col.comment == null ? "" : col.comment);
            colComment.setWidthFull(); // Make it grow
            colComment.setTooltipText("Column comment. Changes are auto-saved.");

            colName.addValueChangeListener(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, event.getOldValue(), event.getValue(), col.definition, col.comment);
                } catch (Exception e) {
                    AL.warn("Failed to update column name.", e);
                }
            });
            hl.add(colName);
            colDefinition.addValueChangeListener(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getValue(), colName.getValue(), event.getValue(), colComment.getValue());
                } catch (Exception e) {
                    AL.warn("Failed to update column definition.", e);
                }
            });
            hl.add(colDefinition);
            colComment.addValueChangeListener(event -> {
                try {
                    updateColumn(listColumns, dbName, t.name, colName.getValue(), colName.getValue(), colDefinition.getValue(), event.getValue());
                } catch (Exception e) {
                    AL.warn("Failed to update column comment.", e);
                }
            });
            hl.add(colComment);
            listColumns.add(hl);

            // Add New Column section
            HorizontalLayout addNewColLayout = new HorizontalLayout();
            addNewColLayout.setSpacing(true);
            addNewColLayout.setWidthFull();
            addNewColLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

            Button btnAddNewCol = new Button("Add");
            TextField newColNameField = new TextField("New column name", "");
            TextField newColDefField = new TextField("New column definition", "");
            TextField newColCommentField = new TextField("New column comment", "");

            btnAddNewCol.addClickListener(event -> {
                try {
                    Column col2 = new Column(newColNameField.getValue());
                    col2.definition = newColDefField.getValue();
                    col2.comment = newColCommentField.getValue();
                    addNewColumn(listColumns, dbName, t.name, col2, col);
                    newColNameField.clear();
                    newColDefField.clear();
                    newColCommentField.clear();
                } catch (Exception e) {
                    AL.warn("Failed to add new column.", e);
                }
            });

            addNewColLayout.add(newColNameField, newColDefField, newColCommentField);
            addNewColLayout.setFlexGrow(1, newColNameField, newColDefField, newColCommentField); // Make textfields grow

            var btnOpenPopup = new Button("+");
            btnOpenPopup.setMaxHeight("10px");
            btnOpenPopup.addClickListener(e -> {
                var p = new Popup();
                p.setMinWidth("50vw");
                p.setContent(addNewColLayout);
                p.setYesBtn(btnAddNewCol);
                btnAddNewCol.addClickListener(e1 -> p.close());
                p.buildAndOpen();
            });
            listColumns.setFlexGrow(1, btnOpenPopup);
            listColumns.add(btnOpenPopup);
        }
    }

    private void updateColumn(VerticalLayout listColumns, String dbName, String tableName, String oldName, String newName, String newDefinition, String newComment) throws IOException {
        AL.info("Updated column " + newName);
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, oldName);
        Objects.requireNonNull(col);
        Column colOld = col.duplicate(); // Create a copy for comparison

        t.updateCol(col, oldName, newName, newDefinition, newComment);

        if (!colOld.name.equals(newName))
            AL.info("Updated column name " + colOld.name + " -> " + newName);
        if (!colOld.definition.equals(newDefinition))
            AL.info("Updated column definition " + colOld.definition + " -> " + newDefinition);
        if (!colOld.comment.equals(newComment))
            AL.info("Updated column comment " + colOld.comment + " -> " + newComment);
        Data.save();
        // Refresh the columns list if needed, particularly if order or count changes
        // For simple updates, the text field itself is updated by Vaadin.
        // If the update involves a structural change that affects other columns or order, re-render.
        // updateColumnsList(listColumns, dbName, tableName); // Uncomment if a full re-render is desired for any change.
    }

    private void addNewColumn(VerticalLayout listColumns, String dbName, String tableName, Column col, Column colAnker) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        col.id = Main.idCounter.getAndIncrement();
        if(colAnker != null) t.insertCol(col, colAnker);
        else t.addCol(col);

        Data.save();
        updateColumnsList(listColumns, dbName, tableName);
        AL.info("Added new column: " + col.name + " to table " + tableName);
    }

    private void deleteColumn(VerticalLayout listColumns, String dbName, String tableName, String columnName) throws IOException {
        Database db = Data.getDatabase(dbName);
        Table t = Data.findTable(db.tables, tableName);
        Objects.requireNonNull(t);
        Column col = Data.findColumn(t.columns, columnName);
        Objects.requireNonNull(col);

        t.removeCol(t, col); // Logic relies on Table.removeCol to update internal structures

        Data.save();
        updateColumnsList(listColumns, dbName, tableName); // Re-render the columns list
        AL.info("Deleted column: " + columnName + " from table " + tableName);
    }

    // Helper for Vaadin FileChooser (simplified for this migration)
    // In a real Vaadin app, you'd use FileUpload or client-side JavaScript for full file system access.
    // This is a placeholder for `filechooser` from Desku.
    public class FileChooserVaadin extends HorizontalLayout {
        private final TextField selectedFilesTextField;
        private final Button selectButton;
        private Consumer<FileSelectionEvent> fileSelectedListener;

        public FileChooserVaadin() {
            setSpacing(true);
            setAlignItems(FlexComponent.Alignment.CENTER);
            setWidthFull();

            selectedFilesTextField = new TextField();
            selectedFilesTextField.setPlaceholder("No directory selected...");
            selectedFilesTextField.setReadOnly(true);
            selectedFilesTextField.setWidthFull();

            selectButton = new Button("Select File/Directory");
            selectButton.addClickListener(event -> {
                // In a real Vaadin app, you'd integrate with a client-side file picker
                // or a custom component that interacts with the user's file system.
                // For a server-side framework, direct file system access from the browser is not typical.
                // This is a placeholder that might open a server-side directory browser dialog
                // or simulate selection for demonstration.
                simulateFileSelection(); // Placeholder for actual file selection logic
            });

            add(selectedFilesTextField, selectButton);
            setFlexGrow(1, selectedFilesTextField);
        }

        public TextField getLabel() {
            return selectedFilesTextField; // Re-purposing for label display
        }

        public Button getSelectButton() {
            return selectButton;
        }

        public void setValue(String path) {
            selectedFilesTextField.setValue(path);
        }

        public void addFileSelectedListener(Consumer<FileSelectionEvent> listener) {
            this.fileSelectedListener = listener;
        }

        private void simulateFileSelection() {
            // This is a placeholder for actual file system interaction.
            // In a real application, you might use a client-side file input
            // and process the path on the server via an upload component,
            // or for desktop-like behavior, a custom integration.
            Dialog fileSelectDialog = new Dialog();

            TextField pathInput = new TextField("Enter directory path (e.g., C:\\Projects\\MyJavaProject)");
            pathInput.setWidthFull();

            Button confirmButton = new Button("Add Directory", e -> {
                String path = pathInput.getValue();
                if (path != null && !path.trim().isEmpty()) {
                    File selectedFile = new File(path);
                    if (fileSelectedListener != null) {
                        fileSelectedListener.accept(new FileSelectionEvent(selectedFile, selectedFile.isDirectory()));
                    }
                    updateChooserJavaProjectDir();
                }
                fileSelectDialog.close();
            });

            fileSelectDialog.add(new VerticalLayout(pathInput, new Div(confirmButton)));
            fileSelectDialog.open();
        }

        public static class FileSelectionEvent {
            private final File file;
            private final boolean isDirectorySelected;

            public FileSelectionEvent(File file, boolean isDirectorySelected) {
                this.file = file;
                this.isDirectorySelected = isDirectorySelected;
            }

            public File getFile() {
                return file;
            }

            public boolean isDirectorySelected() {
                return isDirectorySelected;
            }
        }
    }
}
