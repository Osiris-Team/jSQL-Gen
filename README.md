# jSQL-Gen
Java SQL (JDBC) code generator with GUI. Removes 100% of the boilerplate code and makes
it possible to use SQL databases without writing one line of SQL (compile-safe SQL).

![image](https://user-images.githubusercontent.com/59899645/195866082-e0602e28-dad0-4321-b9e5-318645caa17f.png)

## Usage
1. Install the latest [release](https://github.com/Osiris-Team/jSQL-Gen/releases/tag/latest) (.exe for Windows, other platforms not yet supported).
2. Create a database, tables and their columns via the GUI. 
3. Press `Generate Code` and add the code to your project.
4. Open `Database.java`, fill in your database credentials, and run your app.

<p>
<sub>
*Select a project directory to directly generate the code in there.
**Instead of inserting raw, readable crendentials you can provide functions that fetch
those from a file. 
***Its expected that you have a database specific SQL driver in your projects like
<a href="https://github.com/mysql/mysql-connector-j">mysql-connector-j</a> for example.
</sub>
</p>

## Example
I want to have a table named Person with the fields id, name and age. So I create it with the jSQL-Gen GUI and copy
the generated code into my project. Then I can do the following:
![image](https://github.com/Osiris-Team/jSQL-Gen/assets/59899645/0bcb328f-00c1-4e93-8ef3-0c8ee5cdcd1e)

```java
// The first time you use Person, the database, Person table 
// and its columns (and missing columns) will be created if needed.

// Inserting rows:
Person john = Person.create("John", 34); // id gets automatically set and incremented
Person.add(john);
Person.createAndAdd("John", 34); // The shorter variant

// Updating existing rows:
john.age = 36;
Person.update(john);

// Getting rows:
List<Person> all = Person.get(); // Gets all rows.
List<Person> allNamedJohn = Person.whereName().is("John").get(); // Gets all rows where the name equals "John"
List<Person> allNamedJohn2 = Person.get("WHERE name=?", "John"); // Sames as above, but with regular SQL
// Lazily get rows:
Person.getLazy(results -> { // List with 1000 persons
  // Executed once every 1000 persons until all data is retrieved
}, totalCount -> {
  // Executed when finished
}, 1000); // Limit for each request 

// Deleting rows:
Person.remove(john);
Person.whereName().is("John").remove(); // Removes all rows where the name equals "John"
```

## How?
Generates one class for each table.
The generated class contains static methods like `get() delete() update() add() etc...` to interact with the table.
Each instance/objectof the class represents
one row and has public fields like `obj.id obj.name etc...`.

## Features

### ⚡️ 0% boilerplate, fast development and prototyping with UI
### ⚡️ Typesafe SQL queries via WHERE class
### ⚡️ Various utility methods, like fetching results lazily

#### Pros
- No runtime overhead for class generation (unlike other ORMs).
- 0% boilerplate, thus fast development and prototyping possible.
- Cached connection pool ensures optimal performance on small and huge databases.
Besides that it provides protection against timed out connections.
- (Optional) Cached results for ultra-fast data retrieval 
(cache gets cleared after INSERT/UPDATE/DELETE operations and is
simply a map with SQL statements mapped to their results lists).
- (Optional) Helper WHERE class for generating simple and complex SQL queries, from compile-safe functions.
- Secured against SQL-Injection by using prepared statements.
- Simple UI to design databases within minutes.
- Autosuggestions for field definitions.
- Name your tables/columns however you like since internally names are encapsulated in backticks.
- Easily use multiple databases in a single project.
- The generated Java code does not require any third party libraries and should work with Java 8 or higher. It uses the built in JDBC API for SQL queries.
- The generated SQL code should be compatible with all types of SQL databases.
- Supports all JDBC data types + some extras like enum.

#### Cons
- Updating existing tables is a bit rough (removed fields/columns must be also removed manually from the database, especially "not null fields").
A fix for this is being worked on: https://github.com/Osiris-Team/jSQL-Gen/issues/7

## Tipps
- You can select a project directory to directly generate the code in there. The generated code/files can also be found in the `generated` folder (press `Show Data` on the first tab, to open the location).
- Its possible to add additional Java code at the bottom of each generated class (only works when a project directory was selected).
- Its recommended to name your tables like you name your regular
Java objects, and your columns like your objects' fields.
- You can rename/refactor generated Java classes and their fields/methods etc., but keep
in mind that those changes won't affect the actual database tables/columns.
- Thus changes to the database (specially changes in data types) should be made using the GUI.
- When dealing with big amounts of data its recommended to use the WHERE class to its full extend to avoid going out of memory
or use the lazy loading methods.
- Make sure that all NOT NULL fields are on the upper half of the fields and the rest below. The generated functions will make more sense and will
be generally less error prone.
