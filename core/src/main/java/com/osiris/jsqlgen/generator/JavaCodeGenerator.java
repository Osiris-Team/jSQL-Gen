package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.Alter;
import ru.lanwen.verbalregex.VerbalExpression;

import java.io.File;
import java.util.*;

import static com.osiris.jsqlgen.utils.UString.*;

public class JavaCodeGenerator {

    public static String[] sqlDefinitionWords = {
        // Data Types, not needed since this is checked by another class
        "", // Empty string allowed

        // Some values, (numbers, strings will be excluded later)
        "TRUE", "FALSE",

        // Constraints
        "PRIMARY", "KEY", "FOREIGN", "KEY", "UNIQUE", "NOT", "NULL", "DEFAULT", "CHECK",
        "AUTO_INCREMENT", "IDENTITY", "ON", "DELETE", "ON", "UPDATE", "REFERENCES",

        // Modifiers
        "UNSIGNED", "ZEROFILL", "CHARACTER", "SET", "COLLATE",

        // Indexing
        "INDEX", "FULLTEXT", "SPATIAL", "HASH", "BTREE",

        // Table Options
        "ENGINE", "AUTO_INCREMENT", "CHARSET", "ROW_FORMAT", "COMMENT",

        // Miscellaneous
        "GENERATED", "ALWAYS", "AS", "STORED", "VIRTUAL", "COLUMN",

        // Dialect-Specific Keywords
        // MySQL
        "NOW", "CURDATE", "CURTIME", "CURRENT_TIMESTAMP",

        // PostgreSQL
        "SERIAL", "BIGSERIAL", "SMALLSERIAL", "JSONB", "UUID", "ARRAY",
        "CITEXT", "MONEY", "XML", "TSVECTOR", "TSQUERY", "INTERVAL",

        // Oracle
        "NUMBER", "VARCHAR2", "NVARCHAR2", "CLOB", "NCLOB", "BFILE",
        "RAW", "LONG RAW", "ROWID", "UROWID", "XMLTYPE", "TIMESTAMP", "WITH", "TIME", "ZONE",
        "TIMESTAMP", "WITH", "LOCAL", "TIME", "ZONE",

        // SQL Server
        "NVARCHAR", "NTEXT", "UNIQUEIDENTIFIER", "SMALLDATETIME", "DATETIME2",
        "DATETIMEOFFSET", "SQL_VARIANT", "GEOMETRY", "GEOGRAPHY", "IMAGE",
        "HIERARCHYID", "ROWVERSION"
    };

