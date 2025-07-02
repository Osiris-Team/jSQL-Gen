package com.osiris.jsqlgen.ui.timer;

import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.osiris.jsqlgen.ui.timer.TimeString.toSimpleString;

public class TimerDetails extends VerticalLayout { // Changed from Desku Component to Vaadin VerticalLayout

    private H4 txtSummaryCount; // Changed from Desku text to Vaadin H4
    private VerticalLayout contentLayout; // This will hold the actual timer details
    private Timestamp currentDay; // Keep track of the current day

    // Dependencies injected by Spring
    private final LayoutSliders layoutSliders;
    private final LayoutButtonsTasks layoutButtonsTasks;

    public TimerDetails(LayoutSliders layoutSliders, LayoutButtonsTasks layoutButtonsTasks) { // Inject dependencies
        this.layoutSliders = layoutSliders;
        this.layoutButtonsTasks = layoutButtonsTasks;

        setFlexGrow(1); // Equivalent to Desku's grow(1)
        setPadding(true);
        setSpacing(true); // Equivalent to Desku's childVertical + childGap
        setWidthFull(); // Take full width

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);

        Button refreshButton = new Button(VaadinIcon.REFRESH.create()); // Vaadin Button with Icon
        refreshButton.addClickListener(e -> setValue(currentDay)); // Refresh on click

        txtSummaryCount = new H4("Showing 0 logged timers"); // Vaadin H4
        txtSummaryCount.getStyle().set("flex-grow", "1"); // Equivalent to Desku's grow(1)

        header.add(refreshButton, txtSummaryCount);

        contentLayout = new VerticalLayout();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(true);
        contentLayout.setWidthFull();
        contentLayout.setHeightFull(); // Enable scrolling for content if needed
        contentLayout.getStyle().set("overflow-y", "auto"); // Make it scrollable

        add(header, contentLayout); // Add header and content container
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Ensure initial value is set when attached to UI
        if (currentDay == null) {
            setValue(new Timestamp(System.currentTimeMillis()));
        } else {
            setValue(currentDay); // Refresh if already set
        }
    }

    public TimerDetails setValue(@Nullable Timestamp day) {
        this.currentDay = day; // Store the current day
        contentLayout.removeAll(); // Clear existing details

        if (day == null) {
            txtSummaryCount.setText("Showing 0 logged timers (No day selected)");
            return this;
        }

        // Get timers that started or ended on this day
        var currentDateTime = day.toLocalDateTime();
        var startOfDay = currentDateTime.with(LocalTime.MIDNIGHT);
        var endOfDay = currentDateTime.with(LocalTime.MAX);

        // Fetch timers within the day range
        List<Timer> timers = Timer.whereStart().between(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay))
                .or(Timer.whereEnd().between(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay)))
                .get(); // Show latest timers first
        Collections.reverse(timers);

        var timersAndTasks = new HashMap<Timer, List<TimerTask>>();
        for (Timer timer : timers) {
            timersAndTasks.put(timer, TimerTask.whereTimerId().is(timer.id).get());
        }

        txtSummaryCount.setText("Showing " + timers.size() + " logged timers");

        DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMM uuu HH:mm");

        timersAndTasks.forEach((timer, tasks) -> {
            tasks.sort((o1, o2) -> Double.compare(o2.percentageOfTimer, o1.percentageOfTimer));

            HorizontalLayout timerEntryLayout = new HorizontalLayout();
            timerEntryLayout.setWidthFull();
            timerEntryLayout.setAlignItems(FlexComponent.Alignment.START);
            timerEntryLayout.setSpacing(true);
            timerEntryLayout.setPadding(false); // No internal padding

            Button settingsButton = new Button(VaadinIcon.COG.create()); // Vaadin Button with Icon
            settingsButton.addClickListener(e -> {
                // Open SlidersPopup for this timer
                // Need to pass the injected dependencies
                add(new SlidersPopup(false, timer, layoutSliders, layoutButtonsTasks));
            });
            timerEntryLayout.add(settingsButton);

            String start = timer.start == Timer.NULL ? "?" : df.format(timer.start.toLocalDateTime());
            String end = timer.end == Timer.NULL ? "?" : df.format(timer.end.toLocalDateTime());
            String duration = timer.start == Timer.NULL || timer.end == Timer.NULL ? "?" :
                    toSimpleString(Duration.of(timer.end.getTime() - timer.start.getTime(), ChronoUnit.MILLIS));
            String mainTask = tasks.isEmpty() ? "-" : Task.whereId().is(tasks.getFirst().id).getFirstOrNull().name;

            Pre timerText = new Pre(
                    "Timer from '" + start + "' to '" + end + "'" +
                            " took " + duration +
                            " doing mainly '" + mainTask + "'"
            );
            timerText.getStyle().set("flex-grow", "1"); // Make text grow
            timerText.getStyle().set("margin", "0"); // Remove default margin from Pre

            timerEntryLayout.add(timerText);
            contentLayout.add(timerEntryLayout);
        });

        return this;
    }
}
