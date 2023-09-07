package com.osiris.jsqlgen;

import com.osiris.desku.App;
import com.osiris.jlib.logger.AL;


public class Main {
    public static MainView mainView = new MainView();
    public static void main(String[] args) {
        App.name = "jSQL-Gen";
        // Create and show windows
        try{
            App.uis.create(mainView);
        } catch (Exception e) {
            AL.error(e);
        }
    }
}
