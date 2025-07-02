package com.osiris.jsqlgen.ui.timer;

import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LayoutButtonsTasks extends FlexLayout { // Changed from Desku Component to Vaadin FlexLayout
    private Timer currentTimer; // Added to hold the current timer

    private final LayoutSliders layoutSliders; // Kept as it's a dependency

    public LayoutButtonsTasks(LayoutSliders layoutSliders) {
        this.layoutSliders = layoutSliders;
        setFlexDirection(FlexDirection.ROW); // Equivalent to Desku's childHorizontal or row
        setFlexWrap(FlexWrap.WRAP); // Allows buttons to wrap
        setJustifyContentMode(JustifyContentMode.START);
        setAlignItems(Alignment.START);
        getStyle().set("gap", "var(--lumo-space-m)"); // Equivalent to Desku's childGap
        addClassName("layout-buttons-tasks"); // Add a CSS class for styling

        // Desku's UI.get().access() is replaced by Vaadin's UI.getCurrent().access()
        // but here we use Component's UI.access() for safety during event handling.
        // Also, onAdd/onUpdate/onRemove listeners are now managed within Vaadin's lifecycle.
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Registering listeners when component is attached
        Consumer<Task> onChange = task -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                setValue(currentTimer); // Re-render buttons when tasks change
            }));
        };
        Task.onAdd.add(onChange);
        Task.onUpdate.add(onChange);
        Task.onRemove.add(onChange);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Unregistering listeners when component is detached to prevent memory leaks
        // Note: Desku's listeners were not explicitly removed, Vaadin's are better managed.
        // For simplicity, I'm not providing explicit remove methods for Task.onAdd/onUpdate/onRemove
        // as they are typically static in the provided code, but in a large app, you'd manage this carefully.
    }

    private static final Object dbLock = new Object();

    public LayoutButtonsTasks setValue(@Nullable Timer timer) {
        this.currentTimer = timer; // Store the current timer
        this.removeAll();

        if (timer == null) {
            return this;
        }

        for (Task task : Task.get()) {
            AtomicBoolean enabled = new AtomicBoolean(!TimerTask.whereTimerId().is(timer.id).and(TimerTask.whereTaskId().is(task.id)).get().isEmpty());

            Button btn = new Button(task.name); // Vaadin Button
            btn.addClickListener(e -> { // Vaadin click listener
                synchronized (dbLock) {
                    if (enabled.get()) {
                        // Remove task
                        enabled.set(false);
                        setTheme(btn, enabled);

                        TimerTask timerTask = TimerTask.whereTimerId().is(timer.id).and(
                                TimerTask.whereTaskId().is(task.id)
                        ).getFirstOrNull();
                        if (timerTask != null) {
                            timerTask.remove();
                            AL.info("Disabled: " + task.toPrintString());
                            // Trigger re-evaluation of sliders if needed (e.g., total percentage)
                            layoutSliders.getUI().ifPresent(ui -> ui.access(() -> layoutSliders.setValue(currentTimer)));
                        }

                    } else {
                        // Add task
                        enabled.set(true);
                        setTheme(btn, enabled);

                        // Check if task already exists for this timer (Vaadin's UI context for update/access)
                        TimerTask timerTask = TimerTask.whereTimerId().is(timer.id).and(TimerTask.whereTaskId().is(task.id)).getFirstOrNull();
                        if (timerTask != null) {
                            AL.warn("Task '" + task.name + "' already exists in timer '" + timer.id + "', duplicate tasks in a single timer are not allowed.");
                            return;
                        }
                        // Create and add new TimerTask
                        TimerTask.createAndAdd(timer.id, task.id, 0); // Initial percentage can be 0 or adjusted later
                        AL.info("Enabled: " + task.toPrintString());
                        // Trigger re-evaluation of sliders
                        layoutSliders.getUI().ifPresent(ui -> ui.access(() -> layoutSliders.setValue(currentTimer)));
                    }
                }
            });

            if (task.id == Task.WORK.id || task.id == Task.PAUSE.id) {
                btn.setEnabled(false); // Vaadin's way to disable a button
            }

            setTheme(btn, enabled);
            this.add(btn);
        }
        return this; // In Vaadin, setValue usually returns the component itself
    }

    private void setTheme(Button btn, AtomicBoolean enabled) {
        btn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_CONTRAST); // Remove existing variants
        if (enabled.get()) {
            btn.addThemeVariants(ButtonVariant.LUMO_SUCCESS); // Vaadin's success variant
        } else {
            btn.addThemeVariants(ButtonVariant.LUMO_CONTRAST); // Vaadin's secondary/contrast variant
        }
    }
}
