package com.astronaut_wannabe.pocketutil;

import android.test.AndroidTestCase;

import junit.framework.Assert;

public class TestMediaTypes extends AndroidTestCase {

    private final static String TEST_PDF = "http://okmij.org/ftp/kakuritu/rethinking.pdf";
    private final static String TEST_MP3 = "http://hwcdn.libsyn.com/p/7/9/6/7967434d44da8bf2/scifri201407254.mp3?c_id=7432650&expiration=1426218665&hwt=891483dcba0bccc3d0087d38f0712a78";
    private final static String TEST_HTML_1 = "http://explosm.net/comics/3861/";
    private final static String TEST_HTML_2 = "https://foo.io.me.tv/bar/index.html";


    public void testRegexOnKnownGoodCases() throws Throwable{
        Assert.assertTrue(MediaTypes.isMp3(TEST_MP3));
        Assert.assertTrue(MediaTypes.isPdf(TEST_PDF));
    }

    public void testRegexOnKnownBadCases() throws Throwable{
        Assert.assertFalse(MediaTypes.isMp3(TEST_HTML_1));
        Assert.assertFalse(MediaTypes.isMp3(TEST_HTML_2));
        Assert.assertFalse(MediaTypes.isPdf(TEST_HTML_1));
        Assert.assertFalse(MediaTypes.isPdf(TEST_HTML_2));
    }
}
