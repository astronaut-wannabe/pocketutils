package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ***REMOVED*** on 9/7/14.
 */
public class HomeScreenClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        final int id = v.getId();
        final Context context = v.getContext();

        switch (id) {
            case R.id.home_screen_duplicates:
                Toast.makeText(context, "clicked on duplicates", Toast.LENGTH_SHORT).show();
                final CheckForDuplicatesTask task = new CheckForDuplicatesTask((android.app.Activity) v.getContext());
                task.execute();
                break;
            case R.id.home_screen_media:
                Toast.makeText(context, "clicked on media", Toast.LENGTH_SHORT).show();
                break;
            case R.id.home_screen_pocket_tinder:
                Toast.makeText(context, "clicked on tinder", Toast.LENGTH_SHORT).show();
                final Intent intent = new Intent(context, ListActivity.class);
                intent.putExtra("src", HomeScreenFragment.class.getSimpleName());
                context.startActivity(intent);
                break;
            default:
                break;
        }
    }
}
