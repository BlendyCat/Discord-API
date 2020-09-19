package com.blendycat.discordapi.discord.handler;

import com.blendycat.discordapi.discord.Emoji;
import com.blendycat.discordapi.discord.User;
import org.bukkit.plugin.Plugin;

public interface ReactionHandler {
    Plugin getPlugin();

    void onReactionAdd(User user, String channelID, String messageID, Emoji emoji);

    void onReactionRemove(User user, String channelID, String messageID, Emoji emoji);
}
