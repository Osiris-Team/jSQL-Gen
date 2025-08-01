package com.osiris.jsqlgen.jsqlgen;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Blob;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import java.util.function.Function;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;

/**
Table Tag with id 384785 and 2 changes/version. <br>
Structure (3 fields/columns): <br>
- int id = INT AUTO_INCREMENT NOT NULL PRIMARY KEY <br>
- String name = TEXT DEFAULT '' <br>
- String cssColor = TEXT DEFAULT '' <br>

Generated class by <a href="https://github.com/Osiris-Team/jSQL-Gen">jSQL-Gen</a>
that contains static methods for fetching/updating data from the `tag` table.
A single object/instance of this class represents a single row in the table
and data can be accessed via its public fields. <br>
<br>
You can add your own code to the bottom of this class. <br>
Do not modify the rest of this class since those changes will be removed at regeneration.
If modifications are really needed create a pull request directly to jSQL-Gen instead. <br>
<br>
Enabled modifiers: <br>
- NO EXCEPTIONS is enabled which makes it possible to use this methods outside of try/catch blocks because SQL errors will be caught and thrown as runtime exceptions instead. <br>
- VAADIN FLOW is enabled, which means that an additional obj.toComp() method<br>
will be generated that returns a Vaadin Flow UI Form representation that allows creating/updating/deleting a row/object. <br>
<br>
*/
public class Tag implements Database.Row{
  public static final int TABLE_ID = 384785;
/** Limitation: Not executed in constructor, but only the create methods. */
public static CopyOnWriteArrayList<Consumer<Tag>> onCreate = new CopyOnWriteArrayList<Consumer<Tag>>();
public static CopyOnWriteArrayList<Consumer<Tag>> onAdd = new CopyOnWriteArrayList<Consumer<Tag>>();
public static CopyOnWriteArrayList<Consumer<Tag>> onUpdate = new CopyOnWriteArrayList<Consumer<Tag>>();
public static CopyOnWriteArrayList<Consumer<Tag>> onRemove = new CopyOnWriteArrayList<Consumer<Tag>>();

private static boolean isEqual(Tag obj1, Tag obj2){ return obj1.equals(obj2) || obj1.getId() == obj2.getId(); }
public Object getId(){return id;}
public void setId(Object id){this.id = (int) id;}
public static volatile boolean isSimpleMinimalPrintString = false;
static {
try{
Connection con = Database.getCon();
try{
try (Statement s = con.createStatement()) {
Database.TableMetaData t = Database.getTableMetaData(384785);
for (int i = t.version; i < 2; i++) {
if(i == 0){
if(t.steps < 1){s.executeUpdate("CREATE TABLE IF NOT EXISTS `fakefiletag` (`id` INT AUTO_INCREMENT NOT NULL PRIMARY KEY)");
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 2){try{s.executeUpdate("ALTER TABLE `fakefiletag` ADD COLUMN `name` TEXT DEFAULT ''");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 3){try{s.executeUpdate("ALTER TABLE `fakefiletag` ADD COLUMN `cssColor` TEXT DEFAULT ''");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
t.steps = 0; t.version++;
Database.updateTableMetaData(t);
}
if(i == 1){
if(t.steps < 1){try{s.executeUpdate("ALTER TABLE `fakefiletag` RENAME `tag`");} catch (Exception e1){
try{s.executeUpdate("ALTER TABLE `fakefiletag` RENAME TO `tag`");} catch (Exception e2){try{s.executeUpdate("EXEC sp_rename `fakefiletag`, `tag`");} catch (Exception e3){
try{s.executeUpdate("RENAME  `fakefiletag` TO `tag`");} catch (Exception e4){
e1.printStackTrace();e2.printStackTrace();e3.printStackTrace();e4.printStackTrace(); throw new Exception("Failed to rename this table. Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and rename this table manually for now.");
}}}}t.steps++; Database.updateTableMetaData(t);}
t.steps = 0; t.version++;
Database.updateTableMetaData(t);
}
}
}

}
catch(Exception e){ throw new RuntimeException(e); }
finally {Database.freeCon(con);}

}catch(Exception e){
e.printStackTrace();
System.err.println("Something went really wrong during table (Tag) initialisation, subsequent operations will fail!");}
}

/**
Use the static create method instead of this constructor,
if you plan to add this object to the database in the future, since
that method fetches and sets/reserves the {@link #id}.
*/
public Tag (int id){
initDefaultFields();
this.id = id;
}
/**
Use the static create method instead of this constructor,
if you plan to add this object to the database in the future, since
that method fetches and sets/reserves the {@link #id}.
*/
public Tag (int id, String name, String cssColor){
initDefaultFields();
this.id = id;this.name = name;this.cssColor = cssColor;
}
/**
Database field/value: INT AUTO_INCREMENT NOT NULL PRIMARY KEY. <br>
*/
public int id = Database.defaultInMemoryOnlyObjId;
/**
Database field/value: INT AUTO_INCREMENT NOT NULL PRIMARY KEY. <br>

Convenience builder-like setter with method-chaining.
*/
public Tag id(int id){ this.id = id; return this;}
/**
Database field/value: TEXT DEFAULT ''. <br>

*/
public String name;
/**
Database field/value: TEXT DEFAULT ''. <br>


Convenience builder-like setter with method-chaining.
*/
public Tag name(String name){ this.name = name; return this;}
/**
Database field/value: TEXT DEFAULT ''. <br>

*/
public String cssColor;
/**
Database field/value: TEXT DEFAULT ''. <br>


Convenience builder-like setter with method-chaining.
*/
public Tag cssColor(String cssColor){ this.cssColor = cssColor; return this;}
/**
Initialises the DEFAULT fields with the provided default values mentioned in the columns definition.
*/
protected Tag initDefaultFields() {
this.name=""; this.cssColor=""; return this;
}

/**
Creates and returns an object that can be added to this table. <br>
The parameters of this method represent only the "NOT NULL" fields in the table and thus should not be null. <br>
- Id is NOT incremented, this is handled by the database, thus id is only usable after add() / insertion. <br>
- This method will NOT add the object to the table. <br>
- This is useful for objects that may never be added to the table, otherwise createAndAdd() is recommended. <br>
*/
public static Tag create() {
int id = Database.defaultInMemoryOnlyObjId;
Tag obj = new Tag(id);
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Creates and returns an object that can be added to this table. <br>
- Id is NOT incremented, this is handled by the database, thus id is only usable after add() / insertion. <br>
- This method will NOT add the object to the table. <br>
- This is useful for objects that may never be added to the table, otherwise createAndAdd() is recommended. <br>
*/
public static Tag create(String name, String cssColor)  {
int id = Database.defaultInMemoryOnlyObjId;
Tag obj = new Tag();
obj.id=id; obj.name=name; obj.cssColor=cssColor; 
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
The parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
*/
public static Tag createAndAdd()  {
int id = Database.defaultInMemoryOnlyObjId;
Tag obj = new Tag(id);
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
*/
public static Tag createAndAdd(String name, String cssColor)  {
int id = Database.defaultInMemoryOnlyObjId;
Tag obj = new Tag();
obj.id=id; obj.name=name; obj.cssColor=cssColor; 
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
@return a list containing all objects in this table.
*/
public static List<Tag> get()  {return get(null);}
/**
@return object with the provided id or null if there is no object with the provided id in this table.
@throws Exception on SQL issues.
*/
public static Tag get(int id)  {
try{
return get("WHERE id = "+id).get(0);
}catch(IndexOutOfBoundsException ignored){}
catch(Exception e){throw new RuntimeException(e);}
return null;
}
/**
@return object with the provided id or empty optional if there is no object with the provided id in this table.
@throws Exception on SQL issues.
*/
public static java.util.Optional<Tag> getOptional(int id)  {
try{
return java.util.Optional.of(get("WHERE id = "+id).get(0));
}catch(IndexOutOfBoundsException ignored){}
catch(Exception e){throw new RuntimeException(e);}
return java.util.Optional.empty();
}
/**
Example: <br>
get("WHERE username=? AND age=?", "Peter", 33);  <br>
@param where can be null. Your SQL WHERE statement (with the leading WHERE).
@param whereValues can be null. Your SQL WHERE statement values to set for '?'.
@return a list containing only objects that match the provided SQL WHERE statement (no matches = empty list).
if that statement is null, returns all the contents of this table.
*/
public static List<Tag> get(String where, Object... whereValues)  {
String sql = "SELECT `id`,`name`,`cssColor`" +
" FROM `tag`" +
(where != null ? where : "");
List<Tag> list = new ArrayList<>();
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(where!=null && whereValues!=null)
for (int i = 0; i < whereValues.length; i++) {
Object val = whereValues[i];
ps.setObject(i+1, val);
}
ResultSet rs = ps.executeQuery();
while (rs.next()) {
Tag obj = new Tag();
list.add(obj);
obj.id = rs.getInt(1);
obj.name = rs.getString(2);
obj.cssColor = rs.getString(3);
}
}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);}
return list;
}

    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Tag>> onResultReceived){
        return getLazy(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Tag>> onResultReceived, int limit){
        return getLazy(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Tag>> onResultReceived, Consumer<Long> onFinish){
        return getLazy(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Tag>> onResultReceived, Consumer<Long> onFinish, int limit){
        return getLazy(onResultReceived, onFinish, limit, null);
    }
    /**
     * Instead of using the SQL OFFSET keyword this function uses the primary key / id (must be numeric).
     * We do NOT use OFFSET due to performance and require a numeric id . <br>
     * Loads results lazily in a new thread. <br>
     * Add {@link Thread#sleep(long)} at the end of your onResultReceived code, to sleep between fetches.
     * @param onResultReceived can NOT be null. Gets executed until there are no results left, thus the results list is never empty.
     * @param onFinish can be null. Gets executed when finished receiving all results. Provides the total amount of received elements as parameter.
     * @param limit the maximum amount of elements for each fetch.
     * @param where can be null. This WHERE is not allowed to contain LIMIT and should not contain order by id.
     */
    public static Thread getLazy(Consumer<List<Tag>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
        Thread thread = new Thread(() -> {
            WHERE finalWhere;
            if(where == null) finalWhere = new WHERE("");
            else finalWhere = where;
            List<Tag> results;
            int lastId = -1;
            long count = 0;
            while(true){
                results = whereId().biggerThan(lastId).and(finalWhere).limit(limit).get();
                if(results.isEmpty()) break;
                lastId = (int) results.get(results.size() - 1).getId();
                count += results.size();
                onResultReceived.accept(results);
            }
            if(onFinish!=null) onFinish.accept(count);
        });
        thread.start();
        return thread;
    }

    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Tag>> onResultReceived){
        return getLazySync(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Tag>> onResultReceived, int limit){
        return getLazySync(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Tag>> onResultReceived, Consumer<Long> onFinish){
        return getLazySync(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Tag>> onResultReceived, Consumer<Long> onFinish, int limit){
        return getLazySync(onResultReceived, onFinish, limit, null);
    }
    /**
     * Waits until finished, then returns. <br>     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Tag>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
        Thread thread = getLazy(onResultReceived, onFinish, limit, where);
        while(thread.isAlive()) Thread.yield();
        return thread;
    }


/**
Note that this literally counts the rows thus its extremely slow in larger tables, its recommendedto use a workaround specific to your database instead. 
We are using this approach because its universal to all databases. 
*/
public static int count(){ return count(null, (Object[]) null); }

/**
Note that this literally counts the rows thus its extremely slow in larger tables, its recommendedto use a workaround specific to your database instead. 
We are using this approach because its universal to all databases. 
*/
public static int count(String where, Object... whereValues)  {
String sql = "SELECT COUNT(`id`) FROM `tag`" +
(where != null ? where : ""); 
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(where!=null && whereValues!=null)
for (int i = 0; i < whereValues.length; i++) {
Object val = whereValues[i];
ps.setObject(i+1, val);
}
ResultSet rs = ps.executeQuery();
if (rs.next()) return rs.getInt(1);
}catch(Exception e){throw new RuntimeException(e);}
finally {Database.freeCon(con);
}
return 0;
}

/**
Searches the provided object in the database (by its id),
and updates all its fields.
@throws Exception when failed to find by id or other SQL issues.
*/
public static void update(Tag obj)  {
String sql = "UPDATE `tag` SET `id`=?,`name`=?,`cssColor`=? WHERE id="+obj.getId();
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
ps.setInt(1, obj.id);
ps.setString(2, obj.name);
ps.setString(3, obj.cssColor);
ps.executeUpdate();
}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);
onUpdate.forEach(code -> code.accept(obj));
}
}

/**
Adds the provided object to the database (note that the id is not checked for duplicates).
*/
public static void add(Tag obj)  {
String sql = "INSERT INTO `tag` (`name`,`cssColor`) VALUES (?,?)";
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {
ps.setString(1, obj.name);
ps.setString(2, obj.cssColor);
ps.executeUpdate();
    try (ResultSet generatedKeys = ps.getGeneratedKeys()) { 
        if (generatedKeys.next()) { // Retrieve the first auto-generated ID
            int generatedId = generatedKeys.getInt(1);
            obj.id = generatedId;
        } else {
            //System.out.println("No ID generated."); This should never happen...
        }
    }}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);
onAdd.forEach(code -> code.accept(obj));
}
}

/**
Unsets its references (sets them to -1/'') and deletes the provided object from the database.
*/
public static void remove(Tag obj)  {
remove(obj, true, Database.isRemoveRefs);
}
/**
 * Deletes the provided object from the database.
 * @param unsetRefs If true, sets ids in other tables to -1/''.
 * @param removeRefs !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!! If true removes the complete obj/row(s) in all tables that reference/contain this id.
 *                   This is recursive. It's highly recommended to call removeRefs() before instead, which allows to explicitly exclude some tables.
*/
public static void remove(Tag obj, boolean unsetRefs, boolean removeRefs)  {
if(unsetRefs) unsetRefs(obj, TagEntry.class, true);
if(removeRefs) removeRefs(obj, TagEntry.class, true);
remove("WHERE id = "+obj.getId());
onRemove.forEach(code -> code.accept(obj));
}
/**
Example: <br>
remove("WHERE username=?", "Peter"); <br>
Deletes the objects that are found by the provided SQL WHERE statement, from the database.
@param where can NOT be null.
@param whereValues can be null. Your SQL WHERE statement values to set for '?'.
*/
public static void remove(String where, Object... whereValues)  {
java.util.Objects.requireNonNull(where);
String sql = "DELETE FROM `tag` "+where;
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i+1, val);
                }
ps.executeUpdate();
}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);
}
}

