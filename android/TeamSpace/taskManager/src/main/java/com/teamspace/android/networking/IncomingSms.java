package com.teamspace.android.networking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import com.teamspace.android.caching.DataManager;
import com.teamspace.android.caching.DataManagerCallback;
import com.teamspace.android.models.MigratedTask;
import com.teamspace.android.models.UserAuthData;
import com.teamspace.android.utils.Utils;

import java.util.ArrayList;

/**
 * Created by vivek on 2/20/15.
 */
public class IncomingSms extends BroadcastReceiver {
    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    public void onReceive(final Context context, Intent intent) {

        // If we are not trying to validate OTP right now, don't listen to any SMS.
        if (!DataManager.getInstance(context).validateOTPFromSMS) {
            return;
        }

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String message = currentMessage.getDisplayMessageBody();
                    String [] otp = message.split("SpaceOTP:");
                    if (otp.length < 2) {
                        return;
                    }

                    // If we found "TeamSpaceOTP:" in the SMS, try to validate OTP
                    DataManager dataMgr = DataManager.getInstance(context);
                    dataMgr.verifyOTP(otp[1], new DataManagerCallback() {
                        public void onDataReceivedFromServer(String dataStoreKey) {
                            if (dataStoreKey == null) {
                                return;
                            }
                            UserAuthData data = (UserAuthData)
                                    DataManager.getInstance(context).retrieveData(dataStoreKey);
                            if (data == null) {
                                return;
                            }

                            Utils.setSignedInUserId(data.getUserId());
                            Utils.setSignedInUserKey(data.getKey());
                        }
                    });
                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Utils.log("IncomingSms parsing failure " + e.toString());
            e.printStackTrace();
        }
    }
}
