package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HomeScreenActivity extends Activity {

    public static final String LOG_TAG = HomeScreenActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isAuthorized()) {
            startActivity(new Intent(this, AuthActivity.class));
        } else {
            startActivity(new Intent(this, SwipeActivity.class));
        }
    }

    private boolean isAuthorized() {
        final String prefKey = getString(R.string.pocket_access_key);
        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return prefs.getString(prefKey, null) != null;
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
