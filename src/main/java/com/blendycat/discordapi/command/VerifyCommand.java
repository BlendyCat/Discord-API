package com.blendycat.discordapi.command;

import com.blendycat.discordapi.API;
import com.blendycat.discordapi.Client;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Pattern;

public class VerifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            // Cast command sender to player
            Player player = (Player) sender;
            if(args.length == 1) {
                Client client = API.getClient();

                JSONObject object = new JSONObject();

                try {
                    object.put("type", "verify");
                    object.put("uuid", player.getUniqueId().toString());
                    object.put("code", args[0]);
                    object.put("username", player.getName());
                    // send the verfication object
                    client.request(object, data -> {
                        try {
                            boolean error = data.getBoolean("error");
                            if(!error) {
                                player.sendMessage(ChatColor.GREEN + "Your account is now verified!");
                                String username = player.getName();
                                List<String> cmds = API.getVerifyCommands();
                                if(cmds != null) {
                                    for(String command : cmds) {
                                        Bukkit.getScheduler().runTask(API.getInstance(), ()->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll(Pattern.quote("{username}"), username)));
                                    }
                                }
                            }
                            // Silently ignore errors
                        } catch(JSONException ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        } else {
            sender.sendMessage("This command is only for players!");
        }
        return false;
    }
}
