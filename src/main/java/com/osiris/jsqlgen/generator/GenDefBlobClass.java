package com.osiris.jsqlgen.generator;

import java.util.LinkedHashSet;

public class GenDefBlobClass {
    public static String s(LinkedHashSet<String> imports) {
        imports.add("import java.io.InputStream;");
        imports.add("import java.io.OutputStream;");
        imports.add("import java.io.ByteArrayInputStream;");
        imports.add("import java.sql.Blob;");
        imports.add("import java.sql.SQLException;");

        return "class DefaultBlob implements Blob{\n" +
                "    private byte[] data;\n" +
                "\n" +
                "    // Constructor that accepts a byte array\n" +
                "    public DefaultBlob(byte[] data) {\n" +
                "        this.data = data;\n" +
                "    }\n" +
                "    @Override\n" +
                "    public long length() throws SQLException {\n" +
                "        return data.length;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public byte[] getBytes(long pos, int length) throws SQLException {\n" +
                "        return data;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public InputStream getBinaryStream() throws SQLException {\n" +
                "        return new ByteArrayInputStream(data);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public long position(byte[] pattern, long start) throws SQLException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public long position(Blob pattern, long start) throws SQLException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int setBytes(long pos, byte[] bytes) throws SQLException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public OutputStream setBinaryStream(long pos) throws SQLException {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void truncate(long len) throws SQLException {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void free() throws SQLException {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public InputStream getBinaryStream(long pos, long length) throws SQLException {\n" +
                "        return new ByteArrayInputStream(data);\n" +
                "    }\n" +
                "}\n";
    }
}
