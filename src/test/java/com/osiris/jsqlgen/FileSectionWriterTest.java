package com.osiris.jsqlgen;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FileSectionWriterTest {

    @Test
    public void testWriteFilesFromSections() throws IOException {
        Path inputFile = Paths.get("input.txt");
        assertTrue(Files.exists(inputFile), "Expected 'input.txt' in the current directory.");

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            boolean inCodeBlock = false;
            String currentFilePath = null;
            List<String> codeBuffer = new ArrayList<>();
            int fileCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.stripTrailing();

                if (!inCodeBlock) {
                    if (line.equals("```java")) {
                        inCodeBlock = true;
                        currentFilePath = null;
                        codeBuffer.clear();
                    }
                } else {
                    if (line.startsWith("// file:")) {
                        currentFilePath = line.substring("// file:".length()).trim();
                    } else if (line.equals("```")) {
                        // End of code block
                        assertNotNull(currentFilePath, "Missing file path declaration in code block.");

                        Path filePath = Paths.get(currentFilePath);
                        Files.createDirectories(filePath.getParent());
                        Files.write(filePath, codeBuffer, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("Wrote file: " + currentFilePath);

                        fileCount++;
                        inCodeBlock = false;
                    } else {
                        codeBuffer.add(line);
                    }
                }
            }

            assertTrue(fileCount > 0, "No file sections processed.");
        }
    }
}
