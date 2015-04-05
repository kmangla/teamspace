package com.android.teamspace.networking;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.teamspace.data.MockNetworkData;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NetworkingLayer {
	private static NetworkingLayer instance;
	private RequestQueue mRequestQueue;
	private Context mContext;
	
	private NetworkingLayer(Context context) {
		mContext = context.getApplicationContext();
		mRequestQueue = Volley.newRequestQueue(mContext);
	}
	
	public static NetworkingLayer getInstance(Context context) {
		if (null == instance) {
			instance = new NetworkingLayer(context);
		} 
		return instance;
	} 
	
	public void makeGetJSONArrayRequest(
			final String url, final Response.Listener<JSONArray> responseListener,
			Response.ErrorListener errorListener) { 

		Log.i("Karan", url);
		if (MockNetworkData.shouldMockNetworkData()) {
			// Simulate network delay (4 seconds) for mock data.
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					responseListener.onResponse(MockNetworkData.getJSONArrayResponse(url));					
				}
			}, 4000);
			
			return;
		}
		
		JsonArrayRequest jsObjRequest = new JsonArrayRequest
		        (url, responseListener, errorListener);
		
		// Add the request  
		mRequestQueue.add(jsObjRequest);
	}
	
	public void makePostRequest(
			String url, final HashMap<String, String> params, Listener<String> responseListener,
			Response.ErrorListener errorListener) {
		StringRequest jsObjRequest = new StringRequest(
				Request.Method.POST,
				url,
				responseListener,
		        errorListener
		) {     
		    @Override
		    protected Map<String, String> getParams() 
		    {  
		            return params;
		    }
		};

		// Add the request  
		mRequestQueue.add(jsObjRequest);

	}
	
	public void makeDeleteRequest(
			String url, Listener<String> responseListener, Response.ErrorListener errorListener) {
		Log.i("Karan", url);
		StringRequest jsObjRequest = new StringRequest(
				Request.Method.DELETE,
				url,
				responseListener,
		        errorListener
		);
		// Add the request  
		mRequestQueue.add(jsObjRequest);
	}
}
