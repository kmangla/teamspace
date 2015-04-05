package com.ts.messagespace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pratyus on 2/19/15.
 */
public class MessageList {
    public ArrayList<Message> messageList;

    public static MessageList parseJSON(JSONArray object) {
        MessageList list = new MessageList();
        list.messageList = new ArrayList<Message>();

        for(int i = 0; i < object.length(); i++) {
            try {
                JSONObject msgObject = object.getJSONObject(i);
                Message message = Message.parseJSON(msgObject);
                list.messageList.add(message);
            } catch (Exception e) {

            }
        }

        return list;
    }
}
