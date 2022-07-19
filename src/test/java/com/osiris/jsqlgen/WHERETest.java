package com.osiris.jsqlgen;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WHERETest {

    public static class WHERE{
        public static WHERE id(){
            return new WHERE("id");
        }

        private String columnName;
        /**
         * Remember to prepend WHERE on the final SQL statement.
         * This is not done by this class due to performance reasons. <p>
         *
         * Note that it excepts the generated SQL string to be used by a {@link java.sql.PreparedStatement}
         * to protect against SQL-Injection. <p>
         *
         * Also note that the SQL query gets optimized by the database automatically,
         * thus It's recommended to make queries as readable as possible and
         * not worry that much about performance.
         */
        public StringBuilder sqlBuilder = new StringBuilder();
        List<Object> whereObjects = new ArrayList<>();

        public WHERE(String columnName) {
            this.columnName = columnName;
        }

        /**
         * Generated SQL: <br>
         * AND (...) <br>
         * The provided where statement will be encapsulated in (). <br>
         * Note that the SQL query gets optimized by the database automatically,
         * thus It's recommended to make queries as readable as possible and
         * not worry that much about performance.
         */
        public WHERE and(WHERE where){
            sqlBuilder.append("AND (").append(where.sqlBuilder).append(")");
            return this;
        }

        /**
         * Generated SQL: <br>
         * OR (...) <br>
         * The provided where statement will be encapsulated in (). <br>

         */
        public WHERE or(WHERE where){
            sqlBuilder.append("OR (").append(where.sqlBuilder).append(")");
            return this;
        }

        /**
         * columnName = ? <br>
         */
        public WHERE is(Object obj){
            sqlBuilder.append(columnName).append(" = ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName <> ? <br>
         */
        public WHERE isNot(Object obj){
            sqlBuilder.append(columnName).append(" <> ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName LIKE ? <br>
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE isLike(Object obj){
            sqlBuilder.append(columnName).append(" LIKE ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName NOT LIKE ? <br>
         * @see <a href="https://www.w3schools.com/mysql/mysql_like.asp">https://www.w3schools.com/mysql/mysql_like.asp</a>
         */
        public WHERE isNotLike(Object obj){
            sqlBuilder.append(columnName).append(" NOT LIKE ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName > ? <br>
         */
        public WHERE isBigger(Object obj){
            sqlBuilder.append(columnName).append(" > ? ");
            whereObjects.add(obj);
            return this;
        }

        /**
         * columnName < ? <br>
         */
        public WHERE isSmaller(Object obj){
            sqlBuilder.append(columnName).append(" > ? ");
            whereObjects.add(obj);
            return this;
        }

    }

    public static void get(WHERE where) throws Exception {
    }

    @Test
    void test() {

        // WHERE
        System.out.println(WHERE.id().is(10)
                .and(WHERE.id().is(2).and(WHERE.id().is(5)))
                .and(WHERE.id().is(10)).sqlBuilder.toString());
    }
}
