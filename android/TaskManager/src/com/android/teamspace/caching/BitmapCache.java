package com.android.teamspace.caching;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCache {
	private static BitmapCache instance;
	private LruCache<String, Bitmap> dataStore;
	
	private BitmapCache() {
		int cacheSize = 2 * 1024 * 1024; // 2MiB
		dataStore = new LruCache<String, Bitmap>(cacheSize);
	}
	
	public static BitmapCache getInstance() {
		if (null == instance) {
			instance = new BitmapCache();
		}
		return instance;
	}
	
	public void insertData(String dataStoreKey, Bitmap data) {
		if (dataStoreKey == null || data == null) {
		  return;
		}
		dataStore.put(dataStoreKey, data);
	}
	
	public Bitmap retrieveData(String dataStoreKey) {
		if (dataStoreKey == null) {
			return null;
		}
		
		return dataStore.get(dataStoreKey); 
	}
}
