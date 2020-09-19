package com.blendycat.discordapi;

import com.blendycat.discordapi.command.*;
import com.blendycat.discordapi.discord.Emoji;
import com.blendycat.discordapi.discord.handler.CommandHandler;
import com.blendycat.discordapi.discord.Embed;
import com.blendycat.discordapi.discord.command.ListCommand;
import com.blendycat.discordapi.discord.handler.ReactionHandler;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class API extends JavaPlugin {

    private static API instance;

    private static Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private static List<ReactionHandler> reactionHandlers = new ArrayList<>();
    private BiMap<String, String> roleMap = HashBiMap.create();
    private static Client client;
    private static FileConfiguration config;
    private ChatManager chatManager;
    private static String serverID;
    private static String defaultRole;
    private static Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        saveDefaultConfig();

        config = getConfig();
        JSONObject serverOptions = loadServerOptions();

        // Connect to the API server with the Client
        client = new Client("http://blendycat.com:8080", this, serverOptions);

        // Create a new instance of the ChatManager
        chatManager = new ChatManager();

        // Register chat events to plugin manager
        getServer().getPluginManager().registerEvents(chatManager, this);
        registerCommand("list", new ListCommand(this));
        registerCommand("verify", null);
        registerCommand("unlink", null);

        PluginCommand verify = getCommand("verify");
        if(verify != null) verify.setExecutor(new VerifyCommand());

        PluginCommand unlink = getCommand("unlink");
        if(unlink != null) unlink.setExecutor(new UnlinkCommand());

        PluginCommand user = getCommand("user");
        if(user != null) user.setExecutor(new UserTestCommand());

        PluginCommand dm = getCommand("discorddm");
        if(dm != null) dm.setExecutor(new DirectMessageCommand());
    }

    private JSONObject loadServerOptions() {
        JSONObject options = new JSONObject();
        try {
            // The ID of the server which to send messages to
            serverID = config.getString("server-id");
            options.put("serverID", serverID);

            String token = config.getString("token");
            options.put("token", token);

            // Enforce the nickname so that it matches the in game name
            boolean enforceNickname = config.getBoolean("enforce-nickname");
            options.put("enforceNickname", enforceNickname);

            defaultRole = config.getString("roles.default");
            options.put("defaultRole", defaultRole);

            ConfigurationSection roles = config.getConfigurationSection("roles");
            if(roles != null) {
                for (String key : roles.getKeys(false)) {
                    String roleID = roles.getString(key);
                    roleMap.put(key, roleID);
                }
            }

            // The section of the configuration with all the channels
            ConfigurationSection channelSection = config.getConfigurationSection("channels");

            // Create a JSON array for the channels
            JSONArray channels = new JSONArray();

            // Loop through all the channel keys in the channel section
            if(channelSection != null) {
                for(String key : channelSection.getKeys(false)) {
                    // Get the section for the channel
                    ConfigurationSection channel = channelSection.getConfigurationSection(key);

                    if(channel != null) {
                        // Create a JSON Object for the channel options
                        JSONObject channelOptions = new JSONObject();

                        // Get the ID of the channel
                        String channelID = channel.getString("channel-id");
                        channelOptions.put("channelID", channelID);

                        // Get the Webhook ID for the channel
                        String webhookID = channel.getString("webhook-id");
                        channelOptions.put("webhookID", webhookID);

                        // Get the Webhook token for the channel
                        String webhookToken = channel.getString("webhook-token");
                        channelOptions.put("webhookToken", webhookToken);

                        // Get the options
                        boolean chat = channel.getBoolean("enable-chat", false);
                        boolean info = channel.getBoolean("enable-info", false);
                        boolean requireVerification = channel.getBoolean("require-verification", true);
                        boolean adminCommands = channel.getBoolean("enable-admin-commands", false);

                        // Set the options to the JSON Object
                        channelOptions.put("chat", chat);
                        channelOptions.put("info", info);
                        channelOptions.put("requireVerification", requireVerification);
                        channelOptions.put("adminCommands", adminCommands);

                        // Add the channel to the channel array
                        channels.put(channelOptions);
                    }
                }
            }
            // Add the channels array to the options JSON
            options.put("channels", channels);
        }catch(JSONException ex) {
            ex.printStackTrace();
        }
        // Finally return the completed options
        return options;
    }

    public static String getServerID() {
        return serverID;
    }

    @Override
    public void onDisable() {
        client.disconnect();
    }

    public static API getInstance() {
        return instance;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public static String getDefaultRole() {
        return defaultRole;
    }

    public static List<ReactionHandler> getReactionHandlers() {
        return reactionHandlers;
    }

    public static void registerReactionHandler(ReactionHandler handler) {
        reactionHandlers.add(handler);
    }

    public static List<String> getVerifyCommands() {
        if(config.contains("commands.onverify")) {
            return config.getStringList("commands.onverify");
        }
        return null;
    }

    public static Client getClient() {
        return client;
    }

    public static void addReaction(String channelID, String messageID, Emoji emoji, Client.Callback callback) {
        JSONObject reaction = new JSONObject();
        try {
            reaction.put("type", "react");
            reaction.put("channelID", channelID);
            reaction.put("messageID", messageID);
            reaction.put("reaction", emoji.getName());
            client.request(reaction, callback);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteMessage(String channelID, String messageID, Client.Callback callback) {
        JSONObject object = new JSONObject();
        try {
            object.put("type", "delete");
            object.put("channelID", channelID);
            object.put("messageID", messageID);
            client.request(object, callback);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method sends info messages to the API server
     * @param message The string to be sent to the server
     */
    public static void sendMessage(String message, String channelID) {
        JSONObject object = new JSONObject();
        try {
            // Add the message string to the JSON Object to send
            object.put("message", message);
            object.put("channelID", channelID);
            // Send the object to the server
            client.emit("bot", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an info-type message to the API server
     * @param message the info message to send
     */
    public static void sendInfo(String message) {
        JSONObject object = new JSONObject();
        try {
            object.put("message", message);

            client.emit("info", object);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This message will send a chat message from a player
     * to the API
     */
    public static void sendChatMessage(OfflinePlayer player, String message) {

        // Create a JSON Object to encapsulate the data
        JSONObject object = new JSONObject();

        try {
            // Add the player's username to the object
            object.put("username", player.getName());
            // Add the player's UUID to the object
            object.put("uuid", player.getUniqueId().toString());
            // Add the message to the object
            object.put("message", message);

            client.emit("chat", object);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public static void sendEmbed(String channelID, String message, Embed embed, Client.Callback callback) {
        JSONObject object = new JSONObject();
        try {
            object.put("type", "embed");
            object.put("channelID", channelID);
            object.put("message", message);
            JSONObject embedJSON = null;
            if(embed != null) embedJSON = embed.toJSON();
            object.put("embed", embedJSON);
            client.request(object, callback);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param cmd
     * @param handler The handler which will be called on commands
     * @return true if command is registered; false if command is already registered
     */
    public static boolean registerCommand(String cmd, CommandHandler handler) {
        if(!commandHandlers.containsKey(cmd.toLowerCase())) {
            commandHandlers.put(cmd, handler);
            return true;
        }
        return false;
    }

    /**
     *
     * @param cmd the name of the command
     * @return The command handler for the command
     */
    public CommandHandler getCommandHandler(String cmd) {
        return commandHandlers.get(cmd.toLowerCase());
    }

    /**
     * Simple logging method
     * @param message the message to be sent to console
     */
    public void log(String message) {
        logger.info(message);
    }
}
