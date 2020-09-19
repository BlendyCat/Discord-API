package com.blendycat.discordapi.command;

import com.blendycat.discordapi.API;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

public class UnlinkCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            JSONObject object = new JSONObject();
            try {
                object.put("uuid", player.getUniqueId().toString());
                API.getClient().emit("unlink", object);
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
}