public static void removeAll()  {
getLazySync(objs -> {for(Tag obj : objs) {obj.remove();}});
    }

/**
     * @see #remove(Tag, boolean, boolean) 
     */
public static void unsetRefs(Tag obj, Class<TagEntry> tagId_in_TagEntry, boolean remove_tagId_in_TagEntry)  {
if (remove_tagId_in_TagEntry) {TagEntry.getLazySync(results -> { 
  for(TagEntry refObj : results) {refObj.tagId = -1; refObj.update();};
}, totalCount -> {}, 100, TagEntry.whereTagId().is(obj.id));}

    }

/** !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!!
     * @see #remove(Tag, boolean, boolean) 
     */
public static void removeRefs(Tag obj, Class<TagEntry> tagId_in_TagEntry, boolean remove_tagId_in_TagEntry)  {
// Take care of direct refs and indirect refs
if (remove_tagId_in_TagEntry) {TagEntry.getLazySync(results -> { 
  for(TagEntry refObj : results) {TagEntry.removeRefs(refObj);refObj.remove();};
}, totalCount -> {}, 100, TagEntry.whereTagId().is(obj.id));}



    }

public Tag clone(){
return new Tag(this.id,this.name,this.cssColor);
}
public void add(){
Tag.add(this);
}
public void update(){
Tag.update(this);
}
public void remove(){
Tag.remove(this);
}
public void remove(boolean unsetRefs, boolean removeRefs){
Tag.remove(this, unsetRefs, removeRefs);
}
public String toPrintString(){
return  ""+"id="+this.id+" "+"name="+this.name+" "+"cssColor="+this.cssColor+" ";
}
public String toMinimalPrintString(){ return toMinimalPrintString(true); }
public String toMinimalPrintString(boolean isFirstFieldOnly){
if(isFirstFieldOnly) return "" + this.name;
return ""+this.id+"; "+this.name+"; "+this.cssColor+"; "+"";
}
public static class BooleanSelect extends Select<Boolean> {
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
}// Executed for all objects
public static Consumer<Tag> onCreateV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onCreate.remove(code2);}); Tag.onCreate.add(code2); return code2;
}
// Executed for all objects
public static Consumer<Tag> onAddV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onAdd.remove(code2);}); Tag.onAdd.add(code2); return code2;
}
// Executed for all objects
public static Consumer<Tag> onUpdateV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onUpdate.remove(code2);}); Tag.onUpdate.add(code2); return code2;
}
// Executed for all objects
public static Consumer<Tag> onRemoveV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onRemove.remove(code2);}); Tag.onRemove.add(code2); return code2;
}


