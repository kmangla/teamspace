package com.android.teamspace.caching.datafetcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.interfaces.DataFetchInterface;
import com.android.teamspace.models.MigratedMessage;
import com.android.teamspace.networking.NetworkRoutes;
import com.android.teamspace.networking.NetworkingLayer;
import com.android.teamspace.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class MessageFetchForTask implements DataFetchInterface {

	private String taskID;
	private Context context;
	
	public MessageFetchForTask(Context context, String taskID) {
		this.taskID = taskID;
		this.context = context;
	}

	@Override
	public void fetchDataFromServer(final DataManagerCallback callback) {
		final String dataStoreKey = "messages_from_server_" + System.nanoTime();
		// Update the cache with the fresh data
		final String url = NetworkRoutes.ROUTE_BASE + NetworkRoutes.ROUTE_MESSAGES
					 + "?taskID=" + taskID;
		
		NetworkingLayer.getInstance(context).makeGetJSONArrayRequest(
		url, 
		new Response.Listener<JSONArray>() { 
		    @Override
		    public void onResponse(JSONArray response) {
		    	// We now have the network response. No need to look in the cache anymore.
		    	if (callback != null) {
		    		callback.networkResponseReceived = true;
		    	}
		    	
		    	Utils.log("MessageFetchForTask GET response for url: " + url
						+ " got response: " + response);
		    	
				// Call the callback
				DatabaseCache.getInstance(context).deleteAllMigratedMessagesForTaskBlockingCall(taskID);
				ArrayList<MigratedMessage> messages = new ArrayList<MigratedMessage>();
				for(int i = 0; i < response.length(); i++)
				{
					try {
				      JSONObject object = response.getJSONObject(i);
				      MigratedMessage message = MigratedMessage.parseJSON(object);
				      messages.add(message);
				      DatabaseCache.getInstance(context).setMigratedMessage(message);
					} catch (JSONException e) {
						Log.e("MessageFetchForTask fetchDataFromServer() json_parsing_exception", e.getMessage());
					} catch (java.text.ParseException e) {
						Log.e("MessageFetchForTask fetchDataFromServer() json_parsing_exception", e.getMessage());
					}
				}
				DataManager.getInstance(context).insertData(dataStoreKey, messages);
				if (callback != null) {
					callback.onDataReceivedFromServer(dataStoreKey);
				}
		    }
		},		
		new Response.ErrorListener() {

		    @Override
		    public void onErrorResponse(VolleyError error) {
		    	Utils.log("fetchDataFromServer() network call failed for messages " + url);
		    	error.printStackTrace();
		    }
		});
	}

	@Override
	public void fetchDataFromDatabaseCache(final DataManagerCallback callback) {
		AsyncTask<String, Void, ArrayList<MigratedMessage>> asyncTask = new AsyncTask<String, Void, ArrayList<MigratedMessage>>() {
			@Override
			protected ArrayList<MigratedMessage> doInBackground(String... taskID) {
				return DatabaseCache.getInstance(context).getMigratedMessagesForTaskBlockingCall(taskID[0]);
			}
			
			@Override
		    protected void onPostExecute(ArrayList<MigratedMessage> messages) {
				// Now we have the data from cache. We return this cached data
				// only if the network has not already returned fresh data.
				if (callback != null && !callback.networkResponseReceived) {
					String dataStoreKey = "messages_from_database_" + System.nanoTime();
					DataManager.getInstance(context).insertData(dataStoreKey, messages);
					callback.onDataReceivedFromCache(dataStoreKey);
				}
		    }
		};
		asyncTask.execute(taskID);

	}

}
