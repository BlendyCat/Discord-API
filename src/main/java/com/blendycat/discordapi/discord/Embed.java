package com.blendycat.discordapi.discord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Embed {

    private String title;
    private String description;
    private int color = -1;
    private String url;
    private JSONArray fields;
    private Author author;
    private Thumbnail thumbnail;

    public Embed(String title) {
        this.title = title;
    }

    public Embed setColor(int color) {
        this.color = color;
        return this;
    }

    public Embed setURL(String url) {
        this.url = url;
        return this;
    }

    public Embed setDescription(String description) {
        this.description = description;
        return this;
    }

    public Embed setAuthor(Author author) {
        this.author = author;
        return this;
    }

    public Embed setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public Embed addField(Field field) {
        if(fields == null) fields = new JSONArray();
        fields.put(field.toJSON());
        return this;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("title", title);
            if(description != null) object.put("description", description);
            if(color != -1) object.put("color", color);
            if(url != null) object.put("url", url);
            if(fields != null) object.put("fields", fields);
            if(author != null) object.put("author", author.toJSON());
            if(thumbnail != null) object.put("thumbnail", thumbnail.toJSON());
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
        return object;
    }

    public static class Author {

        private String name;
        private String iconURL;
        private String url;

        public Author(String name) {
            this.name = name;
        }

        public Author setIconURL(String iconURL) {
            this.iconURL = iconURL;
            return this;
        }

        public Author setURL(String url) {
            this.url = url;
            return this;
        }

        // Serialize
        public JSONObject toJSON() {
            JSONObject object = new JSONObject();
            try {
                object.put("name", name);
                if(iconURL != null) object.put("icon_url", iconURL);
                if(url != null) object.put("url", url);
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
            return object;
        }
    }

    public static class Field {

        private String name;
        private String value;
        private boolean inline = false;

        public Field(String name) {
            this.name = name;
        }

        public Field setValue(String value) {
            this.value = value;
            return this;
        }

        public Field setInline() {
            this.inline = true;
            return this;
        }

        public JSONObject toJSON() {
            JSONObject object = new JSONObject();
            try {
                object.put("name", name);
                if(value != null) object.put("value", value);
                if(inline) object.put("inline", true);
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
            return object;
        }
    }

    public static class Thumbnail {

        private String url;

        public Thumbnail(String url) {
            this.url = url;
        }

        public JSONObject toJSON() {
            JSONObject object = new JSONObject();
            try {
                object.put("url", url);
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
            return object;
        }
    }

}
