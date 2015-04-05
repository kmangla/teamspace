package com.ts.messagespace;

import android.app.Application;
import android.content.Context;

/**
 * Created by pratyus on 3/10/15.
 */
public class MessageSpaceApplication extends Application {
    private static Context appContext;

    public static Context getAppContext() {
        return MessageSpaceApplication.appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this.getApplicationContext();
    }
}
