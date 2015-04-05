package com.teamspace.android.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vivek on 2/19/15.
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
