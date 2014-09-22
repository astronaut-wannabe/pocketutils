package com.astronaut_wannabe.pocketutil.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astronaut_wannabe.pocketutil.PocketSwipeItem;
import com.astronaut_wannabe.pocketutil.R;

public class PocketListAdapter extends RecyclerView.Adapter<PocketListAdapter.ViewHolder> {
    private String[] mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title, excerpt, id;

        public ViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.article_title);
            excerpt = (TextView) view.findViewById(R.id.article_excerpt);
            id = (TextView) view.findViewById(R.id.article_id);
        }
    }

    public PocketListAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public PocketListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = new PocketSwipeItem(parent.getContext());
        // set the view's size, margins, paddings and layout parameters
        final int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(matchParent, matchParent);
        v.setLayoutParams(layoutParams);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String test = mDataset[position];
        holder.excerpt.setText(test);
        holder.id.setText(test);
        holder.title.setText(test);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}