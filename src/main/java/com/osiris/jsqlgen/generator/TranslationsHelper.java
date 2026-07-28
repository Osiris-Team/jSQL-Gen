package com.osiris.jsqlgen.generator;

import com.osiris.jsqlgen.Data;
import com.osiris.jsqlgen.model.Column;
import com.osiris.jsqlgen.model.Database;
import com.osiris.jsqlgen.model.Table;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class TranslationsHelper {
    /**
     * Checks for missing translations in user's project files.
     * @param javaProjectGenDir the project generation directory
     * @param db the database model
     * @throws IOException if writing the warning file fails
     */
    public static void check(Data.JavaProjectGenDir javaProjectGenDir, Database db) throws IOException {
        CopyOnWriteArrayList<Table> tables = db.tables;

        // ===== Check for missing translations in user's project files =====
        if (!db.getJavaProjectDirs().isEmpty()) {
            // 1. Collect all required TString field names from the generated content
            //    These are the field names we just generated in s2 (the "needed" translations)
            Set<String> neededFieldNames = new LinkedHashSet<>();
            for (Table t : tables) {
                for (Column col : t.columns) {
                    String nameField = "colName" + col.id;
                    neededFieldNames.add(nameField);
                    if (col.comment != null && !col.comment.isEmpty()) {
                        String commentField = "colComment" + col.id;
                        neededFieldNames.add(commentField);
                    }
                }
            }

            // 2. Scan all project directories for files extending DatabaseTranslationBase
            //    and collect the TString field names they already define
            Set<String> existingFieldNames = new LinkedHashSet<>();
            for (File projectDir : db.getJavaProjectDirs()) {
                if (!projectDir.exists()) continue;
                scanForExistingTranslations(projectDir, existingFieldNames);
            }

            // 3. Determine missing field names
            Set<String> missingFieldNames = new LinkedHashSet<>(neededFieldNames);
            missingFieldNames.removeAll(existingFieldNames);

            // 4. If there are missing translations, write the warning file.
            boolean shouldWriteWarning = !missingFieldNames.isEmpty();

            if (shouldWriteWarning) {
                StringBuilder warningContent = new StringBuilder();
                warningContent.append("WARNING: MISSING TRANSLATIONS\n");
                warningContent.append("============================\n");
                warningContent.append("The following TString fields are defined in the generated DatabaseTranslationBase.java\n");
                warningContent.append("but are MISSING in one or more of your custom translation classes (files extending DatabaseTranslationBase).\n");
                warningContent.append("Please add them to your translation files.\n\n");

                // Show detailed list grouped by table for tables with missing fields
                warningContent.append("Missing fields (grouped by table):\n\n");

                // Group missing fields by table name
                java.util.LinkedHashMap<String, java.util.LinkedHashSet<String>> tableToMissingFields = new java.util.LinkedHashMap<>();
                for (String fieldName : missingFieldNames) {
                    var info = findFieldInfo(tables, fieldName);
                    String tableName = info != null ? info.getLeft().name : "UNKNOWN_TABLE";
                    tableToMissingFields.computeIfAbsent(tableName, k -> new java.util.LinkedHashSet<>()).add(fieldName);
                }

                for (java.util.Map.Entry<String, java.util.LinkedHashSet<String>> entry : tableToMissingFields.entrySet()) {
                    String tableName = entry.getKey();
                    java.util.LinkedHashSet<String> fields = entry.getValue();
                    warningContent.append("  Table: ").append(tableName).append("\n");
                    for (String fieldName : fields) {
                        var info = findFieldInfo(tables, fieldName);
                        warningContent.append("    - ").append(fieldName);
                        if (info != null) {
                            warningContent.append(" ");
                            if (fieldName.contains("Comment"))
                                appendJavaTranslation(info.getLeft(), info.getRight(), warningContent, false, true);
                            else
                                appendJavaTranslation(info.getLeft(), info.getRight(), warningContent, true, false);
                        } else {
                            warningContent.append("\n");
                        }
                    }
                    warningContent.append("\n");
                }

                warningContent.append("---\n");
                warningContent.append("Total missing: ").append(missingFieldNames.size()).append("\n");

                File warningFile = new File(javaProjectGenDir, "A1_WARNING_MISSING_TRANSLATIONS.txt");
                Files.writeString(warningFile.toPath(), warningContent.toString());
                System.err.println("[WARNING] Missing translations detected! Written to: " + warningFile.getAbsolutePath());
            }
        }
    }

    /**
     * Recursively scans a directory for Java files that extend DatabaseTranslationBase,
     * and collects the TString field names they define.
     * Skips the generated DatabaseTranslationBase.java file itself, since we only want
     * to find fields from custom translation classes (e.g. TGerman.java, TFrench.java).
     * @param dir the directory to scan (recursively)
     * @param existingFieldNames the set to populate with found field names
     */
    private static void scanForExistingTranslations(File dir, Set<String> existingFieldNames) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Skip common build directories to speed up scanning
                String name = file.getName();
                if (name.equals(".git") || name.equals(".svn") || name.equals("target")
                    || name.equals("build") || name.equals("node_modules") || name.equals(".idea")
                    || name.equals("bin") || name.equals("out") || name.equals("classes")) {
                    continue;
                }
                scanForExistingTranslations(file, existingFieldNames);
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                // Skip the generated DatabaseTranslationBase.java itself
                if (file.getName().equals("DatabaseTranslationBase.java")) {
                    continue;
                }

                try {
                    String content = Files.readString(file.toPath());

                    // Check if this file extends DatabaseTranslationBase
                    if (!content.contains("extends DatabaseTranslationBase")) {
                        continue;
                    }

                    // Extract all TString field declarations.
                    int searchFrom = 0;
                    while (true) {
                        int idx = content.indexOf("= new TString(\"", searchFrom);
                        if (idx == -1) break;

                        // Find the start of the field name after the opening quote
                        int fieldNameStart = idx + "= new TString(\"".length();
                        int fieldNameEnd = content.indexOf("\"", fieldNameStart);
                        if (fieldNameEnd == -1) break;

                        String fieldName = content.substring(fieldNameStart, fieldNameEnd);
                        if (!fieldName.isEmpty()) {
                            existingFieldNames.add(fieldName);
                        }

                        searchFrom = fieldNameEnd + 1;
                    }
                } catch (IOException e) {
                    // Skip files that can't be read
                    System.err.println("[WARNING] Could not read file: " + file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Given a TString field name (e.g. "colName42" or "colComment42"),
     * looks up the corresponding table and column info for a human-readable description.
     * @param tables the list of all tables
     * @param fieldName the field name to look up (e.g. "colName5" or "colComment12")
     * @return a human-readable string like "Table: users, Column: email" or null if not found
     */
    private static Pair<Table, Column> findFieldInfo(CopyOnWriteArrayList<Table> tables, String fieldName) {
        // Determine if it's a name or comment field and extract the column id
        boolean isComment = fieldName.startsWith("colComment");
        boolean isName = fieldName.startsWith("colName");
        if (!isComment && !isName) return null;

        String prefix = isComment ? "colComment" : "colName";
        String idStr = fieldName.substring(prefix.length());

        long colId;
        try {
            colId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }

        // Search through all tables and columns for a matching column id
        for (Table t : tables) {
            for (Column col : t.columns) {
                if (col.id == colId) {
                    return Pair.of(t, col);
                }
            }
        }

        return null;
    }

    public static void appendJavaTranslation(Table t, Column col, StringBuilder s2) {
        s2.append("/** "+ t.name+"."+ col.name+" */ public static TString colName"+ col.id+
            " = new TString(\"colName"+ col.id+"\", \""+ col.name+"\");\n");
        if(col.comment != null && !col.comment.isEmpty()){
            s2.append("/** "+ t.name+"."+ col.name+" (comment) */ public static TString colComment"+ col.id+
                " = new TString(\"colComment"+ col.id+"\", \""+ col.comment.replace("\"", "\\\"") + "\");\n");
        }
    }

    public static void appendJavaTranslation(Table t, Column col, StringBuilder s2, boolean appendName, boolean appendComment) {
        if(appendName)
            s2.append("/** "+ t.name+"."+ col.name+" */ public static TString colName"+ col.id+
                " = new TString(\"colName"+ col.id+"\", \""+ col.name+"\");\n");

        if(appendComment){
            if(col.comment != null && !col.comment.isEmpty()){
                s2.append("/** "+ t.name+"."+ col.name+" (comment) */ public static TString colComment"+ col.id+
                    " = new TString(\"colComment"+ col.id+"\", \""+ col.comment.replace("\"", "\\\"") + "\");\n");
            }
        }
    }
}
