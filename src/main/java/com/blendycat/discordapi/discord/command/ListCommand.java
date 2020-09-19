package com.blendycat.discordapi.discord.command;

import com.blendycat.discordapi.API;
import com.blendycat.discordapi.discord.handler.CommandHandler;
import com.blendycat.discordapi.discord.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class ListCommand implements CommandHandler {

    private Plugin plugin;

    public ListCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param sender    the user who sent the message
     * @param channelID the id of the channel the command was sent in
     * @param label     the exact string used for the command
     * @param args      the arguments for the command
     */
    @Override
    public void onCommand(User sender, String channelID, String messageID, String label, String[] args) {
        // Get the list of online players
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        boolean areis = online.size() == 1;
        String message = String.format("There %s currently %s %s online: ",
                areis ? "is" : "are",
                online.size(),
                areis ? "player" : "players"
        );
        StringBuilder builder = new StringBuilder(message);

        for(Player player : online) {
            builder.append(player.getName());
            builder.append(", ");
        }

        API.sendMessage(builder.toString().substring(0, builder.length() - 2), channelID);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}
