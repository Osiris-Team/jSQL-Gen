package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Table;
import com.osiris.jsqlgen.model.TableChange;

import java.util.ArrayList;

public class GenStaticTableConstructor {
    public static String s(Table t, String tNameQuoted) {
        StringBuilder s = new StringBuilder();
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
            if(i == 0)
                s.append("s.executeUpdate(\"CREATE TABLE IF NOT EXISTS " + tNameQuoted + " (" + t.columns.get(0).nameQuoted // EXPECTS ID
                        + " " + t.columns.get(0).definition + ")\");\n");
            else if(!change.oldTableName.equals(change.newTableName)){
                // MySQL / MariaDB:
                s.append("try{s.executeUpdate(\"ALTER TABLE `"+change.oldTableName+"` RENAME `"+change.newTableName+"`\");} catch (Exception e1){\n" +
                        // PostgreSQL:
                        "try{s.executeUpdate(\"ALTER TABLE `"+change.oldTableName+"` RENAME TO `"+change.newTableName+"`\");} catch (Exception e2){" +
                        // SQL server:
                        "try{s.executeUpdate(\"EXEC sp_rename `"+change.oldTableName+"`, `"+change.newTableName+"`\");} catch (Exception e3){\n" +
                        // Oracle SQL:
                        "try{s.executeUpdate(\"RENAME  `"+change.oldTableName+"` TO `"+change.newTableName+"`\");} catch (Exception e4){\n" +
                        "e1.printStackTrace();e2.printStackTrace();e3.printStackTrace();e4.printStackTrace(); throw new RuntimeException(\"Failed to rename this table." +
                        " Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and rename this table manually for now.\");\n" +
                        "}}}}");
            }
            int jStart = 0;
            if(i == 0) jStart = 1; // We are in creation aka first change (i=0),
            // thus skip first column (id) to avoid "SQLSyntaxErrorException: Multiple primary key defined"

            // Rename columns
            for (int j = jStart; j < change.oldColumnNames.size(); j++) {
                String oldColName = change.oldColumnNames.get(j);
                String newColName = change.newColumnNames.get(j);
                // MySQL / MariaDB / PostgreSQL / Oracle SQL:
                s.append("try{s.executeUpdate(\"ALTER TABLE `"+change.newTableName+"` RENAME COLUMN `"+oldColName+"` TO `"+newColName+"`\");} catch (Exception e1){\n" +
                        // SQL server: :
                        "try{s.executeUpdate(\"EXEC sp_rename `"+change.newTableName+"."+oldColName+"`, `"+newColName+"`, `COLUMN`\");} catch (Exception e2){" +
                        "e1.printStackTrace();e2.printStackTrace(); throw new RuntimeException(\"Failed to rename this column." +
                        " Your specific SQL database might not be supported, in this case create a PR on Github for jSQL-Gen and rename this column manually for now.\");\n" +
                        "}}");
            }

            // Change columns definitions
            for (int j = jStart; j < change.oldColumnDefinitions.size(); j++) {
                String oldColDef = change.oldColumnDefinitions.get(j);
                String newColDef = change.newColumnDefinitions.get(j);
                String newColName = change.newColumnDefinitions_Names.get(j);
                s.append("s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " MODIFY COLUMN `" + newColName + "` " + newColDef + "\");\n");
            }

            // Delete columns
            for (int j = jStart; j < change.deletedColumnNames.size(); j++) {
                String colName = change.deletedColumnNames.get(j);
                s.append("s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " DROP COLUMN `" + colName + "`\");\n");
            }

            // Add new columns
            for (int j = jStart; j < change.addedColumnNames.size(); j++) {
                String colName = change.addedColumnNames.get(j);
                String colDef = change.addedColumnDefinitions.get(j);
                s.append("try{s.executeUpdate(\"ALTER TABLE " + tNameQuoted + " ADD COLUMN `" + colName + "` " + colDef + "\");}catch(Exception ignored){}\n");
            }

            s.append("t.version++;\n" +
                    "Database.updateTableMetaData(t);\n");
            s.append("}\n"); // CLOSE IF
        }
        s.append("}\n"); // CLOSE FOR LOOP
        s.append(
                "}\n" +
                        "try (PreparedStatement ps = con.prepareStatement(\"SELECT id FROM " + tNameQuoted + " ORDER BY id DESC LIMIT 1\")) {\n" +
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
