package com.osiris.jsqlgen;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

public class UFile {

    public static void showInFileManager(File file) throws IOException {
        if(SystemUtils.IS_OS_WINDOWS)
            Runtime.getRuntime().exec("explorer /select, \""+file+"\"");
        else if (SystemUtils.IS_OS_MAC)
            Runtime.getRuntime().exec("open -R \""+file+"\"");
        else // LINUX based system
            Runtime.getRuntime().exec("xdg-open \""+file+"\"");
    }
}
