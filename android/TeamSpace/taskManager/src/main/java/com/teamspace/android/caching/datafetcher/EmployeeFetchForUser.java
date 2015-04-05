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
import com.teamspace.android.models.MigratedEmployee;
import com.teamspace.android.networking.NetworkRoutes;
import com.teamspace.android.networking.NetworkingLayer;
import com.teamspace.android.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class EmployeeFetchForUser implements DataFetchInterface {

	private String userID;
	private Context context;
	
	public EmployeeFetchForUser(Context context, String userID) {
		this.userID = userID;
		this.context = context;
	}
	
	@Override
	public void fetchDataFromServer(final DataManagerCallback callback) {
		final String dataStoreKey = "employees_from_server_" + System.nanoTime();
		// Update the cache with the fresh data
		final String url = NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_EMPLOYEES
					 + "?key=" + Utils.getSignedInUserKey();
		
		NetworkingLayer.getInstance(context).makeGetJSONArrayRequest(
		url, 
		new Response.Listener<JSONArray>() { 
		    @Override
		    public void onResponse(JSONArray response) {
		    	// We now have the network response. No need to look in the cache anymore.
		    	if (callback != null) {
		    		callback.networkResponseReceived = true;
		    	}
		    	
		    	Utils.log("EmployeeFetchForUser GET response for url: " + url
						+ " got response: " + response);
		    	
				// Call the callback
				DatabaseCache.getInstance(context).deleteAllMigratedEmployeesBlockingCall();
				ArrayList<MigratedEmployee> employees = new ArrayList<MigratedEmployee>();
				for(int i = 0; i < response.length(); i++)
				{
					try {
				      JSONObject object = response.getJSONObject(i);
				      MigratedEmployee employee = MigratedEmployee.parseJSON(object);
				      employees.add(employee);
				      DatabaseCache.getInstance(context).setMigratedEmployeeBlockingCall(employee);
					} catch (JSONException e) {
						Log.e("EmployeeFetchForUser fetchDataFromServer() json_parsing_exception", e.getMessage());
					} catch (java.text.ParseException e) {
						Log.e("EmployeeFetchForUser fetchDataFromServer() json_parsing_exception", e.getMessage());
					}
				}
				DataManager.getInstance(context).insertData(dataStoreKey, employees);
				if (callback != null) {
					callback.onDataReceivedFromServer(dataStoreKey);
				}
		    }
		},		
		new Response.ErrorListener() {

		    @Override
		    public void onErrorResponse(VolleyError error) {
		    	Utils.log("EmployeeFetchForUser fetchDataFromServer() network call failed for employees with url " + url);
		    	error.printStackTrace();
		    }
		});

	}

	@Override
	public void fetchDataFromDatabaseCache(final DataManagerCallback callback) {
		AsyncTask<String, Void, ArrayList<MigratedEmployee>> asyncTask = new AsyncTask<String, Void, ArrayList<MigratedEmployee>>() {
			@Override
			protected ArrayList<MigratedEmployee> doInBackground(String... employee) {
				return DatabaseCache.getInstance(context).getMigratedEmployeesBlockingCall();
			}
			
			@Override
		    protected void onPostExecute(ArrayList<MigratedEmployee> employees) {
				// Now we have the data from cache. We return this cached data
				// only if the network has not already returned fresh data.
				if (callback != null && !callback.networkResponseReceived) {
					String dataStoreKey = "tasks_from_database_" + System.nanoTime();
					DataManager.getInstance(context).insertData(dataStoreKey, employees);
					callback.onDataReceivedFromCache(dataStoreKey);
				}
		    }
		};
		asyncTask.execute(userID);

	}

}
