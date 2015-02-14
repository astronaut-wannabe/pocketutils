package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import com.astronaut_wannabe.pocketutil.pocket.PocketItem;
import com.astronaut_wannabe.pocketutil.sync.PocketUtilSyncAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PocketListFragment extends Fragment implements PocketSwipeItem.PocketSwipeCallbacks{

    public static final String LOG_TAG = PocketListFragment.class.getSimpleName();

    private final static int POCKET_LOADER = 0;

    private ViewFlipperCursorAdapter mAdapter;
    private AdapterViewFlipper mFlipper;
    private Set<String> mArticlesToDelete;
    private Set<String> mArticlesToMoveToTopOfList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POCKET_LOADER, null, mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        PocketUtilSyncAdapter.syncImmediately(getActivity());
        // TODO figure out how to batch this. Also, move to sync adapter.
        final ContentResolver cr = getActivity().getContentResolver();
        final List<String> syncDelete = new ArrayList<String>(mArticlesToDelete.size());
        final List<String> syncAdd = new ArrayList<String>(mArticlesToMoveToTopOfList.size());

        syncDelete.addAll(mArticlesToDelete);
        syncAdd.addAll(mArticlesToMoveToTopOfList);

        PocketUtilSyncAdapter.deleteImmediately(getActivity(), syncDelete, syncAdd);

        mArticlesToDelete.clear();
        mArticlesToMoveToTopOfList.clear();
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
        mArticlesToMoveToTopOfList = new HashSet<String>();
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
        final TextView currentArticle = (TextView) mFlipper.getCurrentView().findViewById(R.id.article_id);
        final String id = currentArticle.getText().toString();
        mArticlesToMoveToTopOfList.add(id);

        final int nextArticle = getRandomArticle();
        mFlipper.setOutAnimation(getActivity(), R.anim.slide_right);
        mFlipper.setDisplayedChild(nextArticle);
    }

    @Override
    public void onTap() {
        final TextView currentArticle = (TextView) mFlipper.getCurrentView().findViewById(R.id.article_id);
        final String id = currentArticle.getText().toString();
        final Uri uri = PocketItemEntry.buildPocketItemUriWithItemId(id);
        final Cursor cursor = getActivity().getContentResolver().query(uri,null,null,null,null);
        if(cursor.moveToFirst()) {
            final int col = cursor.getColumnIndex(PocketItemEntry.COLUMN_RESOLVED_URL);
            final String url = cursor.getString(col);
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"));
            intent.setAction("org.mozilla.gecko.BOOKMARK");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("args", "--url=" + url);
            intent.setData(Uri.parse(url));
            getActivity().startActivity(intent);
        }
    }
}
