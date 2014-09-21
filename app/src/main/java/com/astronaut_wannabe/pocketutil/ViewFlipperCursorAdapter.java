package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private final View.OnClickListener mButtonPlaceholderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(),"Button Clicked:"+v.getId(),Toast.LENGTH_SHORT).show();
        }
    };

    public ViewFlipperCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.list_item_pocket,parent,false);
        final View v = new MyMotionEvent(mContext, parent);
        ((ViewGroup)v).addView(view);
        final ViewHolder vh = new ViewHolder(v);
        v.setTag(vh);
        return v;
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoaderFinished was called:cursor size="+data.getCount());
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

    private static class MyMotionEvent extends LinearLayout {

        private float mStartX;
        private int mCurrentArticlePosition;
        private final AdapterViewFlipper mFlipper;
        private final Context mContext;

        public MyMotionEvent(Context context, View parent) {
            super(context);
            mFlipper = (AdapterViewFlipper) parent;
            mContext = context;
            mCurrentArticlePosition = 0;
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    final int nextArticle = getRandomArticlePosition();
                    if (isSwipedLeft(event)) {
                        deleteArticle();
                        mFlipper.setOutAnimation(mContext, R.anim.slide_left);
                        mFlipper.setDisplayedChild(nextArticle);
                        return true;
                    } else {
                        mFlipper.setOutAnimation(mContext, R.anim.slide_right);
                        mFlipper.setDisplayedChild(nextArticle);
                        return true;
                    }
                default:
                    return super.onTouchEvent(event);
            }
        }

        private boolean isSwipedLeft(final MotionEvent event){
            return mStartX > event.getX();
        }

        private int getRandomArticlePosition(){
            final int size = mFlipper.getCount();
            return (int) ((Math.random() * size) + 1);
        }

        private void deleteArticle(){
            final TextView idView = (TextView) findViewById(R.id.article_id);
            final String articleId = idView.getText().toString();
            Log.d(LOG_TAG, "Deleting article " + articleId);
            getContext().getContentResolver().delete(
                    PocketDataContract.PocketItemEntry.CONTENT_URI,
                    PocketDataContract.PocketItemEntry.COLUMN_POCKET_ITEM_ID + " =?",
                    new String[]{articleId});
        }
    }
}
