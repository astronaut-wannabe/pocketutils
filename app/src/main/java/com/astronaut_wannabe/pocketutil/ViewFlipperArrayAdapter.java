package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.astronaut_wannabe.model.PocketImageItem;
import com.astronaut_wannabe.model.PocketItem;
import com.astronaut_wannabe.pocketutil.data.PocketDataContract;
import com.bumptech.glide.Glide;

public class ViewFlipperArrayAdapter extends ArrayAdapter<PocketItem> {
    private static final String LOG_TAG = ViewFlipperArrayAdapter.class.getSimpleName();

    private PocketSwipeItem.PocketSwipeCallbacks mCallbacks;

    public ViewFlipperArrayAdapter(Context context) {
        super(context, R.layout.list_item_pocket);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = new PocketSwipeItem(parent.getContext());
            ((PocketSwipeItem)convertView).setCallbacks(mCallbacks);
            final ViewHolder vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }

        final ViewHolder vh = (ViewHolder) convertView.getTag();
        final PocketItem item = getItem(position);
        vh.title.setText(Html.fromHtml(item.resolved_title));
        vh.excerpt.setText(item.excerpt);
        vh.id.setText(item.item_id+"");

        final PocketImageItem imageItem = item.has_image == 0 ? null : (PocketImageItem) item.images.values().toArray()[0];
        final String imageUrl = imageItem == null ? "" : imageItem.src;
        if(imageUrl.equals(""))
            Log.d(LOG_TAG, "no image for " + item.resolved_title);
        else {
            Log.d(LOG_TAG, "Loading image for: " + item.resolved_title);
            Glide.with(getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_add_overlay_logo)
                    .crossFade()
                    .into(vh.image);
        }
        return convertView;
    }

    private static class ViewHolder {
        public final TextView title, excerpt, id;
        public final ImageView image;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.article_title);
            excerpt = (TextView) view.findViewById(R.id.article_excerpt);
            id = (TextView) view.findViewById(R.id.article_id);
            image = (ImageView) view.findViewById(R.id.home_screen_image);
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
