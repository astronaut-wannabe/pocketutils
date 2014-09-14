package com.astronaut_wannabe.pocketutil;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;
import com.astronaut_wannabe.pocketutil.data.PocketDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class TestDB extends AndroidTestCase {

    public static final String LOG_TAG = TestDB.class.getSimpleName();
    public static final String TEST_TITLE = "13 potatoes that look like feet";
    public static final String TEST_URL = "www.buzzfeed.com";
    public static final String TEST_ID = "1234";
    public static final String TEST_RESOLVED_ID = "5678";
    public static final String TEST_EXCERPT = "once upon a time there was a young prince. This prince had a thing for feet";
    public static final String TEST_DATE = "12-12-2091";

    public void testCreateDb() throws Throwable{
        // clean up to make sure test starts fresh
        mContext.deleteDatabase(PocketDbHelper.DATABASE_NAME);
        final SQLiteDatabase db  = new PocketDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb(){


        final PocketDbHelper dbHelper = new PocketDbHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentValues values = getContentValues();
        final long pocketRowId = db.insert(PocketItemEntry.TABLE_NAME, null, values);

        assertTrue(pocketRowId != -1);
        Log.d(LOG_TAG, "New row id: " + pocketRowId);

        final Cursor cursor = db.query(
                PocketItemEntry.TABLE_NAME,
                null, // columns (null returns all columns)
                null, // columns for "where"
                null, // values for "where"
                null, // columns to group by
                null, // columns to filter by
                null  // sort order
        );

        if(cursor.moveToFirst()){
            validateCursor(values, cursor);
        } else{
            fail("no rows returned from db");
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
