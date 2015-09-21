package com.astronaut_wannabe.model;

import java.util.HashMap;

public class PocketItem {

    public int item_id;
    public int resolved_id;

    public String given_url;
    public String given_title;

    public int time_added;
    public int time_updated;
    public int time_read;
    public int time_favorited;
    public int sort_id;
    public int favorite;
    public int status;

    public String resolved_title;
    public String resolved_url;
    public String excerpt;

    public int is_article;
    public int is_index;
    public int has_video;
    public int has_image;
    public int word_count;

    public HashMap<Integer, Image> images;
    public Image image;
}
