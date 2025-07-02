package com.osiris.jsqlgen.ui.comps;

import com.osiris.osiris_vaadin_utils.ui.texts.Text;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomSlider extends Div {

    public static class CustomValueChangeEvent extends ComponentEvent<CustomSlider> {
        private final double value;
        private final boolean fromClient;

        public CustomValueChangeEvent(CustomSlider source, boolean fromClient, double value) {
            super(source, fromClient);
            this.value = value;
            this.fromClient = fromClient;
        }

        public double getValue() {
            return value;
        }

        public boolean isFromClient() {
            return fromClient;
        }
    }

    public interface ValueChangeListener {
        void valueChanged(CustomValueChangeEvent event);
    }

    public final Text label = new Text("");
    private final ProgressBar progressBar = new ProgressBar();
    private double min = 0;
    private double max = 100;
    private double value = 0;
    private boolean readOnly = false;

    private final List<ValueChangeListener> valueChangeListeners = new CopyOnWriteArrayList<>();

    public CustomSlider(double min, double max, double initialValue) {
        this.min = min;
        this.max = max;
        this.value = initialValue;

        setWidth("100%");
        addClassName("custom-slider-wrapper");

        label.set(String.valueOf((int) value));
        progressBar.setMin(min);
        progressBar.setMax(max);
        progressBar.setValue(value);
        progressBar.setWidthFull();
        progressBar.addClassName("custom-slider-bar");

        add(label, progressBar);

        // Handle click events from client
        progressBar.getElement().addEventListener("click", e -> {
            double offsetX = e.getEventData().getNumber("event.offsetX");
            double clientWidth = e.getEventData().getNumber("event.target.clientWidth");
            double percentage = offsetX / clientWidth;
            setValue(min + percentage * (max - min), true); // Mark as client-side event
        }).addEventData("event.offsetX").addEventData("event.target.clientWidth");
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double newValue) {
        setValue(newValue, false); // Default to server-side update
    }

    public void setValue(Double newValue, boolean fromClient) {
        if (newValue == null || readOnly) return;

        double clampedValue = Math.max(min, Math.min(max, newValue));
        if (this.value != clampedValue) {
            double old = this.value;
            this.value = clampedValue;
            progressBar.setValue(this.value);
            label.set(String.valueOf((int) this.value));
            notifyListeners(new CustomValueChangeEvent(this, fromClient, this.value));
        }
    }

    private void notifyListeners(CustomValueChangeEvent event) {
        for (ValueChangeListener listener : valueChangeListeners) {
            listener.valueChanged(event);
        }
    }

    public void addValueChangeListener(ValueChangeListener listener) {
        valueChangeListeners.add(listener);
    }

    public void removeValueChangeListener(ValueChangeListener listener) {
        valueChangeListeners.remove(listener);
    }

    public void setMin(double min) {
        this.min = min;
        progressBar.setMin(min);
    }

    public void setMax(double max) {
        this.max = max;
        progressBar.setMax(max);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
