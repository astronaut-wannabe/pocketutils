package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract;

/**
 * Created by ***REMOVED*** on 9/13/14.
 */
public class ViewFlipperCursorAdapter extends CursorAdapter implements LoaderManager.LoaderCallbacks <Cursor>{
    private static final String LOG_TAG = ViewFlipperCursorAdapter.class.getSimpleName();

    private final static String[] POCKET_COLUMNS = {
            PocketDataContract.PocketItemEntry.TABLE_NAME + "." + PocketDataContract.PocketItemEntry._ID,
            PocketDataContract.PocketItemEntry.COLUMN_TITLE,
            PocketDataContract.PocketItemEntry.COLUMN_EXCERPT,
            PocketDataContract.PocketItemEntry.COLUMN_POCKET_ITEM_ID,
            PocketDataContract.PocketItemEntry.COLUMN_RESOLVED_URL
    };

    // Indices tied to the COLUMNS, if the column order changes in the table, these must be updated
    public final static int COL_ID = 0;
    public final static int COL_TITLE = 1;
    public final static int COL_EXCERPT  = 2;
    public final static int COL_POCKET_ITEM_ID = 3;
    public final static int COL_POCKET_URL = 4;

    private final View.OnClickListener mButtonPlaceholderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(),"Button Clicked:"+v.getId(),Toast.LENGTH_SHORT).show();
        }
    };
    public ViewFlipperCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, ViewFlipperCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.list_item_pocket,parent,false);
        final ViewHolder vh = new ViewHolder(view);
        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder vh = (ViewHolder) view.getTag();
        final String title = cursor.getString(COL_TITLE);
        final String excerpt = cursor.getString(COL_EXCERPT);
        final String articleId = cursor.getString(COL_POCKET_ITEM_ID);
        final String htmlString = mContext.getString(R.string.title_and_synopsis_format, title);
        vh.title.setText(Html.fromHtml(htmlString));
        vh.excerpt.setText(excerpt);
        vh.id.setText(articleId);
        vh.delete_btn.setOnClickListener(mButtonPlaceholderClickListener);
        vh.save_btn.setOnClickListener(mButtonPlaceholderClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = PocketDataContract.PocketItemEntry.CONTENT_URI;
        //sort ascending alphabetically by the article title
        final String sortOrder = PocketDataContract.PocketItemEntry.COLUMN_TITLE + " ASC";

        return new CursorLoader(
                mContext,
                uri,
                POCKET_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        swapCursor(null);
    }

    private static class ViewHolder {
        public final TextView title, excerpt,id;
        public final Button delete_btn, save_btn;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.article_title);
            excerpt = (TextView) view.findViewById(R.id.article_excerpt);
            id = (TextView) view.findViewById(R.id.article_id);
            delete_btn = (Button) view.findViewById(R.id.article_delete_btn);
            save_btn = (Button) view.findViewById(R.id.article_save_btn);
        }
    }
}
