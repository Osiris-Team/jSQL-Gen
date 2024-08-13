package com.osiris.jsqlgen.ui.timer;

import com.osiris.desku.Icon;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.layout.PageLayout;
import com.osiris.desku.ui.utils.NoValue;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import com.osiris.jsqlgen.ui.Html;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.osiris.desku.Statics.*;
import static com.osiris.jsqlgen.ui.timer.TimeString.toSimpleString;

public class TimerDetails extends Component<TimerDetails, Timestamp> {
    public PageLayout ly;

    public TimerDetails(Timestamp day) {
        super(day, Timestamp.class, "c");
        grow(1).childVertical();
        setValue(day);
    }

    @Override
    public TimerDetails setValue(@Nullable Timestamp day) {
        removeAll();

        var txt = text("Showing 0 logged timers").sizeL().grow(1);
        add(horizontal().add(button("").add(Icon.solid_arrows_rotate()).onClick(e -> {
            setValue(day);
        }),
            txt));
        add(ly = new PageLayout().grow(1));

        ly.setDataProvider(0, 50, (details) -> {
            List<Component<?, ?>> comps = new ArrayList<>();

            // Get timers that started or ended on this day
            var currentDateTime = day.toLocalDateTime();
            var startOfDay = currentDateTime.with(LocalTime.MIDNIGHT);
            var endOfDay = currentDateTime.with(LocalTime.MAX);
            List<Timer> timers = Timer.whereId().biggerOrEqual(details.iStart).and(
                    Timer.whereStart().between(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay))
                        .or(Timer.whereEnd().between(Timestamp.valueOf(startOfDay), Timestamp.valueOf(endOfDay)))
                )
                .get().reversed(); // Show latest timers first
            if(!timers.isEmpty()) ly.iStart = timers.getLast().id;

            var timersAndTasks = new HashMap<Timer, List<TimerTask>>();
            for (Timer timer : timers) {
                timersAndTasks.put(timer, TimerTask.whereTimerId().is(timer.id).get());
            }

            txt.setValue("Showing "+timers.size()+" logged timers");

            DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMM uuu HH:mm");
            timersAndTasks.forEach((timer, tasks) -> {
                tasks.sort((o1, o2) -> Double.compare(o2.percentageOfTimer, o1.percentageOfTimer));

                var hl = horizontal().padding(false);
                String start = timer.start == Timer.NULL ? "?" : df.format(timer.start.toLocalDateTime());
                String end = timer.end == Timer.NULL ? "?" : df.format(timer.end.toLocalDateTime());
                hl.add(button("").add(Icon.solid_gear()).onClick(e -> {
                    add(new SlidersPopup(false, timer));
                }));
                hl.add(new Html("pre").add(text("Timer from '"+start+ "' to '" +end+"'"+

                    " took "+ (timer.start == Timer.NULL || timer.end == Timer.NULL ? "?" : toSimpleString(
                        Duration.of(timer.end.getTime() - timer.start.getTime(), ChronoUnit.MILLIS)))

                    +" doing mainly '"+ (tasks.isEmpty() ? "-" : Task.whereId().is(tasks.getFirst().id).getFirstOrNull().name) +"'")
                    )
                    .grow(1));

                comps.add(hl);
            });

            return comps;
        });

        return super.setValue(day);
    }
}
