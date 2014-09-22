package com.astronaut_wannabe.pocketutil.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astronaut_wannabe.pocketutil.R;
import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

public class PocketListRecyclerView extends Fragment {

    public static final String LOG_TAG = PocketListRecyclerView.class.getSimpleName();
    private final static int POCKET_LOADER = 0;

    private PocketListAdapter mAdapter;
    private RecyclerView mRecycler;
    private RecyclerView.LayoutManager mLayoutManager;

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
        final View rootView = inflater.inflate(R.layout.recycler_view_test, container, false);
        mRecycler = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        mRecycler.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutManager);

        final String[] testData = new String[]{
          "test 1",
          "test 2",
          "test 3",
          "test 4",
          "test 5",
          "test 6",
          "test 7",
          "test 8",
        };
        mAdapter = new PocketListAdapter(testData);
        mRecycler.setAdapter(mAdapter);

        return rootView;
    }
}
