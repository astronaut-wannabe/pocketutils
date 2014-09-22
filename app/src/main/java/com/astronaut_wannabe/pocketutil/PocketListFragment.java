package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.TextView;
import android.widget.Toast;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

import java.util.HashSet;
import java.util.Set;

public class PocketListFragment extends Fragment implements PocketSwipeItem.PocketSwipeCallbacks{

    public static final String LOG_TAG = PocketListFragment.class.getSimpleName();

    private final static int POCKET_LOADER = 0;

    private ViewFlipperCursorAdapter mAdapter;
    private AdapterViewFlipper mFlipper;
    private Set<String> mArticlesToDelete;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POCKET_LOADER, null, mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO figure out how to batch this. Also, move to sync adapter.
        final ContentResolver cr = getActivity().getContentResolver();
        final String [] arg = new String[1];
        for (String id : mArticlesToDelete){
            arg[0] = id;
            cr.delete(
                    PocketItemEntry.CONTENT_URI,
                    PocketItemEntry.COLUMN_POCKET_ITEM_ID + " =?",
                    arg);
        }
        mArticlesToDelete.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pocket_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
         if (id == R.id.action_list_count){
            final Activity activity = getActivity();
            final Cursor cursor = activity.getContentResolver()
                    .query(PocketItemEntry.CONTENT_URI,null,null,null,null);
            if (cursor.moveToFirst())
                Toast.makeText(activity,"Items in list: " +cursor.getCount(),Toast.LENGTH_LONG).show();
            else
                Toast.makeText(activity,"Items in list: " + 0,Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        mFlipper = (AdapterViewFlipper) inflater.inflate(
                R.layout.fragment_pocket_list, container, false);
        mFlipper.setInAnimation(getActivity(), R.anim.slide_in_from_top);

        mAdapter = new ViewFlipperCursorAdapter(getActivity(), null, 0);
        mFlipper.setAdapter(mAdapter);
        mAdapter.setSwipeCallbacks(this);
        mArticlesToDelete = new HashSet<String>();
        return mFlipper;
    }

    @Override
    public void onLeftSwipe() {
        final TextView currentArticle = (TextView) mFlipper.getCurrentView().findViewById(R.id.article_id);
        final String id = currentArticle.getText().toString();
        mArticlesToDelete.add(id);

        final int nextArticle = getRandomArticle();
        mFlipper.setOutAnimation(getActivity(), R.anim.slide_left);
        mFlipper.setDisplayedChild(nextArticle);
    }
    private int getRandomArticle(){
        final int size = mFlipper.getCount();
        Log.d(LOG_TAG, String.format("current item count = %d", size));
        int randomArticle;
        String articleId;
        do{
            randomArticle = (int) ((Math.random() * size) + 1);
            articleId = Integer.toString(randomArticle);
        }while (mArticlesToDelete.contains(articleId));
        return randomArticle;
    }

    @Override
    public void onRightSwipe() {
        final int nextArticle = getRandomArticle();
        mFlipper.setOutAnimation(getActivity(), R.anim.slide_right);
        mFlipper.setDisplayedChild(nextArticle);
    }
}
