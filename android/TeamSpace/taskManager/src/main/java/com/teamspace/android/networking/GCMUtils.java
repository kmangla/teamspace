package com.teamspace.android.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.tasklist.ui.AllTasksListViewActivity;
import com.teamspace.android.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vivek on 3/17/15.
 */
public class GCMUtils {

    private Context mContext;
    private static GCMUtils instance;
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;

    /**
     * This is the project number obtained
     * from the API Console, as described in "Getting Started."
     * https://developer.android.com/google/gcm/client.html
     */
    String SENDER_ID = "824726709709";

    private GCMUtils() {
        mContext = TaskManagerApplication.getAppContext();
    }

    public static GCMUtils getInstance() {
        if (null == instance) {
            instance = new GCMUtils();
        }
        return instance;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackendBlockingCall(mContext, regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }

        }.execute(null, null, null);

    }

    private static String REGID = "regID";
    private static String DEVICEID = "deviceID";
    private static String APPID = "appID";

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app.
     */
    public void sendRegistrationIdToBackendBlockingCall(Context context, String regid) {
        if (regid == null) {
            return;
        }

        // /token?regID=<token>&phone=<number>
        final String url = NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_TOKEN;
        final HashMap<String, String> params  = new HashMap<String, String>();
        params.put(REGID, regid);
        String phone = Utils.getSignedInUserPhoneNumber();
        if (Utils.isStringEmpty(phone)) {
            phone = Utils.getSignedInUserId();
            if (Utils.isStringEmpty(phone)) {
                phone = "unknown";
            }
        }
        params.put(DEVICEID, phone);
        params.put(APPID, "2");

        NetworkingLayer.getInstance(context).makePostRequest(
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Utils.log("sendRegistrationIdToBackendBlockingCall() POST response for url: " + url + " params: " + params
                                + " got response: " + response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.log("sendRegistrationIdToBackendBlockingCall() POST failed for url " + url + " params: " + params
                                + " with error " + error);
                        error.printStackTrace();
                    }
                });
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = Utils.getAppVersion(context);
        Utils.log("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(AllTasksListViewActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Utils.log("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Utils.log("App version changed.");
            return "";
        }
        return registrationId;
    }
}
