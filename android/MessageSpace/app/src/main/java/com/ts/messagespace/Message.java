package com.ts.messagespace;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pratyus on 2/19/15.
 */
public class Message {
    public String message;
    public String phone;

    public static Message parseJSON(JSONObject msgObject) {
        Message msg = new Message();
        try {
            msg.message = msgObject.getString("message");
            msg.phone = msgObject.getString("phone");
        } catch (Exception e) {

        }
        return msg;
    }
}
