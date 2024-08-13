package com.osiris.jsqlgen.jsqlgen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Blob;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;

/**
Table TimerTask with id 598 and 1 changes/version. <br>
Structure (5 fields/columns): <br>
- int id = INT NOT NULL PRIMARY KEY <br>
- int timerId = INT NOT NULL <br>
- int taskId = INT NOT NULL <br>
- double percentageOfTimer = DOUBLE NOT NULL <br>
- String changelog = TEXT DEFAULT '' <br>

Generated class by <a href="https://github.com/Osiris-Team/jSQL-Gen">jSQL-Gen</a>
that contains static methods for fetching/updating data from the `timertask` table.
A single object/instance of this class represents a single row in the table
and data can be accessed via its public fields. <br>
<br>
You can add your own code to the bottom of this class. <br>
Do not modify the rest of this class since those changes will be removed at regeneration.
If modifications are really needed create a pull request directly to jSQL-Gen instead. <br>
<br>
Enabled modifiers: <br>
- NO EXCEPTIONS is enabled which makes it possible to use this methods outside of try/catch blocks because SQL errors will be caught and thrown as runtime exceptions instead. <br>
<br>
*/
public class TimerTask implements Database.Row{
public static class DefaultBlob implements Blob{
    private byte[] data;

    // Constructor that accepts a byte array
    public DefaultBlob(byte[] data) {
        this.data = data;
    }
    @Override
    public long length() throws SQLException {
        return data.length;
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        return data;
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        return 0;
    }

    @Override
    public long position(Blob pattern, long start) throws SQLException {
        return 0;
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        return 0;
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        return 0;
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        return null;
    }

    @Override
    public void truncate(long len) throws SQLException {

    }

