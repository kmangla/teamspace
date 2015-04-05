package com.teamspace.android.interfaces;

import com.teamspace.android.caching.DataManagerCallback;

public interface DataFetchInterface {
	public void fetchDataFromServer(final DataManagerCallback callback);

	public void fetchDataFromDatabaseCache(final DataManagerCallback callback);
}
