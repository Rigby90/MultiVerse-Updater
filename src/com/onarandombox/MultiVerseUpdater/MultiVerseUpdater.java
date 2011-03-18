package com.onarandombox.MultiVerseUpdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    
    public void onLoad() {
        // Check if MultiVerse folder exists... if not create it.
    }
    
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
     * Download the file from the given URL and save it to the File location.
     * @param downloadURL -- Remote File to download.
     * @param file -- Location to save the file to.
     * @throws Exception
     */
    public void downloadFile(String downloadURL, File file) {
        // Grab the name of the file from the DownloadURL.
        String fileName = downloadURL.substring(downloadURL.lastIndexOf('/') + 1);
        
        // Simple output to state that the plugin is downloading a file.
        MultiVerseUpdater.log.info(MultiVerseUpdater.logPrefix + "- Downloading File - " + fileName);

        // Initialize a URL Variable.
        URL url = null;
        // Attempt to parse the given downloadURL into a URL we can download from.
        try { 
            url = new URL(downloadURL); 
        } 
        catch (MalformedURLException e) { 
            MultiVerseUpdater.log.severe("Error Parsing URL"); 
            MultiVerseUpdater.log.severe(e.toString());
            return;
        }
        
        // If the file already exists then lets rename it to a backup.
        if (file.exists()) {
            file.renameTo(new File(file.toString() + ".backup"));
            //file.delete();
        }

        // Initialize an InputStream and OutputStream
        InputStream inputStream = null;
        OutputStream outputStream = null;
        
        // Attempt to Open the streams ot the URL and to the Local File.
        try {
            inputStream = url.openStream();
            outputStream = new FileOutputStream(file);
        } catch (IOException e) {
            MultiVerseUpdater.log.severe("Error Opening Streams");
            MultiVerseUpdater.log.severe(e.toString());
            return;
        }
        
        // Attempt to write the InputStream to the OutputStream.
        try {
            // Create a byte array for our buffer and limit it to 1024 Bytes.
            byte[] buffer = new byte[1024];
            // Initialize an array to hold the current length within the loop/stream.
            int len = 0;
            // Loop through the InputStream and write to the output stream.
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            MultiVerseUpdater.log.severe("Error Reading/Writing Stream");
            MultiVerseUpdater.log.severe(e.toString());
            return;
        }
        
        // Attempt to Close both the InputStream and OutputStream
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            MultiVerseUpdater.log.severe("Error Closing Streams");
            MultiVerseUpdater.log.severe(e.toString());
            return;
        }
    }
    
    /**
     * Function to load the modules file into the class.
     */
    private void loadUpdaterModules() {
        File file = new File(dataFolder, "modules.yml");
        if(!(file.exists())){
            MultiVerseUpdater.log.info(logPrefix + "- Missing Modules.yml, attempting to download");
            downloadFile("http://bukkit.onarandombox.com/multiverse/modules.yml", file);
            if(!(file.exists())){ return; }
        }
        
        Configuration config = new Configuration(file);
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