// Executed only for this object
public Consumer<Tag> onCreateThisV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onCreate.remove(code2);}); Tag.onCreate.add(code2); return code2;
}
// Executed only for this object
public Consumer<Tag> onAddThisV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onAdd.remove(code2);}); Tag.onAdd.add(code2); return code2;
}
// Executed only for this object
public Consumer<Tag> onUpdateThisV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onUpdate.remove(code2);}); Tag.onUpdate.add(code2); return code2;
}
// Executed only for this object
public Consumer<Tag> onRemoveThisV(Consumer<Tag> code){
UI ui = UI.getCurrent(); Consumer<Tag> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Tag.onRemove.remove(code2);}); Tag.onRemove.add(code2); return code2;
}


public static ComboBox<Tag> newTableComboBox(){ return newTableComboBox(false); }
public static ComboBox<Tag> newTableComboBox(boolean isIncluded_SelfID){
        ComboBox<Tag> comboBox = new ComboBox<Tag>("Tag");
        {comboBox.setItems(Tag.get());
            comboBox.setRenderer(new ComponentRenderer<>(obj -> {
                Div div = new Div();
                div.setText(obj.toMinimalPrintString()/* This columns table must contain only references too if you want to fetch their minimal string content */);
            return div;}));
            comboBox.setItemLabelGenerator(obj -> {
                return obj.toMinimalPrintString()/* This columns table must contain only references too if you want to fetch their minimal string content */;
            });
        }
return comboBox;
}

