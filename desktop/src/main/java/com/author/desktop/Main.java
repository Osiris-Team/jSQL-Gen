package com.author.desktop;

import com.osiris.desku.App;
import com.osiris.desku.DesktopUIManager;

public class Main {

    public static void main(String[] args) {
        try {
            App.init(new DesktopUIManager());
            com.osiris.jsqlgen.Main.main(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
