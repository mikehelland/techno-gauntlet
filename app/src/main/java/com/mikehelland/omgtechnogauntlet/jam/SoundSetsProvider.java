package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;

public abstract class SoundSetsProvider {
    public abstract ArrayList<SoundSet> getSoundSets();
    public abstract SoundSet getSoundSetById(long id);
    public abstract SoundSet getSoundSetByURL(String url);
}
