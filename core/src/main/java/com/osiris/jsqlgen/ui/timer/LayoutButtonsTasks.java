package com.osiris.jsqlgen.ui.timer;

import com.osiris.desku.Icon;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.input.Button;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.osiris.desku.Statics.button;

public class LayoutButtonsTasks extends Component<LayoutButtonsTasks, Timer> {
    public LayoutButtonsTasks(@UnknownNullability Timer timer, LayoutSliders layoutSliders) {
        super(timer, Timer.class, "c");
        scrollable(true, "100%", "fit-content").childGap(true);
        setValue(timer);

        UI ui = UI.get();
        Consumer<Task> onChange = task -> {
            ui.access(() -> {
                setValue(timer);
            });
        };
        Task.onAdd.add(onChange);
        Task.onUpdate.add(onChange);
        Task.onRemove.add(onChange);
    }

    private static final Object dbLock = new Object();

    public LayoutButtonsTasks setValue(@Nullable Timer timer) {
        this.removeAll();

        for (Task task : Task.get()) {
            AtomicBoolean enabled = new AtomicBoolean(!TimerTask.whereTimerId().is(timer.id).and(TimerTask.whereTaskId().is(task.id)).get().isEmpty());

            // Add task
            var btn = button(task.name).onClick(e -> {
                synchronized (dbLock){
                    if(enabled.get()){
                        // Remove task
                        enabled.set(false);
                        setTheme(e.comp, enabled);

                        TimerTask timerTask = TimerTask.whereTimerId().is(timer.id).and(
                            TimerTask.whereTaskId().is(task.id)
                        ).getFirstOrNull();
                        if(timerTask != null) {
                            timerTask.remove();
                            AL.info("Disabled: "+task.toPrintString());
                        }

                    } else{
                        // Add task
                        enabled.set(true);
                        setTheme(e.comp, enabled);

                        TimerTask timerTask = TimerTask.whereTaskId().is(task.id).getFirstOrNull();
                        if(timerTask != null){
                            AL.warn("Task '"+task.name+"' already exits in timer '"+timerTask.timerId+"', duplicate tasks in a single timer are not allowed.");
                            return;
                        }
                        // TODO Disabled: id=2 name=test
                        // TODO then move test slider
                        // TODO then: java.lang.RuntimeException: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '1' for key 'PRIMARY'
                        // TODO or: [WARN] 3 != 2
                        // Looks like the state of the button gets lost after moving the slider, idk
                        TimerTask.createAndAdd(timer.id, task.id, 0);
                        AL.info("Enabled: "+task.toPrintString());
                    }
                }
            });

            if(task.id == Task.WORK.id || task.id == Task.PAUSE.id)
                btn.enable(false);

            setTheme(btn, enabled);
            this.add(btn);
        }
        return super.setValue(timer);
    }

    private void setTheme(Button btn, AtomicBoolean enabled) {
        btn.removeClass("btn-success");
        btn.removeClass("btn-secondary");
        if(enabled.get()) btn.success();
        else btn.secondary();
    }
}
