package com.astronaut_wannabe.pocketutil;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class PocketListFragment extends Fragment implements LoaderManager.LoaderCallbacks <Cursor> {

    private final static int POCKET_LOADER = 0;

    private final static String[] POCKET_COLUMNS = {
            PocketItemEntry.TABLE_NAME + "." + PocketItemEntry._ID,
            PocketItemEntry.COLUMN_TITLE,
            PocketItemEntry.COLUMN_DATETEXT,
            PocketItemEntry.COLUMN_EXCERPT,
            PocketItemEntry.COLUMN_RESOLVED_URL,
            PocketItemEntry.COLUMN_POCKET_ITEM_ID
    };

    // Indices tied to the COLUMNS, if the column order changes in the table, these must be updated
    public final static int COL_ID = 0;
    public final static int COL_TITLE = 1;
    public final static int COL_DATETEXT = 2;
    public final static int COL_EXCERPT  = 3;
    public final static int COL_RESOLVE_URL = 4;
    public final static int COL_POCKET_ITEM_ID = 5;

    private SimpleCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POCKET_LOADER, null, this);
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
        if (id == R.id.action_refresh) {
            new FetchDataTask(getActivity()).execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        final ListView rootView = (ListView) inflater.inflate(R.layout.fragment_pocket_list,
                container, false);
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_pocket,
                null,
                new String[]{
                        PocketItemEntry.COLUMN_TITLE,
                        PocketItemEntry.COLUMN_DATETEXT,
                        PocketItemEntry.COLUMN_EXCERPT,
                        PocketItemEntry.COLUMN_RESOLVED_URL,
                        PocketItemEntry.COLUMN_POCKET_ITEM_ID

                },
                new int[]{
                        R.id.article_title,
                        R.id.article_date,
                        R.id.article_excerpt,
                        R.id.article_url,
                        R.id.article_id
                },
                0
        );

        rootView.setAdapter(mAdapter);
        rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                final Cursor cursor = adapter.getCursor();

                if(null != cursor && cursor.moveToPosition(position)){
                    final String url = cursor.getString(COL_RESOLVE_URL);
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    final Uri uri = Uri.parse(url);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        final Uri uri = PocketItemEntry.CONTENT_URI;

        //sort ascending alphabetically by the article title
        final String sortOrder = PocketItemEntry.COLUMN_TITLE + " ASC";

        return new CursorLoader(
                getActivity(),
                uri,
                POCKET_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }
}
