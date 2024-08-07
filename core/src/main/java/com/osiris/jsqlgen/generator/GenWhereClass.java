package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.model.Table;

public class GenWhereClass {
    public static String s(Table table) {
        return "public static class WHERE<T> {\n" +
                "        /**\n" +
                "         * Remember to prepend WHERE on the final SQL statement.\n" +
                "         * This is not done by this class due to performance reasons. <p>\n" +
                "         * <p>\n" +
                "         * Note that it excepts the generated SQL string to be used by a {@link java.sql.PreparedStatement}\n" +
                "         * to protect against SQL-Injection. <p>\n" +
                "         * <p>\n" +
                "         * Also note that the SQL query gets optimized by the database automatically,\n" +
                "         * thus It's recommended to make queries as readable as possible and\n" +
                "         * not worry that much about performance.\n" +
                "         */\n" +
                "        public StringBuilder sqlBuilder = new StringBuilder();\n" +
                "        public StringBuilder orderByBuilder = new StringBuilder();\n" +
                "        public StringBuilder limitBuilder = new StringBuilder();\n" +
                "        List<Object> whereObjects = new ArrayList<>();\n" +
                "        private final String columnName;\n" +
                "        public WHERE(String columnName) {\n" +
                "            this.columnName = columnName;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and returns a list of objects matching the query.\n" +
                "         */\n" +
                "        public List<" + table.name + "> get() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            String where = sqlBuilder.toString();\n" +
                "            if(!where.isEmpty()) where = \" WHERE \" + where;\n" +
                "            String orderBy = orderByBuilder.toString();\n" +
                "            if(!orderBy.isEmpty()) orderBy = \" ORDER BY \"+orderBy.substring(0, orderBy.length()-2)+\" \";\n" +
                "            if(!whereObjects.isEmpty())\n" +
                "                return " + table.name + ".get(where+orderBy+limitBuilder.toString(), whereObjects.toArray());\n" +
                "            else\n" +
                "                return " + table.name + ".get(where+orderBy+limitBuilder.toString(), (T[]) null);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and returns the first object matching the query or null if none.\n" +
                "         */\n" +
                "        public " + table.name + " getFirstOrNull() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            List<"+table.name+"> results = get();\n" +
                "            if(results.isEmpty()) return null;\n" +
                "            else return results.get(0);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and returns the size of the list of objects matching the query.\n" +
                "         */\n" +
                "        public int count() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            String where = sqlBuilder.toString();\n" +
                "            if(!where.isEmpty()) where = \" WHERE \" + where;\n" +
                "            String orderBy = orderByBuilder.toString();\n" +
                "            if(!orderBy.isEmpty()) orderBy = \" ORDER BY \"+orderBy.substring(0, orderBy.length()-2)+\" \";\n" +
                "            if(!whereObjects.isEmpty())\n" +
                "                return " + table.name + ".count(where+orderBy+limitBuilder.toString(), whereObjects.toArray());\n" +
                "            else\n" +
                "                return " + table.name + ".count(where+orderBy+limitBuilder.toString(), (T[]) null);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * Executes the generated SQL statement\n" +
                "         * and removes the objects matching the query.\n" +
                "         */\n" +
                "        public void remove() " + (table.isNoExceptions ? "" : "throws Exception") + " {\n" +
                "            String where = sqlBuilder.toString();\n" +
                "            if(!where.isEmpty()) where = \" WHERE \" + where;\n" +
                "            String orderBy = orderByBuilder.toString();\n" +
                "            if(!orderBy.isEmpty()) orderBy = \" ORDER BY \"+orderBy.substring(0, orderBy.length()-2)+\" \";\n" +
                "            if(!whereObjects.isEmpty())\n" +
                "                " + table.name + ".remove(where+orderBy+limitBuilder.toString(), whereObjects.toArray());\n" +
                "            else\n" +
                "                " + table.name + ".remove(where+orderBy+limitBuilder.toString(), (T[]) null);\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * AND (...) <br>\n" +
                "         */\n" +
                "        public WHERE<T> and(WHERE<?> where) {\n" +
                "            String sql = where.sqlBuilder.toString();\n" +
                "            if(!sql.isEmpty()) {\n" +
                "            sqlBuilder.append(\"AND (\").append(sql).append(\") \");\n" +
                "            whereObjects.addAll(where.whereObjects);\n" +
                "            }\n" +
                "            orderByBuilder.append(where.orderByBuilder.toString());\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * OR (...) <br>\n" +
                "         */\n" +
                "        public WHERE<T> or(WHERE<?> where) {\n" +
                "            String sql = where.sqlBuilder.toString();\n" +
                "            if(!sql.isEmpty()) {\n" +
                "            sqlBuilder.append(\"OR (\").append(sql).append(\") \");\n" +
                "            whereObjects.addAll(where.whereObjects);\n" +
                "            }\n" +
                "            orderByBuilder.append(where.orderByBuilder.toString());\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName = ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> is(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" = ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName IN (?,?,...) <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_in.asp\">https://www.w3schools.com/mysql/mysql_in.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> is(T... objects) {\n" +
                "            String s = \"\";\n" +
                "            for (T obj : objects) {\n" +
                "                s += \"?,\";\n" +
                "                whereObjects.add(obj);\n" +
                "            }\n" +
                "            s = s.substring(0, s.length() - 1); // Remove last ,\n" +
                "            sqlBuilder.append(columnName).append(\" IN (\" + s + \") \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName <> ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> isNot(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" <> ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName IS NULL <br>\n" +
                "         */\n" +
                "        public WHERE<T> isNull() {\n" +
                "            sqlBuilder.append(columnName).append(\" IS NULL \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName IS NOT NULL <br>\n" +
                "         */\n" +
                "        public WHERE<T> isNotNull() {\n" +
                "            sqlBuilder.append(columnName).append(\" IS NOT NULL \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName LIKE ? <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> like(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" LIKE ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "        /**\n" +
                "         * columnName LIKE ? <br>\n" +
                "         * Example: WHERE CustomerName LIKE 'a%' <br>\n" +
                "         * Explanation: Finds any values that start with \"a\" <br>\n" +
                "         * Note: Your provided obj gets turned to a string and if it already contains '_' or '%' these get escaped with '/' to ensure a correct query. <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> startsWith(T obj) {\n" +
                "            String s = obj.toString().replace(\"_\", \"/_\").replace(\"%\", \"/%\");\n" +
                "            s = s + \"%\";\n" +
                "            sqlBuilder.append(columnName).append(\" LIKE ? ESCAPE '/' \");\n" +
                "            whereObjects.add(s);\n" +
                "            return this;\n" +
                "        }\n" +
                "        /**\n" +
                "         * columnName LIKE ? <br>\n" +
                "         * Example: WHERE CustomerName LIKE '%a' <br>\n" +
                "         * Explanation: Finds any values that end with \"a\" <br>\n" +
                "         * Note: Your provided obj gets turned to a string and if it already contains '_' or '%' these get escaped with '/' to ensure a correct query. <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> endsWith(T obj) {\n" +
                "            String s = obj.toString().replace(\"_\", \"/_\").replace(\"%\", \"/%\");\n" +
                "            s = \"%\" + s;\n" +
                "            sqlBuilder.append(columnName).append(\" LIKE ? ESCAPE '/' \");\n" +
                "            whereObjects.add(s);\n" +
                "            return this;\n" +
                "        }\n" +
                "        /**\n" +
                "         * columnName LIKE ? <br>\n" +
                "         * Example: WHERE CustomerName LIKE '%or%' <br>\n" +
                "         * Explanation: Finds any values that have \"or\" in any position <br>\n" +
                "         * Note: Your provided obj gets turned to a string and if it already contains '_' or '%' these get escaped with '/' to ensure a correct query. <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> contains(T obj) {\n" +
                "            String s = obj.toString().replace(\"_\", \"/_\").replace(\"%\", \"/%\");\n" +
                "            s = \"%\" + s + \"%\";\n" +
                "            sqlBuilder.append(columnName).append(\" LIKE ? ESCAPE '/' \");\n" +
                "            whereObjects.add(s);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName NOT LIKE ? <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> notLike(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" NOT LIKE ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName > ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> biggerThan(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" > ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName < ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> smallerThan(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" < ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName >= ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> biggerOrEqual(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" >= ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName <= ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> smallerOrEqual(T obj) {\n" +
                "            sqlBuilder.append(columnName).append(\" <= ? \");\n" +
                "            whereObjects.add(obj);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName BETWEEN ? AND ? <br>\n" +
                "         */\n" +
                "        public WHERE<T> between(T obj1, T obj2) {\n" +
                "            sqlBuilder.append(columnName).append(\" BETWEEN ? AND ? \");\n" +
                "            whereObjects.add(obj1);\n" +
                "            whereObjects.add(obj2);\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName ASC, <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> smallestFirst() {\n" +
                "            orderByBuilder.append(columnName + \" ASC, \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * columnName DESC, <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_like.asp\">https://www.w3schools.com/mysql/mysql_like.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> biggestFirst() {\n" +
                "            orderByBuilder.append(columnName + \" DESC, \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * LIMIT number <br>\n" +
                "         *\n" +
                "         * @see <a href=\"https://www.w3schools.com/mysql/mysql_limit.asp\">https://www.w3schools.com/mysql/mysql_limit.asp</a>\n" +
                "         */\n" +
                "        public WHERE<T> limit(int num) {\n" +
                "            limitBuilder.append(\"LIMIT \").append(num + \" \");\n" +
                "            return this;\n" +
                "        }\n" +
                "\n" +
                "    }\n";
    }
}
