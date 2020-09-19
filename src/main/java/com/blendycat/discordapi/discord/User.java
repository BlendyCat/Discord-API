package com.blendycat.discordapi.discord;

import com.blendycat.discordapi.API;
import com.blendycat.discordapi.Client;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class User {

    private String username;
    private UUID uuid;
    private String user;
    private String userID;
    private String avatar;
    private boolean verified;

    public User(String username, UUID uuid, String user, String userID) {
        this.user = user;
        this.userID = userID;
        this.username = username;
        this.uuid = uuid;
        verified = username != null;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public User(String userID) {
        this.userID = userID;
        verified = false;
        username = null;
        user = null;
        uuid = null;
    }

    public String getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUserID() {
        return userID;
    }

    /**
     * Bruh do you think even I know what's going on here?
     * @param username The username of the user you're trying to get
     * @param callback the callback method
     */
    public static void getUser(String username, UserCallback callback) {
        Client client = API.getClient();
        JSONObject object = new JSONObject();
        try {
            object.put("username", username);
            object.put("type", "user");
            // Request the user
            client.request(object, data -> {
                try {
                    boolean error = data.getBoolean("error");
                    if(!error) {
                        if(!data.isNull("user")) {
                            JSONObject u = data.getJSONObject("user");
                            if(u != null) {
                                String uname = u.getString("username");
                                UUID uuid = UUID.fromString(u.getString("uuid"));
                                String userID = u.getString("userID");
                                String userString = u.getString("user");

                                User user = new User(uname, uuid, userString, userID);
                                callback.call(user);
                            } else {
                                callback.call(null);
                            }
                        }
                    }
                } catch(JSONException ex) {
                    ex.printStackTrace();
                }
            });
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isVerified() {
        return verified;
    }

    public void addRole(String roleID) {
        JSONObject object = new JSONObject();

        try {
            object.put("uuid", uuid);
            object.put("roleID", roleID);

            API.getClient().emit("addrole", object);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void sendDirectMessage(String message, Embed embed, DirectMessageCallback callback) {
        if(message != null) {
            Client client = API.getClient();

            JSONObject object = new JSONObject();

            try {
                object.put("type", "dm");
                object.put("userID", userID);
                object.put("message", message);
                if(embed != null) {
                    object.put("embed", embed.toJSON());
                }
                // Send a request to the API
                client.request(object, data -> {
                    try {
                        boolean error = data.getBoolean("error");
                        JSONObject res = data.getJSONObject("res");
                        String messageID = null;
                        String channelID = null;
                        if(res != null) {
                            if(res.has("id")) {
                                messageID = res.getString("id");
                            }
                            if(res.has("channel_id")) {
                                channelID = res.getString("channel_id");
                            }
                        }
                        callback.call(error, null, channelID, messageID);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendDirectMessage(String message, Embed embed) {
        sendDirectMessage(message, embed, (err, username, channelID, messageID)->{});
    }

    public void sendDirectMessage(String message) {
        sendDirectMessage(message, null, (err, username, channelID, messageID)->{});
    }

    public static void directMessage(String username, String message, DirectMessageCallback callback) {
        if(message != null) {
            Client client = API.getClient();
            JSONObject object = new JSONObject();
            try {
                object.put("type", "dm");
                object.put("username", username);
                object.put("message", message);
                // Send a request to the API
                client.request(object, data -> {
                    try {
                        boolean error = data.getBoolean("error");
                        String uname = null;
                        // Get the username if there is no error
                        if(!error) uname = data.getString("username");
                        // Call the callback method
                        JSONObject res = data.getJSONObject("res");
                        String messageID = res.getString("id");
                        String channelID = res.getString("channel_id");
                        callback.call(error, uname, channelID, messageID);
                    } catch(JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    public interface DirectMessageCallback {
        void call(boolean error, String username, String channelID, String messageID);
    }

    public interface UserCallback {
        void call(User user);
    }
}
