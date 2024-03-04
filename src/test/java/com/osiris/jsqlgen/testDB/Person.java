package com.osiris.jsqlgen.testDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.ClickEvent;

/**
Generated class by <a href="https://github.com/Osiris-Team/jSQL-Gen">jSQL-Gen</a>
that contains static methods for fetching/updating data from the `person` table.
A single object/instance of this class represents a single row in the table
and data can be accessed via its public fields. <br>
<br>
You can add your own code to the top of this class. <br>
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
Structure (4 fields/columns): <br>
- int id = INT NOT NULL PRIMARY KEY <br>
- String name = TEXT NOT NULL <br>
- int age = INT NOT NULL <br>
- Flair flair = ENUM('COOL', 'CHILL', 'FLY') NOT NULL <br>
*/
public class Person{
// The code below will not be removed when re-generating this class.
// Additional code start -> 
private Person(){}
// Additional code end <- 
public enum Flair {COOL, CHILL, FLY,}
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
public static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);
static {
try{
Connection con = Database.getCon();
try{
try (Statement s = con.createStatement()) {
s.executeUpdate("CREATE TABLE IF NOT EXISTS `person` (`id` INT NOT NULL PRIMARY KEY)");
try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `name` TEXT NOT NULL");}catch(Exception ignored){}
s.executeUpdate("ALTER TABLE `person` MODIFY COLUMN `name` TEXT NOT NULL");
try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `age` INT NOT NULL");}catch(Exception ignored){}
s.executeUpdate("ALTER TABLE `person` MODIFY COLUMN `age` INT NOT NULL");
try{s.executeUpdate("ALTER TABLE `person` ADD COLUMN `flair` ENUM('COOL', 'CHILL', 'FLY') NOT NULL");}catch(Exception ignored){}
s.executeUpdate("ALTER TABLE `person` MODIFY COLUMN `flair` ENUM('COOL', 'CHILL', 'FLY') NOT NULL");
}
try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `person` ORDER BY id DESC LIMIT 1")) {
ResultSet rs = ps.executeQuery();
if (rs.next()) idCounter.set(rs.getInt(1) + 1);
}
}
catch(Exception e){ throw new RuntimeException(e); }
finally {Database.freeCon(con);}
}catch(Exception e){
e.printStackTrace();
System.err.println("Something went really wrong during table (Person) initialisation, thus the program will exit!");System.exit(1);}
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
public Person (int id, String name, int age, Flair flair){
this.id = id;this.name = name;this.age = age;this.flair = flair;
}
/**
Database field/value: INT NOT NULL PRIMARY KEY. <br>
*/
public int id;
/**
Database field/value: TEXT NOT NULL. <br>
*/
public String name;
/**
Database field/value: INT NOT NULL. <br>
*/
public int age;
/**
Database field/value: ENUM('COOL', 'CHILL', 'FLY') NOT NULL. <br>
*/
public Flair flair;
/**
Creates and returns an object that can be added to this table.
Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
Also note that this method will NOT add the object to the table.
*/
public static Person create( String name, int age, Flair flair) {
int id = idCounter.getAndIncrement();
Person obj = new Person(id, name, age, flair);
return obj;
}

