package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;

import java.util.ArrayList;

public class GenStaticTableConstructor {
    public static String s(Table t, String tCurrentNameQuoted) {
        StringBuilder s = new StringBuilder();
        if(t.isDebug) s.append("public static volatile boolean hasChanges = false;\n");
        s.append("static {\n" +
                "try{\n" + // Without this additional try/catch that encapsulates the complete code inside static constructor
                // we somehow get problems like class not found exception
                "Connection con = Database.getCon();\n" +
                "try{\n" +
                "try (Statement s = con.createStatement()) {\n" +
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
            s.append("if(t.steps < "+stepsToComplete+"){");
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
            s.append("t.steps++; Database.updateTableMetaData(t);}\n"); // steps++ and update metadata and close if

            int jStart = 0;
            if(i == 0) jStart = 1; // We are in creation aka first change (i=0),
            // thus skip first column (id) to avoid "SQLSyntaxErrorException: Multiple primary key defined"

            // Rename columns
            for (int j = jStart; j < change.oldColumnNames.size(); j++) {
                stepsToComplete++;
                s.append("if(t.steps < "+stepsToComplete+"){");
                String oldColName = change.oldColumnNames.get(j);
                String newColName = change.newColumnNames.get(j);
                String newColDef = change.newColumnNames_Definitions.get(j);
                // MySQL / MariaDB / PostgreSQL / Oracle SQL:
                s.append("try{s.executeUpdate(\"ALTER TABLE "+ tNameNewQuoted +" RENAME COLUMN `"+oldColName+"` TO `"+newColName+"`\");} catch (Exception e1){\n" +
                        // Older MySQL/MariaDB versions:
                        "try{s.executeUpdate(\"ALTER TABLE "+ tNameNewQuoted +" CHANGE `"+oldColName+"` `"+newColName+"` "+newColDef+"\");} catch (Exception e2){\n" +
                        // SQL server: :
                        "try{s.executeUpdate(\"EXEC sp_rename `"+change.newTableName.toLowerCase()+"."+oldColName+"`, `"+newColName+"`, `COLUMN`\");} catch (Exception e3){" +
                        "e1.printStackTrace();e2.printStackTrace();e3.printStackTrace(); throw new Exception(\"Failed to rename this column." +
                        " Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and rename this column manually for now.\");\n" +
                        "}}}");
                s.append("t.steps++; Database.updateTableMetaData(t);}\n"); // steps++ and update metadata and close if
            }

            // Change columns definitions
            for (int j = jStart; j < change.oldColumnDefinitions.size(); j++) {
                stepsToComplete++;
                s.append("if(t.steps < "+stepsToComplete+"){");
                String oldColDef = change.oldColumnDefinitions.get(j);
                String newColDef = change.newColumnDefinitions.get(j);
                String newColName = change.newColumnDefinitions_Names.get(j);
                s.append("s.executeUpdate(\"ALTER TABLE " + tNameNewQuoted + " MODIFY COLUMN `" + newColName + "` " + newColDef + "\");\n");
                s.append("t.steps++; Database.updateTableMetaData(t);}\n"); // steps++ and update metadata and close if
            }

            // Delete columns
            for (int j = jStart; j < change.deletedColumnNames.size(); j++) {
                stepsToComplete++;
                s.append("if(t.steps < "+stepsToComplete+"){");
                String colName = change.deletedColumnNames.get(j);
                s.append("s.executeUpdate(\"ALTER TABLE " + tNameNewQuoted + " DROP COLUMN `" + colName + "`\");\n");
                s.append("t.steps++; Database.updateTableMetaData(t);}\n"); // steps++ and update metadata and close if
            }

            // Add new columns
            for (int j = jStart; j < change.addedColumnNames.size(); j++) {
                stepsToComplete++;
                s.append("if(t.steps < "+stepsToComplete+"){");
                String colName = change.addedColumnNames.get(j);
                String colDef = change.addedColumnDefinitions.get(j);
                s.append("try{s.executeUpdate(\"ALTER TABLE " + tNameNewQuoted + " ADD COLUMN `" + colName + "` " + colDef + "\");}catch(Exception exAdd){if(!exAdd.getMessage().startsWith(\"Duplicate\")) throw exAdd;}\n");
                s.append("t.steps++; Database.updateTableMetaData(t);}\n"); // steps++ and update metadata and close if
            }

            s.append("t.steps = 0; t.version++;\n" + // All steps completed without exceptions, thus reset for next version, and we can now increment the version
                    "Database.updateTableMetaData(t);\n");
            s.append("}\n"); // CLOSE IF
        }
        s.append("}\n"); // CLOSE FOR LOOP
        s.append("\n" +
                (t.isDebug ?
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
                                "    }).start();\n\n" : "") +
                "}\n" + // CLOSE T TRY/CATCH
                        "try (PreparedStatement ps = con.prepareStatement(\"SELECT id FROM " + tCurrentNameQuoted + " ORDER BY id DESC LIMIT 1\")) {\n" +
                        "ResultSet rs = ps.executeQuery();\n" +
                        "if (rs.next()) idCounter.set(rs.getInt(1) + 1);\n" +
                        "}\n" +
                        "}\n" +
                        "catch(Exception e){ throw new RuntimeException(e); }\n" +
                        "finally {Database.freeCon(con);}\n" +
                        "}catch(Exception e){\n" +
                        "e.printStackTrace();\n" +
                        "System.err.println(\"Something went really wrong during table (" + t.name + ") initialisation, thus the program will exit!\");" +
                        "System.exit(1);}\n" +
                        "}\n\n"); // CLOSE STATIC INIT
        return s.toString();
    }
}
