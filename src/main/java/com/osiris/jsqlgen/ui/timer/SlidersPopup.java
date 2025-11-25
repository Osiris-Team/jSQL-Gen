package com.osiris.jsqlgen.ui.timer;

import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.osiris.jlib.logger.AL;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class SlidersPopup extends Dialog { // Changed from Desku Popup to Vaadin Dialog

    // Spring injected components
    private final Sliders sliders;
    private final ButtonsTasks buttonsTasks;

    public SlidersPopup(boolean isBackFromAFK, Timer timer, Sliders sliders, ButtonsTasks buttonsTasks) {
        // Constructor injection for dependencies
        this.sliders = sliders;
        this.buttonsTasks = buttonsTasks;

        setHeaderTitle(isBackFromAFK ? "Welcome back!" : "Good job!"); // Vaadin Dialog header

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false); // No padding for the internal content layout
        content.setSpacing(true); // Spacing between components
        content.setWidthFull();
        content.setHeightFull(); // Allow vertical scroll if content overflows

        content.add(new Paragraph(isBackFromAFK ? "Please select the amount of work and tasks done while you were away." :
                "Please select the amount of work and tasks done."));

        TextField tfTaskName = new TextField("New task name"); // Vaadin TextField
        tfTaskName.setWidthFull(); // Grow 1

        Button createButton = new Button("Create");
        createButton.addClickListener(e -> {
            String v = tfTaskName.getValue();
            if (v == null || v.trim().isEmpty()) {
                AL.warn("Task name cannot be empty.");
                return;
            }
            if (!Task.whereName().is(v).get().isEmpty()) {
                AL.warn("Timer task with name '" + v + "' already exists.");
                return;
            }
            Task.createAndAdd(v);
            tfTaskName.clear(); // Clear text field after creation
            // Re-render buttons layout via its setValue method
            buttonsTasks.setValue(timer);
        });

        HorizontalLayout hlNewTask = new HorizontalLayout(tfTaskName, createButton);
        hlNewTask.setFlexGrow(1, tfTaskName); // Make textfield grow
        hlNewTask.setWidthFull();
        hlNewTask.setPadding(false);
        hlNewTask.setSpacing(true); // Spacing for horizontal components
        hlNewTask.setAlignItems(FlexComponent.Alignment.END); // Align create button to bottom of textfield


        // Initialize and set value for the layouts (they are Spring-scoped, so inject them)
        this.sliders.setValue(timer);
        this.buttonsTasks.setValue(timer);

        content.add(hlNewTask, this.buttonsTasks, this.sliders); // Add sub-layouts

        add(content); // Add the content layout to the dialog

        Button okButton = new Button("Okay", e -> close()); // Vaadin Button for "Okay"
        getFooter().add(okButton); // Add to dialog footer

        // Initial TimerTask setup logic (from original code)
        if (TimerTask.whereTimerId().is(timer.id).get().isEmpty()) {
            Task task = null;
            for (TimerTask timerTask : TimerTask.whereId().biggestFirst().limit(50).get()) {
                Task t = Task.whereId().is(timerTask.id).getFirstOrNull();
                if (t != null && !t.equals(Task.PAUSE) && !t.equals(Task.WORK)) {
                    task = t;
                    break;
                }
            }

            if (task == null) {
                TimerTask.createAndAdd(timer.id, Task.WORK.id, isBackFromAFK ? 10 : 90);
                TimerTask.createAndAdd(timer.id, Task.PAUSE.id, isBackFromAFK ? 90 : 10);
            } else {
                TimerTask.createAndAdd(timer.id, task.id, isBackFromAFK ? 10 : 90);
                if (!task.equals(Task.PAUSE)) // Only add PAUSE if the selected task is not PAUSE itself
                    TimerTask.createAndAdd(timer.id, Task.PAUSE.id, isBackFromAFK ? 90 : 10);
            }
            // After creating initial tasks, refresh the slider and button layouts
            this.sliders.setValue(timer);
            this.buttonsTasks.setValue(timer);
        }

        open(); // Open the dialog immediately
    }
}
