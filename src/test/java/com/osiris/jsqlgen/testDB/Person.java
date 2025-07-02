package com.osiris.jsqlgen.testDB;
import java.sql.Blob;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.Arrays;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import java.util.function.Function;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;

/**
Table Person with id 1 and 0 changes/version. <br>
Structure (8 fields/columns): <br>
- int id = INT AUTO_INCREMENT NOT NULL PRIMARY KEY <br>
- String name = TEXT NOT NULL <br>
- int age = INT NOT NULL <br>
- Flair flair = ENUM('COOL', 'CHILL', 'FLY') DEFAULT 'COOL' <br>
- String lastName = TEXT DEFAULT '' <br>
- int parentAge = INT DEFAULT 10 <br>
- Blob myblob = BLOB DEFAULT '' <br>
- Timestamp timestamp = TIMESTAMP DEFAULT NOW() <br>

Generated class by <a href="https://github.com/Osiris-Team/jSQL-Gen">jSQL-Gen</a>
that contains static methods for fetching/updating data from the `person` table.
A single object/instance of this class represents a single row in the table
and data can be accessed via its public fields. <br>
<br>
You can add your own code to the bottom of this class. <br>
Do not modify the rest of this class since those changes will be removed at regeneration.
If modifications are really needed create a pull request directly to jSQL-Gen instead. <br>
<br>
Enabled modifiers: <br>
- DEBUG is enabled, thus debug information will be printed out to System.err. <br>
- NO EXCEPTIONS is enabled which makes it possible to use this methods outside of try/catch blocks because SQL errors will be caught and thrown as runtime exceptions instead. <br>
- CACHE is enabled, which means that results of get() are saved in memory <br>
and returned the next time the same request is made. <br>
The returned list is a deep copy, thus you can modify the list and its elements fields in your thread safely. <br>
The cache gets cleared/invalidated at any update/insert/delete. <br>
- VAADIN FLOW is enabled, which means that an additional obj.toComp() method<br>
will be generated that returns a Vaadin Flow UI Form representation that allows creating/updating/deleting a row/object. <br>
<br>
*/
public class Person implements Database.Row{
public enum Flair {COOL, CHILL, FLY,}
/** Limitation: Not executed in constructor, but only the create methods. */
public static CopyOnWriteArrayList<Consumer<Person>> onCreate = new CopyOnWriteArrayList<Consumer<Person>>();
public static CopyOnWriteArrayList<Consumer<Person>> onAdd = new CopyOnWriteArrayList<Consumer<Person>>();
public static CopyOnWriteArrayList<Consumer<Person>> onUpdate = new CopyOnWriteArrayList<Consumer<Person>>();
public static CopyOnWriteArrayList<Consumer<Person>> onRemove = new CopyOnWriteArrayList<Consumer<Person>>();

private static boolean isEqual(Person obj1, Person obj2){ return obj1.equals(obj2) || obj1.getId() == obj2.getId(); }
    /**
     * Only works correctly if the package name is com.osiris.jsqlgen.
     */
    private static String minimalStackString(){
        StackTraceElement[] stack = new Exception().getStackTrace();
        String s = "";
        for (int i = stack.length - 1; i >= 1; i--) {
            StackTraceElement el = stack[i];
            if(el.getClassName().startsWith("java.") ||             el.getClassName().startsWith("com.osiris.jsqlgen")) continue;
            s = el.toString();
            break;
        }
        return s +"..."+ stack[1].toString(); //stack[0] == current method, gets ignored
    }
public Object getId(){return id;}
public void setId(Object id){this.id = (int) id;}
public static volatile boolean hasChanges = false;
static {
try{
Connection con = Database.getCon();
try{
try (Statement s = con.createStatement()) {
Database.TableMetaData t = Database.getTableMetaData(1);
for (int i = t.version; i < 1; i++) {
if(i == 0){
if(t.steps < 1){s.executeUpdate("CREATE TABLE IF NOT EXISTS `person` (`id` INT AUTO_INCREMENT NOT NULL PRIMARY KEY)");
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 2){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `name` TEXT NOT NULL");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 3){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `age` INT NOT NULL");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 4){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `flair` ENUM('COOL', 'CHILL', 'FLY') DEFAULT 'COOL'");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 5){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `lastName` TEXT DEFAULT ''");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 6){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `parentAge` INT DEFAULT 10");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 7){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `myblob` BLOB DEFAULT ''");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 8){try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `timestamp` TIMESTAMP DEFAULT NOW()");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
t.steps = 0; t.version++;
Database.updateTableMetaData(t);
}
}
    new Thread(() -> {
        try{
            onAdd.add(obj -> {hasChanges = true;});
            onRemove.add(obj -> {hasChanges = true;});
            onUpdate.add(obj -> {hasChanges = true;});
            while(true){
                Thread.sleep(10000);
                if(hasChanges){
                    hasChanges = false;
                    System.err.println("Changes for Person detected within the last 10 seconds, printing...");
                    Database.printTable(t);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }).start();

}

}
catch(Exception e){ throw new RuntimeException(e); }
finally {Database.freeCon(con);}

}catch(Exception e){
e.printStackTrace();
System.err.println("Something went really wrong during table (Person) initialisation, subsequent operations will fail!");}
}

    private static final List<CachedResult> cachedResults = new ArrayList<>();
    private static class CachedResult {
        public final String sql;
        public final Object[] whereValues;
        public final List<Person> results;
        public CachedResult(String sql, Object[] whereValues, List<Person> results) {
            this.sql = sql;
            this.whereValues = whereValues;
            this.results = results;
        }
        public List<Person> getResultsCopy(){
            synchronized (results){
                List<Person> list = new ArrayList<>(results.size());
                for (Person obj : results) {
                    list.add(obj.clone());
                }
                return list;
            }
        }
    }
    private static CachedResult cacheContains(String sql, Object[] whereValues){
        synchronized (cachedResults){
            for (CachedResult cr : cachedResults) {
                if(cr.sql.equals(sql) && Arrays.equals(cr.whereValues, whereValues)){
                    return cr;
                }
            }
        }
        return null;
    }
    public static void clearCache(){
        synchronized (cachedResults){ // Invalidate cache
            cachedResults.clear();
        }
    }

/**
Use the static create method instead of this constructor,
if you plan to add this object to the database in the future, since
that method fetches and sets/reserves the {@link #id}.
*/
public Person (int id, String name, int age){
initDefaultFields();
this.id = id;this.name = name;this.age = age;
}
/**
Use the static create method instead of this constructor,
if you plan to add this object to the database in the future, since
that method fetches and sets/reserves the {@link #id}.
*/
public Person (int id, String name, int age, Flair flair, String lastName, int parentAge, Blob myblob, Timestamp timestamp){
initDefaultFields();
this.id = id;this.name = name;this.age = age;this.flair = flair;this.lastName = lastName;this.parentAge = parentAge;this.myblob = myblob;this.timestamp = timestamp;
}
/**
Database field/value: INT AUTO_INCREMENT NOT NULL PRIMARY KEY. <br>
*/
public int id = Database.defaultInMemoryOnlyObjId;
/**
Database field/value: INT AUTO_INCREMENT NOT NULL PRIMARY KEY. <br>

Convenience builder-like setter with method-chaining.
*/
public Person id(int id){ this.id = id; return this;}
/**
Database field/value: TEXT NOT NULL. <br>
*/
public String name;
/**
Database field/value: TEXT NOT NULL. <br>

Convenience builder-like setter with method-chaining.
*/
public Person name(String name){ this.name = name; return this;}
/**
Database field/value: INT NOT NULL. <br>
*/
public int age;
/**
Database field/value: INT NOT NULL. <br>

Convenience builder-like setter with method-chaining.
*/
public Person age(int age){ this.age = age; return this;}
/**
Database field/value: ENUM('COOL', 'CHILL', 'FLY') DEFAULT 'COOL'. <br>
*/
public Flair flair;
/**
Database field/value: ENUM('COOL', 'CHILL', 'FLY') DEFAULT 'COOL'. <br>

Convenience builder-like setter with method-chaining.
*/
public Person flair(Flair flair){ this.flair = flair; return this;}
/**
Database field/value: TEXT DEFAULT ''. <br>
*/
public String lastName;
/**
Database field/value: TEXT DEFAULT ''. <br>

Convenience builder-like setter with method-chaining.
*/
public Person lastName(String lastName){ this.lastName = lastName; return this;}
/**
Database field/value: INT DEFAULT 10. <br>
*/
public int parentAge;
/**
Database field/value: INT DEFAULT 10. <br>

Convenience builder-like setter with method-chaining.
*/
public Person parentAge(int parentAge){ this.parentAge = parentAge; return this;}
/**
Database field/value: BLOB DEFAULT ''. <br>
*/
public Blob myblob;
/**
Database field/value: BLOB DEFAULT ''. <br>

Convenience builder-like setter with method-chaining.
*/
public Person myblob(Blob myblob){ this.myblob = myblob; return this;}
/**
Database field/value: TIMESTAMP DEFAULT NOW(). <br>
*/
public Timestamp timestamp;
/**
Database field/value: TIMESTAMP DEFAULT NOW(). <br>

Convenience builder-like setter with method-chaining.
*/
public Person timestamp(Timestamp timestamp){ this.timestamp = timestamp; return this;}
/**
Initialises the DEFAULT fields with the provided default values mentioned in the columns definition.
*/
protected Person initDefaultFields() {
this.flair=Flair.COOL; this.lastName=""; this.parentAge=10; this.myblob=new Database.DefaultBlob(new byte[0]); this.timestamp=new Timestamp(System.currentTimeMillis()); return this;
}

/**
Creates and returns an object that can be added to this table. <br>
The parameters of this method represent only the "NOT NULL" fields in the table and thus should not be null. <br>
- Id is NOT incremented, this is handled by the database, thus id is only usable after add() / insertion. <br>
- This method will NOT add the object to the table. <br>
- This is useful for objects that may never be added to the table, otherwise createAndAdd() is recommended. <br>
*/
public static Person create(String name, int age) {
int id = Database.defaultInMemoryOnlyObjId;
Person obj = new Person(id, name, age);
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Creates and returns an object that can be added to this table. <br>
- Id is NOT incremented, this is handled by the database, thus id is only usable after add() / insertion. <br>
- This method will NOT add the object to the table. <br>
- This is useful for objects that may never be added to the table, otherwise createAndAdd() is recommended. <br>
*/
public static Person create(String name, int age, Flair flair, String lastName, int parentAge, Blob myblob, Timestamp timestamp)  {
int id = Database.defaultInMemoryOnlyObjId;
Person obj = new Person();
obj.id=id; obj.name=name; obj.age=age; obj.flair=flair; obj.lastName=lastName; obj.parentAge=parentAge; obj.myblob=myblob; obj.timestamp=timestamp; 
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
The parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
*/
public static Person createAndAdd(String name, int age)  {
int id = Database.defaultInMemoryOnlyObjId;
Person obj = new Person(id, name, age);
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
*/
public static Person createAndAdd(String name, int age, Flair flair, String lastName, int parentAge, Blob myblob, Timestamp timestamp)  {
int id = Database.defaultInMemoryOnlyObjId;
Person obj = new Person();
obj.id=id; obj.name=name; obj.age=age; obj.flair=flair; obj.lastName=lastName; obj.parentAge=parentAge; obj.myblob=myblob; obj.timestamp=timestamp; 
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
@return a list containing all objects in this table.
*/
public static List<Person> get()  {return get(null);}
/**
@return object with the provided id or null if there is no object with the provided id in this table.
@throws Exception on SQL issues.
*/
public static Person get(int id)  {
try{
return get("WHERE id = "+id).get(0);
}catch(IndexOutOfBoundsException ignored){}
catch(Exception e){throw new RuntimeException(e);}
return null;
}
/**
Example: <br>
get("WHERE username=? AND age=?", "Peter", 33);  <br>
@param where can be null. Your SQL WHERE statement (with the leading WHERE).
@param whereValues can be null. Your SQL WHERE statement values to set for '?'.
@return a list containing only objects that match the provided SQL WHERE statement (no matches = empty list).
if that statement is null, returns all the contents of this table.
*/
public static List<Person> get(String where, Object... whereValues)  {
String sql = "SELECT `id`,`name`,`age`,`flair`,`lastName`,`parentAge`,`myblob`,`timestamp`" +
" FROM `person`" +
(where != null ? where : "");
synchronized(cachedResults){ CachedResult cachedResult = cacheContains(sql, whereValues);
if(cachedResult != null) return cachedResult.getResultsCopy();
List<Person> list = new ArrayList<>();
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(where!=null && whereValues!=null)
for (int i = 0; i < whereValues.length; i++) {
Object val = whereValues[i];
ps.setObject(i+1, val);
}
ResultSet rs = ps.executeQuery();
while (rs.next()) {
Person obj = new Person();
list.add(obj);
obj.id = rs.getInt(1);
obj.name = rs.getString(2);
obj.age = rs.getInt(3);
obj.flair = Flair.valueOf(rs.getString(4));
obj.lastName = rs.getString(5);
obj.parentAge = rs.getInt(6);
obj.myblob = rs.getBlob(7);
obj.timestamp = rs.getTimestamp(8);
}
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);}
cachedResults.add(new CachedResult(sql, whereValues, list));
return list;}
}

    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Person>> onResultReceived){
        return getLazy(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Person>> onResultReceived, int limit){
        return getLazy(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish){
        return getLazy(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish, int limit){
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
    public static Thread getLazy(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
        Thread thread = new Thread(() -> {
            WHERE finalWhere;
            if(where == null) finalWhere = new WHERE("");
            else finalWhere = where;
            List<Person> results;
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
    public static Thread getLazySync(Consumer<List<Person>> onResultReceived){
        return getLazySync(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Person>> onResultReceived, int limit){
        return getLazySync(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish){
        return getLazySync(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish, int limit){
        return getLazySync(onResultReceived, onFinish, limit, null);
    }
    /**
     * Waits until finished, then returns. <br>     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
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
String sql = "SELECT COUNT(`id`) FROM `person`" +
(where != null ? where : ""); 
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(where!=null && whereValues!=null)
for (int i = 0; i < whereValues.length; i++) {
Object val = whereValues[i];
ps.setObject(i+1, val);
}
ResultSet rs = ps.executeQuery();
if (rs.next()) return rs.getInt(1);
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally {System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);
}
return 0;
}

/**
Searches the provided object in the database (by its id),
and updates all its fields.
@throws Exception when failed to find by id or other SQL issues.
*/
public static void update(Person obj)  {
String sql = "UPDATE `person` SET `id`=?,`name`=?,`age`=?,`flair`=?,`lastName`=?,`parentAge`=?,`myblob`=?,`timestamp`=? WHERE id="+obj.getId();
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql)) {
ps.setInt(1, obj.id);
ps.setString(2, obj.name);
ps.setInt(3, obj.age);
ps.setString(4, obj.flair.name());
ps.setString(5, obj.lastName);
ps.setInt(6, obj.parentAge);
ps.setBlob(7, obj.myblob);
ps.setTimestamp(8, obj.timestamp);
ps.executeUpdate();
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);
clearCache();
onUpdate.forEach(code -> code.accept(obj));
}
}

/**
Adds the provided object to the database (note that the id is not checked for duplicates).
*/
public static void add(Person obj)  {
String sql = "INSERT INTO `person` (`name`,`age`,`flair`,`lastName`,`parentAge`,`myblob`,`timestamp`) VALUES (?,?,?,?,?,?,?)";
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {
ps.setString(1, obj.name);
ps.setInt(2, obj.age);
ps.setString(3, obj.flair.name());
ps.setString(4, obj.lastName);
ps.setInt(5, obj.parentAge);
ps.setBlob(6, obj.myblob);
ps.setTimestamp(7, obj.timestamp);
ps.executeUpdate();
    try (ResultSet generatedKeys = ps.getGeneratedKeys()) { 
        if (generatedKeys.next()) { // Retrieve the first auto-generated ID
            int generatedId = generatedKeys.getInt(1);
            obj.id = generatedId;
        } else {
            //System.out.println("No ID generated."); This should never happen...
        }
    }msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);
clearCache();
onAdd.forEach(code -> code.accept(obj));
}
}

/**
Unsets its references (sets them to -1/'') and deletes the provided object from the database.
*/
public static void remove(Person obj)  {
remove(obj, true, Database.isRemoveRefs);
}
/**
 * Deletes the provided object from the database.
 * @param unsetRefs If true, sets ids in other tables to -1/''.
 * @param removeRefs !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!! If true removes the complete obj/row(s) in all tables that reference/contain this id.
 *                   This is recursive. It's highly recommended to call removeRefs() before instead, which allows to explicitly exclude some tables.
*/
public static void remove(Person obj, boolean unsetRefs, boolean removeRefs)  {
if(unsetRefs) unsetRefs(obj, PersonOrder.class, true);
if(removeRefs) removeRefs(obj, PersonOrder.class, true);
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
String sql = "DELETE FROM `person` "+where;
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(whereValues != null)
                for (int i = 0; i < whereValues.length; i++) {
                    Object val = whereValues[i];
                    ps.setObject(i+1, val);
                }
ps.executeUpdate();
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);
clearCache();
}
}

public static void removeAll()  {
getLazySync(objs -> {for(Person obj : objs) {obj.remove();}});
    }

/**
     * @see #remove(Person, boolean, boolean) 
     */
public static void unsetRefs(Person obj, Class<PersonOrder> personId_in_PersonOrder, boolean remove_personId_in_PersonOrder)  {
if (remove_personId_in_PersonOrder) {PersonOrder.getLazySync(results -> { 
  for(PersonOrder refObj : results) {refObj.personId = -1; refObj.update();};
}, totalCount -> {}, 100, PersonOrder.wherePersonId().is(obj.id));}

    }

/** !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!!
     * @see #remove(Person, boolean, boolean) 
     */
public static void removeRefs(Person obj, Class<PersonOrder> personId_in_PersonOrder, boolean remove_personId_in_PersonOrder)  {
// Take care of direct refs and indirect refs
if (remove_personId_in_PersonOrder) {PersonOrder.getLazySync(results -> { 
  for(PersonOrder refObj : results) {PersonOrder.removeRefs(refObj);refObj.remove();};
}, totalCount -> {}, 100, PersonOrder.wherePersonId().is(obj.id));}



    }

public Person clone(){
return new Person(this.id,this.name,this.age,this.flair,this.lastName,this.parentAge,this.myblob,this.timestamp);
}
public void add(){
Person.add(this);
}
public void update(){
Person.update(this);
}
public void remove(){
Person.remove(this);
}
public void remove(boolean unsetRefs, boolean removeRefs){
Person.remove(this, unsetRefs, removeRefs);
}
public String toPrintString(){
return  ""+"id="+this.id+" "+"name="+this.name+" "+"age="+this.age+" "+"flair="+this.flair+" "+"lastName="+this.lastName+" "+"parentAge="+this.parentAge+" "+"myblob="+this.myblob+" "+"timestamp="+this.timestamp+" ";
}
public String toMinimalPrintString(){
return ""+this.id+"; "+this.name+"; "+this.age+"; "+this.flair+"; "+this.lastName+"; "+this.parentAge+"; "+"";
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
public static Consumer<Person> onCreateV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onCreate.remove(code2);}); Person.onCreate.add(code2); return code2;
}
// Executed for all objects
public static Consumer<Person> onAddV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onAdd.remove(code2);}); Person.onAdd.add(code2); return code2;
}
// Executed for all objects
public static Consumer<Person> onUpdateV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onUpdate.remove(code2);}); Person.onUpdate.add(code2); return code2;
}
// Executed for all objects
public static Consumer<Person> onRemoveV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onRemove.remove(code2);}); Person.onRemove.add(code2); return code2;
}


