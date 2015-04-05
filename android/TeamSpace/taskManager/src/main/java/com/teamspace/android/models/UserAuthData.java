package com.teamspace.android.models;

import android.content.Context;

import com.teamspace.android.utils.Constants;
import com.teamspace.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserAuthData {
	private String userId;
	private String key;
	
	private static String USER_ID = "userID";
	private static String KEY = "key";

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

	@Override
	public String toString() {
		return this.userId;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(USER_ID, this.getUserId());
		obj.put(KEY, this.getKey());
		return obj;
	}
	public HashMap<String, String> toMapObject() {
		HashMap<String, String> obj = new HashMap<String, String>();
		obj.put(USER_ID, this.getUserId());
		obj.put(KEY, this.getKey());
		return obj;
	}

    public static UserAuthData parseJSON(JSONObject object) throws JSONException, ParseException {
        UserAuthData data;
        try {
            data = new UserAuthData();
            data.userId = object.getString(USER_ID);
            data.key = object.getString(KEY);
        } catch (Exception e) {
            data = null;
        }

        return data;
    }
}
