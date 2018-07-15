package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.jam.JamHeader;

import java.util.ArrayList;

public abstract class OnReceiveSavedJamsListener {
    public abstract void onReceiveSavedJams(ArrayList<JamHeader> jams);
}