// Executed only for this object
public Consumer<Person> onCreateThisV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onCreate.remove(code2);}); Person.onCreate.add(code2); return code2;
}
// Executed only for this object
public Consumer<Person> onAddThisV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onAdd.remove(code2);}); Person.onAdd.add(code2); return code2;
}
// Executed only for this object
public Consumer<Person> onUpdateThisV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onUpdate.remove(code2);}); Person.onUpdate.add(code2); return code2;
}
// Executed only for this object
public Consumer<Person> onRemoveThisV(Consumer<Person> code){
UI ui = UI.getCurrent(); Consumer<Person> code2 = (obj) -> {if(!isEqual(this, obj)) return; ui.access(() -> {code.accept(obj);});}; ui.addDetachListener(e -> {Person.onRemove.remove(code2);}); Person.onRemove.add(code2); return code2;
}


public static ComboBox<Person> newTableComboBox(){
         ComboBox<Person> comboBox = new ComboBox<Person>("Person");
        {comboBox.setItems(Person.get());
            comboBox.setRenderer(new ComponentRenderer<>(obj -> {
                Div div = new Div();
                div.setText(obj.toMinimalPrintString());
            return div;}));
            comboBox.setItemLabelGenerator(obj -> {
                return obj.toMinimalPrintString();
            });
        }
return comboBox;
}

