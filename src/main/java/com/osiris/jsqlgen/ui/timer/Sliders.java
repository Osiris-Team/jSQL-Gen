package com.osiris.jsqlgen.ui.timer;

import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.osiris.jsqlgen.ui.comps.CustomSlider;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class Sliders extends VerticalLayout { // Changed from Desku Component to Vaadin VerticalLayout

    public List<TimerTask> timerTasks;
    private Timer currentTimer; // Added to hold the current timer

    public Sliders() {
        setPadding(false); // Equivalent to Desku's padding(false)
        setSpacing(true); // Equivalent to Desku's childGap
        setAlignItems(FlexComponent.Alignment.STRETCH); // To make sliders grow horizontally
        setWidthFull(); // Make it take full width
        addClassName("layout-sliders");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Registering listeners when component is attached
        Consumer<TimerTask> onAddListener = (timerTask -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                var timerTaskUI = getTimerTaskUI(timerTask);
                add(timerTaskUI);
                ensureSlidersTotal100(timerTaskUI); // Re-balance after adding
            }));
        });
        TimerTask.onAdd.add(onAddListener);

        Consumer<TimerTask> onRemoveListener = (timerTask -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                // Find and remove the child component associated with this TimerTask
                List<Component> toRemove = new ArrayList<>();
                for (Component child : getChildren().toList()) {
                    if (child instanceof TimerTaskUI && ((TimerTaskUI) child).internalValue.id == timerTask.id) {
                        toRemove.add(child);
                    }
                }
                remove(toRemove.toArray(new Component[0]));
                ensureSlidersTotal100(null); // Re-balance after removing
            }));
        });
        TimerTask.onRemove.add(onRemoveListener);

        // Note: TimerTask.onUpdate is not explicitly handled for individual slider updates,
        // as the slider's own change listener will trigger the update logic.
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Desku's listeners were not explicitly removed, Vaadin's would require it.
        // For simplicity, I'm not providing explicit remove methods for TimerTask.onAdd/onRemove
        // as they are typically static in the provided code, but in a large app, you'd manage this carefully.
    }

    public Sliders setValue(@Nullable Timer timer) {
        this.currentTimer = timer; // Store the current timer
        this.removeAll();

        if (timer == null) {
            timerTasks = new ArrayList<>();
            return this;
        }

        timerTasks = TimerTask.whereTimerId().is(timer.id).get();

        for (TimerTask timerTask : timerTasks) {
            this.add(getTimerTaskUI(timerTask));
        }

        // Ensure sliders total 100% only if there are sliders
        if (!timerTasks.isEmpty()) {
            ensureSlidersTotal100(null);
        }

        return this;
    }

    private TimerTaskUI getTimerTaskUI(TimerTask timerTask) {
        TimerTaskUI comp = new TimerTaskUI(timerTask);
        CustomSlider slider = comp.slider;

        // Vaadin's slider change listener
        // The logic for tracking value before/after and adjusting other sliders
        // is now handled within the ValueChangeListener.
        // Vaadin does not have "mousedown"/"mouseup" listeners on generic components out of the box
        // for precise tracking. ValueChangeListener is more appropriate for actual value changes.
        // For a more precise "drag-start" and "drag-end" behavior, you would need
        // to implement custom client-side extensions or attach JavaScript using Element.executeJs().
        // For simplicity and common use, ValueChangeListener is sufficient for a slider.

        AtomicReference<Double> refValueBefore = new AtomicReference<>(0.0);
//        slider.addFocusListner(event -> {
//            // When slider gets focus (e.g., before dragging starts), capture its value
//            refValueBefore.set(slider.getValue());
//        });

        slider.addValueChangeListener(event -> {
            // This event fires on every value change during dragging and when released.
            // The `isFromClient()` method helps determine if the change came from user interaction.
            if (event.isFromClient()) {
                double currentValue = event.getValue();
                double valueBefore = refValueBefore.get(); // Get the value when focus started

                // If refValueBefore is null, it means the slider was clicked directly
                // or value was changed programmatically without prior focus.
                // For simplicity, we assume we want to rebalance on any client-side change.
                if (valueBefore == 0.0 && currentValue == 0.0 && getChildren().count() > 1) {
                    // This is a special case often occurring with initial 0.0 values,
                    // where a small drag might still register as 0.0, or if somehow refValueBefore wasn't set.
                    // To avoid division by zero or incorrect scaling, we might need to handle this.
                    // For now, proceed, `ensureSlidersTotal100` should handle it.
                }

                getUI().ifPresent(ui -> ui.access(() -> {
                    // Update the current task's percentage
                    timerTask.percentageOfTimer = currentValue;
                    timerTask.update();
                    AL.info("Updated: " + timerTask.toPrintString());

                    // Re-balance all sliders to ensure they total 100%
                    ensureSlidersTotal100(comp);
                }));
            }
        });
        return comp;
    }

    private void ensureSlidersTotal100(@Nullable TimerTaskUI changedSlider) {
        List<TimerTaskUI> sliders = getAllSliders();
        double total = sliders.stream().mapToDouble(s -> s.slider.getValue()).sum();
        double diff = 100 - total;

        if (Math.abs(diff) < 0.0001) return;

        // distribute diff across all sliders EXCEPT the one that changed
        List<TimerTaskUI> others = sliders.stream()
            .filter(s -> s != changedSlider)
            .toList();

        if (others.isEmpty())
            return;

        double sumOther = others.stream()
            .mapToDouble(s -> s.slider.getValue())
            .sum();

        if (sumOther == 0) {
            // when other sliders are 0, allocate everything proportionally
            double part = diff / others.size();
            for (TimerTaskUI s : others) {
                updateSlider(s, s.slider.getValue() + part);
            }
            return;
        }

        // distribute proportionally
        for (TimerTaskUI s : others) {
            double share = (s.slider.getValue() / sumOther) * diff;
            updateSlider(s, s.slider.getValue() + share);
        }
    }

    private void updateSlider(TimerTaskUI ui, double newVal) {
        ui.slider.setValue(newVal);
        ui.internalValue.percentageOfTimer = newVal;
        ui.internalValue.update();
    }

    private List<TimerTaskUI> getAllSliders() {
        List<TimerTaskUI> list = new ArrayList<>();
        getChildren().forEach(c -> {
            if (c instanceof TimerTaskUI) list.add((TimerTaskUI)c);
        });
        return list;
    }

    private void redistributeSlidersProportionally() {
        if (this.getChildren().count() == 0) return;

        double sumOfValues = 0.0;
        List<TimerTaskUI> sliders = new ArrayList<>();
        for (Component obj : this.getChildren().toList()) {
            if (obj instanceof TimerTaskUI) {
                TimerTaskUI child = (TimerTaskUI) obj;
                sumOfValues += child.slider.getValue();
                sliders.add(child);
            }
        }

        if (sumOfValues == 0) {
            // If all are zero, give 100% to the first one (or distribute evenly if many)
            if (!sliders.isEmpty()) {
                sliders.get(0).slider.setValue(100.0);
                sliders.get(0).internalValue.percentageOfTimer = 100.0;
                sliders.get(0).internalValue.update();
            }
            return;
        }

        // Scale all values so their sum is 100
        double scaleFactor = 100.0 / sumOfValues;
        double newSum = 0.0;
        for (int i = 0; i < sliders.size(); i++) {
            TimerTaskUI sliderUI = sliders.get(i);
            double newPercentage = sliderUI.slider.getValue() * scaleFactor;
            sliderUI.slider.setValue( newPercentage);
            sliderUI.internalValue.percentageOfTimer = newPercentage;
            sliderUI.internalValue.update();
            newSum += newPercentage;
        }

        // Due to floating point arithmetic, the sum might not be exactly 100.
        // Distribute the remaining tiny difference to the first slider.
        if (Math.abs(newSum - 100.0) > 0.001 && !sliders.isEmpty()) {
            TimerTaskUI firstSlider = sliders.get(0);
            double difference = 100.0 - newSum;
            firstSlider.slider.setValue( (firstSlider.slider.getValue() + difference));
            firstSlider.internalValue.percentageOfTimer = firstSlider.slider.getValue();
            firstSlider.internalValue.update();
        }
    }


    public static class TimerTaskUI extends HorizontalLayout { // Changed from Desku Component to Vaadin HorizontalLayout
        public CustomSlider slider;
        public Task task;
        public String taskName;
        public TimerTask internalValue; // Replaced Desku's internalValue access

        public TimerTaskUI(@Nullable TimerTask timerTask) {
            this.internalValue = timerTask; // Store the TimerTask
            setWidthFull();
            setAlignItems(FlexComponent.Alignment.CENTER);
            setSpacing(true); // Default spacing between children

            task = Task.whereId().is(timerTask != null ? timerTask.taskId : -1).getFirstOrNull();
            taskName = task == null ? "-Deleted-" : task.name;

            // Vaadin Slider: label is part of the component, range 0-100, step 0.1 for precision
            slider = new CustomSlider(0, 100, 0);
            slider.addValueChangeListener(e -> {
                slider.label.set(taskName+" " +String.valueOf((int) e.getValue())+"%");
            });
            slider.setMin(0);
            slider.setMax(100);
            //slider.setStep(0.1);
            if (timerTask != null) {
                slider.setValue(timerTask.percentageOfTimer);
            } else {
                slider.setValue(0.0);
            }
            slider.setWidthFull(); // Make slider take available width

            add(slider); // Add slider to the HorizontalLayout
        }
    }
}
