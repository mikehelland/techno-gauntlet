package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
public class ChordsView extends View {

    private Paint paint;
    private Paint paintCurrentBeat;

    private int width = -1;
    private int height;

    private int marginY;

    private int boxWidth;
    private int boxHeight;

    private Jam mJam;

    final private Bitmap images[];

    private int[] chords;
    private int[] scale;

    public ChordsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 0, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(16.0f);

        paintCurrentBeat = new Paint();
        paintCurrentBeat.setARGB(128, 255, 0, 0);
        paintCurrentBeat.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeat.setStyle(Paint.Style.FILL_AND_STROKE);

        images = ((Main)context).getImages().getChordImages();

        setBackgroundColor(Color.WHITE);
    }

    public void onDraw(Canvas canvas) {

        if (width == -1) {
            width = getWidth();
            height = getHeight();
            boxHeight = images[0].getHeight();
            boxWidth = images[0].getWidth();
            marginY = (height - boxHeight) / 2;
        }

        int chordInProgression = -1;

        if (mJam != null) {
            chords = mJam.getProgression();
            scale = mJam.getScale();
            chordInProgression = mJam.getChordInProgression();
        }

        if (chords == null)
            return ;

        int numberOfChords = chords.length;
        int startAt = (width - numberOfChords * boxWidth) / 2;
        int at;
        int scalei;
        for (int i = 0; i < chords.length; i++) {

            at = startAt + boxWidth * i;

            scalei = chords[i] % scale.length;
            if (scalei < 0)
                scalei += scale.length;
            drawChord(canvas, scale[scalei], at);

            if (chordInProgression == i) {
                canvas.drawRect(at, marginY,
                        at + boxWidth,
                        marginY + boxHeight,
                        paintCurrentBeat);

            }
        }
    }


    private Bitmap draw_image;


    public void drawChord(Canvas canvas, int chord, int at) {


        draw_image = null;
        if (chord == 0) {
            draw_image = images[0];
        }
        if (chord == 2) {
            draw_image = images[1];
        }
        if (chord == 3 || chord == 4) {
            draw_image = images[2];
        }
        if (chord == 5) {
            draw_image = images[3];
        }
        if (chord == 6) {
            draw_image = images[4];
        }
        if (chord == 7) {
            draw_image = images[5];
        }
        if (chord == 8 || chord == 9) {
            draw_image = images[6];
        }
        if (chord == 10 || chord == 11) {
            draw_image = images[7];
        }

        if (draw_image != null) {
            canvas.drawBitmap(draw_image, at, marginY, null);
        }
        else {
            canvas.drawText(Integer.toString(chord),
                    at, marginY+ 50,
                    paint);
        }

    }


    public void setJam(Jam jam) {
        mJam = jam;
    }


   public void setChords(int[] chords, int[] scale) {
       this.chords = chords;
       this.scale = scale;
       invalidate();
   }

   public int[] getChords() {
       return chords;
   }
}
