package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class Utilities {
    public static void openPdf(final String url, final Context activity) {
        final String googleDocsUrl = "http://docs.google.com/viewer?url=";
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(googleDocsUrl + url), "text/html");

        final PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            activity.startActivity(intent);
        }
    }

    public static void openMp3(final String url, final Activity activity) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri uri = Uri.parse(url);
        intent.setData(uri);

        final PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            activity.startActivity(intent);
        }
    }

    public static void openWebPage(final String url, final Activity activity) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri uri = Uri.parse(url);
        intent.setData(uri);

        final PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            activity.startActivity(intent);
        }
    }
}
