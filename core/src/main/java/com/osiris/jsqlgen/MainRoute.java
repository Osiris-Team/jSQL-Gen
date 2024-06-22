package com.osiris.jsqlgen;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.DesktopUI;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.*;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.SmartLayout;
import com.osiris.desku.ui.layout.TabLayout;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.generator.GenDatabaseFile;
import com.osiris.jsqlgen.generator.JavaCodeGenerator;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.ColumnType;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.utils.AsyncReader;
import com.osiris.jsqlgen.utils.MyTeeOutputStream;
import com.osiris.jsqlgen.utils.UFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.osiris.desku.Statics.*;
import static com.osiris.jsqlgen.Data.*;

public class MainRoute extends Route {

    public MainRoute() {
        super("/");
    }

    @Override
    public Component<?, ?> loadContent() {
        return new MainView();
    }
}
