package com.osiris.jsqlgen.model;

import com.osiris.jsqlgen.utils.UString;

public class ColumnType {
    // INTEGER TYPES:
    public static ColumnType BIT = new ColumnType(new String[]{"BIT", "BOOLEAN"},
            "boolean", "setBoolean", "getBoolean");
    public static ColumnType BYTE = new ColumnType(new String[]{"TINYINT", "BYTE"},
            "byte", "setByte", "getByte");
    public static ColumnType SHORT = new ColumnType(new String[]{"SMALLINT"},
            "short", "setShort", "getShort");
    public static ColumnType INT = new ColumnType(new String[]{"MEDIUMINT", "INT"},
            "int", "setInt", "getInt");
    public static ColumnType LONG = new ColumnType(new String[]{"BIGINT", "LONG"},
            "long", "setLong", "getLong");
    // DECIMAL TYPES:
    public static ColumnType DECIMAL = new ColumnType(new String[]{"DECIMAL", "NUMERIC"},
            "java.math.BigDecimal", "setBigDecimal", "getBigDecimal");
    // FLOATING POINT TYPES:
    public static ColumnType FLOAT = new ColumnType(new String[]{"FLOAT", "REAL"},
            "float", "setFloat", "getFloat");
    public static ColumnType DOUBLE = new ColumnType(new String[]{"DOUBLE", "DOUBLE PRECISION"},
            "double", "setDouble", "getDouble");
    // TIME/DATE TYPES:
    public static ColumnType DATE = new ColumnType(new String[]{"DATE", "DATETIME"},
            "java.sql.Date", "setDate", "getDate");
    public static ColumnType TIMESTAMP = new ColumnType(new String[]{"TIMESTAMP"},
            "java.sql.Timestamp", "setTimestamp", "getTimestamp");
    public static ColumnType TIME = new ColumnType(new String[]{"TIME"},
            "java.sql.Time", "setTime", "getTime");
    public static ColumnType YEAR = new ColumnType(new String[]{"YEAR"},
            "int", "setInt", "getInt");
    // STRING/TEXT TYPES:
    public static ColumnType STRING = new ColumnType(new String[]
            {"CHAR", "VARCHAR", "BINARY", "VARBINARY", "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT", "ENUM", "SET"},
            "String", "setString", "getString");
    public static ColumnType BLOB = new ColumnType(new String[]{"TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB"},
            "java.sql.Blob", "setBlob", "getBlob");


    public static ColumnType[] allTypes = new ColumnType[]{
            BYTE, SHORT, INT, LONG, DECIMAL, FLOAT, DOUBLE,
            DATE, TIMESTAMP, TIME, YEAR,
            STRING, BLOB
            // TODO add new datatype here
    };
    public String[] inSQL;
    public String inJava;
    public String inJBDCSet;
    public String inJBDCGet;

    public ColumnType(String[] inSQL, String inJava, String inJBDCSet, String inJBDCGet) {
        this.inSQL = inSQL;
        this.inJava = inJava;
        this.inJBDCSet = inJBDCSet;
        this.inJBDCGet = inJBDCGet;
    }

    /**
     * Provided string must start with the SQL type name (case is ignored)
     * and can continue with anything else (perfect for SQL DEFINITION strings).
     *
     * @return null if no match found.
     */
    public static ColumnType findBySQLDefinition(String s) {
        for (ColumnType t : allTypes) {
            for (String sqlTypeName : t.inSQL) {
                if (UString.startsWithIgnoreCase(s, sqlTypeName))
                    return t;
            }
        }
        return null;
    }

    // TODO SPATIAL DATA TYPES: https://dev.mysql.com/doc/refman/8.0/en/data-types.html
    // TODO JSON DATA TYPES: https://dev.mysql.com/doc/refman/8.0/en/data-types.html
}
