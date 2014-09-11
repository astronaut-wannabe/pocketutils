package com.astronaut_wannabe.pocketutil.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

/**
 * Manages a local database for weather data.
 *
 * Created by ***REMOVED*** on 9/6/14.
 */
public class PocketDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "pocket.db";

    public PocketDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_POCKET_TABLE = "CREATE TABLE " + PocketItemEntry.TABLE_NAME + " (" +
                PocketItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the pocket item entry
                PocketItemEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                PocketItemEntry.COLUMN_POCKET_RESOLVED_ID + " TEXT NOT NULL, " +
                PocketItemEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                PocketItemEntry.COLUMN_EXCERPT + " TEXT NOT NULL, " +
                PocketItemEntry.COLUMN_RESOLVED_URL + " TEXT NOT NULL, " +
                PocketItemEntry.COLUMN_POCKET_ITEM_ID + " TEXT NOT NULL, " +

                // To assure the application have just one of each unique item, it's created a
                // UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + PocketItemEntry.COLUMN_POCKET_ITEM_ID  + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_POCKET_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PocketItemEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
