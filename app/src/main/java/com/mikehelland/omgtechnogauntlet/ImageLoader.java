package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by m on 1/26/18.
 * take the weight off views and load images here once
 */

class ImageLoader {

    private Bitmap[] mChordImages;
    private Bitmap[][] mNoteImages;

    ImageLoader(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mChordImages = new Bitmap[8];
                mChordImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_one_w);
                mChordImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_two_w);
                mChordImages[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_three_w);
                mChordImages[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_four_w);
                mChordImages[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_flatfive_w);
                mChordImages[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_five_w);
                mChordImages[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_six_w);
                mChordImages[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_seven_w);

                mNoteImages = new Bitmap[8][2];
                mNoteImages[0][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_half);
                mNoteImages[0][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_half);
                mNoteImages[1][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_dotted_quarter);
                mNoteImages[1][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_dotted_quarter);
                mNoteImages[2][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_quarter);
                mNoteImages[2][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_quarter);
                mNoteImages[3][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_dotted_eighth);
                mNoteImages[3][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_dotted_eighth);
                mNoteImages[4][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_eighth);
                mNoteImages[4][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_eighth);
                mNoteImages[5][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_dotted_sixteenth);
                mNoteImages[5][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_dotted_sixteenth);
                mNoteImages[6][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_sixteenth);
                mNoteImages[6][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_sixteenth);
                mNoteImages[7][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_thirtysecond);
                mNoteImages[7][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_thirtysecond);

            }
        }).start();

    }

    Bitmap[] getChordImages() {
        return mChordImages;
    }

    Bitmap[][] getNoteImages() {
        return mNoteImages;
    }
}
