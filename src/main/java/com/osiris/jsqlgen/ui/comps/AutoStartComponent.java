package com.osiris.jsqlgen.ui.comps;

import com.osiris.jsqlgen.utils.UtilsJar;
import com.osiris.jsqlgen.utils.UtilsNative;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.io.File;
import java.net.URISyntaxException;

/**
 * A Vaadin UI component that exposes a checkbox named "isStartOnBoot"
 * and uses UtilsNative + UtilsJar to control OS autostart.
 */
public class AutoStartComponent extends VerticalLayout {

    private final Checkbox isStartOnBoot = new Checkbox("Start on Boot");
    private final UtilsNative utilsNative;
    private final UtilsJar utilsJar;

    public AutoStartComponent() {
        this.utilsNative = new UtilsNative("jSQL-Gen"); // uses sensible defaults
        this.utilsJar = new UtilsJar();

        setPadding(true);
        setSpacing(true);

        add(isStartOnBoot);

        initState();
        initListeners();
    }

    /**
     * Reads system autostart state on load and updates checkbox.
     */
    private void initState() {
        try {
            boolean registered = utilsNative.isRegistered();
            isStartOnBoot.setValue(registered);
        } catch (Exception e) {
            Notification.show("Failed to read autostart state: " + e.getMessage(), 5000,
                Notification.Position.MIDDLE);
        }
    }

    /**
     * Adds listener to update autostart mode when checkbox is toggled.
     */
    private void initListeners() {
        isStartOnBoot.addValueChangeListener(event -> {
            try {
                File thisJar = null;
                try {
                    thisJar = utilsJar.getThisJar();
                } catch (NullPointerException e) {
                    //TODO extract zip automatically after build, and reference start script here
                    thisJar = new File(System.getProperty("user.dir")+"/target/"); // TODO script path
                    if(!thisJar.exists())
                        throw new NullPointerException();
                }

                if (event.getValue()) {
                    utilsNative.enableStartOnBootIfNeeded(thisJar);
                    Notification.show("Enabled start on boot", 3000, Notification.Position.BOTTOM_CENTER);
                } else {
                    utilsNative.disableStartOnBootIfNeeded();
                    Notification.show("Disabled start on boot", 3000, Notification.Position.BOTTOM_CENTER);
                }

            } catch (Exception e) {
                Notification.show("Failed to update autostart: " + e.getMessage(), 5000,
                    Notification.Position.MIDDLE);

                // revert UI state
                initState();
            }
        });
    }
}

