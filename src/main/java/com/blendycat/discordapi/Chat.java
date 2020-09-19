package com.blendycat.discordapi;

import org.bukkit.ChatColor;

public class Chat {
    public static String format(String message, Object... args) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        return String.format(message, args);
    }
}
