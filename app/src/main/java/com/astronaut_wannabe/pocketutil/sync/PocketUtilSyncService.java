package com.astronaut_wannabe.pocketutil.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by ***REMOVED*** on 9/14/14.
 */
public class PocketUtilSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static PocketUtilSyncAdapter sPocketSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("PocketUtilSyncService", "onCreate - PocketUtilSyncService");
        synchronized (sSyncAdapterLock) {
            if (sPocketSyncAdapter == null) {
                sPocketSyncAdapter = new PocketUtilSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sPocketSyncAdapter.getSyncAdapterBinder();
    }
}
