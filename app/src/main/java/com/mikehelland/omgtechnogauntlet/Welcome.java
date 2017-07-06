package com.mikehelland.omgtechnogauntlet;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

/**
 * Created by m on 5/27/14.
 */
public class Welcome {

    private Main mMainActivity;
    private TextView mWelcomeText;

    public Welcome(Main mainActivity) {
        mMainActivity = mainActivity;
        mWelcomeText = (TextView)mainActivity.findViewById(R.id.welcome_text);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlphaAnimation alphaAnimationIn = new AlphaAnimation(0.0f, 1.0f);
                final AlphaAnimation alphaAnimationOut = new AlphaAnimation(1.0f, 0.0f);
                alphaAnimationIn.setDuration(1000);
                alphaAnimationOut.setDuration(1000);
                alphaAnimationOut.setFillAfter(true);
                alphaAnimationIn.setFillAfter(true);

                sleep(1000);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mWelcomeText.setText("Hi!");
                        mWelcomeText.setVisibility(View.VISIBLE);
                        mWelcomeText.startAnimation(alphaAnimationIn);
                    }
                });

                sleep(2000);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWelcomeText.startAnimation(alphaAnimationOut);
                    }
                });

                sleep(1500);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWelcomeText.setText("The Monkey changes things");
                        mWelcomeText.startAnimation(alphaAnimationIn);
                    }
                });


                sleep(4000);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWelcomeText.startAnimation(alphaAnimationOut);
                    }
                });


                sleep(1500);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mWelcomeText.setText("The Banana saves things");
                        mWelcomeText.startAnimation(alphaAnimationIn);
                    }
                });

                sleep(4000);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWelcomeText.startAnimation(alphaAnimationOut);
                    }
                });

                sleep(1500);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    mWelcomeText.setText("Have fun!");
                    mWelcomeText.startAnimation(alphaAnimationIn);
                    }
                });

                sleep(4000);

                mMainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWelcomeText.startAnimation(alphaAnimationOut);
                    }
                });

            }
        }).start();
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
