package com.astronaut_wannabe.pocketutil.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class PocketProvider extends ContentProvider {
    private static final int POCKET_ITEMS = 100;
    private static final int POCKET_ITEM_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = PocketProvider.class.getSimpleName();

    private PocketDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PocketDataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PocketDataContract.PATH_ITEM, POCKET_ITEMS);
        matcher.addURI(authority, PocketDataContract.PATH_ITEM + "/*", POCKET_ITEM_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PocketDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor returnedCursor;
        switch (sUriMatcher.match(uri)){
            case POCKET_ITEMS:
                returnedCursor = mDbHelper.getReadableDatabase().query(
                        PocketItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case POCKET_ITEM_WITH_ID:
                returnedCursor = mDbHelper.getReadableDatabase().query(
                        PocketItemEntry.TABLE_NAME,
                        projection,
                        PocketItemEntry.COLUMN_POCKET_ITEM_ID + " = " +
                                PocketItemEntry.getArticleIdFromUri(uri),
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        returnedCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnedCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case POCKET_ITEMS:
                return PocketItemEntry.CONTENT_TYPE;
            case POCKET_ITEM_WITH_ID:
                return PocketItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final Uri returnedUri;
        switch (sUriMatcher.match(uri)){
            case POCKET_ITEMS:
                final long _id = mDbHelper.getWritableDatabase().insert(
                        PocketItemEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    returnedUri = PocketItemEntry.buildPocketItemUri(_id);
                } else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:

                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(returnedUri, null);
        return returnedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "in delete()");
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int rowsDeleted;
        switch (sUriMatcher.match(uri)){
            case POCKET_ITEMS:
                Log.d(LOG_TAG, "deleting all rows");
                rowsDeleted = db.delete(PocketItemEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // because a null deletes everything
        if(null == selection || 0 != rowsDeleted){
            Log.d(LOG_TAG, "notifying loaders of change.");
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int rowsUpdated;
        switch (sUriMatcher.match(uri)){
            case POCKET_ITEMS:
                rowsUpdated = db.update(PocketItemEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        // because a null deletes everything
        if(0 != rowsUpdated){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match){
            case POCKET_ITEMS:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for (ContentValues value : values){
                        if (value.get("item_id") != null && value.getAsInteger("resolved_item_id") == 0) {
                            final long delete_id = db.delete(
                                    PocketItemEntry.TABLE_NAME,
                                    PocketItemEntry.COLUMN_POCKET_ITEM_ID + " = ?",
                                    new String[] {value.getAsString("item_id")}
                            );
                            returnCount--;
                        } else {
                            final long _id = db.insert(PocketItemEntry.TABLE_NAME, null, value);
                            if (-1 != _id) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
