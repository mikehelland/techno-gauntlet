package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;

public abstract class JamsProvider {
    public abstract ArrayList<JamHeader> getJams();
    public abstract String getJamJson(long id);

}