/**
Creates and returns an in-memory object with -1 as id, that can be added to this table
AFTER you manually did obj.id = idCounter.getAndIncrement().
This is useful for objects that may never be added to the table.
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
Also note that this method will NOT add the object to the table.
*/
public static Person createInMem( String name, int age, Flair flair) {
int id = -1;
Person obj = new Person(id, name, age, flair);
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
*/
public static Person createAndAdd( String name, int age, Flair flair)  {
int id = idCounter.getAndIncrement();
Person obj = new Person(id, name, age, flair);
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
String sql = "SELECT `id`,`name`,`age`,`flair`" +
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
    public static void getLazy(Consumer<List<Person>> onResultReceived){
        getLazy(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static void getLazy(Consumer<List<Person>> onResultReceived, int limit){
        getLazy(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static void getLazy(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish){
        getLazy(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static void getLazy(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish, int limit){
        getLazy(onResultReceived, onFinish, limit, null);
    }
    /**
     * Loads results lazily in a new thread. <br>
     * Add {@link Thread#sleep(long)} at the end of your onResultReceived code, to sleep between fetches.
     * @param onResultReceived can NOT be null. Gets executed until there are no results left, thus the results list is never empty.
     * @param onFinish can be null. Gets executed when finished receiving all results. Provides the total amount of received elements as parameter.
     * @param limit the maximum amount of elements for each fetch.
     * @param where can be null. This WHERE is not allowed to contain LIMIT and should not contain order by id.
     */
    public static void getLazy(Consumer<List<Person>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
        new Thread(() -> {
            WHERE finalWhere;
            if(where == null) finalWhere = new WHERE("");
            else finalWhere = where;
            List<Person> results;
            int lastId = -1;
            long count = 0;
            while(true){
                results = whereId().biggerThan(lastId).and(finalWhere).limit(limit).get();
                if(results.isEmpty()) break;
                lastId = results.get(results.size() - 1).id;
                count += results.size();
                onResultReceived.accept(results);
            }
            if(onFinish!=null) onFinish.accept(count);
        }).start();
    }

public static int count(){ return count(null, null); }

public static int count(String where, Object... whereValues)  {
String sql = "SELECT COUNT(`id`) AS recordCount FROM `person`" +
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
if (rs.next()) return rs.getInt("recordCount");
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally {System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);}
return 0;
}

/**
Searches the provided object in the database (by its id),
and updates all its fields.
@throws Exception when failed to find by id or other SQL issues.
*/
public static void update(Person obj)  {
String sql = "UPDATE `person` SET `id`=?,`name`=?,`age`=?,`flair`=? WHERE id="+obj.id;
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql)) {
ps.setInt(1, obj.id);
ps.setString(2, obj.name);
ps.setInt(3, obj.age);
ps.setString(4, obj.flair.name());
ps.executeUpdate();
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);}
clearCache();
}

/**
Adds the provided object to the database (note that the id is not checked for duplicates).
*/
public static void add(Person obj)  {
String sql = "INSERT INTO `person` (`id`,`name`,`age`,`flair`) VALUES (?,?,?,?)";
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
try (PreparedStatement ps = con.prepareStatement(sql)) {
ps.setInt(1, obj.id);
ps.setString(2, obj.name);
ps.setInt(3, obj.age);
ps.setString(4, obj.flair.name());
ps.executeUpdate();
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);}
clearCache();
}

/**
Deletes the provided object from the database.
*/
public static void remove(Person obj)  {
remove("WHERE id = "+obj.id);
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
Database.freeCon(con);}
clearCache();
}

public static void removeAll()  {
String sql = "DELETE FROM `person`";
long msGetCon = System.currentTimeMillis(); long msJDBC = 0;
Connection con = Database.getCon();
msGetCon = System.currentTimeMillis() - msGetCon;
msJDBC = System.currentTimeMillis();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
msJDBC = System.currentTimeMillis() - msJDBC;
}catch(Exception e){throw new RuntimeException(e);}
        finally{System.err.println(sql+" /* //// msGetCon="+msGetCon+" msJDBC="+msJDBC+" con="+con+" minimalStack="+minimalStackString()+" */");
Database.freeCon(con);}
    }

public Person clone(){
return new Person(this.id,this.name,this.age,this.flair);
}
public Person add(){
Person.add(this);
return this;
}
public Person update(){
Person.update(this);
return this;
}
public Person remove(){
Person.remove(this);
return this;
}
public String toPrintString(){
return  ""+"id="+this.id+" "+"name="+this.name+" "+"age="+this.age+" "+"flair="+this.flair+" ";
}
    public static class PersonComp extends VerticalLayout{
        public Person data;

        // Form and fields
        public FormLayout form = new FormLayout();
        public NumberField nfId = new NumberField("Id");
        public TextField tfName = new TextField("Name");
        public NumberField nfAge = new NumberField("Age");
        public Select<Person.Flair> selFlair = new Select<Person.Flair>();
        {selFlair.setLabel("Flair"); selFlair.setItems(Person.Flair.values()); }
        // Buttons
        public HorizontalLayout hlButtons = new HorizontalLayout();
        public Button btnAdd = new Button("Add");
        public Consumer<ClickEvent<Button>> onBtnAddClick = (e) -> {
                btnAdd.setEnabled(false);
                data.id = idCounter.getAndIncrement();
                Person.add(data);
                e.unregisterListener(); // Make sure it gets only added once to the database
                updateButtons();
};
        public Button btnSave = new Button("Save");
        public Consumer<ClickEvent<Button>> onBtnSaveClick = (e) -> {
                btnSave.setEnabled(false);
                updateData();
                Person.update(data);
                btnSave.setEnabled(true);
                updateButtons();
};
        public Button btnDelete = new Button("Delete");
        public Consumer<ClickEvent<Button>> onBtnDeleteClick = (e) -> {
                btnDelete.setEnabled(false);
                Person.remove(data);
                e.unregisterListener(); // Make sure it gets only added once to the database
                updateButtons();
};

        public PersonComp(Person data) {
            this.data = data;
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
        }
        public void updateData(){
            data.id = (int) nfId.getValue().doubleValue();
            data.name = tfName.getValue();
            data.age = (int) nfAge.getValue().doubleValue();
            data.flair = selFlair.getValue();
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

    public PersonComp toComp(){
        return new PersonComp(this);
    }

public boolean isOnlyInMemory(){
return id < 0;
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
}
