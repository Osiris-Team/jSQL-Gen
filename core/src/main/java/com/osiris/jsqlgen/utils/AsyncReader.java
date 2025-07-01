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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AsyncReader {
    public final InputStream inputStream;
    public final Thread thread;
    public List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();
    public boolean isCacheUnreadLines = true;
    private List<String> cacheUnreadLines = new ArrayList<>();

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
                if(millisUntilNextCheck >= 0)
                    Thread.sleep(millisUntilNextCheck);

                while (true) {
                    line = br.readLine(); // blocks
                    if(line != null) {
                        if(this.listeners.isEmpty() && isCacheUnreadLines)
                            cacheUnreadLines.add(line);
                        else{
                            if(isCacheUnreadLines){
                                // Consume cached lines
                                for (String cachedLine : cacheUnreadLines) {
                                    for (Consumer<String> listener : this.listeners) {
                                        listener.accept(cachedLine);
                                    }
                                }
                                cacheUnreadLines.clear();
                            }

                            // Consume new line
                            for (Consumer<String> listener :
                                this.listeners) {
                                listener.accept(line);
                            }
                        }
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
