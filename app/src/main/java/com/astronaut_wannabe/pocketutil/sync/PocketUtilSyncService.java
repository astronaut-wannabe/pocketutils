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
    private static PocketUtilSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new PocketUtilSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}
