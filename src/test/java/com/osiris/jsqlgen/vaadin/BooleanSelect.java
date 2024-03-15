package com.osiris.jsqlgen.vaadin;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class BooleanSelect extends Select<Boolean> {
    public Span yes = genYesLabel();
    public Span no = genNoLabel();

    public BooleanSelect(String label, boolean b) {
        super();
        setLabel(label);
        setItems(true, false);
        setRenderer(new ComponentRenderer<>(b_ -> {
            if(b_) return yes;
            else return no;
        }));
        setValue(b);
    }

    public Span genLabel(){
        Span txt = new Span("");
        txt.getStyle().set("color", "var(--lumo-base-color)");
        txt.getStyle().set("text-align", "center");
        txt.getStyle().set("padding-left", "10px");
        txt.getStyle().set("padding-right", "10px");
        txt.getStyle().set("border-radius", "10px");
        return txt;
    }

    public Span genYesLabel(){
        Span txt = genLabel();
        txt.setText("Yes");
        txt.getStyle().set("background-color", "var(--lumo-success-color)");
        return txt;
    }

    public Span genNoLabel(){
        Span txt = genLabel();
        txt.setText("No");
        txt.getStyle().set("background-color", "var(--lumo-error-color)");
        return txt;
    }
}