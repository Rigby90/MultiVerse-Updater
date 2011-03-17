package com.onarandombox.MultiVerseUpdater;

import java.util.List;

public class MVModule {

    public String name;
    public List<String> alias;
    public String description;
    public Double version;
    public String url;
    
    public MVModule(){
        
    }
    
    public MVModule(String name, List<String> alias, String description, Double version, String url){
        this.name = name;
        this.alias = alias;
        this.description = description;
        this.version = version;
        this.url = url;
    }
}
