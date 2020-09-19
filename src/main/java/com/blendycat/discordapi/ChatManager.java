package com.blendycat.discordapi;

import com.blendycat.discordapi.discord.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatManager implements Listener {

    public void sendMessage(User user, String message) {
        String format = ChatColor.translateAlternateColorCodes('&', "&7[&3Discord&7] &7%s:&f ");

        String username = user.getUser();
        if(user.isVerified()) username = user.getUsername();
        // Add the user's name to the message prefix
        String prefix = String.format(format, username);

        // Send the message to the server
        Bukkit.broadcastMessage(prefix + message);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        // The player who sent the message
        Player player = e.getPlayer();
        // The message string
        String message = e.getMessage();
        API.sendChatMessage(player, message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        String message = String.format("`%s joined the game`", player.getName());

        API.sendInfo(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        String message = String.format("`%s left the game`", player.getName());

        API.sendInfo(message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        String message = '`' + e.getDeathMessage() + '`';
        API.sendInfo(message);
    }
}
