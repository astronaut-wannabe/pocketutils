package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by ***REMOVED*** on 9/10/14.
 */
public class CheckForDuplicatesTask extends AsyncTask<Void, Void, List<String>> {

    public static final String LOG_TAG = CheckForDuplicatesTask.class.getSimpleName().toString();

    private final static String[] POCKET_COLUMNS = {
            PocketItemEntry.TABLE_NAME + "." + PocketItemEntry._ID,
            PocketItemEntry.COLUMN_POCKET_ITEM_ID,
            PocketItemEntry.COLUMN_POCKET_RESOLVED_ID,
            PocketItemEntry.COLUMN_TITLE
    };

    // Indices tied to the COLUMNS, if the column order changes in the table, these must be updated
    private final static int COL_ID = 0;
    private final static int COL_POCKET_ITEM_ID = 1;
    private final static int COL_POCKET_RESOLVED_ID = 2;
    private final static int COL_POCKET_TITLE = 3;
    private final Activity mContext;

    public CheckForDuplicatesTask(Activity context) {
        mContext = context;
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        //sort descending numerically by resolved_id
        final String sortOrder = PocketItemEntry.COLUMN_POCKET_RESOLVED_ID + " DESC";

        // get all (item ids, resolved_id) pairs
        final Cursor cursor = mContext.getContentResolver().query(
                PocketItemEntry.CONTENT_URI,
                POCKET_COLUMNS,
                null,
                null,
                sortOrder);

        final List<String> duplicates = new ArrayList<String>();
        final Set<String> idSet = new TreeSet<String>();
        while (cursor.moveToNext()){
            final String r_id = cursor.getString(COL_POCKET_RESOLVED_ID);
            final String id = cursor.getString(COL_POCKET_ITEM_ID);
            final String title = cursor.getString(COL_POCKET_TITLE);

            if(!idSet.add(r_id)){
                duplicates.add(title);
                Log.d(LOG_TAG, String.format("item id=%s, resolved_id=%s, title=%s", id,r_id, title));
            }
        }
        cursor.close();
        return duplicates;
    }

}