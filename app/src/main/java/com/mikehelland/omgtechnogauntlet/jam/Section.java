package com.mikehelland.omgtechnogauntlet.jam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Section {

    String tags = "";
    BeatParameters beatParameters;
    KeyParameters keyParameters;
    int[] progression = {0};
    List<Part> parts = new CopyOnWriteArrayList<>();

}
