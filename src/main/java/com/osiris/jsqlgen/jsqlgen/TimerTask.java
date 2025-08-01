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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;

/**
Table TimerTask with id 598 and 2 changes/version. <br>
Structure (5 fields/columns): <br>
- int id = INT AUTO_INCREMENT NOT NULL PRIMARY KEY <br>
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
  public static final int TABLE_ID = 598;
/** Limitation: Not executed in constructor, but only the create methods. */
public static CopyOnWriteArrayList<Consumer<TimerTask>> onCreate = new CopyOnWriteArrayList<Consumer<TimerTask>>();
public static CopyOnWriteArrayList<Consumer<TimerTask>> onAdd = new CopyOnWriteArrayList<Consumer<TimerTask>>();
public static CopyOnWriteArrayList<Consumer<TimerTask>> onUpdate = new CopyOnWriteArrayList<Consumer<TimerTask>>();
public static CopyOnWriteArrayList<Consumer<TimerTask>> onRemove = new CopyOnWriteArrayList<Consumer<TimerTask>>();

private static boolean isEqual(TimerTask obj1, TimerTask obj2){ return obj1.equals(obj2) || obj1.getId() == obj2.getId(); }
public Object getId(){return id;}
public void setId(Object id){this.id = (int) id;}
public static volatile boolean isSimpleMinimalPrintString = false;
static {
try{
Connection con = Database.getCon();
try{
try (Statement s = con.createStatement()) {
Database.TableMetaData t = Database.getTableMetaData(598);
for (int i = t.version; i < 2; i++) {
if(i == 0){
if(t.steps < 1){s.executeUpdate("CREATE TABLE IF NOT EXISTS `timertask` (`id` INT AUTO_INCREMENT NOT NULL PRIMARY KEY)");
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
if(i == 1){
if(t.steps < 1){t.steps++; Database.updateTableMetaData(t);}
if(t.steps < 2){s.execute("SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';");
s.executeUpdate("ALTER TABLE `timertask` MODIFY COLUMN `id` INT AUTO_INCREMENT NOT NULL ");
s.execute("SET SESSION sql_mode='';");
t.steps++; Database.updateTableMetaData(t);}
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
System.err.println("Something went really wrong during table (TimerTask) initialisation, subsequent operations will fail!");}
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
Database field/value: INT AUTO_INCREMENT NOT NULL PRIMARY KEY. <br>
*/
public int id = Database.defaultInMemoryOnlyObjId;
/**
Database field/value: INT AUTO_INCREMENT NOT NULL PRIMARY KEY. <br>

Convenience builder-like setter with method-chaining.
*/
public TimerTask id(int id){ this.id = id; return this;}
/**
Database field/value: INT NOT NULL. <br>

*/
public int timerId;
/**
Database field/value: INT NOT NULL. <br>


Convenience builder-like setter with method-chaining.
*/
public TimerTask timerId(int timerId){ this.timerId = timerId; return this;}
/**
Database field/value: INT NOT NULL. <br>

*/
public int taskId;
/**
Database field/value: INT NOT NULL. <br>


Convenience builder-like setter with method-chaining.
*/
public TimerTask taskId(int taskId){ this.taskId = taskId; return this;}
/**
Database field/value: DOUBLE NOT NULL. <br>
0-100%
*/
public double percentageOfTimer;
/**
Database field/value: DOUBLE NOT NULL. <br>
0-100%

Convenience builder-like setter with method-chaining.
*/
public TimerTask percentageOfTimer(double percentageOfTimer){ this.percentageOfTimer = percentageOfTimer; return this;}
/**
Database field/value: TEXT DEFAULT ''. <br>

*/
public String changelog;
/**
Database field/value: TEXT DEFAULT ''. <br>


Convenience builder-like setter with method-chaining.
*/
public TimerTask changelog(String changelog){ this.changelog = changelog; return this;}
/**
Initialises the DEFAULT fields with the provided default values mentioned in the columns definition.
*/
protected TimerTask initDefaultFields() {
this.changelog=""; return this;
}

/**
Creates and returns an object that can be added to this table. <br>
The parameters of this method represent only the "NOT NULL" fields in the table and thus should not be null. <br>
- Id is NOT incremented, this is handled by the database, thus id is only usable after add() / insertion. <br>
- This method will NOT add the object to the table. <br>
- This is useful for objects that may never be added to the table, otherwise createAndAdd() is recommended. <br>
*/
public static TimerTask create(int timerId, int taskId, double percentageOfTimer) {
int id = Database.defaultInMemoryOnlyObjId;
TimerTask obj = new TimerTask(id, timerId, taskId, percentageOfTimer);
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Creates and returns an object that can be added to this table. <br>
- Id is NOT incremented, this is handled by the database, thus id is only usable after add() / insertion. <br>
- This method will NOT add the object to the table. <br>
- This is useful for objects that may never be added to the table, otherwise createAndAdd() is recommended. <br>
*/
public static TimerTask create(int timerId, int taskId, double percentageOfTimer, String changelog)  {
int id = Database.defaultInMemoryOnlyObjId;
TimerTask obj = new TimerTask();
obj.id=id; obj.timerId=timerId; obj.taskId=taskId; obj.percentageOfTimer=percentageOfTimer; obj.changelog=changelog; 
onCreate.forEach(code -> code.accept(obj));
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
The parameters of this method represent "NOT NULL" fields in the table and thus should not be null.
*/
public static TimerTask createAndAdd(int timerId, int taskId, double percentageOfTimer)  {
int id = Database.defaultInMemoryOnlyObjId;
TimerTask obj = new TimerTask(id, timerId, taskId, percentageOfTimer);
onCreate.forEach(code -> code.accept(obj));
add(obj);
return obj;
}

/**
Convenience method for creating and directly adding a new object to the table.
*/
public static TimerTask createAndAdd(int timerId, int taskId, double percentageOfTimer, String changelog)  {
int id = Database.defaultInMemoryOnlyObjId;
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
@return object with the provided id or empty optional if there is no object with the provided id in this table.
@throws Exception on SQL issues.
*/
public static java.util.Optional<TimerTask> getOptional(int id)  {
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
     * Instead of using the SQL OFFSET keyword this function uses the primary key / id (must be numeric).
     * We do NOT use OFFSET due to performance and require a numeric id . <br>
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
String sql = "SELECT COUNT(`id`) FROM `timertask`" +
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
public static void update(TimerTask obj)  {
String sql = "UPDATE `timertask` SET `id`=?,`timerId`=?,`taskId`=?,`percentageOfTimer`=?,`changelog`=? WHERE id="+obj.getId();
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
String sql = "INSERT INTO `timertask` (`timerId`,`taskId`,`percentageOfTimer`,`changelog`) VALUES (?,?,?,?)";
Connection con = Database.getCon();
try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {
ps.setInt(1, obj.timerId);
ps.setInt(2, obj.taskId);
ps.setDouble(3, obj.percentageOfTimer);
ps.setString(4, obj.changelog);
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
public String toMinimalPrintString(){ return toMinimalPrintString(true); }
public String toMinimalPrintString(boolean isFirstFieldOnly){
if(isFirstFieldOnly) return "" + this.timerId;
return ""+this.id+"; "+this.timerId+"; "+this.taskId+"; "+this.percentageOfTimer+"; "+this.changelog+"; "+"";
}
public boolean isOnlyInMemory(){
return id == Database.defaultInMemoryOnlyObjId;
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
         * and returns the first object matching the query or empty optional if none.
         */
        public java.util.Optional<TimerTask> getOptional()  {
            List<TimerTask> results = get();
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
