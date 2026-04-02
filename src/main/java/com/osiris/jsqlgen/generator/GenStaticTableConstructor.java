package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;
import com.osiris.jsqlgen.utils.UString;

import java.util.ArrayList;

public class GenStaticTableConstructor {
    public static String s(Database db, Table t, String tCurrentNameQuoted) {
        StringBuilder s = new StringBuilder();
        if(t.isDebug) s.append("public static volatile boolean hasChanges = false;\n");
        s.append("public static volatile boolean isSimpleMinimalPrintString = false;\n");
        s.append("static {\n" +
                "try{\n" + // Without this additional try/catch that encapsulates the complete code inside static constructor
                // we somehow get problems like class not found exception
                "Connection con = Database.getCon();\n" +
                "try{\n");

        if(db.isVersioning){
            s.append("try (Statement s = con.createStatement()) {\n" +
                "Database.TableMetaData t = Database.getTableMetaData("+t.id+");\n");
            s.append("for (int i = t.version; i < "+t.changes.size()+"; i++) {\n");
            ArrayList<TableChange> changes = t.changes;
            for (int i = 0; i < changes.size(); i++) {
                s.append("if(i == "+i+"){\n");
                TableChange change = changes.get(i);
                String tNameNewQuoted = JavaCodeGenerator.getSQLTableNameQuoted(change.newTableName);
                String tNameOldQuoted = JavaCodeGenerator.getSQLTableNameQuoted(change.oldTableName);
                int stepsToComplete = 0;

                stepsToComplete++;
                s.append("if(t.steps < "+stepsToComplete+"){" +
                    "Database.beforeTableChange.forEach(code -> code.accept(t));\n");
                if(i == 0){
                    s.append("s.executeUpdate(\"CREATE TABLE IF NOT EXISTS " + tNameNewQuoted + " (" + t.columns.get(0).nameQuoted // EXPECTS ID
                        + " " + t.columns.get(0).definition + ")\");\n");
                }
                else if(!change.oldTableName.equals(change.newTableName)){
                    // MySQL / MariaDB:
                    s.append("try{s.executeUpdate(\"ALTER TABLE "+ tNameOldQuoted +" RENAME "+ tNameNewQuoted +"\");} catch (Exception e1){\n" +
                        // PostgreSQL:
                        "try{s.executeUpdate(\"ALTER TABLE "+ tNameOldQuoted +" RENAME TO "+ tNameNewQuoted +"\");} catch (Exception e2){" +
                        // SQL server:
                        "try{s.executeUpdate(\"EXEC sp_rename "+ tNameOldQuoted +", "+ tNameNewQuoted +"\");} catch (Exception e3){\n" +
                        // Oracle SQL:
                        "try{s.executeUpdate(\"RENAME  "+ tNameOldQuoted +" TO "+ tNameNewQuoted +"\");} catch (Exception e4){\n" +
                        "e1.printStackTrace();e2.printStackTrace();e3.printStackTrace();e4.printStackTrace(); throw new Exception(\"Failed to rename this table." +
                        " Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and rename this table manually for now.\");\n" +
                        "}}}}");
                }
                s.append("t.steps++; Database.updateTableMetaData(t); Database.afterTableChange.forEach(code -> code.accept(t));}\n"); // steps++ and update metadata and close if

                int jStart = 0;
                if(i == 0) jStart = 1; // We are in creation aka first change (i=0),
                // thus skip first column (id) to avoid "SQLSyntaxErrorException: Multiple primary key defined"

                // Rename columns
                for (int j = jStart; j < change.oldColumnNames.size(); j++) {
                    try{
                        stepsToComplete++;
                        s.append("if(t.steps < "+stepsToComplete+"){" +
                            "Database.beforeTableChange.forEach(code -> code.accept(t));\n");
                        String oldColName = change.oldColumnNames.get(j);
                        String newColName = change.newColumnNames.get(j);
                        String newColDef = change.newColumnNames_Definitions.get(j);
                        // MySQL / MariaDB / PostgreSQL / Oracle SQL:
                        s.append("" +
                            "try{s.executeUpdate(\"ALTER TABLE "+ tNameNewQuoted +" RENAME COLUMN `"+oldColName+"` TO `"+newColName+"`\");} catch (Exception e1){\n" +
                            // Older MySQL/MariaDB versions:
                            "try{s.executeUpdate(\"ALTER TABLE "+ tNameNewQuoted +" CHANGE `"+oldColName+"` `"+newColName+"` "+newColDef+"\");} catch (Exception e2){\n" +
                            // SQL server: :
                            "try{s.executeUpdate(\"EXEC sp_rename `"+change.newTableName.toLowerCase()+"."+oldColName+"`, `"+newColName+"`, `COLUMN`\");} catch (Exception e3){" +
                            "e1.printStackTrace();e2.printStackTrace();e3.printStackTrace(); throw new Exception(\"Failed to rename this column." +
                            " Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and rename this column manually for now.\");\n" +
                            "}}}");
                        s.append("t.steps++; Database.updateTableMetaData(t); Database.afterTableChange.forEach(code -> code.accept(t));}\n"); // steps++ and update metadata and close if
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate 'rename columns' step for "+t.name+" with change: \n"+ Data.parser.toJson(change), e);
                    }
                }

                // Change columns definitions
                for (int j = jStart; j < change.newColumnDefinitions.size(); j++) {
                    stepsToComplete++;
                    s.append("if(t.steps < "+stepsToComplete+"){\n" +
                        "Database.beforeTableChange.forEach(code -> code.accept(t));\n");
                    String oldColDef = change.oldColumnDefinitions.get(j);
                    String newColDef = change.newColumnDefinitions.get(j);
                    String newColName = change.newColumnDefinitions_Names.get(j);
                    boolean isNewPrimaryKey = UString.containsIgnoreCase(newColDef, "PRIMARY KEY");
                    boolean isOldPrimaryKey = UString.containsIgnoreCase(oldColDef, "PRIMARY KEY");
                    boolean isNewAutoIncrement = UString.containsIgnoreCase(newColDef, "AUTO_INCREMENT");
                    if(isNewPrimaryKey) // fix issues with primary key updating, now we detect that case and remove the primary key constraint before updating to avoid issues
                        newColDef = UString.replaceAllIgnoreCase(newColDef, "PRIMARY KEY", "");
                    if(isNewAutoIncrement){
                        // To fix issues where the data might use legacy indexing starting at 0, we need to temporarily allow that
                        // to avoid: java.sql.SQLIntegrityConstraintViolationException: ALTER TABLE causes auto_increment resequencing, resulting in duplicate entry '1' for key 'PRIMARY'
                        s.append("s.execute(\"SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';\");\n");
                    }

                    // Update
                    s.append("s.executeUpdate(\"ALTER TABLE " + tNameNewQuoted + " MODIFY COLUMN `" + newColName + "` " + newColDef + "\");\n");

                    if(isOldPrimaryKey && !isNewPrimaryKey){ // Dropped the primary key definition
                        // This needs to be done after the update where optimally both the primary key and auto_incremnt
                        // are removed from the definition, if we do the below before we get: java.sql.SQLSyntaxErrorException: Incorrect table definition; there can be only one auto column and it must be defined as a key
                        s.append("" +
                            // MySQL/MariaDB:
                            "try{s.executeUpdate(\"ALTER TABLE "+ tNameNewQuoted +" DROP PRIMARY KEY\");} catch (Exception e1){\n" +
                            // PostgreSQL:
                            "try{s.execute(\"\"\"\n" +
                            "                    DO $$\n" +
                            "                    DECLARE\n" +
                            "                        pk_name text;\n" +
                            "                    BEGIN\n" +
                            "                        SELECT constraint_name INTO pk_name\n" +
                            "                        FROM information_schema.table_constraints\n" +
                            "                        WHERE table_name = '"+t.name+"' AND constraint_type = 'PRIMARY KEY';\n" +
                            "                        IF pk_name IS NOT NULL THEN\n" +
                            "                            EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', '"+t.name+"', pk_name);\n" +
                            "                        END IF;\n" +
                            "                    END $$;\n" +
                            "                    \"\"\");} catch (Exception e2){\n" +
                            // SQL server: :
                            "try{s.execute(\"\"\"\n" +
                            "                    DECLARE @pk_name NVARCHAR(255);\n" +
                            "                    SELECT @pk_name = kc.name\n" +
                            "                    FROM sys.key_constraints kc\n" +
                            "                    JOIN sys.tables t ON kc.parent_object_id = t.object_id\n" +
                            "                    WHERE kc.type = 'PK' AND t.name = '"+t.name+"';\n" +
                            "                    IF @pk_name IS NOT NULL\n" +
                            "                    BEGIN\n" +
                            "                        EXEC('ALTER TABLE ["+t.name+"] DROP CONSTRAINT [' + @pk_name + ']');\n" +
                            "                    END\n" +
                            "                    \"\"\");} catch (Exception e3){ if(e1.getMessage().contains(\"it exists\")) System.out.println(\"Ignored exception \"+e1.getMessage()); else {" +
                            "e1.printStackTrace();e2.printStackTrace();e3.printStackTrace(); throw new Exception(\"Failed to drop primary key constraint." +
                            " Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and do it manually for now.\");}\n" +
                            "}}}");
                    }

                    if(isNewAutoIncrement){
                        s.append("s.execute(\"SET SESSION sql_mode='';\");\n");
                    }
                    s.append("t.steps++; Database.updateTableMetaData(t); Database.afterTableChange.forEach(code -> code.accept(t));}\n"); // steps++ and update metadata and close if
                }

                // Delete columns
                for (int j = jStart; j < change.deletedColumnNames.size(); j++) {
                    stepsToComplete++;
                    s.append("if(t.steps < "+stepsToComplete+"){" +
                        "Database.beforeTableChange.forEach(code -> code.accept(t));\n");
                    String colName = change.deletedColumnNames.get(j);
                    s.append("s.executeUpdate(\"ALTER TABLE " + tNameNewQuoted + " DROP COLUMN `" + colName + "`\");\n");
                    s.append("t.steps++; Database.updateTableMetaData(t); Database.afterTableChange.forEach(code -> code.accept(t));}\n"); // steps++ and update metadata and close if
                }

                // Add new columns
                for (int j = jStart; j < change.addedColumnNames.size(); j++) {
                    stepsToComplete++;
                    s.append("if(t.steps < "+stepsToComplete+"){" +
                        "Database.beforeTableChange.forEach(code -> code.accept(t));\n");
                    String colName = change.addedColumnNames.get(j);
                    String colDef = change.addedColumnDefinitions.get(j);
                    s.append("try{s.executeUpdate(\"ALTER TABLE " + tNameNewQuoted + " ADD COLUMN `" + colName + "` " + colDef + "\");}catch(Exception exAdd){if(!exAdd.getMessage().toLowerCase().contains(\"duplicate column\")) throw exAdd;}\n");
                    s.append("t.steps++; Database.updateTableMetaData(t); Database.afterTableChange.forEach(code -> code.accept(t));}\n"); // steps++ and update metadata and close if
                }

                s.append("t.steps = 0; t.version++;\n" + // All steps completed without exceptions, thus reset for next version, and we can now increment the version
                    "Database.updateTableMetaData(t);\n");
                s.append("}\n"); // CLOSE IF
            }
            s.append("}\n"); // CLOSE FOR LOOP

            s.append((t.isDebug ?
                "    new Thread(() -> {\n" +
                    "        try{\n" +
                    "            onAdd.add(obj -> {hasChanges = true;});\n" +
                    "            onRemove.add(obj -> {hasChanges = true;});\n" +
                    "            onUpdate.add(obj -> {hasChanges = true;});\n" +
                    "            while(true){\n" +
                    "                Thread.sleep(10000);\n" +
                    "                if(hasChanges){\n" +
                    "                    hasChanges = false;\n" +
                    "                    System.err.println(\"Changes for "+t.name+" detected within the last 10 seconds, printing...\");\n" +
                    "                    Database.printTable(t);\n" +
                    "                }\n" +
                    "            }\n" +
                    "        } catch (Exception e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }\n" +
                    "    }).start();\n\n" : ""));

            s.append("}\n"); // CLOSE TRY/CATCH
        }

        var idCol = t.columns.get(0);
        s.append("\n" +
                        "}\n" +
                        "catch(Exception e){ throw new RuntimeException(e); }\n" +
                        "finally {Database.freeCon(con);}\n" +
                        "\n" +
                        "}catch(Exception e){\n" +
                        "e.printStackTrace();\n" +
                        "System.err.println(\"Something went really wrong during table (" + t.name + ") initialisation, subsequent operations will fail!\");" +
                        "}\n" +
                        "}\n\n"); // CLOSE STATIC INIT
        return s.toString();
    }
}
