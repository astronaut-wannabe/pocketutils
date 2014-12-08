package com.astronaut_wannabe.pocketutil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ***REMOVED*** on 12/7/14.
 */
public class ImageUtils {
    private static final String LOG_TAG = ImageUtils.class.getSimpleName();

    /**
     * Loads an image from the specified URL
     *
     * @return The image at the specified URL or null if an error occured.
     */
    public static Bitmap load(String url) {
        Bitmap expiring = null;
        final HttpGet get = new HttpGet(url);

        HttpEntity entity = null;
        try {
            final HttpResponse response =  HttpManager.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = response.getEntity();

                InputStream in = null;

                try {
                    in = entity.getContent();
                    expiring = BitmapFactory.decodeStream(in);
                } catch (IOException e) {
                    android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
                } finally {
                    in.close();
                }
            }
        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
        } finally {
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e) {
                    android.util.Log.e(LOG_TAG, "Could not load image from " + url, e);
                }
            }
        }

        return expiring;
    }
}
