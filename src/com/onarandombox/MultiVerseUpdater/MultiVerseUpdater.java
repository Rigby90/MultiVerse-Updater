package com.onarandombox.MultiVerseUpdater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiVerseUpdater.commands.MVInstall;
import com.onarandombox.MultiVerseUpdater.commands.MVUpdate;

public class MultiVerseUpdater extends JavaPlugin {

    // Useless stuff to keep us going.
    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String logPrefix = "[MultiVerse-Updater] ";
    public static final File dataFolder = new File("plugins" + File.separator + "MultiVerse");
    
    // Setup our Map for our Commands using the CommandHandler.
    private Map<String, MVCommandHandler> commands = new HashMap<String, MVCommandHandler>();
    
    // MultiVerse Modules for the Updater/Installer
    public static HashMap<String,MVModule> modules = new HashMap<String,MVModule>();
    
    @Override
    public void onDisable() {
        log.info(logPrefix + "- Disabled");
    }

    @Override
    public void onEnable() {
        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());

        setupCommands();

        loadUpdaterModules();
    }

    /**
     * onCommand
     */
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(this.isEnabled() == false){
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        
        MVCommandHandler handler = commands.get(command.getName().toLowerCase());
            
        if (handler!=null) {
            return handler.perform(sender, args);
        } else {
            return false;
        }
    }
    
    private void loadUpdaterModules() {
        if(!(new File(dataFolder, "modules.yml").exists())){
            return;
        }
        
        Configuration config = new Configuration(new File(dataFolder, "modules.yml"));
        config.load();
        
        MultiVerseUpdater.modules = new HashMap<String,MVModule>();
        
        List<String> moduleKeys = config.getKeys("modules"); // Grab all the Worlds from the Config.
        
        if(moduleKeys != null){
            for (String moduleKey : moduleKeys){
                MVModule temp = new MVModule();
                temp.name = config.getString("modules." + moduleKey + ".name");
                temp.alias = config.getStringList("modules." + moduleKey + ".alias", new ArrayList<String>());
                temp.version = config.getDouble("modules." + moduleKey + ".version", 0.1);
                temp.description = config.getString("modules." + moduleKey + ".description");
                temp.url = config.getString("modules." + moduleKey + ".url");
                MultiVerseUpdater.modules.put(moduleKey.toUpperCase(), temp);
            }
        }
        
    }

    
    /**
     * Setup commands to the Command Handler
     */
    private void setupCommands() {
        commands.put("mvupdate", new MVUpdate(this));
        commands.put("mvinstall", new MVInstall(this));
    }
    
    /**
     * Parse the Authors Array into a readable String with ',' and 'and'.
     * @return
     */
    private String getAuthors(){
        String authors = "";
        ArrayList<String> auths = this.getDescription().getAuthors();
        
        if(auths.size()==1){
            return auths.get(0);
        }
        
        for(int i=0;i<auths.size();i++){
            if(i==this.getDescription().getAuthors().size()-1){
                authors += " and " + this.getDescription().getAuthors().get(i);
            } else {
                authors += ", " + this.getDescription().getAuthors().get(i);
            }
        }
        return authors.substring(2);
    }
}