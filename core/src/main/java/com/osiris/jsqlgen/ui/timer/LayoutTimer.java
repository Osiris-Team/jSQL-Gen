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
                        ui.fullscreen(true);
                        ui.focus(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                Timer timer = Timer.whereId().biggestFirst().limit(1).getFirstOrNull();
                if(timer != null && timer.end != Timer.NULL){
                    add(new SlidersPopup(true, timer));
                }
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

    public static void fillBtnTasks(Timer timer, Horizontal lyBtnsTasks, Vertical lyWithTimerUI) {
        lyBtnsTasks.removeAll();
        for (Task task : Task.get()) {
            boolean alreadyAdded = false;
            for (Component child : lyWithTimerUI.children) {
                TimerTaskUI comp = (TimerTaskUI) child;
                if(comp.internalValue.taskId == task.id){
                    alreadyAdded = true;
                    break;
                }
            }

            // Add task
            lyBtnsTasks.add(button(task.name).onClick(e -> {
                TimerTask timerTask = TimerTask.whereTaskId().is(task.id).getFirstOrNull();
                if(timerTask != null){
                    AL.warn("Task '"+task.name+"' already exits in timer '"+timerTask.timerId+"', duplicate tasks in a single timer are not allowed.");
                    return;
                }
                TimerTask.createAndAdd(timer.id, task.id, 0);
            }).enable(!alreadyAdded));

            // Remove task
            if(task.id != Task.WORK.id && task.id != Task.PAUSE.id)
                lyBtnsTasks.add(button("").danger().add(Icon.solid_trash()).onClick(e -> {
                    TimerTask timerTask = TimerTask.whereTimerId().is(timer.id).and(
                        TimerTask.whereTaskId().is(task.id)
                    ).getFirstOrNull();
                    if(timerTask != null) timerTask.remove();
                }).setTooltip("This does not delete the task itself, but instead its entry/slider for this timer."));
        }
    }

    public static class TimerTaskUI extends Component<TimerTaskUI, TimerTask>{
        public Slider slider;
        public Task task;
        public String taskName;

        public TimerTaskUI(@UnknownNullability TimerTask timerTask) {
            super(timerTask, TimerTask.class, "c");
            task = Task.whereId().is(timerTask.taskId).getFirstOrNull();
            taskName = task == null ? "-Deleted-" : task.name;
            slider = new Slider(taskName, timerTask.percentageOfTimer).grow(1);
            add(slider);
        }
    }

    public static Vertical fillTimerTasks(Component ly, Timer timer) {
        ly.removeAll();

        for (TimerTask timerTask : TimerTask.whereTimerId().is(timer.id).get()) {
            TimerTaskUI comp = new TimerTaskUI(timerTask);
            ly.add(comp);

            var _this = comp.slider;
            AtomicReference<Double> refValueBefore = new AtomicReference<>(comp.slider.getValue());
            AtomicBoolean isHolding = new AtomicBoolean();
            UI.get().registerJSListener("mousedown", _this, (msg) -> {
                refValueBefore.set(comp.slider.getValue());
                isHolding.set(true);
            });
            UI.get().registerJSListener("mouseup", _this, (msg) -> {
                if(isHolding.get()){
                    isHolding.set(false);
                    //if(e.isProgrammatic) return; // ignore programmatic changes to avoid infinite loop

                    TimerTask v = comp.getValue();
                    double currentValue = comp.slider.getValue();
                    double valueBefore = refValueBefore.get();
                    v.percentageOfTimer = currentValue;
                    v.update();

                    // Decrease or increase the other sliders
                    double change = currentValue - valueBefore;
                    int relevantChildrenSize = ly.children.isEmpty() ? 1 : ly.children.size() - 1; // -1 because we dont include the current slider
                    double changePerSlider = change > 0 ?
                        change /  relevantChildrenSize * -1 : // Positive change, thus decrease others
                        Math.abs(change / relevantChildrenSize); // Negative change, thus increase others

                    for (Object child : ly.children) {
                        TimerTaskUI otherComp = (TimerTaskUI) child;
                        if(otherComp.slider == comp.slider) continue;

                        double newVal = otherComp.slider.getValue() + changePerSlider;
                        otherComp.slider.setValue(newVal);
                        v = otherComp.getValue();
                        v.percentageOfTimer = newVal;
                        v.update();
                        AL.info("Updated: "+v.toPrintString());
                    }
                }
            });
        }
        return null;
    }


}
