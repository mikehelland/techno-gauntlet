package com.mikehelland.omgtechnogauntlet.jam;

import java.util.concurrent.CopyOnWriteArrayList;

class Section {

    String tags = "";
    BeatParameters beatParameters;
    KeyParameters keyParameters;
    int[] progression = {0};
    CopyOnWriteArrayList<Part> parts = new CopyOnWriteArrayList<>();

}
