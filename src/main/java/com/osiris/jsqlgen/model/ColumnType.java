package com.osiris.jsqlgen.model;

import com.osiris.jsqlgen.utils.UString;

import java.util.Arrays;
import java.util.Objects;

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
            "Date","java.sql.Date", "setDate", "getDate");
    public static ColumnType TIMESTAMP = new ColumnType(new String[]{"TIMESTAMP"},
            "Timestamp", "java.sql.Timestamp", "setTimestamp", "getTimestamp");
    public static ColumnType TIME = new ColumnType(new String[]{"TIME"},
            "Time", "java.sql.Time","setTime", "getTime");
    public static ColumnType YEAR = new ColumnType(new String[]{"YEAR"},
            "int", "setInt", "getInt");
    // STRING/TEXT TYPES:
    public static ColumnType STRING = new ColumnType(new String[]
            {"CHAR", "VARCHAR", "BINARY", "VARBINARY", "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT"},
            "String", "setString", "getString");
    public static ColumnType BLOB = new ColumnType(new String[]{"TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB"},
            "Blob", "java.sql.Blob", "setBlob", "getBlob");
    // ENUM
    public static ColumnType ENUM = new ColumnType(new String[]
            {"ENUM"},
            null, // Must be set later by the generator
            "setString", "getString");
    // TODO support SET (aka list with only a SET of allowed values) and lists


    public static ColumnType[] allTypes = new ColumnType[]{
            BIT,
            BYTE, SHORT, INT, LONG, DECIMAL, FLOAT, DOUBLE,
            DATE, TIMESTAMP, TIME, YEAR,
            STRING, BLOB, ENUM
            // TODO add new datatype here
    };
    public String[] inSQL;
    public String inJava;
    public String inJavaWithPackage;
    public String inJBDCSet;
    public String inJBDCGet;

    public ColumnType(String[] inSQL, String inJava, String inJBDCSet, String inJBDCGet) {
        this.inSQL = inSQL;
        this.inJava = inJava;
        this.inJBDCSet = inJBDCSet;
        this.inJBDCGet = inJBDCGet;
    }

    public ColumnType(String[] inSQL, String inJava, String inJavaWithPackage, String inJBDCSet, String inJBDCGet) {
        this.inSQL = inSQL;
        this.inJava = inJava;
        this.inJavaWithPackage = inJavaWithPackage;
        this.inJBDCSet = inJBDCSet;
        this.inJBDCGet = inJBDCGet;
    }

    public boolean isEnum(){
        return inSQL[0].equals("ENUM");
    }

    public boolean isNumber(){
        return this == LONG || this == INT || this == SHORT || this == BYTE || this == BIT || this == YEAR;
    }

    public boolean isDecimalNumber(){
        return this == DECIMAL || this == DOUBLE || this == FLOAT;
    }

    public boolean isDate(){
        return this == DATE;
    }

    public boolean isTime(){
        return this == TIME;
    }

    public boolean isTimestamp(){
        return this == TIMESTAMP;
    }

    public boolean isDateOrTime(){
        return this == DATE || this == TIMESTAMP || this == TIME || this == YEAR;
    }

    public boolean isText(){
        return this == STRING;
    }

    public boolean isBlob(){
        return this == BLOB;
    }

    public boolean isBitOrBoolean() {
        return this == BIT;
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
                if (UString.startsWithWordIgnoreCase(s, sqlTypeName))
                    return t;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnType that = (ColumnType) o;
        return Arrays.equals(inSQL, that.inSQL) && Objects.equals(inJava, that.inJava) && Objects.equals(inJavaWithPackage, that.inJavaWithPackage) && Objects.equals(inJBDCSet, that.inJBDCSet) && Objects.equals(inJBDCGet, that.inJBDCGet);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(inSQL);
        result = 31 * result + Objects.hashCode(inJava);
        result = 31 * result + Objects.hashCode(inJavaWithPackage);
        result = 31 * result + Objects.hashCode(inJBDCSet);
        result = 31 * result + Objects.hashCode(inJBDCGet);
        return result;
    }

    public boolean equalsInJava(ColumnType otherType){
        if(this.inJava.equals(otherType.inJava)) return true;
        return false;
    }


    // TODO SPATIAL DATA TYPES: https://dev.mysql.com/doc/refman/8.0/en/data-types.html
    // TODO JSON DATA TYPES: https://dev.mysql.com/doc/refman/8.0/en/data-types.html
}
