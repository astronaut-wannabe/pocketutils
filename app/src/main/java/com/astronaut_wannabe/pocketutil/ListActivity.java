package com.astronaut_wannabe.pocketutil;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Created by ***REMOVED*** on 9/7/14.
 */
public class ListActivity extends FragmentActivity {

    public static final String LOG_TAG = ListActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Starting the pocket list.");

        setContentView(R.layout.activity_signin);
        final FragmentManager fm = getSupportFragmentManager();
        final Intent intent = getIntent();
        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.container, new PocketListFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.signin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}