package com.astronaut_wannabe.pocketutil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract;

public class ListActivity extends FragmentActivity {

    public static final String LOG_TAG = ListActivity.class.getSimpleName();
    public static final String FRAGMENT_TAG = PocketListFragment.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        final FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.container, new PocketListFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_clear_data){
            resetFragmentToEmpty();
            return true;
        } else if (id == R.id.action_refresh) {
            //fetchList();
            return true;
        }else
            return super.onOptionsItemSelected(item);
    }

    private void resetFragmentToEmpty(){
        // reset the since date so that we fetch the whole list on the next refresh
        final SharedPreferences.Editor editor = getSharedPreferences("prefs",MODE_PRIVATE).edit();
        editor.putString(getString(R.string.pocket_since_date), null);
        editor.commit();

        // destroy the fragment containing the list
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG);
        fm.beginTransaction().remove(fragment).commit();

        // Actually delete the whole database
        getContentResolver().delete(PocketDataContract.PocketItemEntry.CONTENT_URI, null, null);

        // Create a new, empty list fragment
        fm.beginTransaction()
                .add(R.id.container, new PocketListFragment(), FRAGMENT_TAG)
                .commit();

    }

}