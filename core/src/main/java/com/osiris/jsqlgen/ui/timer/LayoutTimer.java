package com.osiris.jsqlgen.ui.timer;


import com.osiris.desku.Icon;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.DesktopUI;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.Slider;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.osiris.jsqlgen.ui.Refreshable;
import org.jetbrains.annotations.UnknownNullability;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.osiris.desku.Statics.*;

public class LayoutTimer extends Vertical implements Refreshable {
    public volatile Timer existingTimer = null;
    public volatile Timestamp day = new Timestamp(System.currentTimeMillis());
    public static final long DAY_AS_MILLIS = 86400000;
    public static final long WEEK_AS_MILLIS = DAY_AS_MILLIS * 7;
    public static final long MONTH_AS_MILLIS = WEEK_AS_MILLIS * 4;
    public static final AFKDetector afk = new AFKDetector((__) -> {}, (__) -> {});

    private volatile boolean isPendingAFKPopup = false;

    public LayoutTimer() {
        UI ui = UI.get();

        afk.onAFK = (msLastActivity) -> {
            ui.access(() -> {
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
            ui.access(() -> {
                // TODO what happens when user presses shutdown button without interacting with mouse/keyboard?
                // TODO what happens when user closes app with running timer
                if(!isPendingAFKPopup) return;
                isPendingAFKPopup = false;
                if(ui instanceof DesktopUI){
                    try{
                        ui.maximize(true);
                        ui.focus(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                Timer timer = Timer.whereId().biggestFirst().limit(1).getFirstOrNull();
                Objects.requireNonNull(timer);
                if(msLastActivity <= 0) throw new RuntimeException("msLastActivity="+msLastActivity+" for timer="+timer.toPrintString());
                if(timer.end == Timer.NULL){
                    timer.end = new Timestamp(msLastActivity);
                    timer.update();
                }
                // Open the popup to determine the previous actual work timer work amount
                add(new SlidersPopup(true, timer));
                // Create a timer for the AFK portion
                timer = Timer.createAndAdd(new Timestamp(msLastActivity), new Timestamp(System.currentTimeMillis()));
                add(new SlidersPopup(true, timer));
            });
        };
        refresh();
    }

    @Override
    public void refresh() {
        this.removeAll();

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
        // TODO automatically stop timer at PC shutdown

        var btnStartStop = button(btnStartStopLabel).onClick(e -> {
            if(existingTimer == null){
                existingTimer = Timer.createAndAdd(new Timestamp(System.currentTimeMillis()), Timer.NULL);
                e.comp.label.setValue("Started at "+existingTimer.start+", click to stop.");
            } else{
                // Expect a running timer, thus stop it
                if(existingTimer.end.equals(Timer.NULL)){
                    existingTimer.end = new Timestamp(System.currentTimeMillis());
                    existingTimer.update();
                    e.comp.setValue("Start");
                    add(new SlidersPopup(false, existingTimer));
                }
            }
            afk.startActivityMonitorIfNeeded();
        });
        if(existingTimer != null) afk.startActivityMonitorIfNeeded();

        var summary = horizontalCL();
        var txtSummary = new TimerSummary(day);
        var lyTimerDetails = new TimerDetails(day);
        summary.add(button("-1 Month").onClick(e -> {
            day = new Timestamp(day.getTime() - MONTH_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summary.add(button("-1 Week").onClick(e -> {
            day = new Timestamp(day.getTime() - WEEK_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summary.add(button("-1 Day").onClick(e -> {
            day = new Timestamp(day.getTime() - DAY_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summary.add(txtSummary.grow(4));
        summary.add(button("+1 Day").onClick(e -> {
            day = new Timestamp(day.getTime() + DAY_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summary.add(button("+1 Week").onClick(e -> {
            day = new Timestamp(day.getTime() + WEEK_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));
        summary.add(button("+1 Month").onClick(e -> {
            day = new Timestamp(day.getTime() + MONTH_AS_MILLIS);
            txtSummary.setValue(day);
            lyTimerDetails.setValue(day);
        }));

        add(btnStartStop, summary, lyTimerDetails);
    }

}
