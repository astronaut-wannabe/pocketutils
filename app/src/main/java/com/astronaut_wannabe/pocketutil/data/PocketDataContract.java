package com.astronaut_wannabe.pocketutil.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Defines the table and column names for the database that holds the data from a user's Pocket
 * account.
 *
 * Created by ***REMOVED*** on 9/6/14.
 */
public class PocketDataContract {
    /** The name for the entire content provider */
    public static final String CONTENT_AUTHORITY = "com.astronaut_wannabe.pocketutil";
    /** The base for all calls to the content provider */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The different paths to stored data. Currently we only have a path to a PocketItem
    public static final String PATH_ITEM = "pocket_item";

    // Format used for storing dates in the database.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup.
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    public static String getDbDateString(Date date){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    /* Inner class that defines the table contents of the PocketItem table */
    public static final class PocketItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;

        public static final String TABLE_NAME = "pocket_items";

        // Date, stored as Text with format yyyy-MM-dd
        public static final String COLUMN_DATETEXT = "date";
        // the item id returned from the Pocket API
        public static final String COLUMN_POCKET_ITEM_ID = "item_id";
        // Short excerpt from the article
        public static final String COLUMN_EXCERPT = "excerpt";
        // the resolved url of article
        public static String COLUMN_RESOLVED_URL = "resolved_url";
        // the title of the item
        public static String COLUMN_TITLE = "title";

        public static Uri buildPocketItemUriWithItemId(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).build();
        }

        public static Uri buildPocketItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getArticleIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
