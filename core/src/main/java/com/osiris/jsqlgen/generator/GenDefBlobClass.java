package com.osiris.jsqlgen.generator;

import java.util.LinkedHashSet;

public class GenDefBlobClass {
    public static String s(LinkedHashSet<String> imports) {
        imports.add("import java.io.InputStream;");
        imports.add("import java.io.OutputStream;");
        imports.add("import java.io.ByteArrayInputStream;");
        imports.add("import java.sql.Blob;");
        imports.add("import java.sql.SQLException;");

        return """
            public static class DefaultBlob implements Blob{
                private byte[] data;

                // Constructor that accepts a byte array
                public DefaultBlob(byte[] data) {
                    this.data = data;
                }
                @Override
                public long length() throws SQLException {
                    return data.length;
                }

                @Override
                public byte[] getBytes(long pos, int length) throws SQLException {
                    return data;
                }

                @Override
                public InputStream getBinaryStream() throws SQLException {
                    return new ByteArrayInputStream(data);
                }

                @Override
                public long position(byte[] pattern, long start) throws SQLException {
                    return 0;
                }

                @Override
                public long position(Blob pattern, long start) throws SQLException {
                    return 0;
                }

                @Override
                public int setBytes(long pos, byte[] bytes) throws SQLException {
                    return 0;
                }

                @Override
                public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
                    return 0;
                }

                @Override
                public OutputStream setBinaryStream(long pos) throws SQLException {
                    return null;
                }

                @Override
                public void truncate(long len) throws SQLException {

                }

                @Override
                public void free() throws SQLException {

                }

                @Override
                public InputStream getBinaryStream(long pos, long length) throws SQLException {
                    return new ByteArrayInputStream(data);
                }
            }
            """;
    }
}
