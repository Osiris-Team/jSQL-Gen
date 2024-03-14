package com.osiris.jsqlgen;

import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Rectangle;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class DataJson {
    public Rectangle window = new Rectangle(10, 10, 600, 400);
    public CopyOnWriteArrayList<Database> databases = new CopyOnWriteArrayList<>();
}
