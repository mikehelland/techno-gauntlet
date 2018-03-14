package com.mikehelland.omgtechnogauntlet;

/**
 * Created by m on 3/13/18.
 * hold info for ui
 */

class Surface {
    private String mName = "";
    private String mURL = SufacesDataHelper.PRESET_SEQUENCER;
    private long mID;

    private int mZoomSkipBotton = 0;
    private int mZoomSkipTop = 0;

    Surface() {}
    Surface(String url) {
        mURL = url;
    }

    void setName(String name) {mName = name;}
    void setURL(String url) {mURL = url;}
    void setSkipBottomAndTop(int bottom, int top) {
        mZoomSkipBotton = bottom;
        mZoomSkipTop = top;
    }

    int[] getSkipBottomAndTop() {
        return new int[]{mZoomSkipBotton, mZoomSkipTop};
    }

    String getURL() {return mURL;}

    Surface copy() {
        Surface surface = new Surface();
        surface.setURL(mURL);
        surface.setName(mName);
        surface.setSkipBottomAndTop(mZoomSkipBotton, mZoomSkipTop);
        return surface;
    }
}
