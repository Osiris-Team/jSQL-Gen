package com.osiris.jsqlgen.ui.timer;

import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.layout.Popup;
import com.osiris.jlib.logger.AL;
import com.osiris.jsqlgen.jsqlgen.Task;
import com.osiris.jsqlgen.jsqlgen.Timer;
import com.osiris.jsqlgen.jsqlgen.TimerTask;

import java.util.function.Consumer;

import static com.osiris.desku.Statics.*;
import static com.osiris.desku.Statics.text;

public class SlidersPopup extends Popup {
    public SlidersPopup(boolean isBackFromAFK, Timer timer) {
        super(text(isBackFromAFK ? "Welcome back!" : "Good job!"), button("Okay"), null);
        UI ui = UI.get();

        var tfTaskName = textfield("New task name").grow(1);
        var hlNewTask = horizontal().grow(1).padding(false)
            .add(tfTaskName)
            .add(button("Create").onClick(e -> {
                String v = tfTaskName.getValue();
                if(!Task.whereName().is(v).get().isEmpty()){
                    AL.warn("Timer task with name '"+ v +"' already exists.");
                    return;
                }
                Task.createAndAdd(v);
            }));

        // Get recently used task
        Task task = null;
        for (TimerTask timerTask : TimerTask.whereId().biggestFirst().limit(50).get()) {
            var t = Task.whereId().is(timerTask.getId()).getFirstOrNull();
            if(t != null && t != Task.PAUSE && t != Task.WORK){
                task = t;
                break;
            }
        }

        if(TimerTask.whereTimerId().is(timer.id).get().isEmpty()){
            if(task == null){
                TimerTask.createAndAdd(timer.id, Task.WORK.id, isBackFromAFK ? 10 : 90);
                TimerTask.createAndAdd(timer.id, Task.PAUSE.id, isBackFromAFK ? 90 : 10);
            } else{
                TimerTask.createAndAdd(timer.id, task.id, isBackFromAFK ? 10 : 90);
                if(task.id != Task.PAUSE.id)
                    TimerTask.createAndAdd(timer.id, Task.PAUSE.id, isBackFromAFK ? 90 : 10);
            }
        }


        this.body.scrollable(true, "100%", "100%");
        this.add(text(isBackFromAFK ? "Please select the amount of work and tasks done while you were away." :
            "Please select the amount of work and tasks done."));

        var lySliders = new LayoutSliders(timer).padding(false).grow(1);
        var lyBtnsTasks = new LayoutButtonsTasks(timer, lySliders);
        this.add(hlNewTask, lyBtnsTasks, lySliders);
    }
}
