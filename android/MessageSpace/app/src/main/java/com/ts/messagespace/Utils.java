package com.ts.messagespace;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.SyncStateContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;

/**
 * Created by pratyus on 2/12/15.
 */
public class Utils {
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    public static void sendSMS(String phoneNumber, String message)
    {
        if (phoneNumber == null || message == null) {
            return;
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private static String TEXT = "description";
    private static String SENDER = "sentBy";

    public static void sendToServer(Context context, String phoneNumber, String message)
    {
        if (phoneNumber == null || message == null) {
            return;
        }

        final String url =  NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_MESSAGE;
        final HashMap<String, String> params  = new HashMap<String, String>();
        params.put(TEXT, message);
        params.put(SENDER, phoneNumber);

        NetworkingLayer.getInstance(context).makePostRequest(
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MessageSpace", "createMessage() POST response for url: " + url + " params: " + params
                                + " got response: " + response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MessageSpace", "createMessage() POST failed for url " + url + " params: " + params
                                + " with error " + error);
                        error.printStackTrace();
                    }
                });

    }

    private static String REGID = "regID";
    private static String DEVICEID = "deviceID";
    private static String APPID = "appID";


    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app.
     */
    public static void sendRegistrationIdToBackend(Context context, String regid) {
        if (regid == null) {
            return;
        }

        // /token?regID=<token>&phone=<number>
        final String url = NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_TOKEN;
        final HashMap<String, String> params  = new HashMap<String, String>();
        params.put(REGID, regid);
        params.put(DEVICEID, Utils.getSelfPhoneNumber(context));
        params.put(APPID, "1");

        NetworkingLayer.getInstance(context).makePostRequest(
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MessageSpace", "sendRegistrationIdToBackend() POST response for url: " + url + " params: " + params
                                + " got response: " + response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MessageSpace", "sendRegistrationIdToBackend() POST failed for url " + url + " params: " + params
                                + " with error " + error);
                        error.printStackTrace();
                    }
                });
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
            Log.i("MessageSpace", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("MessageSpace", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public static String getSelfPhoneNumber(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = tMgr.getLine1Number();
        return phoneNumber;
    }


    public static void saveServer(Context context, String s) {
        if (s == null || s.length() == 0) {
            return;
        }

        writeStringToSharedPrefs(context, "server", s);
    }

    public static String getServer(Context context) {
        return readStringFromSharedPrefs(context, "server");
    }

    public static String getPort(Context context) {
        String serverPort = readStringFromSharedPrefs(context, "port");

        if (serverPort == null || serverPort.equalsIgnoreCase(":0")) {
            serverPort = "";
        }

        return serverPort;
    }

    public static void savePort(Context context, String s) {
        if (s == null || s.length() == 0) {
            return;
        }
        if (s.indexOf(":") != 0) {
            s = ":" + s;
        }
        writeStringToSharedPrefs(context, "port", s);
    }

    public static void writeStringToSharedPrefs(Context context, String key, String value) {
        SharedPreferences sharedPref = context.
                getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String readStringFromSharedPrefs(Context context, String key) {
        SharedPreferences sharedPref = context.
                getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }
}
