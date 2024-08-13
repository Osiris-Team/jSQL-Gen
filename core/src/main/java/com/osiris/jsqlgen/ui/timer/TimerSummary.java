package com.osiris.jsqlgen.ui.timer;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.input.Button;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.osiris.jsqlgen.ui.Html;
import org.apache.commons.collections4.map.LinkedMap;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.osiris.desku.Statics.text;
import static com.osiris.jsqlgen.ui.timer.TimeString.toSimpleString;

public class TimerSummary extends Component<TimerSummary, Timestamp>{
    public Button btn = new Button("").secondary().grow(1);

    public TimerSummary(Timestamp day) {
        super(day, Timestamp.class);
        setValue(day);
    }

    @Override
    public TimerSummary setValue(Timestamp day) {
        removeAll();
        add(btn);
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
            s += "\""+e.getKey()+"\" took "+ toSimpleString(Duration.of(e.getValue().longValue(), ChronoUnit.MILLIS))+"\n";
        }


        String dayString = TimeString.toSimpleString(day.toLocalDateTime());
        if(s.isEmpty()) s = "- No data -";
        s = dayString+"\n" + s;
        btn.removeAll();
        btn.add(new Html("pre").add(text(s)));
        btn.setTooltip("Note that this includes timers that started and/or ended in this day.");
        return super.setValue(day);
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
                if(existingMsTask == null) existingMsTask = 0.0;
                existingMsTask += msTask;
                tasksAndTimes.put(taskName, existingMsTask);
            }
        }
        return tasksAndTimes;
    }


}