public static MultiSelectComboBox<Tag> newTableMultiSelect(){ return newTableMultiSelect(false); }
public static MultiSelectComboBox<Tag> newTableMultiSelect(boolean isIncluded_SelfID){
        MultiSelectComboBox<Tag> multiSelect = new MultiSelectComboBox<Tag>("Tag");
        {multiSelect.setItems(Tag.get());
            multiSelect.setRenderer(new ComponentRenderer<>(obj -> {
                Div div = new Div();
                div.setText(obj.toMinimalPrintString()/* This columns table must contain only references too if you want to fetch their minimal string content */);
            return div;}));
            multiSelect.setItemLabelGenerator(obj -> {
                return obj.toMinimalPrintString()/* This columns table must contain only references too if you want to fetch their minimal string content */;
            });
        }
return multiSelect;
}

public static NumberField newNfId(){
         NumberField nfId = new NumberField("Id");
return nfId;
}

public static TextField newTfName(){
         TextField tfName = new TextField("Name");
return tfName;
}

public static TextField newTfCssColor(){
         TextField tfCssColor = new TextField("CssColor");
return tfCssColor;
}

    /**
     * Gets executed later if {@link #isOnlyInMemory()}, otherwise provided
     * code gets executed directly.
     */    public void whenReadyV(Consumer<Tag> code) {
        if(isOnlyInMemory()) onAddThisV(obj -> code.accept(obj));
        else code.accept(this);
    }

    public static class Comp extends Database.RowCRUDVaadinComponent<Tag>{

        public Tag dataTag;

        // Form and fields
        public NumberField nfId = new NumberField("Id");
        public TextField tfName = new TextField("Name");
        public TextField tfCssColor = new TextField("CssColor");
        // Buttons

        {btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}
        public Consumer<ClickEvent<Button>> onBtnAddClick = (e) -> {
                btnAdd.setEnabled(false);
                updateData();
                Tag.add(data);
                e.unregisterListener(); // Make sure it gets only executed once
                updateButtons();
};
        
        {btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}
        public Consumer<ClickEvent<Button>> onBtnSaveClick = (e) -> {
                btnSave.setEnabled(false);
                updateData();
                Tag.update(data);
                btnSave.setEnabled(true);
                updateButtons();
};
        
        {btnDelete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);}
        public Consumer<ClickEvent<Button>> onBtnDeleteClick = (e) -> {
                btnDelete.setEnabled(false);
                Tag.remove(data);
                e.unregisterListener(); // Make sure it gets only executed once
                updateButtons();
};

        public Comp(Tag data) {
            this.data = data;
            this.dataTag = this.data;
            setWidthFull();
            setPadding(false);

            // Set defaults
            updateFields();

            // Add fields
            addAndExpand(form);
            form.setWidthFull();
            form.add(nfId);
            form.add(tfName);
            form.add(tfCssColor);


            // Tooltips
            Database.TableMetaData t = Database.getTableMetaData(384785);
            nfId.setTooltipText(t.comments[0]);
            tfName.setTooltipText(t.comments[1]);
            tfCssColor.setTooltipText(t.comments[2]);


            // Add buttons
            add(hlButtons);
            hlButtons.setPadding(false);
            hlButtons.setWidthFull();
            updateButtons();

            // Add button listeners
            btnAdd.addClickListener(e -> {onBtnAddClick.accept(e);});
            btnSave.addClickListener(e -> {onBtnSaveClick.accept(e);});
            btnDelete.addClickListener(e -> {onBtnDeleteClick.accept(e);});
        }

        public void updateFields(){
            nfId.setValue(0.0 + data.id);
            tfName.setValue(data.name);
            tfCssColor.setValue(data.cssColor);
        }
        public void updateData(){
            data.id = (int) nfId.getValue().doubleValue();
            data.name = tfName.getValue();
            data.cssColor = tfCssColor.getValue();
        }

        public void updateButtons(){
            hlButtons.removeAll();

            if(data.id < 0){ // In memory only, doesn't exist in db yet
                hlButtons.addAndExpand(btnAdd);
                return;
            }
            // Already exists
            hlButtons.add(btnDelete);
            hlButtons.addAndExpand(btnSave);
        }

    }

    public static volatile Function<Tag, Tag.Comp> global_fn_toComp = (obj) -> {return new Tag.Comp(obj);};
    public volatile Function<Void, Tag.Comp> fn_toComp = (_null) -> {return global_fn_toComp.apply(this);};
    public Tag.Comp toComp(){
        return fn_toComp.apply(null);
    }

