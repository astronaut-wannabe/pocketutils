package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astronaut_wannabe.pocketutil.service.SignInFragment;


public class SigninActivity extends Activity {

    public static final String LOG_TAG = SigninActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        final SharedPreferences prefs = getPreferences(this.MODE_PRIVATE);
        final boolean pocketAuthorized = prefs.getBoolean(getString(R.string.pocket_authorized), false);
        final FragmentManager fm = getFragmentManager();

        Log.d(LOG_TAG, "pocket authorized = " + pocketAuthorized);

        if (savedInstanceState == null && !pocketAuthorized) {
            fm.beginTransaction()
                    .add(R.id.container, new SignInFragment())
                    .commit();
        }else{
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
