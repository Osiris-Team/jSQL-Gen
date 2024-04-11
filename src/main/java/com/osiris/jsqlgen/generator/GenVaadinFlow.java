package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.osiris.jsqlgen.generator.GenReferences.getRefTable;
import static com.osiris.jsqlgen.utils.UString.containsIgnoreCase;
import static com.osiris.jsqlgen.utils.UString.firstToUpperCase;

public class GenVaadinFlow {
    public static class ExtraInfo{
        public String fieldName;
        public boolean isColumnRef;
        public Table refTable;

        public ExtraInfo(String fieldName, boolean isColumnRef, Table refTable) {
            this.fieldName = fieldName;
            this.isColumnRef = isColumnRef;
            this.refTable = refTable;
        }
    }
    /**
     * Should be compatible with Vaadin 14 and up.
     */
    public static String s(Database db, Table t, LinkedHashSet<String> importsList) {
        StringBuilder s = new StringBuilder();

        // Add imports
        importsList.add("import com.vaadin.flow.component.button.Button;");
        importsList.add("import com.vaadin.flow.component.formlayout.FormLayout;");
        importsList.add("import com.vaadin.flow.component.orderedlayout.HorizontalLayout;");
        importsList.add("import com.vaadin.flow.component.orderedlayout.VerticalLayout;");
        importsList.add("import com.vaadin.flow.component.select.Select;");
        importsList.add("import com.vaadin.flow.component.textfield.NumberField;");
        importsList.add("import com.vaadin.flow.component.textfield.TextField;");
        importsList.add("import com.vaadin.flow.component.ClickEvent;");
        importsList.add("import com.vaadin.flow.component.datepicker.DatePicker;");
        importsList.add("import com.vaadin.flow.component.datetimepicker.DateTimePicker;");
        importsList.add("import java.time.LocalDateTime;");
        importsList.add("import java.time.OffsetDateTime;");
        importsList.add("import com.vaadin.flow.component.combobox.ComboBox;");
        importsList.add("import com.vaadin.flow.component.html.Div;");
        importsList.add("import com.vaadin.flow.data.renderer.ComponentRenderer;");
        importsList.add("import com.vaadin.flow.component.button.ButtonVariant;");


        // Create the class first
        s.append("    public static class Comp extends VerticalLayout{\n" +
                "\n" +
                genBooleanSelectClass(importsList) +
                "\n" +
                "        public " + t.name + " data;\n" +
                "\n" +
                "        // Form and fields\n" +
                "        public FormLayout form = new FormLayout();\n");
        Map<Column, ExtraInfo> mapExtraInfo = new HashMap<>();
        for (Column col : t.columns) {
            if (col.type.isBlob()) {
                continue; // TODO currently not supported
            }
            String colName = firstToUpperCase(col.name);
            String fieldName = "";
            boolean isColumnRef = false;
            Table refTable = null;
            if (col.type.isEnum()) {
                fieldName = "sel" + colName;
                s.append("        public Select<" + t.name + "." + col.type.inJava + "> " + fieldName +
                        " = new Select<" + t.name + "." + col.type.inJava + ">();\n" +
                        "        {" + fieldName + ".setLabel(\"" + colName + "\"); " + fieldName + ".setItems(" + t.name + "." + col.type.inJava + ".values()); }\n");
            } else if (col.type.inJava.equals("String")) {
                fieldName = "tf" + colName;
                s.append("        public TextField " + fieldName + " = new TextField(\"" + colName + "\");\n");
            } else if (col.type.isDate()) {
                fieldName = "df" + colName;
                s.append("        public DatePicker " + fieldName + " = new DatePicker(\"" + colName + "\");\n");
            } else if (col.type.isTime()) {
                fieldName = "df" + colName;
                s.append("        public DateTimePicker " + fieldName + " = new DateTimePicker(\"" + colName + "\");\n");
            } else if (col.type.isTimestamp()) {
                fieldName = "df" + colName;
                s.append("        public DateTimePicker " + fieldName + " = new DateTimePicker(\"" + colName + "\");\n");
            } else if (col.type.isBitOrBoolean()) {
                fieldName = "bs" + colName;
                s.append("        public BooleanSelect " + fieldName + " = new BooleanSelect(\"" + colName + "\", false);\n");
            } else {
                // This might be an id / reference to another table
                refTable = getRefTable(db, colName);
                if(refTable != null){
                    isColumnRef = true;
                    colName = colName.substring(0, colName.toLowerCase().lastIndexOf("id"));
                    fieldName = "cb" + colName;
                    s.append("        public ComboBox<"+refTable.name+"> " + fieldName + " = new ComboBox<"+refTable.name+">(\"" + colName + "\");\n");
                    s.append("        {"+fieldName+".setItems("+refTable.name+".get());\n" +
                            "            "+fieldName+".setRenderer(new ComponentRenderer<>(obj -> {\n" +
                            "                Div div = new Div();\n"+
                            "                div.setText(obj.toMinimalPrintString());\n" +
                            "            return div;}));\n" +
                            "            "+fieldName+".setItemLabelGenerator(obj -> {\n" +
                            "                return obj.toMinimalPrintString();\n" +
                            "            });\n" +
                            "        }\n");
                } else{
                    fieldName = "nf" + colName;
                    s.append("        public NumberField " + fieldName + " = new NumberField(\"" + colName + "\");\n");
                }
            }
            mapExtraInfo.put(col, new ExtraInfo(fieldName, isColumnRef, refTable));
        }

        s.append("        // Buttons\n" +
                "        public HorizontalLayout hlButtons = new HorizontalLayout();\n" +
                "        public Button btnAdd = new Button(\"Add\");\n" +
                "        {btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}\n" +
                "        public Consumer<ClickEvent<Button>> onBtnAddClick = (e) -> {\n" +
                "                btnAdd.setEnabled(false);\n" +
                "                updateData();\n" +
                "                data.id = idCounter.getAndIncrement();\n" +
                "                " + t.name + ".add(data);\n" +
                "                e.unregisterListener(); // Make sure it gets only added once to the database\n" +
                "                updateButtons();\n" +
                "};\n" +
                "        public Button btnSave = new Button(\"Save\");\n" +
                "        {btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}\n" +
                "        public Consumer<ClickEvent<Button>> onBtnSaveClick = (e) -> {\n" +
                "                btnSave.setEnabled(false);\n" +
                "                updateData();\n" +
                "                " + t.name + ".update(data);\n" +
                "                btnSave.setEnabled(true);\n" +
                "                updateButtons();\n" +
                "};\n" +
                "        public Button btnDelete = new Button(\"Delete\");\n" +
                "        {btnDelete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);}\n" +
                "        public Consumer<ClickEvent<Button>> onBtnDeleteClick = (e) -> {\n" +
                "                btnDelete.setEnabled(false);\n" +
                "                " + t.name + ".remove(data);\n" +
                "                e.unregisterListener(); // Make sure it gets only added once to the database\n" +
                "                updateButtons();\n" +
                "};\n" +
                "\n" +
                "        public Comp(" + t.name + " data) {\n" +
                "            this.data = data;\n" +
                "            setWidthFull();\n" +
                "            setPadding(false);\n" +
                "\n" +
                "            // Set defaults\n" +
                "            updateFields();\n" +
                "\n" +
                "            // Add fields\n" +
                "            addAndExpand(form);\n" +
                "            form.setWidthFull();\n");
        for (Column col : t.columns) {
            if (col.type.isBlob()) {
                continue; // TODO currently not supported
            }
            ExtraInfo extraInfo = mapExtraInfo.get(col);
            String fieldName = extraInfo.fieldName;
            s.append("            form.add(" + fieldName + ");\n");
        }
        s.append("\n" +
                "            // Add buttons\n" +
                "            add(hlButtons);\n" +
                "            hlButtons.setPadding(false);\n" +
                "            hlButtons.setWidthFull();\n" +
                "            updateButtons();\n" +
                "\n" +
                "            // Add button listeners\n" +
                "            btnAdd.addClickListener(e -> {onBtnAddClick.accept(e);});\n" +
                "            btnSave.addClickListener(e -> {onBtnSaveClick.accept(e);});\n" +
                "            btnDelete.addClickListener(e -> {onBtnDeleteClick.accept(e);});\n" +
                "        }\n" +
                "\n" +
                "        public void updateFields(){\n");
        for (Column col : t.columns) {
            if (col.type.isBlob()) {
                continue; // TODO currently not supported
            }
            ExtraInfo extra = mapExtraInfo.get(col);
            String fieldName = extra.fieldName;

            if(extra.isColumnRef)
                s.append("            " + fieldName + ".setValue(data." + col.name + " != -1 ? "+extra.refTable.name+".get(data." + col.name + ") : null);\n");
            else if (!col.type.isBitOrBoolean() && col.type.isNumber())
                s.append("            " + fieldName + ".setValue(0.0 + data." + col.name + ");\n");
            else if (col.type.isDate()) {
                s.append("            " + fieldName + ".setValue(data." + col.name + ".toLocalDate());\n");
            } else if (col.type.isTime()) {
                s.append("            " + fieldName + ".setValue(LocalDateTime.from(data." + col.name + ".toLocalTime()));\n");
            } else if (col.type.isTimestamp()) {
                s.append("            " + fieldName + ".setValue(data." + col.name + ".toLocalDateTime());\n");
            } else
                s.append("            " + fieldName + ".setValue(data." + col.name + ");\n");
        }
        s.append("        }\n" +
                "        public void updateData(){\n");
        for (Column col : t.columns) {
            if (col.type.isBlob()) {
                continue; // TODO currently not supported
            }
            ExtraInfo extra = mapExtraInfo.get(col);
            String fieldName = extra.fieldName;
            if(extra.isColumnRef)
                s.append("            data." + col.name + " = " + fieldName + ".getValue() != null ? " + fieldName + ".getValue().id : -1;\n");
            else if (!col.type.isBitOrBoolean() && (col.type.isNumber() || col.type.isDecimalNumber()))
                s.append("            data." + col.name + " = (" + col.type.inJava + ") " + fieldName + ".getValue().doubleValue();\n");
            else if (col.type.isDate()) {
                s.append("            data." + col.name + " = new java.sql.Date(" + fieldName + ".getValue().toEpochDay() * 86400000L);\n");
            } else if (col.type.isTime()) {
                s.append("            data." + col.name + " = new java.sql.Time("+fieldName+".getValue().toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);\n");
            } else if (col.type.isTimestamp()) {
                s.append("            data." + col.name + " = new java.sql.Timestamp("+fieldName+".getValue().toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);\n");
            }
            else
                s.append("            data." + col.name + " = " + fieldName + ".getValue();\n");
        }
        s.append("        }\n" +
                "\n" +
                "        public void updateButtons(){\n" +
                "            hlButtons.removeAll();\n" +
                "\n" +
                "            if(data.id < 0){ // In memory only, doesn't exist in db yet\n" +
                "                hlButtons.addAndExpand(btnAdd);\n" +
                "                return;\n" +
                "            }\n" +
                "            // Already exists\n" +
                "            hlButtons.add(btnDelete);\n" +
                "            hlButtons.addAndExpand(btnSave);\n" +
                "        }\n" +
                "    }\n" + // CLOSE CLASS
                "\n" +
                "    public " + t.name + ".Comp toComp(){\n" +
                "        return new " + t.name + ".Comp(this);\n" +
                "    }\n" +
                "\n");

        return s.toString();
    }

