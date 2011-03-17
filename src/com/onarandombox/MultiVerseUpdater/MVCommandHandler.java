package com.onarandombox.MultiVerseUpdater;

import org.bukkit.command.CommandSender;

public abstract class MVCommandHandler {

    protected final MultiVerseUpdater plugin;
    
    public MVCommandHandler(MultiVerseUpdater plugin){
        this.plugin = plugin;
    }
    
    public abstract boolean perform(CommandSender sender, String[] args);
}
