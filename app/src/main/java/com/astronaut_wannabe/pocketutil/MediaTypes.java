package com.astronaut_wannabe.pocketutil;

import java.util.regex.Pattern;

/**
 * Utility class to help determine if the url of the item in Pocket is a file format that Pocket
 * doesn't handle well.
 */
public class MediaTypes {
    private static final Pattern PDF_REGEX = Pattern.compile("/{1}([^/]*\\.pdf)");
    private static final Pattern MP3_REGEX = Pattern.compile("/{1}([^/]*\\.mp3)");
    private static final Pattern TXT_REGEX = Pattern.compile("/{1}([^/]*\\.txt)");

    /**
     * @param url a url from the Pocket api
     * @return true if the url points at a pdf doc
     */
    public static boolean isPdf(String url) {
        return PDF_REGEX.matcher(url).find();
    }

    /**
     * @param url a url from the Pocket api
     * @return true if the url points at an mp3
     */
    public static boolean isMp3(String url) {
        return MP3_REGEX.matcher(url).find();
    }
    /**
     * @param url a url from the Pocket api
     * @return true if the url points at a plaintext doc
     */
    public static boolean isTxt(String url) {
        return TXT_REGEX.matcher(url).find();
    }
}
