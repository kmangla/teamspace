package com.teamspace.android.models;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.teamspace.android.caching.BitmapCache;
import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.QuickContactHelperTask.ContactBadge;
import com.teamspace.android.utils.Utils;

public final class QuickContactHelperTask extends AsyncTask<ContactBadge, Void, Bitmap> {
	
	public static class ContactBadge {
		private final QuickContactBadge badge;
		private final TextView initials;
		private final String employeeId;
		private final ContentResolver contentResolver;
		private MigratedEmployee employee;
		
		public ContactBadge(Context context, QuickContactBadge badge,
				String employeeId, TextView defaultView) {
			this.badge = badge;
			this.employeeId = employeeId;
			contentResolver = context.getContentResolver();
			this.initials = defaultView;
		}
		
		public ContactBadge(Context context, QuickContactBadge badge,
				MigratedEmployee employee, TextView defaultView) {
			this.badge = badge;
			this.employee = employee;
			this.employeeId = employee.getEmployeeID();
			contentResolver = context.getContentResolver();
			this.initials = defaultView;
		}

		public void updateEmployeeDetails() {
			if (Utils.isStringNotEmpty(employeeId)) {
				if (employeeId.equalsIgnoreCase(Utils.getSignedInUserId())) {
					employee = new MigratedEmployee();
					employee.setEmployeeID(employeeId);
					employee.setName(Utils.getSignedInUserName());
					employee.setPhoneWithContryCode(Utils.getSignedInUserPhoneNumber());
				} else {
					employee = DatabaseCache.getInstance(
							TaskManagerApplication.getAppContext())
							.getMigratedEmployeeBlockingCall(employeeId);
				}
			}
		}
	}

	private static final String[] PHOTO_ID_PROJECTION = new String[] { ContactsContract.Contacts.PHOTO_ID };
	private static final String[] PHOTO_BITMAP_PROJECTION = new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO };
	private ContactBadge mBadge;
	
	@Override
	protected Bitmap doInBackground(ContactBadge... badges) {
		mBadge = badges[0];	
		
		// Must fetch the employee details if we were only provided the empId.
		if (mBadge.employee == null) {
			mBadge.updateEmployeeDetails();
			Utils.log("Employee is null while empID: " + mBadge.employeeId);
		}
		
		if (mBadge.employee == null) {
			Utils.log("Employee is null giving up");
			return null;
		}
		
		final Integer thumbnailId = fetchThumbnailId();
		Bitmap thumbnail = null;
		if (thumbnailId != null) {
			thumbnail = fetchThumbnail(thumbnailId);
		}
		
		return thumbnail;
	}
	
	@Override
    protected void onPostExecute(Bitmap thumbnail) {
		if (mBadge.badge != null) {
			mBadge.badge.setVisibility(View.GONE);
		}
		
		if (mBadge.initials != null) {
			mBadge.initials.setVisibility(View.GONE);
		}
		
		if (thumbnail != null) {
			BitmapCache.getInstance().insertData(mBadge.employeeId, thumbnail);
			mBadge.badge.setImageBitmap(thumbnail);
			mBadge.badge.setVisibility(View.VISIBLE);
		} else if (mBadge.employee != null && mBadge.employee.getName() != null) {
			// Initials = first char PLUS the char after space.
			String initials = Utils.extractInitialsFromName(mBadge.employee.getName());
            if (mBadge.initials != null) {
                mBadge.initials.setText(initials);
                mBadge.initials.setVisibility(View.VISIBLE);
            }
		}
	}	
	
	private Integer fetchThumbnailId() {
		final Uri uri = Uri.withAppendedPath(
				ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
				Uri.encode(mBadge.employee.getPhoneWithCountryCode()));
		final Cursor cursor = mBadge.contentResolver.query(uri, PHOTO_ID_PROJECTION,
				null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

		try {
			Integer thumbnailId = null;
			if (cursor.moveToFirst()) {
				thumbnailId = cursor.getInt(cursor
						.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
			}
			return thumbnailId;
		} finally {
			cursor.close();
		}
	}

	final Bitmap fetchThumbnail(final int thumbnailId) {
		final Uri uri = ContentUris.withAppendedId(
				ContactsContract.Data.CONTENT_URI, thumbnailId);
		final Cursor cursor = mBadge.contentResolver.query(uri,
				PHOTO_BITMAP_PROJECTION, null, null, null);

		try {
			Bitmap thumbnail = null;
			if (cursor.moveToFirst()) {
				final byte[] thumbnailBytes = cursor.getBlob(0);
				if (thumbnailBytes != null) {
					thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes,
							0, thumbnailBytes.length);
				}
			}
			return thumbnail;
		} finally {
			cursor.close();
		}
	}

	
}
