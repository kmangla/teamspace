package com.teamspace.android.caching.datafetcher;

import android.content.Context;
import android.util.Log;

import com.teamspace.android.caching.DataManager;
import com.teamspace.android.caching.DataManagerCallback;
import com.teamspace.android.models.UserAuthData;
import com.teamspace.android.networking.NetworkRoutes;
import com.teamspace.android.networking.NetworkingLayer;
import com.teamspace.android.utils.Constants;
import com.teamspace.android.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vivek on 2/22/15.
 */
public class RegistrationFetcher {
    private Context context;

    public RegistrationFetcher(Context context) {
        this.context = context;
    }

    public void getOrCreateUser(String otp, final DataManagerCallback callback) {
        final String dataStoreKey = "userId_from_server_" + System.nanoTime();
        String phoneNumber = Utils.getSignedInUserPhoneNumber();
        if (Utils.isStringEmpty(phoneNumber)) {
            return;
        }

        // Update the cache with the fresh data
        String tempUrl = NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_VERIFY_OTP
                + "?phoneNumber=" + phoneNumber + "&otp=" + otp;

        String userName = Utils.getSignedInUserName().replace(" ", "");
        if (Utils.isStringNotEmpty(Utils.getSignedInUserName())) {
            tempUrl += "&name=" + userName;
        }

        final String url = tempUrl;

        NetworkingLayer.getInstance(context).makeGetJSONArrayRequest(
                url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Utils.log("RegistrationFetcher getOrCreateUser() GET response for url: " + url
                                + " got response: " + response);

                        UserAuthData userData = null;
                        // Call the callback
                        try {
                            JSONObject object = response.getJSONObject(0);
                            userData = UserAuthData.parseJSON(object);
                        } catch (JSONException e) {
                            Log.e("RegistrationFetcher getOrCreateUser() json_parsing_exception", e.getMessage());
                        } catch (java.text.ParseException e) {
                            Log.e("RegistrationFetcher getOrCreateUser() json_parsing_exception", e.getMessage());
                        }

                        if (userData != null) {
                            DataManager.getInstance(context).insertData(dataStoreKey, userData);

                            if (callback != null) {
                                callback.onDataReceivedFromServer(dataStoreKey);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure(Constants.EMPTY_STRING);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.log("RegistrationFetcher getOrCreateUser() network call failed for url " + url);
                        error.printStackTrace();
                        if (callback != null) {
                            callback.onFailure(error.getLocalizedMessage());
                        }
                    }
                });

    }
}
