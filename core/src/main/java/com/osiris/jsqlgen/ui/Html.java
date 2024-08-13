package com.osiris.jsqlgen.ui;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;
import org.jetbrains.annotations.NotNull;

public class Html extends Component<Html, NoValue> {
    public Html(@NotNull String tag) {
        super(NoValue.GET, NoValue.class, tag);
    }
}