public boolean isOnlyInMemory(){
return id == Database.defaultInMemoryOnlyObjId;
}
public static WHERE<Integer> whereId() {
return new WHERE<Integer>("`id`");
}
public static WHERE<String> whereName() {
return new WHERE<String>("`name`");
}
public static WHERE<String> whereCssColor() {
return new WHERE<String>("`cssColor`");
}
public static class WHERE<T> {
        /**
         * Remember to prepend WHERE on the final SQL statement.
         * This is not done by this class due to performance reasons. <p>
         * <p>
         * Note that it excepts the generated SQL string to be used by a {@link java.sql.PreparedStatement}
         * to protect against SQL-Injection. <p>
         * <p>
         * Also note that the SQL query gets optimized by the database automatically,
         * thus It's recommended to make queries as readable as possible and
         * not worry that much about performance.
         */
        public StringBuilder sqlBuilder = new StringBuilder();
        public StringBuilder orderByBuilder = new StringBuilder();
        public StringBuilder limitBuilder = new StringBuilder();
        List<Object> whereObjects = new ArrayList<>();
        private final String columnName;
        public WHERE(String columnName) {
            this.columnName = columnName;
        }

        /**
         * Executes the generated SQL statement
         * and returns a list of objects matching the query.
         */
        public List<Tag> get()  {
            String where = sqlBuilder.toString();
            if(!where.isEmpty()) where = " WHERE " + where;
            String orderBy = orderByBuilder.toString();
            if(!orderBy.isEmpty()) orderBy = " ORDER BY "+orderBy.substring(0, orderBy.length()-2)+" ";
            if(!whereObjects.isEmpty())
                return Tag.get(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                return Tag.get(where+orderBy+limitBuilder.toString(), (T[]) null);
        }

        /**
         * Executes the generated SQL statement
         * and returns the first object matching the query or null if none.
         */
        public Tag getFirstOrNull()  {
            List<Tag> results = get();
            if(results.isEmpty()) return null;
            else return results.get(0);
        }

        /**
         * Executes the generated SQL statement
         * and returns the first object matching the query or empty optional if none.
         */
        public java.util.Optional<Tag> getOptional()  {
            List<Tag> results = get();
            if(results.isEmpty()) return java.util.Optional.empty();
            else return java.util.Optional.of(results.get(0));
        }

        /**
         * Executes the generated SQL statement
         * and returns the size of the list of objects matching the query.
         */
        public int count()  {
            String where = sqlBuilder.toString();
            if(!where.isEmpty()) where = " WHERE " + where;
            String orderBy = orderByBuilder.toString();
            if(!orderBy.isEmpty()) orderBy = " ORDER BY "+orderBy.substring(0, orderBy.length()-2)+" ";
            if(!whereObjects.isEmpty())
                return Tag.count(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                return Tag.count(where+orderBy+limitBuilder.toString(), (T[]) null);
        }

        /**
         * Executes the generated SQL statement
         * and removes the objects matching the query.
         */
        public void remove()  {
            String where = sqlBuilder.toString();
            if(!where.isEmpty()) where = " WHERE " + where;
            String orderBy = orderByBuilder.toString();
            if(!orderBy.isEmpty()) orderBy = " ORDER BY "+orderBy.substring(0, orderBy.length()-2)+" ";
            if(!whereObjects.isEmpty())
                Tag.remove(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                Tag.remove(where+orderBy+limitBuilder.toString(), (T[]) null);
        }

        /**
         * AND (...) <br>
         */
        public WHERE<T> and(WHERE<?> where) {
            String sql = where.sqlBuilder.toString();
            if(!sql.isEmpty()) {
            sqlBuilder.append("AND (").append(sql).append(") ");
            whereObjects.addAll(where.whereObjects);
            }
            orderByBuilder.append(where.orderByBuilder.toString());
            return this;
        }

        /**
         * OR (...) <br>
         */
        public WHERE<T> or(WHERE<?> where) {
            String sql = where.sqlBuilder.toString();
            if(!sql.isEmpty()) {
            sqlBuilder.append("OR (").append(sql).append(") ");
            whereObjects.addAll(where.whereObjects);
            }
            orderByBuilder.append(where.orderByBuilder.toString());
            return this;
        }

        /**
         * columnName = ? <br>
         */
        public WHERE<T> is(T obj) {
            sqlBuilder.append(columnName).append(" = ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName IN (?,?,...) <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_in.asp">https://www.w3schools.com/mysql/mysql_in.asp</a>
         */
        public WHERE<T> is(T... objects) {
            String s = "";
            for (T obj : objects) {
                s += "?,";
                whereObjects.add(obj);
            }
            s = s.substring(0, s.length() - 1); // Remove last ,
            sqlBuilder.append(columnName).append(" IN (" + s + ") ");
            return this;
        }

        /**
         * columnName <> ? <br>
         */
        public WHERE<T> isNot(T obj) {
            sqlBuilder.append(columnName).append(" <> ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName IS NULL <br>
         */
        public WHERE<T> isNull() {
            sqlBuilder.append(columnName).append(" IS NULL ");
            return this;
        }

        /**
         * columnName IS NOT NULL <br>
         */
        public WHERE<T> isNotNull() {
            sqlBuilder.append(columnName).append(" IS NOT NULL ");
            return this;
        }

        /**
         * columnName LIKE ? <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> like(T obj) {
            sqlBuilder.append(columnName).append(" LIKE ? ");
            whereObjects.add(obj);
            return this;
        }
        /**
         * columnName LIKE ? <br>
         * Example: WHERE CustomerName LIKE 'a%' <br>
         * Explanation: Finds any values that start with "a" <br>
         * Note: Your provided obj gets turned to a string and if it already contains '_' or '%' these get escaped with '/' to ensure a correct query. <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> startsWith(T obj) {
            String s = obj.toString().replace("_", "/_").replace("%", "/%");
            s = s + "%";
            sqlBuilder.append(columnName).append(" LIKE ? ESCAPE '/' ");
            whereObjects.add(s);
            return this;
        }
        /**
         * columnName LIKE ? <br>
         * Example: WHERE CustomerName LIKE '%a' <br>
         * Explanation: Finds any values that end with "a" <br>
         * Note: Your provided obj gets turned to a string and if it already contains '_' or '%' these get escaped with '/' to ensure a correct query. <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> endsWith(T obj) {
            String s = obj.toString().replace("_", "/_").replace("%", "/%");
            s = "%" + s;
            sqlBuilder.append(columnName).append(" LIKE ? ESCAPE '/' ");
            whereObjects.add(s);
            return this;
        }
        /**
         * columnName LIKE ? <br>
         * Example: WHERE CustomerName LIKE '%or%' <br>
         * Explanation: Finds any values that have "or" in any position <br>
         * Note: Your provided obj gets turned to a string and if it already contains '_' or '%' these get escaped with '/' to ensure a correct query. <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> contains(T obj) {
            String s = obj.toString().replace("_", "/_").replace("%", "/%");
            s = "%" + s + "%";
            sqlBuilder.append(columnName).append(" LIKE ? ESCAPE '/' ");
            whereObjects.add(s);
            return this;
        }

        /**
         * columnName NOT LIKE ? <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> notLike(T obj) {
            sqlBuilder.append(columnName).append(" NOT LIKE ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName > ? <br>
         */
        public WHERE<T> biggerThan(T obj) {
            sqlBuilder.append(columnName).append(" > ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName < ? <br>
         */
        public WHERE<T> smallerThan(T obj) {
            sqlBuilder.append(columnName).append(" < ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName >= ? <br>
         */
        public WHERE<T> biggerOrEqual(T obj) {
            sqlBuilder.append(columnName).append(" >= ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName <= ? <br>
         */
        public WHERE<T> smallerOrEqual(T obj) {
            sqlBuilder.append(columnName).append(" <= ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName BETWEEN ? AND ? <br>
         */
        public WHERE<T> between(T obj1, T obj2) {
            sqlBuilder.append(columnName).append(" BETWEEN ? AND ? ");
            whereObjects.add(obj1);
            whereObjects.add(obj2);
            return this;
        }

        /**
         * columnName ASC, <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> smallestFirst() {
            orderByBuilder.append(columnName + " ASC, ");
            return this;
        }

        /**
         * columnName DESC, <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE<T> biggestFirst() {
            orderByBuilder.append(columnName + " DESC, ");
            return this;
        }

        /**
         * LIMIT number <br>
         *
         * @see <a href="https://www.w3schools.com/mysql/mysql_limit.asp">https://www.w3schools.com/mysql/mysql_limit.asp</a>
         */
        public WHERE<T> limit(int num) {
            limitBuilder.append("LIMIT ").append(num + " ");
            return this;
        }

    }
// The code below will not be removed when re-generating this class.
// Additional code start -> 
    private Tag(){}
// Additional code end <- 
}
