package com.onarandombox.MultiVerseUpdater.commands;

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
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import com.onarandombox.MultiVerseUpdater.MVCommandHandler;
import com.onarandombox.MultiVerseUpdater.MVModule;
import com.onarandombox.MultiVerseUpdater.MVUpdater;
import com.onarandombox.MultiVerseUpdater.MultiVerseUpdater;

public class MVUpdate extends MVCommandHandler {

    private CommandSender sender;
    
    public MVUpdate(MultiVerseUpdater plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        this.sender = sender;
        /**
         * List Modules Section -- Outputs all the possible MultiVerse modules.
         */
        if((args.length==1 || args.length==2) && args[0].equalsIgnoreCase("list")){
            // If the current 'modules' HashMap is empty OR we wan't to force a module list update.
            if(!(MultiVerseUpdater.modules.size()>0) || (args.length==2 && args[1].equalsIgnoreCase("update"))){
                downloadModulesList(); // Download the new Modules File.
                updateModuleList(); // Process this file into the HashMap.
            }
            
            // However if the HashMap is still empty then something has gone wrong and we end here.
            if(MultiVerseUpdater.modules.size()<=0){
                sender.sendMessage("No modules available"); // Simple Output.
                return true; // Return true because the command was correct we just can't get any module information.
            }
            
            sender.sendMessage(ChatColor.RED + "MultiVerse Modules -"); // Simple Output
            
            Set<String> moduleKeys = MultiVerseUpdater.modules.keySet(); // Grab the keys from the HashMap.
            // Cycle through all the entries in the HashMap
            for(String moduleKey : moduleKeys){
                String description = MultiVerseUpdater.modules.get(moduleKey).description; // Grab the Description
                String name = MultiVerseUpdater.modules.get(moduleKey).name; // Grab the REAL Name
                Double version = MultiVerseUpdater.modules.get(moduleKey).version; // Grab the Version
                
                sender.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + " - " + ChatColor.RED + "v" + version + ChatColor.WHITE + " - " + description); // Output the details in a readable format.
            }
            return true;
        }
        
        /**
         * Check Modules Section -- Check the installed modules against the latest module list for updates.
         */
        if(args.length==1 && args[0].equalsIgnoreCase("check")){
            downloadModulesList(); // Download the up to date 'modules' file.
            updateModuleList(); // Process the file.
            updateCheck(); // Perform version checks on the installed modules.
            return true;
        }
        
        /**
         * Update Modules Section -- Update/Install a given Module.
         */
        if(args.length==2 && (args[0].equalsIgnoreCase("update") || args[0].equalsIgnoreCase("install"))){
            downloadModulesList(); // Download the up to date 'modules' file.
            updateModuleList(); // Process the file.
            // First check if the given module actually exists.
            if(MultiVerseUpdater.modules.containsKey(args[1].toString().toUpperCase())){
                MVModule module = MultiVerseUpdater.modules.get(args[1].toString().toUpperCase()); // Grab the Module and its details.
                Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin(args[1].toString()); // Grab the module from the plugin manager.
                
                // If 'plugin' returns null then it means the module isn't installed, so we are free to do as we please.
                // However if it's not null then the module is already installed so we wan't/need to perform a version check first
                // and only update if it is out of date.
                if(plugin!=null){
                    String v1 = normalisedVersion(module.version.toString());
                    String v2 = normalisedVersion(plugin.getDescription().getVersion());
                    int compare = v1.compareTo(v2);
                    if(compare<=0){
                        sender.sendMessage("Module is up to date!");
                        return true;
                    }
                }
                
                // TRY to Update the Module.
                try {
                    // If the module exists we will attempt to Install/Update it.
                    new MVUpdater(this.plugin).update(module);
                } catch (Exception e) {
                    // Output accordingly if an Error occurs.
                    sender.sendMessage("An Error Occured!");
                    MultiVerseUpdater.log.info(e.toString());
                }
                return true;
            }
        }
        /**
         * Return False because we clearly didn't do anything :O
         */
        return false;
    }

    /**
     * Check the currently installed modules against the up to date module list.
     */
    private void updateCheck() {
        Set<String> moduleKeys = MultiVerseUpdater.modules.keySet(); // Grab the keys from the HashMap.
        // Cycle through all the entries in the HashMap
        for(String moduleKey : moduleKeys){
            String name = MultiVerseUpdater.modules.get(moduleKey).name; // Grab the REAL Name
            Double version = MultiVerseUpdater.modules.get(moduleKey).version; // Grab the Version

            Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin(name);
            
            if(plugin!=null){
                String v1 = normalisedVersion(version.toString());
                String v2 = normalisedVersion(plugin.getDescription().getVersion());

                int compare = v1.compareTo(v2);

                if(compare > 0){
                    sender.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + " - " + ChatColor.RED + "v" + version + ChatColor.WHITE + " - " + ChatColor.RED + "Update Available!");
                } else {
                    sender.sendMessage(ChatColor.GREEN + name + ChatColor.WHITE + " - " + ChatColor.RED + "v" + version + ChatColor.WHITE + " - " + ChatColor.GREEN + "Up To Date!");
                }
            }
        }
    }
    
    /**
     * Download the 'modules.yml' file, this should be the most up to date one from the server.
     */
    private void downloadModulesList() {
        String downloadURL = "http://bukkit.onarandombox.com/multiverse/modules.yml"; // URL that contains the 'module.yml'
        String fileName = downloadURL.substring(downloadURL.lastIndexOf('/') + 1); // Grab the file name from the URL.

        sender.sendMessage("Downloading latest modules file."); // Simple output.

        URL url = null; // Initialize a URL Variable.
        
        try {
            url = new URL(downloadURL); // Attempt to parse the URL String into a URL which we can utilize.
        } catch (MalformedURLException e) {
            sender.sendMessage("An Error Occured!");
            MultiVerseUpdater.log.info(e.getMessage()); // Basic error output if things go wrong.
            return; // Return, we don't want to continue any further.
        }
        
        File file = new File("plugins/MultiVerse/" + fileName); // Grab the file/location where we will save the data.
        
        if (file.exists()) {
            file.delete(); // If the file already exists we're going to delete it.
        }

        try {
            InputStream inputStream = url.openStream(); // Start an InputStream using the URL we setup.
            OutputStream outputStream = new FileOutputStream(file); // Start an OutputStream to the File we setup.

            byte[] buffer = new byte[1024]; // Simple byte array limited to 1024 bytes (1MB).
            int len = 0;

            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len); // Cycle through the InputStream saving what we find to the File we created.
            }
            
            inputStream.close(); // Close the inputStream.
            outputStream.close(); // Close the outputStream.
        } catch (IOException e) {
            sender.sendMessage("An Error Occured!");
            MultiVerseUpdater.log.info(e.getMessage()); // Basic error output if things go wrong.
        }
    }
    
    /**
     * Process the 'modules.yml' and place the values into the class etc.
     */
    private void updateModuleList() {
        Configuration config = new Configuration(new File(MultiVerseUpdater.dataFolder, "modules.yml"));
        config.load();
        
        MultiVerseUpdater.modules = new HashMap<String,MVModule>();
        
        List<String> moduleKeys = config.getKeys("modules");
        
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
     * Convert the given Version String to a Normalised Version String so we can compare it.
     * @param version
     * @return
     */
    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }
    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
}
