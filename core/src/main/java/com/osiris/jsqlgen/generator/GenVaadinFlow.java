package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.osiris.jsqlgen.generator.GenReferences.getAllRefs;
import static com.osiris.jsqlgen.generator.GenReferences.getRefTable;
import static com.osiris.jsqlgen.utils.UString.*;

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
        importsList.add("import com.vaadin.flow.component.dialog.Dialog;");
        importsList.add("import java.util.function.Function;");
        importsList.add("import java.util.function.Consumer;");
        importsList.add("import com.vaadin.flow.component.UI;");

        // Generate needed classes
        s.append(genBooleanSelectClass(importsList));


        s.append("" +
                "// Executed for all objects\n" +
                "public static Consumer<"+t.name+"> onCreateV(Consumer<"+t.name+"> code){\n" +
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onCreate.remove(code2);}); "+t.name+".onCreate.add(code2); return code2;\n}\n"+

                "// Executed for all objects\n" +
                "public static Consumer<"+t.name+"> onAddV(Consumer<"+t.name+"> code){\n"+
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onAdd.remove(code2);}); "+t.name+".onAdd.add(code2); return code2;\n}\n"+

                "// Executed for all objects\n" +
                "public static Consumer<"+t.name+"> onUpdateV(Consumer<"+t.name+"> code){\n"+
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onUpdate.remove(code2);}); "+t.name+".onUpdate.add(code2); return code2;\n}\n"+

                "// Executed for all objects\n" +
                "public static Consumer<"+t.name+"> onRemoveV(Consumer<"+t.name+"> code){\n"+
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onRemove.remove(code2);}); "+t.name+".onRemove.add(code2); return code2;\n}\n"+
                "\n\n");
        s.append("" +
                "// Executed only for this object\n" +
                "public Consumer<"+t.name+"> onCreateThisV(Consumer<"+t.name+"> code){\n" +
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onCreate.remove(code2);}); "+t.name+".onCreate.add(code2); return code2;\n}\n"+

                "// Executed only for this object\n" +
                "public Consumer<"+t.name+"> onAddThisV(Consumer<"+t.name+"> code){\n"+
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onAdd.remove(code2);}); "+t.name+".onAdd.add(code2); return code2;\n}\n"+

                "// Executed only for this object\n" +
                "public Consumer<"+t.name+"> onUpdateThisV(Consumer<"+t.name+"> code){\n"+
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onUpdate.remove(code2);}); "+t.name+".onUpdate.add(code2); return code2;\n}\n"+

                "// Executed only for this object\n" +
                "public Consumer<"+t.name+"> onRemoveThisV(Consumer<"+t.name+"> code){\n"+
                "UI ui = UI.getCurrent(); Consumer<"+t.name+"> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});};" +
                " ui.addDetachListener(e -> {"+t.name+".onRemove.remove(code2);}); "+t.name+".onRemove.add(code2); return code2;\n}\n"+
                "\n\n");

        // Create static, table related components
        s.append("public static ComboBox<"+t.name+"> newTableComboBox(){\n");
        s.append(getComboBoxWithTableContent(t, "comboBox", t.name).replaceFirst("public", ""));
        s.append("return comboBox;\n" +
                "}\n\n");
        // Static new methods for comp creation of each column / data field
        for (Column col : t.columns) {
            if (col.type.isBlob()) {
                continue; // TODO currently not supported
            }
            Result result = colToVaadinComp(db, t, col);
            s.append("public static "+result.compType+" new"+firstToUpperCase(result.fieldName)+"(){\n");
            s.append(result.generatedCode.replaceFirst("public", ""));
            s.append("return "+result.fieldName+";\n" +
                    "}\n\n");
        }

        // Create regular methods
        s.append("" +
                "    /**\n" +
                "     * Gets executed later if {@link #isOnlyInMemory()}, otherwise provided\n" +
                "     * code gets executed directly.\n" +
                "     */" +
                "    public void whenReadyV(Consumer<"+t.name+"> code) {\n" +
                "        if(isOnlyInMemory()) onAddThisV(obj -> code.accept(obj));\n" +
                "        else code.accept(this);\n" +
                "    }\n\n");



        // Create the class first
        s.append("    public static class Comp extends VerticalLayout{\n" +
                "\n" +
                "        public " + t.name + " data"+t.name+";\n" +
                "        public " + t.name + " data;\n" +
                "\n" +
                "        // Form and fields\n" +
                "        public FormLayout form = new FormLayout();\n");
        Map<Column, ExtraInfo> mapExtraInfo = new HashMap<>();
        for (Column col : t.columns) {
            if (col.type.isBlob()) {
                continue; // TODO currently not supported
            }
            Result result = colToVaadinComp(db, t, col);
            s.append(result.generatedCode);
            mapExtraInfo.put(col, new ExtraInfo(result.fieldName(), result.isColumnRef(), result.refTable()));
        }

        s.append("        // Buttons\n" +
                "        public HorizontalLayout hlButtons = new HorizontalLayout();\n" +
                "        public Button btnAdd = new Button(\"Add\");\n" +
                "        {btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}\n" +
                "        public Consumer<ClickEvent<Button>> onBtnAddClick = (e) -> {\n" +
                "                btnAdd.setEnabled(false);\n" +
                "                updateData();\n" +
                "                " + t.name + ".add(data);\n" +
                "                e.unregisterListener(); // Make sure it gets only executed once\n" +
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
                "                e.unregisterListener(); // Make sure it gets only executed once\n" +
                "                updateButtons();\n" +
                "};\n" +
                "\n" +
                "        public Comp(" + t.name + " data) {\n" +
                "            this.data = data;\n" +
                "            this.data"+t.name+" = this.data;\n" +
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
                s.append("            " + fieldName + ".setValue(data."+col.name+" == null ? null : data." + col.name + ".toLocalDate());\n");
            } else if (col.type.isTime()) {
                s.append("            " + fieldName + ".setValue(data."+col.name+" == null ? null : LocalDateTime.from(data." + col.name + ".toLocalTime()));\n");
            } else if (col.type.isTimestamp()) {
                s.append("            " + fieldName + ".setValue(data."+col.name+" == null ? null : data." + col.name + ".toLocalDateTime());\n");
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
                s.append("            data." + col.name + " = "+fieldName+".getValue() == null ? null : new java.sql.Date(" + fieldName + ".getValue().toEpochDay() * 86400000L);\n");
            } else if (col.type.isTime()) {
                s.append("            data." + col.name + " = "+fieldName+".getValue() == null ? null : new java.sql.Time("+fieldName+".getValue().toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);\n");
            } else if (col.type.isTimestamp()) {
                s.append("            data." + col.name + " = "+fieldName+".getValue() == null ? null : new java.sql.Timestamp("+fieldName+".getValue().toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);\n");
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
                "        }\n\n" +
                /* TODO
                "        public boolean isRemoveRefs = false;\n" +
                "        public class RemoveDialog extends Dialog{\n" +
                "            public Class<?>[] allRefs = {");
        s.append(genRefsClassList(getAllRefs(db, t)));
        s.append("}\n"+
                "            public RemoveDialog(boolean isRemoveRefs){\n" +
                "                add(\"Are you sure that you want to delete this? There are \"+allRefs.length+\" direct/indirect references to other tables.\");\n" +
                "                add(\"These reference in those rows will be either unset (set to -1) or the complete row deleted.\");\n" +
                "                add(\"You can choose below how to proceed with each reference.\");\n" +
                "            }\n" +
                "        }\n\n" +
                "        public RemoveDialog getRemoveDialog(){\n" +
                "            return new RemoveDialog(isRemoveRefs);" +
                "        }\n" +
                */
                "    }\n" + // CLOSE CLASS
                "\n" +
                "    public static volatile Function<"+t.name+", "+t.name+".Comp> global_fn_toComp = (obj) -> {return new "+t.name+".Comp(obj);};\n" +
                "    public volatile Function<Void, " + t.name + ".Comp> fn_toComp = (_null) -> {return global_fn_toComp.apply(this);};\n" +
                "    public " + t.name + ".Comp toComp(){\n" +
                "        return fn_toComp.apply(null);\n" +
                "    }\n" +
                "\n");

        return s.toString();
    }

    private static @NotNull Result colToVaadinComp(Database db, Table t, Column col) {
        StringBuilder s = new StringBuilder();
        String colName = firstToUpperCase(col.name);
        String compType = "";
        String fieldName = "";
        boolean isColumnRef = false;
        Table refTable = null;
        if (col.type.isEnum()) {
            fieldName = "sel" + colName;
            compType = "Select<" + t.name + "." + col.type.inJava + ">";
            s.append("        public "+compType+" " + fieldName +
                    " = new "+compType+"();\n" +
                    "        {" + fieldName + ".setLabel(\"" + colName + "\"); " + fieldName + ".setItems(" + t.name + "." + col.type.inJava + ".values()); }\n");
        } else if (col.type.inJava.equals("String")) {
            fieldName = "tf" + colName;
            compType = "TextField";
            s.append("        public TextField " + fieldName + " = new TextField(\"" + colName + "\");\n");
        } else if (col.type.isDate()) {
            fieldName = "df" + colName;
            compType = "DatePicker";
            s.append("        public DatePicker " + fieldName + " = new DatePicker(\"" + colName + "\");\n");
        } else if (col.type.isTime()) {
            fieldName = "df" + colName;
            compType = "DateTimePicker";
            s.append("        public DateTimePicker " + fieldName + " = new DateTimePicker(\"" + colName + "\");\n");
        } else if (col.type.isTimestamp()) {
            fieldName = "df" + colName;
            compType = "DateTimePicker";
            s.append("        public DateTimePicker " + fieldName + " = new DateTimePicker(\"" + colName + "\");\n");
        } else if (col.type.isBitOrBoolean()) {
            fieldName = "bs" + colName;
            compType = "BooleanSelect";
            s.append("        public BooleanSelect " + fieldName + " = new BooleanSelect(\"" + colName + "\", false);\n");
        } else {
            // This might be an id / reference to another table
            refTable = getRefTable(db, colName);
            if(refTable != null){
                isColumnRef = true;
                colName = colName.substring(0, colName.toLowerCase().lastIndexOf("id"));
                fieldName = "cb" + colName;
                compType = "ComboBox<" + refTable.name + ">";
                s.append(getComboBoxWithTableContent(refTable, fieldName, colName));
            } else{
                fieldName = "nf" + colName;
                compType = "NumberField";
                s.append("        public NumberField " + fieldName + " = new NumberField(\"" + colName + "\");\n");
            }
        }
        Result result = new Result(s.toString(), compType, fieldName, isColumnRef, refTable);
        return result;
    }

    private record Result(String generatedCode, String compType, String fieldName, boolean isColumnRef, Table refTable) {
    }

    private static String getComboBoxWithTableContent(Table t, String fieldName, String colName) {
        StringBuilder s = new StringBuilder();
        s.append("        public ComboBox<"+ t.name+"> " + fieldName + " = new ComboBox<"+ t.name+">(\"" + colName + "\");\n");
        s.append("        {"+ fieldName +".setItems("+ t.name+".get());\n" +
                "            "+ fieldName +".setRenderer(new ComponentRenderer<>(obj -> {\n" +
                "                Div div = new Div();\n"+
                "                div.setText(obj.toMinimalPrintString());\n" +
                "            return div;}));\n" +
                "            "+ fieldName +".setItemLabelGenerator(obj -> {\n" +
                "                return obj.toMinimalPrintString();\n" +
                "            });\n" +
                "        }\n");
        return s.toString();
    }

    public static String genRefsClassList(Map<Table, List<Column>> map) {
        StringBuilder paramsBuilder = new StringBuilder();
        map.forEach((t1, columns) -> {
            paramsBuilder.append(t1.name + ".class, ");
        });
        String params = paramsBuilder.toString();
        if (params.endsWith(", "))
            params = params.substring(0, params.length() - 2);
        return params;
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
