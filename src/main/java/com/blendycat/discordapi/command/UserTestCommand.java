package com.blendycat.discordapi.command;

import com.blendycat.discordapi.discord.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UserTestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 1 && sender.hasPermission("discord.admin")) {
            User.getUser(args[0], user -> {
                sender.sendMessage(new String[]{
                        "User: " + user.getUser(),
                        "UserID: " + user.getUserID(),
                        "MC-Username: " + user.getUsername(),
                        "UUID: " + user.getUUID().toString()
                });
            });
        }
        return true;
    }
}
