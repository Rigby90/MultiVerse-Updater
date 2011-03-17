package com.onarandombox.MultiVerseUpdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class MVUpdater {
    
    MultiVerseUpdater plugin;
   
    public MVUpdater(MultiVerseUpdater plugin){
        this.plugin = plugin;
    }

    public void update(MVModule mvModule) throws Exception {
        String downloadURL = mvModule.url;
        String pluginName = mvModule.name;
        File pluginFile = new File("plugins/" + downloadURL.substring(downloadURL.lastIndexOf('/') + 1));
        // TODO: Perform checks to see if it is installed already... if so grab the proper file name incase people rename it.
        
        // Disable the Plugin if it already exists.
        disable(pluginName);
        // Download the New Version to the Plugin File.
        download(downloadURL,pluginFile);
        // Reload the Plugin File, this is a precaution to make sure it is definitely reloaded.
        reload(pluginFile);
        // Now Enable the Plugin again.
        enable(pluginName);
    }
    
    /**
     * Disable the given Plugin.
     * @param name
     */
    private void disable(String name){
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin(name);
        if(plugin!=null){
            //MultiVerseCore.log.info(MultiVerseCore.logPrefix + "- Disabling - " + plugin.getDescription().getName());
            this.plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    
    /**
     * Enable the given Plugin.
     * @param name
     */
    private void enable(String name){
        try
        {
            final PluginManager pm = plugin.getServer().getPluginManager();
            final Plugin plugin = pm.getPlugin(name);
            if (!plugin.isEnabled()) new Thread(new Runnable()
                {
                    public void run()
                    {
                        synchronized (pm)
                        {
                            pm.enablePlugin(plugin);
                        }
                    }
                }).start();
        }
        catch (Throwable ex)
        {
            MultiVerseUpdater.log.info(MultiVerseUpdater.logPrefix + "- Error enabling - " + name);
        }
    }
    
    /**
     * Download the Plugin from the given URL.
     * @param downloadURL
     * @throws Exception
     */
    private void download(String downloadURL, File file) throws Exception {
        // BASIC FILE UPDATE CRAP
        String fileName = downloadURL;
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        MultiVerseUpdater.log.info(MultiVerseUpdater.logPrefix + "- Downloading Module - " + fileName.replace(".jar", ""));

        URL url = new URL(downloadURL);
        
        if (file.exists()) {
            file.delete();
        }

        InputStream inputStream = url.openStream();
        OutputStream outputStream = new FileOutputStream(file);

        save(inputStream, outputStream);

        inputStream.close();
        outputStream.close();
    }
    
    /**
     * Save the InputStream to the OutputStream
     * @param inputStream
     * @param outputStream
     * @throws IOException
     */
    private void save(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;

        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
    }
    
    /**
     * Reload the given Plugin File.
     * @param file
     */
    public void reload(File file){
        //this.plugin.getServer().reload();
        //return;
        try
        {
            String fileName = file.toString();
            fileName = (fileName.substring(fileName.lastIndexOf(File.separator) + 1)).replace(".jar", "");
            MultiVerseUpdater.log.info(MultiVerseUpdater.logPrefix + "- (Re-)Loading - " + fileName);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.loadPlugin(file);
        }
        catch (Throwable ex)
        {
            MultiVerseUpdater.log.info("Could not load plugin");
        }
    }
}