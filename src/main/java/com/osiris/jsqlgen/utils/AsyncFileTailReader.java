package com.osiris.jsqlgen.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AsyncFileTailReader {
    public final File file;
    public final Thread thread;
    public List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();
    public boolean isCacheUnreadLines = true;
    private List<String> cacheUnreadLines = new ArrayList<>();

    /**
     * Creates a file tailing reader that reads new lines as they are appended to the file.
     * @param file File to be tailed.
     * @param listeners Consumers that will receive new lines.
     */
    @SafeVarargs
    public AsyncFileTailReader(File file, Consumer<String>... listeners) {
        this(file, 1000, listeners);
    }

    /**
     * Creates a file tailing reader with custom polling interval.
     * @param file File to be tailed.
     * @param millisUntilNextCheck Polling interval in milliseconds.
     * @param listeners Consumers that will receive new lines.
     */
    @SafeVarargs
    public AsyncFileTailReader(File file, int millisUntilNextCheck, Consumer<String>... listeners) {
        this.file = file;
        if (listeners != null && listeners.length != 0) this.listeners.addAll(Arrays.asList(listeners));

        String threadName = "AsyncFileTailReader-" + file.getName();
        thread = new Thread(() -> {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                long filePointer = raf.length(); // start at end of file

                while (true) {
                    long fileLength = file.length();
                    if (fileLength < filePointer) {
                        // File was truncated or rotated
                        raf.seek(0);
                        filePointer = 0;
                    }

                    if (fileLength > filePointer) {
                        raf.seek(filePointer);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            // Decode from ISO-8859-1 to UTF-8
                            line = new String(line.getBytes("ISO-8859-1"), "UTF-8");

                            if (this.listeners.isEmpty() && isCacheUnreadLines) {
                                cacheUnreadLines.add(line);
                            } else {
                                if (isCacheUnreadLines) {
                                    for (String cachedLine : cacheUnreadLines) {
                                        for (Consumer<String> listener : this.listeners) {
                                            listener.accept(cachedLine);
                                        }
                                    }
                                    cacheUnreadLines.clear();
                                }

                                for (Consumer<String> listener : this.listeners) {
                                    listener.accept(line);
                                }
                            }
                        }
                        filePointer = raf.getFilePointer();
                    }

                    Thread.sleep(millisUntilNextCheck);
                }
            } catch (Exception e) {
                System.err.println("Error in " + threadName + ":");
                e.printStackTrace();
            }
        }, threadName);

        thread.setDaemon(true);
        thread.start();
    }
}