    public static String genBooleanSelectClass(LinkedHashSet<String> importsList){
        importsList.add("import com.vaadin.flow.component.html.Span;");
        importsList.add("import com.vaadin.flow.component.select.Select;");
        importsList.add("import com.vaadin.flow.data.renderer.ComponentRenderer;");
        StringBuilder s = new StringBuilder();

        s.append(
                "public static class BooleanSelect extends Select<Boolean> {\n" +
                "    public Span yes = genYesLabel();\n" +
                "    public Span no = genNoLabel();\n" +
                "\n" +
                "    public BooleanSelect(String label, boolean b) {\n" +
                "        super();\n" +
                "        setLabel(label);\n" +
                "        setItems(true, false);\n" +
                "        setRenderer(new ComponentRenderer<>(b_ -> {\n" +
                "            if(b_) return yes;\n" +
                "            else return no;\n" +
                "        }));\n" +
                "        setValue(b);\n" +
                "    }\n" +
                "\n" +
                "    public Span genLabel(){\n" +
                "        Span txt = new Span(\"\");\n" +
                "        txt.getStyle().set(\"color\", \"var(--lumo-base-color)\");\n" +
                "        txt.getStyle().set(\"text-align\", \"center\");\n" +
                "        txt.getStyle().set(\"padding-left\", \"10px\");\n" +
                "        txt.getStyle().set(\"padding-right\", \"10px\");\n" +
                "        txt.getStyle().set(\"border-radius\", \"10px\");\n" +
                "        return txt;\n" +
                "    }\n" +
                "\n" +
                "    public Span genYesLabel(){\n" +
                "        Span txt = genLabel();\n" +
                "        txt.setText(\"Yes\");\n" +
                "        txt.getStyle().set(\"background-color\", \"var(--lumo-success-color)\");\n" +
                "        return txt;\n" +
                "    }\n" +
                "\n" +
                "    public Span genNoLabel(){\n" +
                "        Span txt = genLabel();\n" +
                "        txt.setText(\"No\");\n" +
                "        txt.getStyle().set(\"background-color\", \"var(--lumo-error-color)\");\n" +
                "        return txt;\n" +
                "    }\n" +
                "}");
        return s.toString();
    }
}
