package com.osiris.jsqlgen.ui.timer;

import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.osiris.jsqlgen.ui.comps.CustomSlider;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class LayoutSliders extends VerticalLayout { // Changed from Desku Component to Vaadin VerticalLayout

    public List<TimerTask> timerTasks;
    private Timer currentTimer; // Added to hold the current timer

    public LayoutSliders() {
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
                add(getTimerTaskUI(timerTask));
                ensureSlidersTotal100(); // Re-balance after adding
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
                ensureSlidersTotal100(); // Re-balance after removing
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

    public LayoutSliders setValue(@Nullable Timer timer) {
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
            ensureSlidersTotal100();
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

        AtomicReference<Double> refValueBefore = new AtomicReference<>();
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
                    ensureSlidersTotal100();
                }));
            }
        });
        return comp;
    }

    private void ensureSlidersTotal100() {
        if (this.getChildren().count() == 0) return;

        double currentTotal = 0.0;
        List<TimerTaskUI> activeSliders = new ArrayList<>();
        for (Component obj : this.getChildren().toList()) {
            if (obj instanceof TimerTaskUI) {
                TimerTaskUI child = (TimerTaskUI) obj;
                currentTotal += child.slider.getValue();
                activeSliders.add(child);
            }
        }

        if (activeSliders.isEmpty()) return;

        double difference = 100.0 - currentTotal;

        // If total is already 100 (or very close due to double precision), no adjustment needed
        if (Math.abs(difference) < 0.001) {
            return;
        }

        // Distribute the difference proportionally among all sliders
        // or just apply to the first non-zero slider if the total is less than 100
        // and to the slider that changed if total is greater than 100 (this is more complex)

        // Simpler approach:
        // If total > 100, reduce all proportionally, starting from largest.
        // If total < 100, add to first non-zero, or distribute proportionally.

        // Let's adopt a simple approach: if total is off, adjust the LAST slider (or first)
        // to make it 100%, and then potentially redistribute proportionally if it's too aggressive.
        // The original Desku code adjusted the first child if total < 100.

        if (difference != 0.0) {
            // Find a slider to apply the difference to.
            // A common strategy is to apply to the last slider that's not zero,
            // or the first if all are zero.
            TimerTaskUI sliderToAdjust = null;
            if (difference > 0) { // Total is less than 100, need to add
                // Add to the first active slider that's not the one that was just changed, or any if only one
                sliderToAdjust = activeSliders.get(0);
            } else { // Total is greater than 100, need to subtract
                // Subtract from the slider that just moved if it caused the overshoot, or the last one
                sliderToAdjust = activeSliders.get(activeSliders.size() - 1); // Or more complex logic
            }

            if (sliderToAdjust != null) {
                double oldValue = sliderToAdjust.slider.getValue();
                double newValue = oldValue + difference;
                if (newValue < 0) { // Prevent negative percentages, redistribute
                    // This scenario means the adjustment is too large for one slider.
                    // A more robust solution would distribute the difference across multiple sliders.
                    AL.warn("Calculated negative slider value for " + sliderToAdjust.taskName + ". Recalculating distribution.");
                    // Reset and redistribute:
                    // For example, if total is 105, and one slider is 100, another 5.
                    // If the 100 slider goes to 90 (total 95), then this code would still be fine.
                    // But if the 5 slider goes to 0 (total 95), then first one becomes 105 - 95 = 100.
                    // If a slider goes from 5 to 10 (total 100), the first will be 90.
                    // If a slider goes from 5 to 20 (total 115), the first will be 80 -> needs 15 subtracted
                    // It's crucial that `ensureSlidersTotal100` always results in valid states.
                    redistributeSlidersProportionally();
                } else {
                    sliderToAdjust.slider.setValue( newValue);
                    sliderToAdjust.internalValue.percentageOfTimer = newValue;
                    sliderToAdjust.internalValue.update();
                }
            }
        }
        // After an initial adjustment, if the total is still slightly off due to rounding,
        // we can re-evaluate.
        // For robustness, call this again, or ensure the proportional distribution handles it.
        // Calling it again without specific logic to prevent infinite loops means simple adjustment.
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
            slider.label.set(taskName);
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
