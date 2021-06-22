package com.example.fantasyscore;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class Match {

    String id;
    String t1;
    String t2;
    Date time;

    Match (String id, String t1, String t2, Date time) {
        this.id = id;
        this.t1 = t1;
        this.t2 = t2;
        this.time = time;
    }

    static Match[] parseMatchesJson(JSONObject obj) {
        try {
            Iterator<String> keys = obj.keys();
            Match[] matches = new Match[obj.length()];
            int i = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject matchJson = obj.getJSONObject(key);
                String t1 = matchJson.getString("t1");
                String t2 = matchJson.getString("t2");
                Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(matchJson.getString("time"));
                matches[i++] = new Match(key, t1, t2, date);
            }
            return matches;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Match[0];
    }
}
