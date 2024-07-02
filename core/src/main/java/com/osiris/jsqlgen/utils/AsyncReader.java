/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.jsqlgen.utils;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AsyncReader {
    public final InputStream inputStream;
    public final Thread thread;
    public List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    public AsyncReader(InputStream inputStream, Consumer<String>... listeners) {
        this(inputStream, -1, listeners);
    }

    @SafeVarargs
    public AsyncReader(InputStream inputStream, int millisUntilNextCheck, Consumer<String>... listeners) {
        this.inputStream = inputStream;
        if (listeners != null && listeners.length != 0) this.listeners.addAll(Arrays.asList(listeners));
        Object o = this;
        thread = new Thread(() -> {
            String line = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                while (true) {
                    line = br.readLine();
                    if(line != null)
                        for (Consumer<String> listener :
                            this.listeners) {
                            listener.accept(line);
                        }
                    else if(millisUntilNextCheck >= 0)
                        Thread.sleep(millisUntilNextCheck);
                    else
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error in thread for object '" + o + "' Details:");
                e.printStackTrace();
            }
        });
        thread.start();
    }

}
