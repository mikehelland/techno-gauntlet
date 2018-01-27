package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by m on 1/26/18.
 * take the weight off views and load images here once
 */

class ImageLoader {

    Bitmap[] mChordImages;

    ImageLoader(Context context) {

        mChordImages = new Bitmap[8];
        mChordImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_one);
        mChordImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_two);
        mChordImages[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_three);
        mChordImages[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_four);
        mChordImages[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_flatfive);
        mChordImages[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_five);
        mChordImages[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_six);
        mChordImages[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_seven);


    }

    Bitmap[] getChordImages() {
        return mChordImages;
    }
}
