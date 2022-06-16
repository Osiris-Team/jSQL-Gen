/*
 * Copyright (c) 2021-2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.jsqlgen;


import java.io.IOException;
import java.io.OutputStream;

/**
 * Only used for testing.
 * Original TeeOutputStream seems to be more performant.
 */
public final class MyTeeOutputStream extends OutputStream {

    private final OutputStream out;

    private final OutputStream tee;

    public MyTeeOutputStream(OutputStream out, OutputStream tee) {
        if (out == null)
            throw new NullPointerException();
        else if (tee == null)
            throw new NullPointerException();

        this.out = out;
        this.tee = tee;
    }


    @Override
    public void write(int b) throws IOException {
        out.write(b);
        tee.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        tee.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        tee.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
        tee.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            out.close();
        } finally {
            tee.close();
        }
    }
}