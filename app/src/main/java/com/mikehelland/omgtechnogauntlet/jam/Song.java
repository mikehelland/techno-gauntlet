package com.mikehelland.omgtechnogauntlet.jam;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by m on 4/29/18.
 * hold sections according to OMG
 */

class Song {
    CopyOnWriteArrayList<Section> sections = new CopyOnWriteArrayList<>();
    KeyParameters keyParameters;
    BeatParameters beatParameters;
    AudioParameters audioParameters;

    String tags = "";

    //todo name (NAME!? NAME?! Tags dummy!) and user, date time, localid, omgid? what else
}
