package com.blendycat.discordapi.discord.handler;

import com.blendycat.discordapi.discord.User;
import org.bukkit.plugin.Plugin;

public interface CommandHandler {

    /**
     *
     * @param sender the user who sent the message
     * @param channelID the id of the channel the command was sent in
     * @param label the exact string used for the command
     * @param args the arguments for the command
     *
     */
    void onCommand(User sender, String channelID, String messageID, String label, String[] args);

    Plugin getPlugin();

}
