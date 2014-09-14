package com.astronaut_wannabe.pocketutil.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;
import com.astronaut_wannabe.pocketutil.data.PocketDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public static final String TEST_TITLE = "13 potatoes that look like feet";
    public static final String TEST_URL = "www.buzzfeed.com";
    public static final String TEST_ID = "1234";
    public static final String TEST_RESOLVED_ID = "5678";
    public static final String TEST_EXCERPT = "once upon a time there was a young prince. This prince had a thing for feet";
    public static final String TEST_DATE = "12-12-2091";

    public void testDeleteAllRecords(){
        mContext.getContentResolver().delete(PocketItemEntry.CONTENT_URI,null,null);

        final int cursor = mContext.getContentResolver().delete(
                PocketItemEntry.CONTENT_URI,
                null,
                null);
    }

    public void testGetType(){
        // content://com.astronaut_wannabe.pocketutil/pocket_item
        final String type1 = mContext.getContentResolver().getType(PocketItemEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.astronaut_wannabe.pocketutil/pocket_item
        assertEquals(PocketItemEntry.CONTENT_TYPE, type1);

        // content://com.astronaut_wannabe.pocketutil/pocket_item/1234
        final String type2 = mContext.getContentResolver().getType(
                PocketItemEntry.buildPocketItemUriWithItemId(TEST_ID));
        // vnd.android.cursor.item/com.astronaut_wannabe.pocketutil/pocket_item/1234
        assertEquals(PocketItemEntry.CONTENT_ITEM_TYPE, type2);

    }

    public void testInsertReadProvider(){


        final PocketDbHelper dbHelper = new PocketDbHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentValues values = getContentValues();
        final Uri insertedUri = mContext.getContentResolver()
                .insert(PocketItemEntry.CONTENT_URI, values);
        final long pocketRowId = ContentUris.parseId(insertedUri);
        Log.d(LOG_TAG, "New row id: " + pocketRowId);

        final Cursor cursor = mContext.getContentResolver().query(PocketItemEntry.CONTENT_URI,
                null,null,null,null); // nulls return all rows

        if(cursor.moveToFirst()){
            validateCursor(values, cursor);
        } else{
            fail("no rows returned from db");
        }

        cursor.close();

        final Cursor itemCursor = mContext.getContentResolver().query(
                PocketItemEntry.buildPocketItemUriWithItemId(TEST_ID),
                null,null,null,null); // nulls return all rows

        if(itemCursor.moveToFirst()){
            validateCursor(values, itemCursor);
        } else{
            fail("no rows returned from db");
        }

        itemCursor.close();

        testDeleteAllRecords();
    }

    public void testUpdateArticle(){
        testDeleteAllRecords();

        final ContentValues values = getContentValues();
        final Uri uri = mContext.getContentResolver().insert(PocketItemEntry.CONTENT_URI,values);
        final long rowId = ContentUris.parseId(uri);

        // verify the insert worked
        assertTrue(rowId != -1);
        Log.d(LOG_TAG, "New row id: " + rowId);

        final ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(PocketItemEntry._ID, rowId);
        updatedValues.put(PocketItemEntry.COLUMN_EXCERPT, "Mary had a little lamb until she got hungry");

        final int updateRowCount = mContext.getContentResolver().update(PocketItemEntry.CONTENT_URI,
                updatedValues,
                PocketItemEntry._ID + " = ?",
                new String[] {Long.toString(rowId)});

        assertEquals(1, updateRowCount);

        final Cursor cursor = mContext.getContentResolver().query(
                PocketItemEntry.buildPocketItemUri(rowId),
                null,null,null,null

        );

        if (cursor.moveToFirst()){
            validateCursor(updatedValues, cursor);
        }

        cursor.close();
    }
    private void validateCursor(ContentValues expectedValues, Cursor cursor){
        final Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String,Object> entry : valueSet){
            final String columnName = entry.getKey();
            final int index = cursor.getColumnIndex(columnName);
            assertFalse(-1 == index);
            final String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, cursor.getString(index));
        }
    }

    private ContentValues getContentValues(){
        final ContentValues values = new ContentValues();
        values.put(PocketItemEntry.COLUMN_POCKET_ITEM_ID, TEST_ID);
        values.put(PocketItemEntry.COLUMN_POCKET_RESOLVED_ID, TEST_RESOLVED_ID);
        values.put(PocketItemEntry.COLUMN_RESOLVED_URL, TEST_URL);
        values.put(PocketItemEntry.COLUMN_TITLE, TEST_TITLE);
        values.put(PocketItemEntry.COLUMN_EXCERPT, TEST_EXCERPT);
        values.put(PocketItemEntry.COLUMN_DATETEXT, TEST_DATE);
        return values;
    }
}
