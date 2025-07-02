package com.osiris.jsqlgen.model;

public class Rectangle {
    public double x, y, width, height;
    public boolean isMaximized = true;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
