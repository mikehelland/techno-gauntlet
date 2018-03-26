package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/13/18.
 * hold info for ui
 */

public class Surface {
    final static String PRESET_FRETBOARD = "PRESET_FRETBOARD";
    final static String PRESET_SEQUENCER = "PRESET_SEQUENCER";
    final static String PRESET_VERTICAL = "PRESET_VERTICAL";

    private String mName = "";
    private String mURL = PRESET_VERTICAL;

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

    public Surface copy() {
        Surface surface = new Surface();
        surface.setURL(mURL);
        surface.setName(mName);
        surface.setSkipBottomAndTop(mZoomSkipBotton, mZoomSkipTop);
        return surface;
    }

    void getData(StringBuilder sb) {
        sb.append("{\"url\": \"");
        sb.append(mURL);
        sb.append("\", \"name\": \"");
        sb.append(mName);
        sb.append("\", \"skipBottom\": ");
        sb.append(mZoomSkipBotton);
        sb.append(", \"skipTop\": ");
        sb.append(mZoomSkipTop);
        sb.append("}");
    }
}
