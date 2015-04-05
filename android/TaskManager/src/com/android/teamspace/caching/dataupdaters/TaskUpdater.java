package com.android.teamspace.caching.dataupdaters;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.models.MigratedTask;
import com.android.teamspace.networking.NetworkRoutes;
import com.android.teamspace.networking.NetworkingLayer;
import com.android.teamspace.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class TaskUpdater {		
	
	private Context context;
	private MigratedTask task;
	private DataManagerCallback mCallback;
	
	public TaskUpdater(Context context, MigratedTask task, DataManagerCallback callback) {
		this.context = context;
		this.task = task;
		this.mCallback = callback;
	}

	public void create() {
		final String url = NetworkRoutes.ROUTE_BASE + NetworkRoutes.ROUTE_TASK;		
		final HashMap<String, String> params = task.toMapObject();
		NetworkingLayer.getInstance(context).makePostRequest(
				url,
				params,
				new Response.Listener<String>() {
				    @Override
				    public void onResponse(String response) {
						Utils.log("createTask() POST response for url: " + url + " params: " + params
								+ " got response: " + response);
						task.setTaskID(response);
						DatabaseCache.getInstance(context).setMigratedTask(task);
						if (mCallback != null) {
							mCallback.onSuccess(response);
						}
				    }
				},		
				new Response.ErrorListener() {

				    @Override 
				    public void onErrorResponse(VolleyError error) {
						Utils.log("createTask() POST failed for url " + url + " params: " + params
								+ " with error " + error);
						error.printStackTrace();
						if (mCallback != null) {
				    		mCallback.onFailure(error.getLocalizedMessage());
						}
				    }  
				});
	}
	
	public void update() {
		final String url = NetworkRoutes.ROUTE_BASE + NetworkRoutes.ROUTE_TASK + 
				"/" + task.getTaskID();
		final HashMap<String, String> params = task.toMapObject();
		NetworkingLayer.getInstance(context).makePostRequest(
				url,
				params,
				new Response.Listener<String>() {
				    @Override
				    public void onResponse(String response) {
				    	Utils.log("updateTask() POST response for url: " + url + " params: " + params
								+ " got response: " + response);
						DatabaseCache.getInstance(context).updateMigratedTask(task);
				    }
				},		
				new Response.ErrorListener() {

				    @Override
				    public void onErrorResponse(VolleyError error) {
				    	Utils.log("updateTask() POST failed for url " + url + " params: " + params
								+ " with error " + error);
				    	error.printStackTrace();
				    }  
				});


	}
	
	public void delete() {
		delete(this.task.getTaskID());
	}

	public void delete(final String taskID) {
		// Delete the employee from cache
		DatabaseCache.getInstance(context).deleteMigratedTask(context, taskID);
		
		final String url = NetworkRoutes.ROUTE_BASE + NetworkRoutes.ROUTE_TASK + 
				"/" + taskID + "?key=" + Utils.getSignedInUserPhoneNumber();
		NetworkingLayer.getInstance(context).makeDeleteRequest(
				url,
				new Response.Listener<String>() {
				    @Override
				    public void onResponse(String response) {
				    	Utils.log("deleteTask() DELETE response for url: " + url
								+ " got response: " + response);
				    	if (mCallback != null) {
				    		mCallback.onSuccess(response);
				    	}
				    }
				},		
				new Response.ErrorListener() {

				    @Override
				    public void onErrorResponse(VolleyError error) {
				    	Utils.log("deleteTask() DELETE failed for url " + url
								+ " with error " + error);
				    	error.printStackTrace();
				    	
				    	// We had already deleted the task from cache. We
						// now need to fix our cache
						DataManager.getInstance(context).fetchTasksForUser(
								Utils.getSignedInUserId(), null);
						
						if (mCallback != null) {
							mCallback.onFailure(error.getMessage());
				    	}
				    }  
				});


	}

}
