package com.blendycat.discordapi;

import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class BungeeProxy extends Plugin {

    private static BungeeProxy instance;
    private File properties;

    @Override
    public void onEnable() {
        instance = this;

        // Load or create the server options file
        properties = new File(getDataFolder(), "api.properties");
        if(!properties.exists()) {
            if(properties.mkdirs()) {
                InputStream is = getClass().getResourceAsStream("/api.properties");
                copy(is, properties.getAbsolutePath());
            }
        }

        JSONObject serverOptions = loadServerOptions();
    }

    private JSONObject loadServerOptions() {
        JSONObject options = new JSONObject();
        try {
            Scanner scanner = new Scanner(properties);
            String line;
            while(scanner.hasNextLine()) {
                line = scanner.nextLine();
                String[] val = line.split("=");
                if(val.length == 2) {
                    options.put(val[0], val[1]);
                } else {
                    options.put(val[0], "");
                }
            }
        } catch(FileNotFoundException | JSONException ex) {
            ex.printStackTrace();
        }
        return options;
    }

    private static void copy(InputStream source , String destination) {
        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
         
    }
}
