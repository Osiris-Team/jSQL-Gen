package com.osiris.jsqlgen;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;

public class Const {
    private static String VERSION;
    public static String getVersion(){
        if(VERSION == null){
            try{
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader("pom.xml"));
                VERSION = model.getVersion();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return VERSION;
    }
}
