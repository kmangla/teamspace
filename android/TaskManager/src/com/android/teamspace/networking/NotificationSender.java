package com.android.teamspace.networking;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.appcompat.R;

import com.android.teamspace.common.ui.TaskUpdateActivity;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.Task;

public class NotificationSender {

	public static void sendNotification(Context c, Employee e, Task t) {

		Intent resultIntent = new Intent(c, TaskUpdateActivity.class);
		resultIntent.putExtra("TASK_ID", t.getId());	

	
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(
						c,
						0,
						resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
						);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(c)
			    .setContentTitle("My notification")
			    .setContentText("Hello World!")
			    .setContentIntent(resultPendingIntent);
		

		NotificationManager mNotifyMgr = 
				(NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
			// Builds the notification and issues it.
		mNotifyMgr.notify((int)t.getId(), mBuilder.build());
	}
}
