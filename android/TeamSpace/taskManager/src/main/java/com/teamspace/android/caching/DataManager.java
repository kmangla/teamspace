package com.teamspace.android.caching;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.widget.Toast;

import com.teamspace.android.R;
import com.teamspace.android.caching.datafetcher.EmployeeFetchForUser;
import com.teamspace.android.caching.datafetcher.MessageFetchForTask;
import com.teamspace.android.caching.datafetcher.RegistrationFetcher;
import com.teamspace.android.caching.datafetcher.TaskFetchForEmployee;
import com.teamspace.android.caching.datafetcher.TaskFetchForUser;
import com.teamspace.android.caching.dataupdaters.EmployeeUpdater;
import com.teamspace.android.caching.dataupdaters.MessageUpdater;
import com.teamspace.android.caching.dataupdaters.RegistrationUpdater;
import com.teamspace.android.caching.dataupdaters.TaskUpdater;
import com.teamspace.android.interfaces.DataFetchInterface;
import com.teamspace.android.models.MigratedEmployee;
import com.teamspace.android.models.MigratedMessage;
import com.teamspace.android.models.MigratedTask;

public class DataManager {
	private Context mContext;
	private static DataManager instance;
	private LruCache<String, Object> dataStore;

    // validateOTPFromSMS = true means we are listening to incoming SMS on this device.
    public boolean validateOTPFromSMS;

    private DataManager(Context context) {
		mContext = context.getApplicationContext();
		int cacheSize = 4 * 1024 * 1024; // 4MiB
		dataStore = new LruCache<String, Object>(cacheSize);
	}
	
	public static DataManager getInstance(Context context) {
		if (null == instance) {
			instance = new DataManager(context);
		}
		return instance;
	}
	
	public void insertData(String dataStoreKey, Object data) {
		if (dataStoreKey == null || data == null) {
		  return;
		}
		dataStore.put(dataStoreKey, data);
	}
	
	public Object retrieveData(String dataStoreKey) {
		if (dataStoreKey == null) {
			return null;
		}
		
		return dataStore.get(dataStoreKey); 
	}
	
	private void fetchData(DataFetchInterface dataFetcher, DataManagerCallback callback) {
		dataFetcher.fetchDataFromDatabaseCache(callback);
		dataFetcher.fetchDataFromServer(callback);
	}
	
	public void fetchTasksForEmployee(String employeeID, DataManagerCallback callback) {
		DataFetchInterface dataFetcher = new TaskFetchForEmployee(mContext, employeeID);
		fetchData(dataFetcher, callback);
	}	
	
	public void fetchTasksForUser(String userID, DataManagerCallback callback) {
		DataFetchInterface dataFetcher = new TaskFetchForUser(mContext, userID);
		fetchData(dataFetcher, callback);
	}
	
	public void fetchMessagesForTask(String taskId, DataManagerCallback callback) {
		DataFetchInterface dataFetcher = new MessageFetchForTask(mContext, taskId);
		fetchData(dataFetcher, callback);
	}

	public void fetchEmployeesForUser(String userID, DataManagerCallback callback) {
		DataFetchInterface dataFetcher = new EmployeeFetchForUser(mContext, userID);
		fetchData(dataFetcher, callback); 
	}	 
	
	public void deleteEmployee(String empID, DataManagerCallback callback) {
		EmployeeUpdater updater = new EmployeeUpdater(mContext, null, callback);
		updater.delete(empID);
	}
	
	public void updateEmployee(MigratedEmployee emp, DataManagerCallback callback) {
		EmployeeUpdater updater = new EmployeeUpdater(mContext, emp, callback);
		updater.update();
	}
	
	public void createEmployee(MigratedEmployee emp, DataManagerCallback callback) {
		EmployeeUpdater updater = new EmployeeUpdater(mContext, emp, callback);
		updater.create();
	}
	
	public void updateTask(MigratedTask task, DataManagerCallback callback) {
		TaskUpdater updater = new TaskUpdater(mContext, task, callback);
		updater.update();
	}
	
	public void createTask(MigratedTask task, DataManagerCallback callback) {
		TaskUpdater updater = new TaskUpdater(mContext, task, callback);
		updater.create();
	}
	
	public void deleteTask(String taskID, DataManagerCallback callback) {
		TaskUpdater updater = new TaskUpdater(mContext, null, callback);
		updater.delete(taskID);
	}
	
	public void createMessage(MigratedMessage message, DataManagerCallback callback) {
		MessageUpdater updater = new MessageUpdater(mContext, message, callback);
		updater.create();
	}

	public void updateMessage(MigratedMessage message,
			DataManagerCallback dataManagerCallback) {
	}

    public void signUpOrSignIn(final DataManagerCallback callback) {
        validateOTPFromSMS = true;
        RegistrationUpdater updater = new RegistrationUpdater(mContext, new DataManagerCallback() {

            @Override
            public void onSuccess(String response) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }

            @Override
            public void onFailure(String response) {
                validateOTPFromSMS = false;

                if (callback != null) {
                    callback.onFailure(response);
                }
            }
        });
        updater.generateOTP();
    }

    public void verifyOTP(String otp, final DataManagerCallback callback) {
        // We will no longer listen to SMS since we have already tried to validate using some OTP
        validateOTPFromSMS = false;
        RegistrationFetcher fetcher = new RegistrationFetcher(mContext);
        fetcher.getOrCreateUser(otp, callback);
    }
}
