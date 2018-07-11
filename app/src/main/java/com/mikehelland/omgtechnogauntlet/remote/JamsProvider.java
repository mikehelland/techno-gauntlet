package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.jam.JamHeader;

import java.util.ArrayList;

public abstract class JamsProvider {
    public abstract ArrayList<JamHeader> getJams();
    public abstract String getJamJson(long id);

}
