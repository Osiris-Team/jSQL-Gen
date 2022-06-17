# jSQL-Gen
Java SQL code generator. SQL and OOP finally united.

![](https://preview.redd.it/d4cc3ja872691.png?width=1154&format=png&auto=webp&s=9b0ecaecaf6087a4d4b9ecb065da587e782d62f5)

## Usage
- Download and run the latest [release](https://github.com/Osiris-Team/jSQL-Gen/releases/tag/latest).
- Create a database, tables and their columns via the GUI.
- Press `Generate Code` and add the code to your project.
The generated code/files can also be found in the `generated` folder (press `Show Data` on the first tab, to open the location).
- Open `Database.java` and fill in your database credentials, and run your app.

## What?
- Generates one class/object for each table.
- The generated object contains static methods like `get() delete() update() add() etc...` to interact with the table.
- The generated object contains fields for each table column `obj.id obj.name etc...`.
- The generated code does not require any third party libraries and should work with Java 8 or higher. It uses the built in JDBC API for SQL queries. 
