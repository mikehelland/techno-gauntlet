package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

import java.util.ArrayList;

public abstract class OnGetSoundSetsListener {
    public abstract ArrayList<SoundSet> getSoundSets();
    public abstract SoundSet getSoundSet(long id);
}