public static NumberField newNfId(){
         NumberField nfId = new NumberField("Id");
return nfId;
}

public static TextField newTfName(){
         TextField tfName = new TextField("Name");
return tfName;
}

public static NumberField newNfAge(){
         NumberField nfAge = new NumberField("Age");
return nfAge;
}

public static Select<Person.Flair> newSelFlair(){
         Select<Person.Flair> selFlair = new Select<Person.Flair>();
        {selFlair.setLabel("Flair"); selFlair.setItems(Person.Flair.values()); }
return selFlair;
}

public static TextField newTfLastName(){
         TextField tfLastName = new TextField("LastName");
return tfLastName;
}

public static NumberField newNfParentAge(){
         NumberField nfParentAge = new NumberField("ParentAge");
return nfParentAge;
}

public static DateTimePicker newDfTimestamp(){
         DateTimePicker dfTimestamp = new DateTimePicker("Timestamp");
return dfTimestamp;
}

    /**
     * Gets executed later if {@link #isOnlyInMemory()}, otherwise provided
     * code gets executed directly.
     */    public void whenReadyV(Consumer<Person> code) {
        if(isOnlyInMemory()) onAddThisV(obj -> code.accept(obj));
        else code.accept(this);
    }

    public static class Comp extends VerticalLayout{

        public Person dataPerson;
        public Person data;

        // Form and fields
        public FormLayout form = new FormLayout();
        public NumberField nfId = new NumberField("Id");
        public TextField tfName = new TextField("Name");
        public NumberField nfAge = new NumberField("Age");
        public Select<Person.Flair> selFlair = new Select<Person.Flair>();
        {selFlair.setLabel("Flair"); selFlair.setItems(Person.Flair.values()); }
        public TextField tfLastName = new TextField("LastName");
        public NumberField nfParentAge = new NumberField("ParentAge");
        public DateTimePicker dfTimestamp = new DateTimePicker("Timestamp");
        // Buttons
        public HorizontalLayout hlButtons = new HorizontalLayout();
        public Button btnAdd = new Button("Add");
        {btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}
        public Consumer<ClickEvent<Button>> onBtnAddClick = (e) -> {
                btnAdd.setEnabled(false);
                updateData();
                Person.add(data);
                e.unregisterListener(); // Make sure it gets only executed once
                updateButtons();
};
        public Button btnSave = new Button("Save");
        {btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);}
        public Consumer<ClickEvent<Button>> onBtnSaveClick = (e) -> {
                btnSave.setEnabled(false);
                updateData();
                Person.update(data);
                btnSave.setEnabled(true);
                updateButtons();
};
        public Button btnDelete = new Button("Delete");
        {btnDelete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);}
        public Consumer<ClickEvent<Button>> onBtnDeleteClick = (e) -> {
                btnDelete.setEnabled(false);
                Person.remove(data);
                e.unregisterListener(); // Make sure it gets only executed once
                updateButtons();
};

        public Comp(Person data) {
            this.data = data;
            this.dataPerson = this.data;
            setWidthFull();
            setPadding(false);

            // Set defaults
            updateFields();

            // Add fields
            addAndExpand(form);
            form.setWidthFull();
            form.add(nfId);
            form.add(tfName);
            form.add(nfAge);
            form.add(selFlair);
            form.add(tfLastName);
            form.add(nfParentAge);
            form.add(dfTimestamp);

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
            nfAge.setValue(0.0 + data.age);
            selFlair.setValue(data.flair);
            tfLastName.setValue(data.lastName);
            nfParentAge.setValue(0.0 + data.parentAge);
            dfTimestamp.setValue(data.timestamp.toLocalDateTime());
        }
        public void updateData(){
            data.id = (int) nfId.getValue().doubleValue();
            data.name = tfName.getValue();
            data.age = (int) nfAge.getValue().doubleValue();
            data.flair = selFlair.getValue();
            data.lastName = tfLastName.getValue();
            data.parentAge = (int) nfParentAge.getValue().doubleValue();
            data.timestamp = new java.sql.Timestamp(dfTimestamp.getValue().toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);
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

    public static volatile Function<Person, Person.Comp> global_fn_toComp = (obj) -> {return new Person.Comp(obj);};
    public volatile Function<Void, Person.Comp> fn_toComp = (_null) -> {return global_fn_toComp.apply(this);};
    public Person.Comp toComp(){
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
public static WHERE<Integer> whereAge() {
return new WHERE<Integer>("`age`");
}
public static WHERE<String> whereFlair() {
return new WHERE<String>("`flair`");
}
public static WHERE<String> whereLastName() {
return new WHERE<String>("`lastName`");
}
public static WHERE<Integer> whereParentAge() {
return new WHERE<Integer>("`parentAge`");
}
public static WHERE<Blob> whereMyblob() {
return new WHERE<Blob>("`myblob`");
}
public static WHERE<Timestamp> whereTimestamp() {
return new WHERE<Timestamp>("`timestamp`");
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
        public List<Person> get()  {
            String where = sqlBuilder.toString();
            if(!where.isEmpty()) where = " WHERE " + where;
            String orderBy = orderByBuilder.toString();
            if(!orderBy.isEmpty()) orderBy = " ORDER BY "+orderBy.substring(0, orderBy.length()-2)+" ";
            if(!whereObjects.isEmpty())
                return Person.get(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                return Person.get(where+orderBy+limitBuilder.toString(), (T[]) null);
        }

        /**
         * Executes the generated SQL statement
         * and returns the first object matching the query or null if none.
         */
        public Person getFirstOrNull()  {
            List<Person> results = get();
            if(results.isEmpty()) return null;
            else return results.get(0);
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
                return Person.count(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                return Person.count(where+orderBy+limitBuilder.toString(), (T[]) null);
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
                Person.remove(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                Person.remove(where+orderBy+limitBuilder.toString(), (T[]) null);
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
    private Person(){}
// Additional code end <- 
}
