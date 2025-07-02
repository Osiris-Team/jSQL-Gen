package com.osiris.jsqlgen.ui.timer;

import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.ui.Refreshable;
import com.osiris.jlib.logger.AL;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


import java.sql.Timestamp;
import java.util.Objects;


public class LayoutTimer extends VerticalLayout implements Refreshable { // Changed from Desku Vertical to Vaadin VerticalLayout
    public volatile Timer existingTimer = null;
    public volatile Timestamp day = new Timestamp(System.currentTimeMillis());
    public static final long DAY_AS_MILLIS = 86400000;
    public static final long WEEK_AS_MILLIS = DAY_AS_MILLIS * 7;
    public static final long MONTH_AS_MILLIS = WEEK_AS_MILLIS * 4;
    public static final AFKDetector afk = new AFKDetector((__) -> {}, (__) -> {});

    private volatile boolean isPendingAFKPopup = false;

    private Button btnStartStop; // Vaadin Button
    private TimerSummary txtSummary; // Vaadin Component
    private TimerDetails lyTimerDetails; // Vaadin Component

    private LayoutSliders layoutSliders;
    private LayoutButtonsTasks layoutButtonsTasks;

    public LayoutTimer(LayoutSliders layoutSliders, LayoutButtonsTasks layoutButtonsTasks) {
        setPadding(true);
        setSpacing(true); // Equivalent to Desku's childGap
        setWidthFull();
        setHeightFull(); // To fill the tab content
        setAlignItems(FlexComponent.Alignment.STRETCH); // Children stretch horizontally

        // Initialize components
        btnStartStop = new Button();
        txtSummary = new TimerSummary(day);
        lyTimerDetails = new TimerDetails(layoutSliders, layoutButtonsTasks);
        lyTimerDetails.setValue(day);

        // AFK Detector integration with Vaadin UI.getCurrent().access()
        afk.onAFK = (msLastActivity) -> {
            UI.getCurrent().access(() -> {
                // Force stop latest timer, and restart onBack
                for (Timer timer : Timer.whereId().biggestFirst().limit(1).get()) {
                    if(timer.end.equals(Timer.NULL)){
                        timer.end = new Timestamp(msLastActivity);
                        timer.update();
                        isPendingAFKPopup = true;
                    }
                }
            });
        };
        afk.onBack = (msLastActivity) -> {
            UI.getCurrent().access(() -> {
                if(!isPendingAFKPopup) return;
                isPendingAFKPopup = false;

                // Vaadin doesn't have a direct 'maximize' or 'focus' equivalent for the entire window
                // as it's a web application. This part needs to be handled by the browser or OS.
                // If it were a desktop app using Vaadin Embed, then it might be possible.
                // For a web app, this typically means bringing the browser tab to foreground,
                // which is outside the control of server-side Vaadin.
                AL.info("Returning from AFK. (DesktopUI maximize/focus skipped for Vaadin Web)");

                Timer timer = Timer.whereId().biggestFirst().limit(1).getFirstOrNull();
                Objects.requireNonNull(timer);
                if(msLastActivity <= 0) throw new RuntimeException("msLastActivity="+msLastActivity+" for timer="+timer.toPrintString());
                if(timer.end == Timer.NULL){
                    timer.end = new Timestamp(msLastActivity);
                    timer.update();
                }
                // Open the popup to determine the previous actual work timer work amount
                add(new SlidersPopup(true, timer, layoutSliders, layoutButtonsTasks)); // Add popup to the current layout
                // Create a timer for the AFK portion
                timer = Timer.createAndAdd(new Timestamp(msLastActivity), new Timestamp(System.currentTimeMillis()));
                add(new SlidersPopup(true, timer, layoutSliders, layoutButtonsTasks)); // Add another popup for AFK duration
            });
        };
        refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Ensure UI updates reflect immediately when attached
        refresh();
    }

    @Override
    public void refresh() {
        this.removeAll(); // Clear existing components

        // Check db if the last timer is still running
        String btnStartStopLabel = "Start";
        for (Timer timer : Timer.whereId().biggestFirst().limit(1).get()) {
            if(timer.start.equals(Timer.NULL)){
                // Not started
                timer.start = new Timestamp(System.currentTimeMillis());
                timer.update();
                existingTimer = timer;
            }
            if(timer.end.equals(Timer.NULL)){
                // Not ended
                existingTimer = timer;
                btnStartStopLabel = "Started at "+existingTimer.start+", click to stop.";
            }
        }

        btnStartStop.setText(btnStartStopLabel); // Set button text
        btnStartStop.addClickListener(e -> { // Vaadin click listener
            if(existingTimer == null){
                existingTimer = Timer.createAndAdd(new Timestamp(System.currentTimeMillis()), Timer.NULL);
                btnStartStop.setText("Started at "+existingTimer.start+", click to stop.");
            } else {
                // Expect a running timer, thus stop it
                if(existingTimer.end.equals(Timer.NULL)){
                    existingTimer.end = new Timestamp(System.currentTimeMillis());
                    existingTimer.update();
                    btnStartStop.setText("Start");
                    add(new SlidersPopup(false, existingTimer, layoutSliders, layoutButtonsTasks)); // Add popup to the current layout
                }
            }
            afk.startActivityMonitorIfNeeded();
        });

        if(existingTimer != null) afk.startActivityMonitorIfNeeded();

        HorizontalLayout summaryControls = new HorizontalLayout(); // Vaadin HorizontalLayout
        summaryControls.setWidthFull();
        summaryControls.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        summaryControls.setAlignItems(FlexComponent.Alignment.CENTER);
        summaryControls.setSpacing(true);

        summaryControls.add(new Button(VaadinIcon.ANGLE_DOUBLE_LEFT.create(), e -> { // -1 Month button
            day = new Timestamp(day.getTime() - MONTH_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summaryControls.add(new Button(VaadinIcon.ANGLE_LEFT.create(), e -> { // -1 Week button
            day = new Timestamp(day.getTime() - WEEK_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summaryControls.add(new Button(VaadinIcon.CARET_LEFT.create(), e -> { // -1 Day button
            day = new Timestamp(day.getTime() - DAY_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));

        txtSummary.setWidthFull(); // Make summary grow
        summaryControls.add(txtSummary);

        summaryControls.add(new Button(VaadinIcon.CARET_RIGHT.create(), e -> { // +1 Day button
            day = new Timestamp(day.getTime() + DAY_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summaryControls.add(new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> { // +1 Week button
            day = new Timestamp(day.getTime() + WEEK_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summaryControls.add(new Button(VaadinIcon.ANGLE_DOUBLE_RIGHT.create(), e -> { // +1 Month button
            day = new Timestamp(day.getTime() + MONTH_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));

        add(btnStartStop, summaryControls, lyTimerDetails);
    }

}
