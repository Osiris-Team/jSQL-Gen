package com.osiris.jsqlgen;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Const {
    private static String VERSION;
    public static String getVersion() throws IOException, XmlPullParserException {
        if(VERSION == null){
            final Properties properties = new Properties();
            properties.load(Const.class.getClassLoader().getResourceAsStream("jsqlgen.properties"));
            VERSION = properties.getProperty("version");
        }
        return VERSION;
    }
}
