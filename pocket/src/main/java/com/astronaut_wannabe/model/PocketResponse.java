package com.astronaut_wannabe.model;

import java.util.HashMap;

public class PocketResponse {
    public int status;
    public int complete;
    public HashMap<Integer,PocketItem> list;
    public int since;
}
