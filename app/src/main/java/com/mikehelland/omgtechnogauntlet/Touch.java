package com.mikehelland.omgtechnogauntlet;

/**
 * User: m
 * Date: 7/1/13
 * Time: 2:29 PM
 */
public class Touch {
    int onFret;
    int onString;
    float x;
    float y;
    int id;
    int channelId;
    int playingHandle;

    Touch(float x, float y, int id){
        this.x = x;
        this.y = y;
        this.id = id;
    }

    FretMapElement fretMapping(FretMap fretMap) {
        return fretMapping(onString,  onFret, fretMap);
    }

    static FretMapElement fretMapping(int onString, int onFret, FretMap fretMap) {
        int string =Math.max(0, Math.min(fretMap.size() - 1, onString));
        int fret =Math.max(0, Math.min(fretMap.get(0).size() - 1, onFret));
        //return 12 + fretMap[string][fret];
        return fretMap.get(string).get(fret);
    }

}
