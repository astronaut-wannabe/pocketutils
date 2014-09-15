package com.astronaut_wannabe.pocketutil.pocket;

import java.util.HashMap;

public class PocketItem {
    public int item_id;
    public int resolved_id;
    public String given_url;
    public String given_title;
    public int time_added;
    public int favorite;
    public int status;
    public String resolved_title;
    public String resolved_url;
    public String excerpt;
    public int is_article;
    public int has_video;
    public int has_image;
    public int word_count;
    public HashMap<Integer, PocketImageItem> images;
    public HashMap<Integer, PocketVideoItem> videos;
}
