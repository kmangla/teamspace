package com.teamspace.android.caching.datafetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teamspace.android.caching.DataManager;
import com.teamspace.android.caching.DataManagerCallback;
import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.interfaces.DataFetchInterface;
import com.teamspace.android.models.MigratedTask;
import com.teamspace.android.networking.NetworkRoutes;
import com.teamspace.android.networking.NetworkingLayer;
import com.teamspace.android.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class TaskFetchForUser implements DataFetchInterface {

	private String userID;
	private Context context;
	
	public TaskFetchForUser(Context context, String userID) {
		this.userID = userID;
		this.context = context;
	}
	
	@Override
	public void fetchDataFromServer(final DataManagerCallback callback) {
		final String dataStoreKey = "tasks_from_server_" + System.nanoTime();
		// Update the cache with the fresh data
		final String url = NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_ALL_TASKS + "?status=open&toPopulate=assignedTo,lastMessage"
					 + "&key=" + Utils.getSignedInUserKey();
		
		NetworkingLayer.getInstance(context).makeGetJSONArrayRequest(
		url,
		new Response.Listener<JSONArray>() {
		    @Override
		    public void onResponse(JSONArray response) {
		    	// We now have the network response. No need to look in the cache anymore.
		    	if (callback != null) {
		    		callback.networkResponseReceived = true;
		    	}
		    	
		    	Utils.log("TaskFetchForUser GET response for url: " + url
						+ " got response: " + response);
		    	
				// Call the callback
				DatabaseCache.getInstance(context).deleteAllMigratedTasksForUserBlockingCall(userID);
				ArrayList<MigratedTask> tasks = new ArrayList<MigratedTask>();
				for(int i = 0; i < response.length(); i++)
				{
					try {
				      JSONObject object = response.getJSONObject(i);
				      MigratedTask task = MigratedTask.parseJSON(object);
				      tasks.add(task);
				      DatabaseCache.getInstance(context).setMigratedTask(task);
					} catch (JSONException e) {
						Log.e("TaskFetchForUser fetchDataFromServer json_parsing_exception", e.getMessage());
					} catch (java.text.ParseException e) {
						Log.e("TaskFetchForUser fetchDataFromServer parsing_exception", e.getMessage());
					}
				}
				DataManager.getInstance(context).insertData(dataStoreKey, tasks);
				if (callback != null) {
					callback.onDataReceivedFromServer(dataStoreKey);
				}
		    }
		},		
		new Response.ErrorListener() {

		    @Override
		    public void onErrorResponse(VolleyError error) {
		    	Utils.log("fetchDataFromServer() network call failed for tasks: " + url);
		    	error.printStackTrace();
		    }  
		});
	}

	@Override
	public void fetchDataFromDatabaseCache(final DataManagerCallback callback) {
		AsyncTask<String, Void, ArrayList<MigratedTask>> asyncTask = new AsyncTask<String, Void, ArrayList<MigratedTask>>() {
			@Override
			protected ArrayList<MigratedTask> doInBackground(String... user) {
                if (user[0] == null) {
                    return null;
                }
				return DatabaseCache.getInstance(context).getMigratedTasksForUserBlockingCall(user[0]);
			}
			
			@Override
		    protected void onPostExecute(ArrayList<MigratedTask> tasks) {
                if (tasks == null) {
                    return;
                }

				// Now we have the data from cache. We return this cached data
				// only if the network has not already returned fresh data.
				if (callback != null && !callback.networkResponseReceived) {
					String dataStoreKey = "tasks_from_database_" + System.nanoTime();
					DataManager.getInstance(context).insertData(dataStoreKey, tasks);
					callback.onDataReceivedFromCache(dataStoreKey);
				}
		    }
		};
		asyncTask.execute(userID);

	}

}