    @Override
    public void free() throws SQLException {

    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        return new ByteArrayInputStream(data);
    }
}
/** Limitation: Not executed in constructor, but only the create methods. */
public static CopyOnWriteArrayList<Consumer<TimerTask>> onCreate = new CopyOnWriteArrayList<Consumer<TimerTask>>();
public static CopyOnWriteArrayList<Consumer<TimerTask>> onAdd = new CopyOnWriteArrayList<Consumer<TimerTask>>();
public static CopyOnWriteArrayList<Consumer<TimerTask>> onUpdate = new CopyOnWriteArrayList<Consumer<TimerTask>>();
public static CopyOnWriteArrayList<Consumer<TimerTask>> onRemove = new CopyOnWriteArrayList<Consumer<TimerTask>>();

private static boolean isEqual(TimerTask obj1, TimerTask obj2){ return obj1.equals(obj2) || obj1.id == obj2.id; }
public static java.util.concurrent.atomic.AtomicInteger idCounter = new java.util.concurrent.atomic.AtomicInteger(0);
public int getId(){return id;}
public void setId(int id){this.id = id;}
static {
try{
Connection con = Database.getCon();
try{
try (Statement s = con.createStatement()) {
Database.TableMetaData t = Database.getTableMetaData(598);
for (int i = t.version; i < 1; i++) {
if(i == 0){
if(t.steps < 1){s.executeUpdate("CREATE TABLE IF NOT EXISTS `timertask` (`id` INT NOT NULL PRIMARY KEY)");
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 2){try{s.executeUpdate("ALTER TABLE `timertask` ADD COLUMN `timerId` INT NOT NULL");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 3){try{s.executeUpdate("ALTER TABLE `timertask` ADD COLUMN `taskId` INT NOT NULL");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 4){try{s.executeUpdate("ALTER TABLE `timertask` ADD COLUMN `percentageOfTimer` DOUBLE NOT NULL");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 5){try{s.executeUpdate("ALTER TABLE `timertask` ADD COLUMN `changelog` TEXT DEFAULT ''");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains("duplicate column")) throw exAdd;}
t.steps++; Database.updateTableMetaData(t);}
t.steps = 0; t.version++;
Database.updateTableMetaData(t);
}
}

}
try (PreparedStatement ps = con.prepareStatement("SELECT id FROM `timertask` ORDER BY id DESC LIMIT 1")) {
ResultSet rs = ps.executeQuery();
if (rs.next()) idCounter.set(rs.getInt(1) + 1);
}
}
catch(Exception e){ throw new RuntimeException(e); }
finally {Database.freeCon(con);}
}catch(Exception e){
e.printStackTrace();
System.err.println("Something went really wrong during table (TimerTask) initialisation, thus the program will exit!");System.exit(1);}
}

/**
Use the static create method instead of this constructor,
if you plan to add this object to the database in the future, since
that method fetches and sets/reserves the {@link #id}.
*/
public TimerTask (int id, int timerId, int taskId, double percentageOfTimer){
initDefaultFields();
this.id = id;this.timerId = timerId;this.taskId = taskId;this.percentageOfTimer = percentageOfTimer;
}
/**
Use the static create method instead of this constructor,
if you plan to add this object to the database in the future, since
that method fetches and sets/reserves the {@link #id}.
*/
public TimerTask (int id, int timerId, int taskId, double percentageOfTimer, String changelog){
initDefaultFields();
this.id = id;this.timerId = timerId;this.taskId = taskId;this.percentageOfTimer = percentageOfTimer;this.changelog = changelog;
}
/**
Database field/value: INT NOT NULL PRIMARY KEY. <br>
*/
public int id;
/**
Database field/value: INT NOT NULL. <br>

*/
public int timerId;
/**
Database field/value: INT NOT NULL. <br>

*/
public int taskId;
/**
Database field/value: DOUBLE NOT NULL. <br>
0-100%
*/
public double percentageOfTimer;
/**
Database field/value: TEXT DEFAULT ''. <br>

*/
public String changelog;
/**
Initialises the DEFAULT fields with the provided default values mentioned in the columns definition.
*/
protected TimerTask initDefaultFields() {
this.changelog=""; return this;
}

/**
Creates and returns an object that can be added to this table.
Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
Also note that this method will NOT add the object to the table.
*/
public static TimerTask create(int timerId, int taskId, double percentageOfTimer) {
int id = idCounter.getAndIncrement();
TimerTask obj = new TimerTask(id, timerId, taskId, percentageOfTimer);
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Creates and returns an object that can be added to this table.
Increments the id (thread-safe) and sets it for this object (basically reserves a space in the database).
Note that this method will NOT add the object to the table.
*/
public static TimerTask create(int timerId, int taskId, double percentageOfTimer, String changelog)  {
int id = idCounter.getAndIncrement();
TimerTask obj = new TimerTask();
obj.id=id; obj.timerId=timerId; obj.taskId=taskId; obj.percentageOfTimer=percentageOfTimer; obj.changelog=changelog; 
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Creates and returns an in-memory object with -1 as id, that can be added to this table
AFTER you manually did obj.id = idCounter.getAndIncrement().
This is useful for objects that may never be added to the table.
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
Also note that this method will NOT add the object to the table.
*/
public static TimerTask createInMem(int timerId, int taskId, double percentageOfTimer) {
int id = -1;
TimerTask obj = new TimerTask(id, timerId, taskId, percentageOfTimer);
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Creates and returns an in-memory object with -1 as id, that can be added to this table
AFTER you manually did obj.id = idCounter.getAndIncrement().
This is useful for objects that may never be added to the table.
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
Also note that this method will NOT add the object to the table.
*/
public static TimerTask createInMem(int timerId, int taskId, double percentageOfTimer, String changelog)  {
int id = -1;
TimerTask obj = new TimerTask();
obj.id=id; obj.timerId=timerId; obj.taskId=taskId; obj.percentageOfTimer=percentageOfTimer; obj.changelog=changelog; 
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
Note that the parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
*/
public static TimerTask createAndAdd(int timerId, int taskId, double percentageOfTimer)  {
int id = idCounter.getAndIncrement();
TimerTask obj = new TimerTask(id, timerId, taskId, percentageOfTimer);
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
*/
public static TimerTask createAndAdd(int timerId, int taskId, double percentageOfTimer, String changelog)  {
int id = idCounter.getAndIncrement();
TimerTask obj = new TimerTask();
obj.id=id; obj.timerId=timerId; obj.taskId=taskId; obj.percentageOfTimer=percentageOfTimer; obj.changelog=changelog; 
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
@return a list containing all objects in this table.
*/
public static List<TimerTask> get()  {return get(null);}
/**
@return object with the provided id or null if there is no object with the provided id in this table.
@throws Exception on SQL issues.
*/
public static TimerTask get(int id)  {
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
public static List<TimerTask> get(String where, Object... whereValues)  {
String sql = "SELECT `id`,`timerId`,`taskId`,`percentageOfTimer`,`changelog`" +
" FROM `timertask`" +
(where != null ? where : "");
List<TimerTask> list = new ArrayList<>();
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(where!=null && whereValues!=null)
for (int i = 0; i < whereValues.length; i++) {
Object val = whereValues[i];
ps.setObject(i+1, val);
}
ResultSet rs = ps.executeQuery();
while (rs.next()) {
TimerTask obj = new TimerTask();
list.add(obj);
obj.id = rs.getInt(1);
obj.timerId = rs.getInt(2);
obj.taskId = rs.getInt(3);
obj.percentageOfTimer = rs.getDouble(4);
obj.changelog = rs.getString(5);
}
}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);}
return list;
}

    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<TimerTask>> onResultReceived){
        return getLazy(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<TimerTask>> onResultReceived, int limit){
        return getLazy(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<TimerTask>> onResultReceived, Consumer<Long> onFinish){
        return getLazy(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazy(Consumer<List<TimerTask>> onResultReceived, Consumer<Long> onFinish, int limit){
        return getLazy(onResultReceived, onFinish, limit, null);
    }
    /**
     * Loads results lazily in a new thread. <br>
     * Add {@link Thread#sleep(long)} at the end of your onResultReceived code, to sleep between fetches.
     * @param onResultReceived can NOT be null. Gets executed until there are no results left, thus the results list is never empty.
     * @param onFinish can be null. Gets executed when finished receiving all results. Provides the total amount of received elements as parameter.
     * @param limit the maximum amount of elements for each fetch.
     * @param where can be null. This WHERE is not allowed to contain LIMIT and should not contain order by id.
     */
    public static Thread getLazy(Consumer<List<TimerTask>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
        Thread thread = new Thread(() -> {
            WHERE finalWhere;
            if(where == null) finalWhere = new WHERE("");
            else finalWhere = where;
            List<TimerTask> results;
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
        });
        thread.start();
        return thread;
    }

    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<TimerTask>> onResultReceived){
        return getLazySync(onResultReceived, null, 500, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<TimerTask>> onResultReceived, int limit){
        return getLazySync(onResultReceived, null, limit, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<TimerTask>> onResultReceived, Consumer<Long> onFinish){
        return getLazySync(onResultReceived, onFinish, 500, null);
    }
    /**
     * See {@link #getLazySync(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<TimerTask>> onResultReceived, Consumer<Long> onFinish, int limit){
        return getLazySync(onResultReceived, onFinish, limit, null);
    }
    /**
     * Waits until finished, then returns. <br>     * See {@link #getLazy(Consumer, Consumer, int, WHERE)} for details.
     */
    public static Thread getLazySync(Consumer<List<TimerTask>> onResultReceived, Consumer<Long> onFinish, int limit, WHERE where) {
        Thread thread = getLazy(onResultReceived, onFinish, limit, where);
        while(thread.isAlive()) Thread.yield();
        return thread;
    }

public static int count(){ return count(null, null); }

public static int count(String where, Object... whereValues)  {
String sql = "SELECT COUNT(`id`) AS recordCount FROM `timertask`" +
(where != null ? where : ""); 
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
if(where!=null && whereValues!=null)
for (int i = 0; i < whereValues.length; i++) {
Object val = whereValues[i];
ps.setObject(i+1, val);
}
ResultSet rs = ps.executeQuery();
if (rs.next()) return rs.getInt("recordCount");
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
public static void update(TimerTask obj)  {
String sql = "UPDATE `timertask` SET `id`=?,`timerId`=?,`taskId`=?,`percentageOfTimer`=?,`changelog`=? WHERE id="+obj.id;
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
ps.setInt(1, obj.id);
ps.setInt(2, obj.timerId);
ps.setInt(3, obj.taskId);
ps.setDouble(4, obj.percentageOfTimer);
ps.setString(5, obj.changelog);
ps.executeUpdate();
}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);
onUpdate.forEach(code -> code.accept(obj));
}
}

/**
Adds the provided object to the database (note that the id is not checked for duplicates).
*/
public static void add(TimerTask obj)  {
String sql = "INSERT INTO `timertask` (`id`,`timerId`,`taskId`,`percentageOfTimer`,`changelog`) VALUES (?,?,?,?,?)";
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql)) {
ps.setInt(1, obj.id);
ps.setInt(2, obj.timerId);
ps.setInt(3, obj.taskId);
ps.setDouble(4, obj.percentageOfTimer);
ps.setString(5, obj.changelog);
ps.executeUpdate();
}catch(Exception e){throw new RuntimeException(e);}
finally{Database.freeCon(con);
onAdd.forEach(code -> code.accept(obj));
}
}

/**
Unsets its references (sets them to -1/'') and deletes the provided object from the database.
*/
public static void remove(TimerTask obj)  {
remove(obj, true, Database.isRemoveRefs);
}
/**
 * Deletes the provided object from the database.
 * @param unsetRefs If true, sets ids in other tables to -1/''.
 * @param removeRefs !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!! If true removes the complete obj/row(s) in all tables that reference/contain this id.
 *                   This is recursive. It's highly recommended to call removeRefs() before instead, which allows to explicitly exclude some tables.
*/
public static void remove(TimerTask obj, boolean unsetRefs, boolean removeRefs)  {
if(unsetRefs) unsetRefs(obj);
if(removeRefs) removeRefs(obj);
remove("WHERE id = "+obj.id);
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
String sql = "DELETE FROM `timertask` "+where;
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
getLazySync(objs -> {for(TimerTask obj : objs) {obj.remove();}});
    }

/**
     * @see #remove(TimerTask, boolean, boolean) 
     */
public static void unsetRefs(TimerTask obj)  {
    }

/** !!! EXTREME CAUTION REQUIRED, MAJOR DATA-LOSS POSSIBLE !!!
     * @see #remove(TimerTask, boolean, boolean) 
     */
public static void removeRefs(TimerTask obj)  {
// Take care of direct refs and indirect refs
    }

public TimerTask clone(){
return new TimerTask(this.id,this.timerId,this.taskId,this.percentageOfTimer,this.changelog);
}
public void add(){
TimerTask.add(this);
}
public void update(){
TimerTask.update(this);
}
public void remove(){
TimerTask.remove(this);
}
public void remove(boolean unsetRefs, boolean removeRefs){
TimerTask.remove(this, unsetRefs, removeRefs);
}
public String toPrintString(){
return  ""+"id="+this.id+" "+"timerId="+this.timerId+" "+"taskId="+this.taskId+" "+"percentageOfTimer="+this.percentageOfTimer+" "+"changelog="+this.changelog+" ";
}
public String toMinimalPrintString(){
return ""+this.id+"; "+this.timerId+"; "+this.taskId+"; "+this.percentageOfTimer+"; "+this.changelog+"; "+"";
}
public boolean isOnlyInMemory(){
return id < 0;
}
public static WHERE<Integer> whereId() {
return new WHERE<Integer>("`id`");
}
public static WHERE<Integer> whereTimerId() {
return new WHERE<Integer>("`timerId`");
}
public static WHERE<Integer> whereTaskId() {
return new WHERE<Integer>("`taskId`");
}
public static WHERE<Double> wherePercentageOfTimer() {
return new WHERE<Double>("`percentageOfTimer`");
}
public static WHERE<String> whereChangelog() {
return new WHERE<String>("`changelog`");
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
        public List<TimerTask> get()  {
            String where = sqlBuilder.toString();
            if(!where.isEmpty()) where = " WHERE " + where;
            String orderBy = orderByBuilder.toString();
            if(!orderBy.isEmpty()) orderBy = " ORDER BY "+orderBy.substring(0, orderBy.length()-2)+" ";
            if(!whereObjects.isEmpty())
                return TimerTask.get(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                return TimerTask.get(where+orderBy+limitBuilder.toString(), (T[]) null);
        }

        /**
         * Executes the generated SQL statement
         * and returns the first object matching the query or null if none.
         */
        public TimerTask getFirstOrNull()  {
            List<TimerTask> results = get();
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
                return TimerTask.count(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                return TimerTask.count(where+orderBy+limitBuilder.toString(), (T[]) null);
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
                TimerTask.remove(where+orderBy+limitBuilder.toString(), whereObjects.toArray());
            else
                TimerTask.remove(where+orderBy+limitBuilder.toString(), (T[]) null);
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
    private TimerTask(){}
// Additional code end <- 
}
