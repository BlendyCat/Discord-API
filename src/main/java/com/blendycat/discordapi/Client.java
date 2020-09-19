package com.blendycat.discordapi;

import com.blendycat.discordapi.discord.Emoji;
import com.blendycat.discordapi.discord.handler.CommandHandler;
import com.blendycat.discordapi.discord.User;
import com.blendycat.discordapi.discord.handler.ReactionHandler;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is used to communicate with the NodeJS
 * Discord Bot Server
 *
 * Author: Evan Merz
 */
public class Client {

    private long requestID = 0;
    private final Map<Long, Callback> requests = new HashMap<>();
    private Socket socket;
    private final Map<String, Callback> userCallback = new HashMap<>();

    Client(String address, API api, JSONObject serverOptions) {
        try {
            // Connect to the server socket
            socket = IO.socket(address);

            // This will be called when the socket connects to the server
            socket.on(Socket.EVENT_CONNECT, (objects)-> {
                api.log("Connected to Discord API Server!");
                // Send the server options to the API server
                socket.emit("options", serverOptions);
            });

            // This will be called when a command is called from the server
            socket.on("command", args -> {
                // Get the JSON command object
                JSONObject json = (JSONObject) args[0];
                try {
                    // The JSON object for the command sender
                    JSONObject sender = json.getJSONObject("sender");
                    JSONObject author = json.getJSONObject("author");

                    // The user's credentials
                    String userString = sender.getString("user");
                    String username = sender.getString("username");
                    String userID = sender.getString("userID");
                    UUID uuid = null;
                    if(sender.has("uuid") && !sender.isNull("uuid")) {
                        uuid = UUID.fromString(sender.getString("uuid"));
                    }

                    // Create a user object with these credentials
                    User user = new User(username, uuid, userString, userID);
                    user.setAvatar(author.getString("avatar"));

                    // The label for the command
                    String cmd = json.getString("command");
                    String channelID = json.getString("channelID");
                    String messageID = json.getString("messageID");

                    // The arguments for the command
                    JSONArray jsonArgs = json.getJSONArray("args");
                    String[] arguments = new String[jsonArgs.length()];

                    for(int i = 0; i < jsonArgs.length(); i++) {
                        arguments[i] = jsonArgs.getString(i);
                    }

                    // Fire the command off to the proper command handler
                    CommandHandler handler = api.getCommandHandler(cmd);

                    if(handler == null) {
                        // Options for the Command Not Found Event
                        JSONObject cnf = new JSONObject();

                        // the user who sent the command
                        cnf.put("user", user);
                        cnf.put("userID", userID);
                        // the command name
                        cnf.put("command", cmd);
                        cnf.put("channelID", channelID);

                        // Send the information back to the API server
                        socket.emit("cnf", cnf);
                    } else {
                        // Fire the command synchronously
                        Bukkit.getScheduler().runTask(handler.getPlugin(), ()->
                            handler.onCommand(user, channelID, messageID, cmd, arguments)
                        );
                        StringBuilder fullCmd = new StringBuilder(cmd + " ");
                        // Recreate the command message
                        for(String arg : arguments) {
                            fullCmd.append(arg);
                            fullCmd.append(" ");
                        }
                        api.log(String.format("%s issued Discord command: %s", user.getUser(), fullCmd.toString().trim()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            // This will be called when a message is sent from a user or console
            socket.on("message", args -> {
                JSONObject json = (JSONObject) args[0];

                try {
                    // The user who sent the message
                    JSONObject sender = json.getJSONObject("sender");

                    // The credentials of the user who sent the message
                    String userString = sender.getString("user");
                    String username = sender.getString("username");
                    String userID = sender.getString("userID");
                    UUID uuid = UUID.fromString(sender.getString("uuid"));

                    // Create a user object for the user
                    User user = new User(username, uuid, userString, userID);

                    // Get the string of the message
                    String message = json.getString("message");

                    String channelID = json.getString("channelID");

                    // Send the message to the chat manager synchronously
                    Bukkit.getScheduler().runTask(api, ()->
                        api.getChatManager().sendMessage(user, message)
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            // This will be called when a user on discord tries to verify their in game account
            socket.on("verify", args -> {
                JSONObject object = (JSONObject) args[0];

                try {
                    // Pull the data from the json object
                    String user = object.getString("user");
                    String userID = object.getString("userID");
                    String channelID = object.getString("channelID");
                    String username = object.getString("username");
                    String code = object.getString("code");

                    JSONObject discord = object.getJSONObject("discord");
                    String dcUsername = discord.getString("username");
                    String discriminator = discord.getString("discriminator");

                    // Find the player with a matching username
                    Player player = Bukkit.getPlayer(username);

                    // Create a json object for the callback
                    JSONObject callback = new JSONObject();
                    if(player != null) {
                        // Put all the necessary data in the callback object
                        callback.put("user", user);
                        callback.put("userID", userID);
                        callback.put("uuid", player.getUniqueId());
                        callback.put("serverID", API.getServerID());
                        callback.put("code", code);
                        callback.put("error", false);

                        // Create a clickable message
                        TextComponent click = new TextComponent("Click here");
                        click.setColor(ChatColor.BLUE);
                        click.setUnderlined(true);
                        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/verify " + code));

                        ComponentBuilder cb = new ComponentBuilder()
                                .color(ChatColor.GRAY)
                                .append(" to link this account to Discord user ")
                                .append(dcUsername)
                                .append("#")
                                .color(ChatColor.DARK_GRAY)
                                .append(discriminator);
                        TextComponent component = new TextComponent(cb.create());
                        // Send the message to the player
                        player.spigot().sendMessage(click, component);
                    } else {
                        // put the error and the channel ID in the object
                        callback.put("error", true);
                    }
                    callback.put("channelID", channelID);
                    // send the callback message
                    socket.emit("vcb", callback);
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
            });

            // This will be called when a user tries to unlink their account
            socket.on("unlinkcb", args -> {
                JSONObject object = (JSONObject) args[0];

                try{
                    UUID uuid = UUID.fromString(object.getString("uuid"));
                    boolean success = object.getBoolean("success");

                    Player player = Bukkit.getPlayer(uuid);

                    if(player != null) {
                        if(success) {
                            player.sendMessage(ChatColor.GREEN + "Unlinked account successfully!");
                        } else {
                            player.sendMessage(ChatColor.RED + "No account to unlink!");
                        }
                    }
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
            });

            socket.on("usercb", args-> {
                JSONObject object = (JSONObject) args[0];
                try{
                    String req = object.getString("req");
                    Callback callback = userCallback.get(req);
                    if(callback != null) {
                        callback.call(object);
                        userCallback.remove(req);
                    }
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
            });

            // Callback for the request
            socket.on("callback", args-> {
                JSONObject data = (JSONObject) args[0];
                try {
                    long id = data.getLong("id");
                    Callback cb = requests.get(id);
                    if(cb != null) {
                        cb.call(data);
                        requests.remove(id);
                    }
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
            });

            socket.on("reactionEvent", args -> {
                JSONObject object = (JSONObject) args[0];
                try {
                    String type = object.getString("type");
                    String channelID = object.getString("channelID");
                    String messageID = object.getString("messageID");
                    JSONObject emojiJSON = object.getJSONObject("emoji");
                    JSONObject userJSON = object.getJSONObject("user");

                    Emoji emoji = new Emoji(
                            emojiJSON.getString("name"), // The name of the emoji
                            emojiJSON.getString("id") // The id for the emoji
                    );

                    boolean verified = userJSON.getBoolean("verified");
                    User user;
                    String userString = null;
                    if(userJSON.has("user")) {
                        userString = userJSON.getString("user");
                    }
                    String userID = userJSON.getString("userID");
                    if(verified) {
                        UUID uuid = UUID.fromString(userJSON.getString("uuid"));
                        String username = userJSON.getString("username");

                        user = new User(username, uuid, userString, userID);
                    } else {
                        user = new User(null, null, userString, userID);
                    }
                    List<ReactionHandler> handlers = API.getReactionHandlers();
                    if(type.equals("MESSAGE_REACTION_ADD")) {
                        handlers.forEach(handler-> {
                            Bukkit.getScheduler().runTask(handler.getPlugin(), ()->handler.onReactionAdd(user, channelID, messageID, emoji));
                        });
                    } else if(type.equals("MESSAGE_REACTION_REMOVE")) {
                        handlers.forEach(handler-> {
                            Bukkit.getScheduler().runTask(handler.getPlugin(), ()->handler.onReactionRemove(user, channelID, messageID, emoji));
                        });
                    }
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the connection to the server
     */
    public void disconnect() {
        socket.disconnect();
        socket.close();
    }

    public void emit(String event, JSONObject object) {
        if(socket.connected()) {
            socket.emit(event, object);
        }
    }

    public void request(JSONObject request, Callback callback) {
        try {
            request.put("id", requestID);
            requests.put(requestID, callback);
            requestID++;
            socket.emit("request", request);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void getUser(String username, Callback callback) {
        JSONObject query = new JSONObject();
        try{
            query.put("req", username);

            socket.emit("getuser", query);

            userCallback.put(username, callback);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }


    public interface Callback {
        void call(JSONObject data);
    }
}