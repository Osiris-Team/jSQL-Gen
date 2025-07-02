package com.osiris.jsqlgen.ui.timer;

import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import org.apache.commons.collections4.map.LinkedMap;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.osiris.jsqlgen.ui.timer.TimeString.toSimpleString;


public class TimerSummary extends VerticalLayout { // Changed from Desku Component to Vaadin VerticalLayout

    public Button btn = new Button(); // Vaadin Button

    public TimerSummary(Timestamp day) {
        setPadding(false); // Equivalent to Desku's padding(false)
        setSpacing(false); // No default spacing for this component's children
        setWidthFull(); // Equivalent to Desku's grow(1) if placed inside a horizontal layout

        btn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_CONTRAST); // Secondary style
        btn.setWidthFull(); // Make the button take full width

        // Tooltip for Vaadin Button
        btn.setTooltipText("Note that this includes timers that started and/or ended in this day.");

        add(btn); // Add the button to the layout
        setValue(day); // Set initial value
    }

    public TimerSummary setValue(Timestamp day) {
        String s = "";

        // Get timers that started or ended on this day
        var currentDateTime = day.toLocalDateTime();
        var startOfDay = currentDateTime.with(LocalTime.MIDNIGHT);
        var endOfDay = currentDateTime.with(LocalTime.MAX);
        List<Timer> timers = Timer.whereStart().between(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay))
                .or(Timer.whereEnd().between(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay)))
                .get();
        var timersAndTasks = new HashMap<Timer, List<TimerTask>>();
        for (Timer timer : timers) {
            timersAndTasks.put(timer, TimerTask.whereTimerId().is(timer.id).get());
        }

        // Calculate how much each task took for this day
        var tasksAndTimes = getTasksAndTimes(timers, timersAndTasks);

        // Sort the list in descending order (largest value first)
        List<Map.Entry<String, Double>> list = new ArrayList<>(tasksAndTimes.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        for (Map.Entry<String, Double> e : list) {
            s += "\"" + e.getKey() + "\" took " + toSimpleString(Duration.of(e.getValue().longValue(), ChronoUnit.MILLIS)) + "\n";
        }

        String dayString = TimeString.toSimpleString(day.toLocalDateTime());
        if (s.isEmpty()) s = "- No data -";
        s = dayString + "\n" + s;

        btn.setText(s); // Set text content of the Pre component
        return this;
    }

    public static @NotNull LinkedMap<String, Double> getTasksAndTimes(List<Timer> timers, HashMap<Timer, List<TimerTask>> timersAndTasks) {
        var tasksAndTimes = new LinkedMap<String, Double>();
        for (Timer timer : timers) {
            double msTotal = timer.end.getTime() - timer.start.getTime();
            List<TimerTask> timerTasks = timersAndTasks.get(timer);
            for (TimerTask timerTask : timerTasks) {
                Task task = Task.whereId().is(timerTask.taskId).getFirstOrNull();
                String taskName = task == null ? "- Deleted -" : task.name;
                double msTask = (msTotal / 100.0) * timerTask.percentageOfTimer;
                Double existingMsTask = tasksAndTimes.get(taskName);
                if (existingMsTask == null) existingMsTask = 0.0;
                existingMsTask += msTask;
                tasksAndTimes.put(taskName, existingMsTask);
            }
        }
        return tasksAndTimes;
    }
}
