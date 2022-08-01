# jSQL-Gen
Java SQL (JDBC) code generator with GUI. SQL and OOP finally united.

![](https://preview.redd.it/d4cc3ja872691.png?width=1154&format=png&auto=webp&s=9b0ecaecaf6087a4d4b9ecb065da587e782d62f5)

## Usage
- Install the latest [release](https://github.com/Osiris-Team/jSQL-Gen/releases/tag/latest).
- Create a database, tables and their columns via the GUI. Its recommended to name your tables like you name your regular
Java objects, and your columns like your objects' fields.
- Press `Generate Code` and add the code to your project.
The generated code/files can also be found in the `generated` folder (press `Show Data` on the first tab, to open the location).
- Open `Database.java` and fill in your database credentials, and run your app.

## How?
- Generates one class/object for each table.
- The generated object contains static methods like `get() delete() update() add() etc...` to interact with the table.
- The generated object contains fields for each column `obj.id obj.name etc...`.
- The generated code does not require any third party libraries and should work with Java 8 or higher. It uses the built in JDBC API for SQL queries.

## Features

#### Pros
- No runtime overhead for class generation (unlike other ORMs).
- Helper/Optional WHERE class for generating simple and complex SQL queries.
- Secured by default against SQL-Injection by using prepared statements.
- Simple UI to design databases within minutes.
- Autosuggestions for field definitions.

#### Cons
- Updating existing tables is a bit rough (removed fields/columns must be also removed manually from the database, especially "not null fields").

## Tipps
- You can rename/refactor generated Java classes and their fields/methods etc., but keep
in mind that those changes won't affect the actual database tables/columns.
- Thus changes to the database (specially changes in data types) should be made using the GUI.
