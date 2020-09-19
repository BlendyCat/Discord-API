package com.blendycat.discordapi.discord;

import com.blendycat.discordapi.discord.handler.ReactionHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Test implements ReactionHandler {

    private Plugin plugin;

    public Test(Plugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onReactionAdd(User user, String channelID, String messageID, Emoji emoji) {
        Bukkit.broadcastMessage("User: " + user.toString());
        Bukkit.broadcastMessage("channelID: " + channelID);
        Bukkit.broadcastMessage("messageID: " + messageID);
        Bukkit.broadcastMessage("emoji: " + emoji.toString());
    }

    @Override
    public void onReactionRemove(User user, String channelID, String messageID, Emoji emoji) {
        Bukkit.broadcastMessage("User: " + user.toString());
        Bukkit.broadcastMessage("channelID: " + channelID);
        Bukkit.broadcastMessage("messageID: " + messageID);
        Bukkit.broadcastMessage("emoji: " + emoji.toString());
    }
}
