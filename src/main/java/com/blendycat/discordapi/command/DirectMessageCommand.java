package com.blendycat.discordapi.command;

import com.blendycat.discordapi.Chat;
import com.blendycat.discordapi.discord.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.sqlite.util.StringUtils;

import java.util.Arrays;

public class DirectMessageCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("discord.admin")) {
            if (args.length >= 2) {
                // Get the username which is the first argument
                String username = args[0];
                // Take the first index out of the array
                args = Arrays.copyOfRange(args, 1, args.length);
                // Join the arguments to form the message
                String msg = StringUtils.join(Arrays.asList(args), " ");

                String message = String.format("`[%s -> You]: %s`", sender.getName(), msg);

                User.directMessage(username, message, (error, name, channelID, messageID) -> {
                    if(!error) {
                        sender.sendMessage(Chat.format("&7[&3Discord&7] [&fYou&8 -> &f%s&7]: &f%s",
                                name, msg));
                    } else {
                        sender.sendMessage(ChatColor.RED + "No user found with that username!");
                    }
                });
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/" + label + " <user> <message>");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permissions!");
        }
        return true;
    }
}
