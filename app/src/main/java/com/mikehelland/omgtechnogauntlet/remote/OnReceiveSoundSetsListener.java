package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

import java.util.ArrayList;

public abstract class OnReceiveSoundSetsListener {
    public abstract void onReceiveSoundSets(ArrayList<SoundSet> soundSets);
}
