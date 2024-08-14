package com.osiris.jsqlgen.ui.timer;

import com.osiris.desku.App;
import com.osiris.desku.Value;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.Slider;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class LayoutSliders extends Component<LayoutSliders, Timer> {
    public LayoutSliders(@UnknownNullability Timer timer) {
        super(timer, Timer.class, "c");
        childVertical();

        setValue(timer);

        UI ui = UI.get();
        TimerTask.onAdd.add((timerTask -> {
            add(getTimerTaskUI(timerTask));
        }));
        //TimerTask.onUpdate.add((val) -> {});
        TimerTask.onRemove.add(timerTask -> {
            for (Component obj : children) {
                TimerTaskUI child = (TimerTaskUI) obj;
                if(child.internalValue.id == timerTask.id) remove(child);
            }
        });
    }

    public List<TimerTask> timerTasks;

    @Override
    public LayoutSliders setValue(@Nullable Timer timer) {
        this.removeAll();

        timerTasks = TimerTask.whereTimerId().is(timer.id).get();

        for (TimerTask timerTask : timerTasks) {
            this.add(getTimerTaskUI(timerTask));
        }

        ensureSlidersTotal100();

        return super.setValue(timer);
    }

    private Component<?, ?> getTimerTaskUI(TimerTask timerTask) {
        TimerTaskUI comp = new TimerTaskUI(timerTask);
        var slider = comp.slider;
        AtomicReference<Double> refValueBefore = new AtomicReference<>(slider.getValue());
        AtomicBoolean isHolding = new AtomicBoolean();

        UI.get().registerJSListener("mousedown", slider, (msg) -> {
            refValueBefore.set(slider.getValue());
            isHolding.set(true);
        });

        UI.get().registerJSListener("mouseup", slider, (msg) -> {
            try{
                if (isHolding.get()) {
                    isHolding.set(false);

                    double currentValue = slider.getValue();
                    double valueBefore = refValueBefore.get();
                    double change = currentValue - valueBefore;

                    // Adjust other sliders proportionally
                    double remainingPercentage = 100.0 - currentValue;
                    double scaleFactor = remainingPercentage / (100.0 - currentValue);

                    if(this.children.size() != timerTasks.size())
                        throw new RuntimeException(this.children.size() +" != "+ timerTasks.size());
                    for (Object obj : this.children) {
                        TimerTaskUI otherComp = (TimerTaskUI) obj;
                        AL.info("EXPECTING GETVALUE() FROM "+otherComp.toPrintString());
                        TimerTask otherTask = debugGetValue(otherComp);
                        AL.info("SUCCESS "+otherComp.toPrintString());

                        if (otherComp.slider == slider) {
                            otherTask.percentageOfTimer = currentValue;
                        } else {
                            double newVal = otherComp.slider.getValue() * scaleFactor;
                            otherComp.slider.setValue(newVal);
                            otherTask.percentageOfTimer = newVal;
                        }

                        otherTask.update();
                        AL.info("Updated: " + otherTask.toPrintString());
                    }
                }
            } catch (Exception e) {
                AL.warn(e);
            }
        });
        return comp;
    }

    private TimerTask debugGetValue(TimerTaskUI comp) {
        UI ui = UI.get();
        AtomicReference<TimerTask> val = new AtomicReference<>();
        if(!comp.isAttached() || ui == null || ui.isLoading()) { // Since never attached once, user didn't have a chance to change the value, thus return internal directly
            val.set(comp.internalValue);
            if(App.isInDepthDebugging) AL.debug(comp.getClass(), comp.toPrintString()+" getValue() returns internalValue = "+ internalValue);
        }
        else{
            gatr("value", valueAsString -> {
                if(App.isInDepthDebugging) AL.debug(comp.getClass(), comp.toPrintString()+" getValue() returns from javascript value attribute = "+valueAsString);
                TimerTask value = null;
                try {
                    value = Value.stringToVal(valueAsString, comp);
                } catch (Exception e) {
                    AL.warn(e);
                }
                val.set(value);
            });
        }
        while (val.get() == null) Thread.yield();
        return val.get();
    }

    private void ensureSlidersTotal100() {
        if(this.children.isEmpty()) return;
        double total = 0.0;
        for (Component obj : this.children) {
            TimerTaskUI child = (TimerTaskUI) obj;
            double total1 = total + child.slider.getValue();
            if(total1 > 100.0){
                double newVal = 100.0 - total;
                child.slider.setValue(newVal);
                child.internalValue.percentageOfTimer = newVal;
                child.internalValue.update();
                total = 100.0;
            } else{
                total = total1;
            }
        }

        // If smaller 100, update first child with missing amount
        if(total < 100.0){
            TimerTaskUI firstChild = (TimerTaskUI) this.children.get(0);
            double newVal = 100.0 - total;
            firstChild.slider.setValue(newVal);
            firstChild.internalValue.percentageOfTimer = newVal;
            firstChild.internalValue.update();
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
}
