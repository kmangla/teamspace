package com.teamspace.android.caching.dataupdaters;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.teamspace.android.caching.DataManager;
import com.teamspace.android.caching.DataManagerCallback;
import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.models.MigratedMessage;
import com.teamspace.android.models.MigratedTask;
import com.teamspace.android.networking.NetworkRoutes;
import com.teamspace.android.networking.NetworkingLayer;
import com.teamspace.android.utils.Utils;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class MessageUpdater {		
	
	private Context context;
	private MigratedMessage message;
	private DataManagerCallback mCallback;
	
	public MessageUpdater(Context context, MigratedMessage message, DataManagerCallback callback) {
		this.context = context;
		this.message = message;
		this.mCallback = callback;
	}

	public void create() {
		final String url = NetworkRoutes.getRouteBase() + NetworkRoutes.ROUTE_MESSAGE;
		final HashMap<String, String> params = message.toMapObject();
		NetworkingLayer.getInstance(context).makePostRequest(
				url,
				params,
				new Response.Listener<String>() {
				    @Override
				    public void onResponse(String response) {
						Utils.log("createMessage() POST response for url: " + url + " params: " + params
								+ " got response: " + response);
						message.setMessageID(response);
						DatabaseCache.getInstance(context).setMigratedMessage(message);
						if (mCallback != null) {
							mCallback.onSuccess(response);
						}
				    }
				},		
				new Response.ErrorListener() {

				    @Override 
				    public void onErrorResponse(VolleyError error) {
						Utils.log("createMessage() POST failed for url " + url + " params: " + params
								+ " with error " + error);
						error.printStackTrace();
						if (mCallback != null) {
				    		mCallback.onFailure(error.getLocalizedMessage());
						}
				    }  
				});
	}
	
//	public void update() {
//		final String url = NetworkRoutes.ROUTE_BASE + NetworkRoutes.ROUTE_TASK + 
//				"/" + task.getTaskID();
//		final HashMap<String, String> params = task.toMapObject();
//		NetworkingLayer.getInstance(context).makePostRequest(
//				url,
//				params,
//				new Response.Listener<String>() {
//				    @Override
//				    public void onResponse(String response) {
//				    	Utils.log("updateTask() POST response for url: " + url + " params: " + params
//								+ " got response: " + response);
//						DatabaseCache.getInstance(context).updateMigratedTask(task);
//				    }
//				},		
//				new Response.ErrorListener() {
//
//				    @Override
//				    public void onErrorResponse(VolleyError error) {
//				    	Utils.log("updateTask() POST failed for url " + url + " params: " + params
//								+ " with error " + error);
//				    	error.printStackTrace();
//				    }  
//				});
//
//
//	}
//	
//	public void delete() {
//		delete(this.task.getTaskID());
//	}
//
//	public void delete(final String taskID) {
//		// Delete the employee from cache
//		DatabaseCache.getInstance(context).deleteMigratedTask(context, taskID);
//		
//		final String url = NetworkRoutes.ROUTE_BASE + NetworkRoutes.ROUTE_TASK + 
//				"/" + taskID + "?key=" + Utils.getSignedInUserPhoneNumber();
//		NetworkingLayer.getInstance(context).makeDeleteRequest(
//				url,
//				new Response.Listener<String>() {
//				    @Override
//				    public void onResponse(String response) {
//				    	Utils.log("deleteTask() DELETE response for url: " + url
//								+ " got response: " + response);
//				    	if (mCallback != null) {
//				    		mCallback.onSuccess(response);
//				    	}
//				    }
//				},		
//				new Response.ErrorListener() {
//
//				    @Override
//				    public void onErrorResponse(VolleyError error) {
//				    	Utils.log("deleteTask() DELETE failed for url " + url
//								+ " with error " + error);
//				    	error.printStackTrace();
//				    	
//				    	// We had already deleted the task from cache. We
//						// now need to fix our cache
//						DataManager.getInstance(context).fetchTasksForUser(
//								Utils.getSignedInUserId(), null);
//						
//						if (mCallback != null) {
//							mCallback.onFailure(error.getMessage());
//				    	}
//				    }  
//				});
//
//
//	}

}
