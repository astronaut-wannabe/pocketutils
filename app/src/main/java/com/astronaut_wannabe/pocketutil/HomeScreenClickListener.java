package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class HomeScreenClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        final int id = v.getId();
        final Context context = v.getContext();

        switch (id) {
            case R.id.home_screen_duplicates:
                final CheckForDuplicatesTask task = new CheckForDuplicatesTask((android.app.Activity) v.getContext());
                task.execute();
                break;
            case R.id.home_screen_pocket_tinder:
                final Intent intent = new Intent(context, ListActivity.class);
                intent.putExtra("src", HomeScreenFragment.class.getSimpleName());
                context.startActivity(intent);
                break;
            default:
                break;
        }
    }
}
