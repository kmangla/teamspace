package com.android.teamspace.interfaces;

import com.android.teamspace.caching.DataManagerCallback;

public interface DataFetchInterface {
	public void fetchDataFromServer(final DataManagerCallback callback);

	public void fetchDataFromDatabaseCache(final DataManagerCallback callback);
}
