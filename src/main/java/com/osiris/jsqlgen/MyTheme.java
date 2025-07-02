package com.osiris.jsqlgen;

import com.osiris.desku.ui.css.CSS;
import com.osiris.desku.ui.css.Theme;

public class MyTheme extends Theme {
    public CSS.Attribute spaceXS = new CSS.Attribute("--bs-btn-bg", "var(--color-primary)");
    public MyTheme() {
        this.colorPrimary.setValue("rgba(33, 101, 101, 1.0)");
        this.colorPrimay50.setValue("rgba(33, 101, 101, 0.5)");
        this.colorPrimary10.setValue("rgba(33, 101, 101, 0.1)");
    }
}
