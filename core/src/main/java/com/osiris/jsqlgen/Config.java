package com.osiris.jsqlgen;

import com.osiris.dyml.*;

import java.io.File;

public class Config extends Yaml{
    public YamlSection idCounter;
    public Config() {
        super(new File(Main.dir + "/config.yml"));
        try{
            load();
            idCounter = put("idCounter").setDefValues("1");
            save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
