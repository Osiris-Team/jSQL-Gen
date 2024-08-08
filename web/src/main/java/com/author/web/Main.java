package com.author.web;

import com.osiris.desku.App;
import com.osiris.desku.ui.DesktopUIManager;

public class Main {

    public static void main(String[] args) {
        try {
            App.uis = new DesktopUIManager();// TODO create WebUIManager that actually launches springboot
            com.osiris.jsqlgen.Main.main(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
