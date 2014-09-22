package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract;

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

    private PocketSwipeItem.PocketSwipeCallbacks mCallbacks;

    public ViewFlipperCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = new PocketSwipeItem(context);
        ((PocketSwipeItem)view).setCallbacks(mCallbacks);
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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader was called");

        final Uri uri = PocketDataContract.PocketItemEntry.CONTENT_URI;
        //sort ascending alphabetically by the article title
        final String sortOrder = PocketDataContract.PocketItemEntry.COLUMN_DATETEXT + " DESC";

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
    protected void onContentChanged() {
        Log.d(LOG_TAG, "onContentChanged");
        super.onContentChanged();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoaderFinished was called:cursor size="+data.getCount());
        swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        swapCursor(null);
    }

    private static class ViewHolder {
        public final TextView title, excerpt, id;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.article_title);
            excerpt = (TextView) view.findViewById(R.id.article_excerpt);
            id = (TextView) view.findViewById(R.id.article_id);
            Log.d(LOG_TAG, String.format("title=%s\nexcerpt=%s\nid=%s\n",title,excerpt,id));
        }
    }

    public void setSwipeCallbacks (PocketSwipeItem.PocketSwipeCallbacks cb){
        mCallbacks = cb;
    }
    private static class DeleteItemTask extends AsyncTask<Integer, Void, Void>{
        private final Context mContext;

        public DeleteItemTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            final Integer articleIdToDelete = params[0];
            deleteArticle(articleIdToDelete);
            return null;
        }
        private void deleteArticle(Integer id){
            Log.d(LOG_TAG, "Deleting article " + id);
            mContext.getContentResolver().delete(
                    PocketDataContract.PocketItemEntry.CONTENT_URI,
                    PocketDataContract.PocketItemEntry.COLUMN_POCKET_ITEM_ID + " =?",
                    new String[]{id.toString()});
        }
    }
}
