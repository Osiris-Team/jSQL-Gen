package com.osiris.jsqlgen;

public class CoolType {
    public String[] inSQL;
    public String inJava;
    public String inJBDCSet;
    public String inJBDCGet;

    public CoolType(String[] inSQL, String inJava, String inJBDCSet, String inJBDCGet) {
        this.inSQL = inSQL;
        this.inJava = inJava;
        this.inJBDCSet = inJBDCSet;
        this.inJBDCGet = inJBDCGet;
    }

    /**
     * Provided string must start with the SQL type name (case is ignored)
     * and can continue with anything else (perfect for SQL DEFINITION strings).
     * @return null if no match found.
     */
    public static CoolType findBySQLType(String s){
        CoolType[] types = new CoolType[]{
                BYTE, SHORT, INT, LONG, DECIMAL, FLOAT, DOUBLE,
                DATE, TIMESTAMP, TIME, YEAR,
                STRING, BLOB
                // TODO add new datatype here
        };
        for (CoolType t : types) {
            for (String sqlTypeName : t.inSQL) {
                if(startsWithIgnoreCase(s, sqlTypeName))
                    return t;
            }
        }
        return null;
    }
    private static boolean startsWithIgnoreCase(String s, String query){
        return s.toLowerCase().startsWith(query.toLowerCase());
    }

    // INTEGER TYPES:
    public static CoolType BYTE = new CoolType(new String[]{"TINYINT", "BYTE"},
            "byte","setByte", "getByte");
    public static CoolType SHORT = new CoolType(new String[]{"SMALLINT"},
            "short", "setShort", "getShort");
    public static CoolType INT = new CoolType(new String[]{"MEDIUMINT", "INT"},
            "int", "setInt", "getInt");
    public static CoolType LONG = new CoolType(new String[]{"BIGINT", "LONG"},
            "long", "setLong", "getLong");

    // DECIMAL TYPES:
    public static CoolType DECIMAL = new CoolType(new String[]{"DECIMAL", "NUMERIC"},
            "java.math.BigDecimal","setBigDecimal", "getBigDecimal");

    // FLOATING POINT TYPES:
    public static CoolType FLOAT = new CoolType(new String[]{"FLOAT", "REAL"},
            "float", "setFloat", "getFloat");
    public static CoolType DOUBLE = new CoolType(new String[]{"DOUBLE", "DOUBLE PRECISION"},
            "double", "setDouble", "getDouble");

    // TIME/DATE TYPES:
    public static CoolType DATE = new CoolType(new String[]{"DATE", "DATETIME"},
            "java.sql.Date", "setDate", "getDate");
    public static CoolType TIMESTAMP = new CoolType(new String[]{"TIMESTAMP"},
            "java.sql.Timestamp", "setTimestamp", "getTimestamp");
    public static CoolType TIME = new CoolType(new String[]{"TIME"},
            "java.sql.Time", "setTime", "getTime");
    public static CoolType YEAR = new CoolType(new String[]{"YEAR"},
            "int", "setInt", "getInt");

    // STRING/TEXT TYPES:
    public static CoolType STRING = new CoolType(new String[]
            {"CHAR", "VARCHAR", "BINARY", "VARBINARY", "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT", "ENUM", "SET"},
            "String", "setString", "getString");
    public static CoolType BLOB = new CoolType(new String[]{"TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB"},
            "java.sql.Blob", "setBlob", "getBlob");

    // TODO SPATIAL DATA TYPES: https://dev.mysql.com/doc/refman/8.0/en/data-types.html
    // TODO JSON DATA TYPES: https://dev.mysql.com/doc/refman/8.0/en/data-types.html
}
