package com.osiris.jsqlgen.model;

import java.io.File;
import java.util.ArrayList;

public class Database {
    public String name;
    public ArrayList<Table> tables = new ArrayList<>();
    public File javaProjectDir;
}
