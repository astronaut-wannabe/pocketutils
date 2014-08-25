package com.astronaut_wannabe.pocketutil.pocket;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by ***REMOVED*** on 8/24/14.
 */
public class PocketResponse {
    @SerializedName("status")
    public int status;
    public int complete;
    public HashMap<Integer,PocketItem> list;
}
