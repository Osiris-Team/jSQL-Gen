package com.osiris.jsqlgen;

import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;

public class MainRoute extends Route {

    public MainRoute() {
        super("/");
    }

    @Override
    public Component<?, ?> loadContent() {
        return new MainView();
    }
}
