package com.osiris.jsqlgen.home_minimal;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.layout.Vertical;

import java.io.IOException;

import static com.osiris.desku.Statics.*;

public class HomeMinimal extends Route {
    static {
        try {
            App.appendToGlobalCSS(App.getCSS(HomeMinimal.class));
            App.appendToGlobalJS(App.getJS(HomeMinimal.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HomeMinimal() {
        super("/");
    }

    @Override
    public Component<?,?> loadContent() {
        Vertical ly = vertical().childGap(true).padding(true);
        ly.add(text("Hello World!"));

        // USE THIS FOLDER AS A TEMPLATE TO QUICKLY CREATE NEW ROUTES, THUS DO NOT EDIT THIS DIRECTLY,
        // INSTEAD DUPLICATE THIS FOLDER AND RENAME IT.

        // ALSO REMEMBER TO REGISTER IT BY CREATING AN INSTANCE OF IT AT LEAST ONCE SOMEWHERE
        // PREFERABLY IN THE MAIN CLASS AS A PUBLIC STATIC FIELD.

        // HOME_FULL CONTAINS EXAMPLE USAGES OF MANY COMPONENTS, THUS ALSO KEEP
        // THAT FOLDER TO QUICKLY COPY AND PASTE EXAMPLES INTO YOUR CODE.
        return ly;
    }
}