    public static void prepareTables(Database db) throws Exception {
        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // MAKE EVERYTHING THAT HAS NOT "DEFAULT" OR ONLY "NULL" IN THEIR DEFINITION NOT NULL
                if (containsIgnoreCase(col.definition, "DEFAULT")) continue;
                if (containsIgnoreCase(col.definition, "NOT NULL")) continue;
                if (containsIgnoreCase(col.definition, "NULL")) {
                    throw new Exception("Found suspicious definition using NULL! Please use the DEFAULT keyword instead!");
                } else {
                    System.out.println("Found suspicious definition without NOT NULL, appended it for "+db.name+"."+t.name+"."+col.name);
                    col.definition = col.definition + " NOT NULL";
                }
            }
        }

        for (Table t : db.tables) {
            if(t.columns.isEmpty()) throw new Exception("Table must contain at least one id column!");
            var idCol = t.columns.get(0);

            // MAKE ALL IDS REQUIRE AUTO_INCREMENT
            // TODO support other db dialects for this feature
            if (containsIgnoreCase(idCol.definition, "AUTO_INCREMENT")) continue;
            System.out.println("Found suspicious definition without AUTO_INCREMENT in id, inserted it for "+db.name+"."+t.name+"."+idCol.name);
            int firstSpaceI = idCol.definition.indexOf(" ");
            if(firstSpaceI >= 0){
                idCol.definition = insertAt(idCol.definition, firstSpaceI, " AUTO_INCREMENT");
            } else
                idCol.definition = idCol.definition + " AUTO_INCREMENT";
        }

        /*
        // TODO maybe another time we change int id to long, since this needs also further changes, since you cannot use
        // direct int values for example this: Folder.whereId().is(0).remove(); wouldnt work, instead you must write
        // 0L instead of 0, or provide another method that accepts integers too.
        // This directly could be expanded to all datatypes and generate methods of that accept smaller datatypes.
        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // MAKE SURE TYPE OF ID IS ALWAYS BIGINT
                if(!col.name.equals("id")) continue;
                if (!containsIgnoreCase(col.definition, "BIGINT")) {
                    System.out.println("Found suspicious id definition (at "+t.name+"."+col.name+") that is not BIGINT, fixed it.");
                    col.definition = col.definition.replace(col.definition.split(" ")[0], "BIGINT");
                }
            }
        }

         */

        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // GENERATE TYPES
                col.type = ColumnType.findBySQLDefinition(col.definition);
                if (col.type == null)
                    throw new Exception("Failed to generate code, because failed to find matching java type of definition '" + col.definition
                            + "'. Make sure that the data type is the first word in your definition and that its a supported type by jSQL-Gen.");
                String def = col.definition.toLowerCase();
                if(!def.contains("not null") && def.contains("null")){
                    // use nullable big/object type of primitive
                    // we do this not because it's recommended to use null but only for compatibility reasons
                    col.type = col.type.clone();
                    col.type.inJava = col.type.inJava.equals("int") ? "Integer" : firstToUpperCase(col.type.inJava);
                    col.type.inJBDCSet = "setObject";
                    col.type.inJBDCGet = "getObject";
                }
            }
        }

        var validWordsInDefinition = Arrays.asList(sqlDefinitionWords);
        for (Table t : db.tables) {
            for (Column col : t.columns) {
                // CHECK SQL
                try{
                    Alter sql = (Alter) CCJSqlParserUtil.parse("ALTER TABLE `table` ADD COLUMN `column` " + col.definition);
                    // TODO compare each constraint with a list of all supported/valid constraints, since CCJSqlParserUtil does not do that
                    // or instead launch a MySQL server for example and run the SQL to see if it works
                    // to make this perfect, we would need to run other other servers like PostgreSQL too, if the user wants to use that instead
                    VerbalExpression parenthesesRegex = VerbalExpression.regex()
                        .find("(").anything().find(")")
                        .build();

                    VerbalExpression doubleQuotesRegex = VerbalExpression.regex()
                        .find("\"").anything().find("\"")
                        .build();

                    VerbalExpression singleQuotesRegex = VerbalExpression.regex()
                        .find("'").anything().find("'")
                        .build();

                    VerbalExpression backticksRegex = VerbalExpression.regex()
                        .find("`").anything().find("`")
                        .build();

                    VerbalExpression numbersRegex = VerbalExpression.regex()
                        .maybe("-")          // Optional negative sign
                        .digit()              // Matches digits
                        .zeroOrMore()         // Matches any number of digits (integer part)
                        .maybe(".")           // Matches the decimal point
                        .digit().zeroOrMore() // Matches the decimal part if exists
                        .build();

                    // Test string
                    String input = "Example text (remove this) and \"this too\" or `this one`, even 'this', plus -123.45 and 567 or -89 numbers.";

                    // Test string
                    //String example = "Example text (remove this) and \"this too\" or `this one`, even 'this', plus 123 numbers.";

                    // Remove data types from def since this was already checked before
                    // This also has the side-effect that only one data type is allowed in a definition
                    String def = col.definition;
                    var types = new ArrayList<>(Arrays.asList(col.type.inSQL));
                    // Make sure longer types are always first to ensure CHAR for example is not before VARCHAR, because in that case we would remove CHAR first and stay with VAR left, not ideal...
                    types.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));
                    for (String s : types) {
                        def = def.replaceAll(s, "");
                    }

                    // Apply all regex replacements
                    def = def.replaceAll(parenthesesRegex.toString(), "")
                        .replaceAll(doubleQuotesRegex.toString(), "")
                        .replaceAll(singleQuotesRegex.toString(), "")
                        .replaceAll(backticksRegex.toString(), "")
                        .replaceAll(numbersRegex.toString(), "");
                    for (String word : def.split(" ")) {
                        if(!validWordsInDefinition.contains(word.toUpperCase()))
                            throw new Exception("'"+word+"' is very likely an invalid SQL definition in '"+def+"'! If not create a pull request on GitHub.");
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Invalid SQL found in "+db.name+"."+t.name+"."+col.name+": "+e.getMessage(), e);
                }
            }
        }

    }

    /**
     * Contains a copy of the databases once jSQL-Gen was started and gets
     * updated at the end of {@link GenTableFile#s(File, Table, Database)} if the generation of the table was a success. <br>
     * This is for versioning databases and keeping track of changes between the last and current database. <br>
     * This means that each time you press "Generate Files" the version is incremented by one (if there were changes). <br>
     */
    public static List<Database> oldDatabases = new ArrayList<>();



    public static String getSQLTableNameQuoted(String s) {
        return "`" + s.toLowerCase() + "`";
    }

    public static String genJDBCSet(Column c, int i) {
        if (c.type.isEnum())
            return "ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ".name());\n";
        else
            return "ps." + c.type.inJBDCSet + "(" + (i + 1) + ", obj." + c.name + ");\n";
    }

    public static String genEnum(String enumName, String definition) {
        definition = definition.substring(definition.indexOf("(") + 1, definition.lastIndexOf(")"));
        String[] enumTypeRawNames = definition.split(",");
        String s = "public enum " + enumName + " {";
        for (String enumTypeRawName : enumTypeRawNames) {
            s += enumTypeRawName.replace("\"", "")
                    .replace("'", "")
                    .replace("`", "")
                    + ",";
        }
        s += "}\n";
        return s;
    }

    public static Constructor genConstructor(String objName, List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        StringBuilder paramsWithoutTypesBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();
        Column idCol = columns.get(0);
        for (Column col : columns) {
            paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
            paramsWithoutTypesBuilder.append(col.name + ", ");
            fieldsBuilder.append("this." + col.name + " = " + col.name + ";");
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        constructor.paramsWithoutId = constructor.params.replace((idCol.type.inJava + " " + idCol.name + ", "), "");
        if (constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length() - 2);
        if (constructor.paramsWithoutId.endsWith(", "))
            constructor.paramsWithoutId = constructor.paramsWithoutId.substring(0, constructor.paramsWithoutId.length() - 2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #"+idCol.name+"}.\n" +
                "*/\n" +
                "public " + objName + " (" + constructor.params + "){\ninitDefaultFields();\n" + constructor.fieldAssignments + "\n}\n";

        constructor.paramsWithoutTypes = paramsWithoutTypesBuilder.toString();
        if (constructor.paramsWithoutTypes.endsWith(", "))
            constructor.paramsWithoutTypes = constructor.paramsWithoutTypes.substring(0, constructor.paramsWithoutTypes.length() - 2);

        return constructor;
    }

    /**
     * Generates a constructor with only the not-null fields as parameters.
     */
    public static Constructor genMinimalConstructor(String objName, List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        StringBuilder paramsWithoutTypesBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();
        Column idCol = columns.get(0);
        for (Column col : columns) {
            if (containsIgnoreCase(col.definition, "NOT NULL")) {
                paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
                paramsWithoutTypesBuilder.append(col.name + ", ");
                fieldsBuilder.append("this." + col.name + " = " + col.name + ";");
            }
        }
        Constructor constructor = new Constructor();
        constructor.params = paramsBuilder.toString();
        constructor.paramsWithoutId = constructor.params.replace((idCol.type.inJava + " " + idCol.name + ", "), "");
        if (constructor.params.endsWith(", "))
            constructor.params = constructor.params.substring(0, constructor.params.length() - 2);
        if (constructor.paramsWithoutId.endsWith(", "))
            constructor.paramsWithoutId = constructor.paramsWithoutId.substring(0, constructor.paramsWithoutId.length() - 2);

        constructor.fieldAssignments = fieldsBuilder.toString();

        constructor.asString = "/**\n" +
                "Use the static create method instead of this constructor,\n" +
                "if you plan to add this object to the database in the future, since\n" +
                "that method fetches and sets/reserves the {@link #"+idCol.name+"}.\n" +
                "*/\n" +
                "public " + objName + " (" + constructor.params + "){\ninitDefaultFields();\n" + constructor.fieldAssignments + "\n}\n";

        constructor.paramsWithoutTypes = paramsWithoutTypesBuilder.toString();
        if (constructor.paramsWithoutTypes.endsWith(", "))
            constructor.paramsWithoutTypes = constructor.paramsWithoutTypes.substring(0, constructor.paramsWithoutTypes.length() - 2);

        return constructor;
    }

    public static String genParams(List<Column> columns) {
        StringBuilder paramsBuilder = new StringBuilder();
        for (Column col : columns) {
            paramsBuilder.append(col.type.inJava + " " + col.name + ", ");
        }
        String params = paramsBuilder.toString();
        if (params.endsWith(", "))
            params = params.substring(0, params.length() - 2);
        return params;
    }

    public static String genFieldAssignments(List<Column> columns) {
        return genFieldAssignments("this", columns);
    }

    public static String genFieldAssignments(String objName, List<Column> columns) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            fieldsBuilder.append(objName + "." + col.name + "=" + col.name + "; ");
        }
        return fieldsBuilder.toString();
    }

    public static String genOnlyNotNullFieldAssignments(List<Column> columns) {
        return genOnlyNotNullFieldAssignments("this", columns);
    }

    public static String genOnlyNotNullFieldAssignments(String objName, List<Column> columns) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            if (containsIgnoreCase(col.definition, "NOT NULL")) {
                fieldsBuilder.append(objName + "." + col.name + "=" + col.name + "; ");
            }
        }
        return fieldsBuilder.toString();
    }

    public static String genOnlyDefaultFieldAssignments(List<Column> columns) {
        return genOnlyDefaultFieldAssignments("this", columns);
    }

    public static String genOnlyDefaultFieldAssignments(String objName, List<Column> columns) {
        StringBuilder fieldsBuilder = new StringBuilder();
        for (Column col : columns) {
            if (containsIgnoreCase(col.definition, "DEFAULT")) {
                String val = col.getDefaultValue();
                if (col.type.isEnum())
                    fieldsBuilder.append(objName + "." + col.name + "=" + col.type.inJava + "." + val + "; ");
                else if (col.type.isText()) {
                    fieldsBuilder.append(objName + "." + col.name + "=\"" + val + "\"; ");
                } else if (col.type.isDateOrTime()) {
                    if(containsIgnoreCase(val, "NOW")
                            || containsIgnoreCase(val, "CURDATE")
                            || containsIgnoreCase(val, "CURTIME")
                            || containsIgnoreCase(val, "CURRENT_TIMESTAMP")) val = "System.currentTimeMillis()";
                    if (col.type == ColumnType.YEAR) fieldsBuilder.append(objName + "." + col.name + "=" + val + "; ");
                    else fieldsBuilder.append(objName + "." + col.name + "=new " + col.type.inJava + "(" + val + "); ");
                } else if (col.type.isBlob()) {
                    fieldsBuilder.append(objName + "." + col.name + "=new Database.DefaultBlob(new byte[0]); ");
                    // This is not directly supported by SQL
                } else if (col.type.isNumber() || col.type.isDecimalNumber()) {
                    fieldsBuilder.append(objName + "." + col.name + "=" + val + "; ");
                } else if(col.type.inJBDCGet.equals("getObject")) {
                    fieldsBuilder.append(objName + "." + col.name + "=" + val + "; ");
                } else {
                    fieldsBuilder.append(objName + "." + col.name + "=new " + col.type.inJava + "(" + val + "); ");
                }
            }
        }
        return fieldsBuilder.toString();
    }

    public static class Constructor {
        public String asString;
        public String params;
        public String paramsWithoutId;
        public String paramsWithoutTypes;
        public String fieldAssignments;
    }

}
