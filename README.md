# jSQL-Gen
Java SQL (JDBC) code generator with GUI. Removes boilerplate code and makes
it possible to use SQL databases without writing one line of SQL.

![image](https://user-images.githubusercontent.com/59899645/195866082-e0602e28-dad0-4321-b9e5-318645caa17f.png)

## Usage
- Install the latest [release](https://github.com/Osiris-Team/jSQL-Gen/releases/tag/latest) (.exe for Windows, other platforms not yet supported).
- Create a database, tables and their columns via the GUI. Its recommended to name your tables like you name your regular
Java objects, and your columns like your objects' fields.
- Press `Generate Code` and add the code to your project.
The generated code/files can also be found in the `generated` folder (press `Show Data` on the first tab, to open the location).
- Open `Database.java` and fill in your database credentials, and run your app.

## Example
I want to have a table named Person with the fields id, name and age. So I create it with the jSQL-Gen GUI and copy
the generated code into my project. Then I can do the following:
```java
// The first time you use Person, the database, Person table 
// and its (missing) columns will be created if needed.

// Inserting rows:
Person john = Person.create("John", 34); // id gets automatically set and incremented
Person.add(john);
Person.createAndAdd("John", 34); // The shorter variant

// Updating existing rows:
john.age = 36;
Person.update(john);

// Getting rows:
List<Person> allPersons = Person.get(); // Gets all rows.
List<Person> list = Person.whereName().is("John").get(); // Gets all rows where the name equals "John"

// Deleting rows:
Person.remove(john);
Person.whereName().is("John").remove(); // Removes all rows where the name equals "John"
```

## How?
- Generates one class for each table.
- The generated class contains static methods like `get() delete() update() add() etc...` to interact with the table.
- The generated class contains fields for each column `obj.id obj.name etc...`, which means that each instance/object
of the class represents one row.
- The generated code does not require any third party libraries and should work with Java 8 or higher. It uses the built in JDBC API for SQL queries.

## Features

#### Pros
- No runtime overhead for class generation (unlike other ORMs).
- Cached results for ultra-fast data retrieval 
(cache gets cleared after INSERT/UPDATE/DELETE operations and is
simply a map with SQL statements mapped to their results lists).
- Helper/Optional WHERE class for generating simple and complex SQL queries.
- Secured by default against SQL-Injection by using prepared statements.
- Simple UI to design databases within minutes.
- Autosuggestions for field definitions.
- Name your tables/columns however you like since internally names are encapsulated in backticks.
- Easily use multiple databases in a single project.


#### Cons
- Updating existing tables is a bit rough (removed fields/columns must be also removed manually from the database, especially "not null fields").

## Tipps
- You can rename/refactor generated Java classes and their fields/methods etc., but keep
in mind that those changes won't affect the actual database tables/columns.
- Thus changes to the database (specially changes in data types) should be made using the GUI.
- When dealing with big amounts of data its recommended to use the WHERE class to its full extend to avoid going out of memory. 
Lazy loading (https://github.com/Osiris-Team/jSQL-Gen/issues/10) not implemented yet.

## Details
### Cached Results

