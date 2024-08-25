package nl.softworks.calendarAggregator.domain.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;

import java.util.Iterator;

public class JsonToXml {

    public static JsonToXml ofPretty() {
        return new JsonToXml("  ", "\n");
    }

    public static JsonToXml ofUnformatted() {
        return new JsonToXml("", "");
    }

    public final String lineBreak;
    public final String indent;

    private JsonToXml(String indent, String lineBreak) {
        this.indent = indent;
        this.lineBreak = lineBreak;
    }

    public String convert(String json) {
        // if the json starts with an array, prefix a node to name each entry in the array
        if (json.trim().startsWith("[")) {
            json = "{\"entry\":" + json + "}";
        }

        Object o = new JSONTokener(json).nextValue();
        return convert(0, "root", o);
    }

    private String convert(int indent, String key, Object o) {
        if (o instanceof JSONObject jsonObject) {
            return convert(indent, key, jsonObject);
        }
        if (o instanceof JSONArray jsonArray) {
            return convert(indent, key, jsonArray);
        }
        if (o instanceof String string) {
            String tagName = sanitizeTag(key);
            return this.indent.repeat(indent) + "<" + tagName + ">" + XML.escape(string) + "</" + tagName + ">" + lineBreak;
        }
        return "";
    }

    private String convert(int indent, String key, JSONObject jsonObject) {
        String result = "";
        for (String k : iterable(jsonObject.keys())) {
            try {
                Object o = jsonObject.get(k);
                result += convert(indent + 1, k, o);
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        String tagName = sanitizeTag(key);
        return this.indent.repeat(indent) + "<" + tagName + ">" + lineBreak +
                result +
                this.indent.repeat(indent) + "</" + tagName + ">" + lineBreak;
    }

    private  String convert(int indent, String key, JSONArray jsonArray) {
        try {
            String result = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                Object o = jsonArray.get(i);
                result += convert(indent, key, o);
            }
            return result;
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Iterable<T> iterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    private String sanitizeTag(String tagName) {
        tagName = tagName.replaceAll("[^ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890]", "");
        if (Character.isDigit(tagName.charAt(0))) {
            tagName = "X" + tagName;
        }
        return tagName;
    }

}
