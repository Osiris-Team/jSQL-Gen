# jSQL-Gen
Java SQL code generator. SQL and OOP finally united.

![](https://preview.redd.it/d4cc3ja872691.png?width=1154&format=png&auto=webp&s=9b0ecaecaf6087a4d4b9ecb065da587e782d62f5)

## Usage
- Download and run the latest [release](https://github.com/Osiris-Team/jSQL-Gen/releases/tag/latest).
- Create database, a table and their columns via the GUI.
- Press `Generate Code` and add the code to your project.
- The files are also in the `generated` folder (press `Show Data` on the first tab, to open the location).

## What?
- Generates one class/object for each table.
- The generated object contains static methods like `get() delete() update() add() etc...` to modify/fetch/update the table.
- The generated object contains fields for each table column `obj.id obj.name etc...`.
